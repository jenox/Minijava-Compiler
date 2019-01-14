import Foundation



/*


// Width of a register or instruction.
// Named according to Intel tradition.
public enum RegWidth: Int {
    case byte = 1
    case word = 2
    case double = 4
    case quad = 8
}


// Represents a pseudo-register, represented in source code as %@<index><width-suffix>.
// The index should be numerical or "r0" (the function result pseudo-register),
// the width suffix is assigned in the same way as for r8 through r15:
// 'b' for byte, 'w' for word, 'd' for doubleword, and 'q' for quadword.
public class Register: Equatable, Hashable, CustomStringConvertible {

    // Constructs a new pseudo-register object.
    // :param name: The name of the register. Includes the %@ sigill, the index, and the width suffix.
    public init(_ name: String) {
        let expression = try! NSRegularExpression(pattern: "%@[jr0-9]+[lwd]?", options: [])

        guard name.matches(expression) else {
            fatalError("Register name \(name) is invalid.")
        }

        self.name = name
    }

    public let name: String

    public func hash(into hasher: inout Hasher) {
        self.name.hash(into: &hasher)
    }

    // Compares two pseudo-registers by name.
    public static func ==(lhs: Register, rhs: Register) -> Bool {
        return lhs.name == rhs.name
    }

    // :return: The name of this register.
    public var description: String {
        return self.name
    }

    public var name_without_width: String {
        if self.width != .quad {
            return String(self.name.dropLast())
        }
        else {
            return self.name
        }
    }

    public var width: RegWidth {
        switch self.name.last {
        case "l": return .byte
        case "w": return .word
        case "d": return .double
        default: return .quad
        }
    }
}


// Represents a concrete register.
// The name is only the base name of the register, without % or the width suffix.
// The base names are 'a', 'b', 'c', 'd', 'sp', 'bp', 'si', 'di', 'r8' through 'r15'.
public class ConcreteRegister: CustomStringConvertible {
    public init(_ name: String, _ width: RegWidth) {
        self.name = name
        self.width = width

        // See if the name is valid
        _ = self.asm_name
    }

    public let name: String
    public let width: RegWidth

    public var asm_name: String {
        if ["a", "b", "c", "d"].contains(self.name) {
            switch self.width {
            case .byte: return "\(self.name)l"
            case .word: return "\(self.name)x"
            case .double: return "e\(self.name)x"
            case .quad: return "r\(self.name)x"
            }
        }
        else if ["sp", "bp", "si", "di"].contains(self.name) {
            switch self.width {
            case .byte: return "\(self.name)l"
            case .word: return "\(self.name)"
            case .double: return "e\(self.name)"
            case .quad: return "r\(self.name)"
            }
        }
        else if ["r8", "r9", "r10", "r11", "r12", "r13", "r14", "r15", "r16"].contains(self.name) {
            switch self.width {
            case .byte: return "\(self.name)b"
            case .word: return "\(self.name)w"
            case .double: return "\(self.name)d"
            case .quad: return "\(self.name)"
            }
        }
        else {
            fatalError("unknown concrete register base name \(self.name)")
        }
    }

    // :return: The full register name, including % and the width suffix.
    public var description: String {
        return "%" + self.asm_name
    }
}

// Maps register to frame offset (in bytes)
public class RegisterTable: CustomStringConvertible, CustomDebugStringConvertible {
    public init(_ function: Function) {
        self.function = function
        self._raw = [:]
        self._cur_offset = 0
    }

    public let function: Function
    private var _raw: [String: Int]
    private var _cur_offset: Int

    //  Returns the frame offset of the given pseudo-register.
    // If the register has not yet been seen, a new slot on the frame is allocated,
    // assigned to the given register, and returned.
    //
    // Manually sets the frame offset of a pseudo-register.
    // The offset must be non-negative (i.e. outside the frame),
    // and the register must not have an offset yet.
    public subscript(_ register: Register) -> Int {
        get {
            let regname = register.name_without_width

            if let offset = self._raw[regname] {
                return offset
            }
            else {
                self._cur_offset -= 8
                self._raw[regname] = self._cur_offset

                return self._cur_offset
            }
        }
        set {
            let regname = register.name_without_width

            if let offset = self._raw[regname] {
                fatalError("Register \(register) already has offset \(offset)")
            }
            else if newValue <= 0 {
                fatalError("Fixed-position registers only allowed at positive offsets")
            }

            self._raw[regname] = newValue
        }
    }

    public var description: String {
        return self._raw.description
    }

    // Returns the size of the frame in bytes.
    public var size: Int {
        return -self._cur_offset
    }

    public var debugDescription: String {
        return self._raw.debugDescription
    }
}


// Represents a pseudo-instruction along with its supporting instructions such as movs.
// Instructions are stored in the AsmUnit in a line-based manner.
// The different builder methods each add one or more lines.
public class AsmUnit: CustomStringConvertible {

    // :param usable_regs: Base names of the concrete registers which may be used as temporary storage.
    public init(_ regs: RegisterTable, _ usable_regs: [String]? = nil) {
        self._lines = []
        self._concrete = [:]
        self._regs = regs
        self._usable = usable_regs ?? ["r8", "r9", "r10", "r11", "r12", "r13"]
    }

    private var _lines: [String]
    private var _concrete: [Register: String]
    private var _regs: RegisterTable
    private var _usable: [String]

    // :return: The lines accumulated so far.
    public var description: String {
        return self._lines.joined(separator: "\n")
    }

    // Appends the given string verbatim as a new line.
    // :return: self
    @discardableResult
    public func raw(_ anything: String) -> AsmUnit {
        self._lines.append(anything)
        return self
    }

    @discardableResult
    public func comment(_ comment: String) -> AsmUnit {
        return self.raw("/"+"* \(comment.replacingOccurrences(of: "*"+"/", with: "* /")) *"+"/")
    }

    // Reserve the given concrete register for later use.
    @discardableResult
    public func reserve_register(_ reg: Register) -> AsmUnit {
        let conc = self._pop_free_reg()
        self._concrete[reg] = conc
        return self
    }

    // Add mov from source to any free concrete register.
    @discardableResult
    public func load(_ source: Register) -> AsmUnit {
        let conc = self._pop_free_reg()
        self._concrete[source] = conc
        self._lines.append("movq \(self._regs[source])(%rbp), \(ConcreteRegister(conc, .quad))")
        return self
    }

    // Loads all the given registers.
    @discardableResult
    public func loads(_ sources: [Register]) -> AsmUnit {
        for source in sources {
            _ = self.load(source)
        }

        return self
    }

    // Add an instruction.
    // Replaces pseudo register names with concrete ones added by previous load()/loads()
    @discardableResult
    public func instruction(_ line: String) -> AsmUnit {
        let result = self._replace_pseudo_regs(line)
        self._lines.append(result)
        return self
    }

    public func _replace_pseudo_regs(_ expr: String, _ reg_width: RegWidth? = nil) -> String {
        var expr = expr

        // TODO: make sure this is the same as in the python script
        for (reg, conc) in self._concrete.sorted(by: { $0.key.description.count }).reversed() {
            //            print("iterating sorted \(reg) \(conc)")
            expr = expr.replacingOccurrences(of: reg.description, with: ConcreteRegister(conc, reg_width ?? reg.width).description)
        }

        return expr
    }

    // Adds mov from the concrete register assigned to target to the slot for target on the frame.
    @discardableResult
    public func store(_ target: Register) -> AsmUnit {
        let conc = self[target]
        self._lines.append("movq \(ConcreteRegister(conc, .quad)), \(self._regs[target])(%rbp)")
        return self
    }

    // Stores all the given registers.
    @discardableResult
    public func stores(_ targets: [Register]) -> AsmUnit {
        for target in targets {
            _ = self.store(target)
        }

        return self
    }

    // Adds a 64-bit wide move between the concrete registers representing the given pseudo-registers.
    @discardableResult
    public func move(_ source: Register, _ target: Register) -> AsmUnit {
        self._lines.append("movq \(ConcreteRegister(self[source], .quad)), \(ConcreteRegister(self[target], .quad))")
        return self
    }

    // Adds a 64-bit wide move from the concrete register representing source
    // to the concrete register with base name target.
    @discardableResult
    public func move_to_concrete(_ source: Register, _ target: String) -> AsmUnit {
        self._lines.append("movq \(ConcreteRegister(self[source], .quad)), \(ConcreteRegister(target, .quad))")
        return self
    }

    // The inverse of move_to_concrete.
    @discardableResult
    public func move_from_concrete(_ source: String, _ target: Register) -> AsmUnit {
        self._lines.append("movq \(ConcreteRegister(source, .quad)), \(ConcreteRegister(self[target], .quad))")
        return self
    }

    // Adds a 64-bit wide move from source to the concrete register with base name target.
    // Source may be any register-, immediate- or address-mode-like expression.
    @discardableResult
    public func move_from_anything_to_concrete_reg(_ source: String, _ target: String) -> AsmUnit {
        return self.raw("movq \(self._replace_pseudo_regs(source, .quad)), \(ConcreteRegister(target, .quad))")
    }

    public func _pop_free_reg() -> String {
        return self._usable.removeFirst()
    }

    // :return: The concrete register representing reg.
    public subscript(_ reg: Register) -> String {
        return self._concrete[reg]!
    }
}


// An assembly function.
// In the source code, a function starts with ".function <name> <number of args> <number of results>".
// The maximum number of results is 1.
// A function ends with ".endfunction".
// The function object takes care of prologue and epilogue.
public class Function {

    // :param line: The line containing the ".function" directive.
    public init(_ line_number: Int, _ line: String) {
        let components = line.components(separatedBy: .whitespacesAndNewlines)
        precondition(components.count == 4)

        let keyword = components[0]
        self.name = components[1]
        let args = components[2]
        let ret = components[3]
        precondition(keyword == ".function")
        self.line_number = line_number
        self._instrs = []
        self._num_params = Int(args)!
        self._has_result = ret == "1"
    }

    public let line_number: Int
    public let name: String
    private var _instrs: [Instruction]
    private var _num_params: Int
    private var _has_result: Bool

    // Add instrs to this function.
    @discardableResult
    public func extend(_ instrs: [Instruction]) -> Function {
        self._instrs.append(contentsOf: instrs)
        return self
    }

    public func toAsm() -> String {
        let table = RegisterTable(self)
        for i in 0..<self._num_params {
            let reg = Register("%@\(i)")
            table[reg] = 16 + 8 * i
        }

        // side-effect is to reserve a stack slot
        _ = table[Register("%@r0")]

        let FUNCTION_HEADER  = """
        .globl    _\(self.name)

        _\(self.name):
        pushq    %rbp
        movq    %rsp, %rbp

        """

        let FUNCTION_FOOTER  = """
        popq %rbp
        ret

        """

        var content = ""

        for instr in self._instrs {
            content += instr.toAsm(table)
            content += "\n"

            if !(instr is Directive) {
                content += "\n"
            }
        }

        var result = FUNCTION_HEADER
        result += "sub $\(table.size), %rsp\n\n"
        result += content
        result += self.function_return_label + ":\n"

        if self._has_result {
            result += "movq \(table[Register("%@r0")])(%rbp), %rax\n"
        }

        result += "add $\(table.size), %rsp\n"
        result += FUNCTION_FOOTER

        return result
    }

    public var function_return_label: String {
        return ".\(self.name)____________return_block_of_this_function"
    }
}


// Returns all pseudo-registers occurring in raw.
public func registers_in(_ raw: String) -> [Register] {
    let regex = try! NSRegularExpression(pattern: "%@[jr0-9]+[lwd]?", options: [])
    let range = NSRange(location: 0, length: NSString(string: raw).length)

    var registers: [Register] = []

    for match in regex.matches(in: raw, options: [], range: range) {
        let substring = String(NSString(string: raw).substring(with: match.range))
        registers.append(Register(substring))
    }

    return registers
}


// Might be anything, even a label: everything that constitutes a line in assembler code
public class Instruction {

    // """ assembler source code line """
    public required init(_ line_number: Int, _ line: String) {
        self.line_number = line_number
        self.line = line
    }

    public let line_number: Int
    public let line: String

    public func toAsm(_ regs: RegisterTable) -> String {
        fatalError()
    }

    public var registers: [Register] {
        return registers_in(self.line)
    }

    // :return: A list of pseudo-registers which this instruction writes back to the frame.
    // Default is to write back all registers.
    public var writeback_registers: [Register] {
        return self.registers
    }

    public class func matches(_ line: String) -> Bool {
        return false
    }

    public static func match_line(_ line_number: Int, _ line: String) -> Instruction {
        let instruction_constrs = [
            CallInstruction.self,
            MultInstruction.self,
            DivInstruction.self,
            ShiftInstruction.self,
            ReturnInstruction.self,
            ThreeAddressCode.self,
            BasicInstructionNoWriteback.self,
            BasicInstruction.self
        ]

        for constr in instruction_constrs {
            if constr.matches(line) {
                return constr.init(line_number, line)
            }
        }

        fatalError("No instruction class matches line \(line_number): \(line)")
    }
}

// Syntax: instr [ <source 1> | <source 2> ] -> <target register>
public class ThreeAddressCode: Instruction {
    public override func toAsm(_ regs: RegisterTable) -> String {
        let regex = try! NSRegularExpression(pattern: "([a-z]+)\\s*\\[([^\\]]*)\\]\\s*->\\s*(%@[jr0-9]+[lwd]?)", options: [])
        let range = NSRange(location: 0, length: NSString(string: self.line).length)

        guard let match = regex.firstMatch(in: self.line, options: [], range: range) else {
            fatalError("Invalid three-address instruction")
        }

        let opcode = NSString(string: self.line).substring(with: match.range(at: 1))
        let args = NSString(string: self.line).substring(with: match.range(at: 2)).components(separatedBy: "|").map({ $0.trimmingCharacters(in: .whitespacesAndNewlines) })
        precondition(args.count == 2)
        let source1_raw = args[0]
        let source2_raw = args[1]
        let target = Register(NSString(string: self.line).substring(with: match.range(at: 3)))

        let reg_width = target.width
        let actual = self.get_actual_instruction(opcode, reg_width)

        return AsmUnit(regs).comment(self.line).loads(self.registers).move_from_anything_to_concrete_reg(source1_raw, "d").move_from_anything_to_concrete_reg(source2_raw, "a").instruction(actual).move_from_concrete("a", target).store(target).description
    }

    public override class func matches(_ line: String) -> Bool {
        return line.contains(" -> ")
    }

    public func get_actual_instruction(_ opcode: String, _ reg_width: RegWidth) -> String {
        return "\(opcode) \(ConcreteRegister("d", reg_width)), \(ConcreteRegister("a", reg_width))"
    }
}

// Syntax: [i]mul [ <source 1> | <source 2> ] -> <target register>
public class MultInstruction: ThreeAddressCode {
    public override class func matches(_ line: String) -> Bool {
        return ThreeAddressCode.matches(line) && (line.hasPrefix("mul") || line.hasPrefix("imul"))
    }

    public override func get_actual_instruction(_ opcode: String, _ reg_width: RegWidth) -> String {
        return "\(opcode) \(ConcreteRegister("d", reg_width))"
    }
}

// Syntax: [i]div [ <source 1> | <source 2> ] -> [ <target register div> | <target register mod> ]
public class DivInstruction: Instruction {
    public override func toAsm(_ regs: RegisterTable) -> String {
        let regex = try! NSRegularExpression(pattern: "([a-z]+)\\s*\\[([^\\]]*)\\]\\s*->\\s*\\[([^\\]]*)\\]", options: [])
        let range = NSRange(location: 0, length: NSString(string: self.line).length)

        guard let match = regex.firstMatch(in: self.line, options: [], range: range) else {
            fatalError("Invalid div instruction")
        }

        let opcode = NSString(string: self.line).substring(with: match.range(at: 1))
        let args = NSString(string: self.line).substring(with: match.range(at: 2)).components(separatedBy: "|").map({ $0.trimmingCharacters(in: .whitespacesAndNewlines) })
        let targets = NSString(string: self.line).substring(with: match.range(at: 3)).components(separatedBy: "|").map({ $0.trimmingCharacters(in: .whitespacesAndNewlines) })
        precondition(args.count == 2)
        precondition(targets.count == 2)
        let source1_raw = args[0]
        let source2_raw = args[1]
        let target_div = Register(targets[0])
        let target_mod = Register(targets[1])

        let reg_width = target_div.width

        let first = AsmUnit(regs)
            .comment(self.line)
            .loads(self.registers)
            .move_from_anything_to_concrete_reg(source1_raw, "a")
            .move_from_anything_to_concrete_reg(source2_raw, "b")
            .raw("cltd")
            .instruction("\(opcode) \(ConcreteRegister("b", reg_width))")
            .move_from_concrete("a", target_div)
            .store(target_div).description

        let second = AsmUnit(regs)
            .comment(self.line)
            .loads(self.registers)
            .move_from_anything_to_concrete_reg(source1_raw, "a")
            .move_from_anything_to_concrete_reg(source2_raw, "b")
            .raw("cltd")
            .instruction("\(opcode) \(ConcreteRegister("b", reg_width))")
            .move_from_concrete("d", target_mod)
            .store(target_mod).description

        return first + "\n" + second
    }

    public override class func matches(_ line: String) -> Bool {
        return ThreeAddressCode.matches(line) && (line.hasPrefix("div") || line.hasPrefix("idiv"))
    }
}

// Syntax: {shl,shr,sal,sar,rol,ror} [ <source 1> | <source 2> ] -> <target register>
public class ShiftInstruction: Instruction {
    public override func toAsm(_ regs: RegisterTable) -> String {
        let regex = try! NSRegularExpression(pattern: "([a-z]+)\\s*\\[([^\\]]*)\\]\\s*->\\s*(%@[jr0-9]+[lwd]?)", options: [])
        let range = NSRange(location: 0, length: NSString(string: self.line).length)

        guard let match = regex.firstMatch(in: self.line, options: [], range: range) else {
            fatalError("Invalid shift instruction")
        }

        let opcode = NSString(string: self.line).substring(with: match.range(at: 1))
        let args = NSString(string: self.line).substring(with: match.range(at: 2)).components(separatedBy: "|").map({ $0.trimmingCharacters(in: .whitespacesAndNewlines) })
        precondition(args.count == 2)
        let source1_raw = args[0]
        let source2_raw = args[1]
        let target = Register(NSString(string: self.line).substring(with: match.range(at: 3)))

        let source1 = Register(source1_raw)
        let source2: String
        if source2_raw.contains("%") {
            source2 = "\(regs[Register(source2_raw)])(%rbp)"
        }
        else {
            source2 = source2_raw
        }

        let instr = "\(opcode) %cl, \(source1_raw)"

        return AsmUnit(regs)
            .comment(self.line)
            .load(source1)
            .reserve_register(target)
            .raw("movb \(source2), %cl")
            .instruction(instr)
            .move(source1, target)
            .store(target).description
    }

    public override class func matches(_ line: String) -> Bool {
        return ThreeAddressCode.matches(line) && ["shl", "shr", "sal", "sar", "rol", "ror"].contains(String(line.prefix(3)))
    }
}

// Do not have a counter part in x86
public class MetaInstruction: Instruction {}

// Syntax: call <function name> [ <argument> | <argument> | ... | <argument> ] (-> <result register>)?
public class CallInstruction: MetaInstruction {
    public override func toAsm(_ regs: RegisterTable) -> String {
        let regex = try! NSRegularExpression(pattern: "call\\s+([a-zA-Z._$][\\w$_]*)\\s*\\[([^\\]]*)\\]\\s*(->\\s*(%@[jr0-9]+[lwd]?))?", options: [])
        let range = NSRange(location: 0, length: NSString(string: self.line).length)

        guard let match = regex.firstMatch(in: self.line, options: [], range: range) else {
            fatalError("Invalid call instruction")
        }

        let function_name = NSString(string: self.line).substring(with: match.range(at: 1))
        let args: [String]
        if NSString(string: self.line).substring(with: match.range(at: 2)).trimmingCharacters(in: .whitespacesAndNewlines).count > 0 {
            args = NSString(string: self.line).substring(with: match.range(at: 2)).components(separatedBy: "|").map({ $0.trimmingCharacters(in: .whitespacesAndNewlines) })
        }
        else {
            args = []
        }
        let result_raw: String?
        if match.numberOfRanges > 4, match.range(at: 4).location != NSNotFound {
            result_raw = NSString(string: self.line).substring(with: match.range(at: 4))
        }
        else {
            result_raw = nil
        }

        let asm_unit = AsmUnit(regs, [])
        asm_unit.comment(self.line)
        for arg in args.reversed() {
            let pushq = Instruction.match_line(self.line_number, "pushq \(arg)")
            asm_unit.raw(pushq.toAsm(regs))
        }

        asm_unit.raw("callq _\(function_name)")

        if let result_raw = result_raw {
            asm_unit.raw("movq %rax, \(regs[Register(result_raw)])(%rbp)")
        }

        for _ in args {
            asm_unit.raw("popq %rbx")
        }

        return asm_unit.description
    }

    public override class func matches(_ line: String) -> Bool {
        return line.hasPrefix("call ") && line.contains("[")
    }
}

// Trivially convertable to x86, only pseudo-registers are allocated.
public class BasicInstruction: Instruction {
    public override func toAsm(_ regs: RegisterTable) -> String {
        return AsmUnit(regs).comment(self.line).loads(self.registers).instruction(self.line).stores(self.writeback_registers).description
    }

    public override class func matches(_ line: String) -> Bool {
        return true
    }
}

public class BasicInstructionNoWriteback: BasicInstruction {
    public override var writeback_registers: [Register] {
        return []
    }

    public override class func matches(_ line: String) -> Bool {
        return line.hasPrefix("cmp") || line.hasPrefix("test") || line.hasPrefix("push")
    }
}

public class Directive: Instruction {
    public override func toAsm(_ regs: RegisterTable) -> String {
        return self.line
    }
}

// Return instruction, syntax `return` that jumps to the return block
public class ReturnInstruction: Instruction {
    public override func toAsm(_ regs: RegisterTable) -> String {
        return "jmp \(regs.function.function_return_label)"
    }

    public override class func matches(_ line: String) -> Bool {
        return line.hasPrefix("return")
    }
}

// Converts lines (in pseudo-assembler) to actual assembler.
public func process_lines(_ lines: [String]) -> String {
    var pre_lines: [String] = []
    var functions: [Function] = []
    var cur_func: Function? = nil

    for (i, var line) in lines.map({ $0.trimmingCharacters(in: .whitespacesAndNewlines) }).enumerated() {
        if line.contains("/"+"*") {
            let split = line.components(separatedBy: "/"+"*")
            precondition(split.count == 2)
            line = split[0]
            let comment = split[1]

            if let cur_func = cur_func {
                cur_func.extend([Directive(i, "/"+"*" + comment)])
            }
            else {
                pre_lines.append("/"+"*" + comment)
            }
        }

        if line.trimmingCharacters(in: .whitespacesAndNewlines).count == 0 {
            if let cur_func = cur_func {
                cur_func.extend([Directive(i, "")])
            }
            else {
                pre_lines.append("")
            }
        }
        else if line.hasPrefix(".function") {
            if cur_func != nil {
                print("Warning: Inserted missing .endfunction before line \(i)")
            }
            cur_func = Function(i, line)
            functions.append(cur_func!)
        }
        else if line.hasPrefix(".endfunction") {
            cur_func = nil
        }
        else if cur_func == nil {
            pre_lines.append(line)
        }
        else if line.hasPrefix(".") {
            cur_func!.extend([Directive(i, line)])
        }
        else {
            cur_func!.extend([Instruction.match_line(i, line)])
        }
    }

    var result = pre_lines.joined(separator: "\n") + """
    .text
    """

    for f in functions {
        result += "\n\n"
        result += f.toAsm()
    }

    return result
}

public func process(_ code: String) -> String {
    return process_lines(code.components(separatedBy: .newlines))
}
*/
