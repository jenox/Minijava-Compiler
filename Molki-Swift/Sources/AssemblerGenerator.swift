//
//  AssemblerGenerator.swift
//  Molki
//
//  Created by Christian Schnorr on 31.12.18.
//  Copyright © 2018 Christian Schnorr. All rights reserved.
//

import Foundation


// https://cs.brown.edu/courses/cs033/docs/guides/x64_cheatsheet.pdf
class AssemblerGenerator {

    // load constant into reg
    // load pseudoreg into reg
    // load memory into reg

    // to keep track of which pseudoreg is loaded into which reg
    // remember to nil out when loading constant/mem
    //    var map: [String: Register] = [:]

    init(function: Function) {
        self.function = function

        // caller pushes arguments on stack, their offset is fixed
        for (index, width) in function.parameterWidths.enumerated() {

            // 2 slots reserved:
            //  - return address
            //  - old base pointer? (pushq %rbp in prologue)
            self.table[.numbered(index)] = 16 + 8 * index
            self.lastWrittenWidth[.numbered(index)] = width
        }

        if let width = function.returnValueWidth {
            self.reserveStackSlot(for: .reserved)
            self.lastWrittenWidth[.reserved] = width
        }

        for instruction in function.instructions {
            self.currentInstruction = instruction
            self.handle(instruction)
            self.currentInstructionIndex += 1
        }

        var lines: [String] = []
        func print(_ text: String) {
            lines.append(text)
        }

        let size = (abs(self.currentOffset) - 8).ceiled(toMultipleOf: 16) + 8

        print("\n/* prologue of function \(function.name) */")
        #if os(Linux)
        print(".globl \(function.name)")
        print(".type \(function.name), @function")
        print("\(function.name):")
        #else
        print(".globl \(function.name)")
        print("\(function.name):")
        #endif
        print("pushq %rbp")
        print("movq %rsp, %rbp")
        print("sub $\(size), %rsp")

        for line in self.body {
            print(line)
        }

        if let width = function.returnValueWidth {
            let reg = X86Register.rax

            // cant use loadPseudoregister here cause we are no longer buffering
            switch width {
            case .byte:
                print("movb \(self.table[.reserved]!)(%rbp), \(reg.name(for: width))")
            case .word:
                print("movw \(self.table[.reserved]!)(%rbp), \(reg.name(for: width))")
            case .double:
                print("movl \(self.table[.reserved]!)(%rbp), \(reg.name(for: width))")
            case .quad:
                print("movq \(self.table[.reserved]!)(%rbp), \(reg.name(for: width))")
            }
        }

        print("\n/* epilogue of function \(function.name) */")
        print("movq %rbp, %rsp")
        print("popq %rbp")
        print("ret")

        self.lines = lines
    }


    private let function: Function
    private var currentInstruction: Instruction? = nil
    private var currentInstructionIndex: Int = 0
    private var body: [String] = []
    public var lines: [String] = []

    private func precondition(_ condition: Bool, _ message: @autoclosure () -> String) {
        if !condition {
            print("Assertion Error: \(message())")
            print("Current Function:", self.function.name)
            print("Current Instruction:", self.currentInstruction!, "(index \(self.currentInstructionIndex + 1))")
            exit(-1)
        }
    }

    func emit(_ assembler: String, comment: String? = nil) {
        if let comment = comment {
            self.body.append(assembler + String(repeating: " ", count: max(0, 24 - assembler.count)) + " " + "/* \(comment) */")
        }
        else {
            self.body.append(assembler)
        }
    }




    private func handle(_ instruction: Instruction) {
        switch instruction {
        case .labelInstruction(let instruction):
            self.handle(instruction)
        case .jumpInstruction(let instruction):
            self.handle(instruction)
        case .callInstruction(let instruction):
            self.handle(instruction)
        case .moveInstruction(let instruction):
            self.handle(instruction)
        case .comparisonInstruction(let instruction):
            self.handle(instruction)
        case .additionInstruction(let instruction):
            self.handle(instruction)
        case .subtractionInstruction(let instruction):
            self.handle(instruction)
        case .multiplicationInstruction(let instruction):
            self.handle(instruction)
        case .divisionInstruction(let instruction):
            self.handle(instruction)
        case .numericNegationInstruction(let instruction):
            self.handle(instruction)
        case .logicalNegationInstruction(let instruction):
            self.handle(instruction)
        }
    }

    private func handle(_ instruction: LabelInstruction) {
        self.emit("\n/* \(instruction) */")
        self.emit("\(instruction.name):")
    }

    private func handle(_ instruction: JumpInstruction) {
        self.emit("\n/* \(instruction) */")
        self.emit("\(instruction.condition.rawValue) \(instruction.target)")
    }

    private func handle(_ instruction: CallInstruction) {
        self.emit("\n/* \(instruction) */")

        let needsDummyArgument = instruction.arguments.count.isMultiple(of: 2)

        if needsDummyArgument {
            self.emit("pushq $0", comment: "dummy argument to align stack")
        }

        for (index, argument) in instruction.arguments.enumerated().reversed() {
            let register = X86Register.r8.with(argument.width)

            self.load(argument, into: register)
            self.emit("pushq %r8", comment: "push argument #\(index) onto stack")
        }

        #if os(Linux)
        self.emit("callq \(instruction.target)")
        #else
        self.emit("callq \(instruction.target)")
        #endif

        for index in instruction.arguments.indices {
            self.emit("popq %rbx", comment: "pop argument #\(index) from stack")
        }

        if needsDummyArgument {
            self.emit("popq %rbx", comment: "pop dummy argument from stack")
        }

        if let result = instruction.result {
            let register = X86Register.rax.with(result.width)
            self.store(register, to: result)
        }
    }

    private func handle(_ instruction: MoveInstruction) {
        let reg = X86Register.r8.with(instruction.width)

        self.emit("\n/* \(instruction) */")
        self.load(instruction.source, into: reg)
        self.store(reg, to: instruction.target)
    }

    private func handle(_ instruction: ComparisonInstruction) {
        let lhs_reg = X86Register.r8.with(instruction.width)
        let rhs_reg = X86Register.r9.with(instruction.width)

        self.emit("\n/* \(instruction) */")
        self.load(instruction.lhs, into: lhs_reg)
        self.load(instruction.rhs, into: rhs_reg)

        switch instruction.width {
        case .byte: self.emit("cmpb \(rhs_reg), \(lhs_reg)")
        case .word: self.emit("cmpw \(rhs_reg), \(lhs_reg)")
        case .double: self.emit("cmpl \(rhs_reg), \(lhs_reg)")
        case .quad: self.emit("cmpq \(rhs_reg), \(lhs_reg)")
        }
    }

    private func handle(_ instruction: AdditionInstruction) {
        let src = X86Register.r8.with(.double)
        let dest = X86Register.r9.with(.double)

        self.emit("\n/* \(instruction) */")
        self.load(instruction.augend, into: dest)
        self.load(instruction.addend, into: src)
        self.emit("addl \(src), \(dest)")
        self.store(dest, to: instruction.sum)
    }

    private func handle(_ instruction: SubtractionInstruction) {
        let src = X86Register.r8.with(.double)
        let dest = X86Register.r9.with(.double)

        self.emit("\n/* \(instruction) */")
        self.load(instruction.minuend, into: dest)
        self.load(instruction.subtrahend, into: src)
        self.emit("subl \(src), \(dest)")
        self.store(dest, to: instruction.difference)
    }

    private func handle(_ instruction: MultiplicationInstruction) {
        let src = X86Register.r8.with(.double)
        let dest = X86Register.r9.with(.double)

        self.emit("\n/* \(instruction) */")
        self.load(instruction.multiplicand, into: dest)
        self.load(instruction.multiplier, into: src)
        self.emit("imull \(src), \(dest)")
        self.store(dest, to: instruction.product)
    }

    private func handle(_ instruction: DivisionInstruction) {
        let reg = X86Register.r8.with(.double)

        self.emit("\n/* \(instruction) */")
        self.load(instruction.dividend, into: X86Register.rax.with(.double))
        self.load(instruction.divisor, into: reg)
        self.emit("cltd", comment: "sing-extend eax -> edx:eax")
        self.emit("idivl \(reg)")
        self.store(X86Register.rax.with(.double), to: instruction.quotient)
        self.store(X86Register.rdx.with(.double), to: instruction.remainder)
    }

    private func handle(_ instruction: NumericNegationInstruction) {
        let reg = X86Register.r8.with(.double)

        self.emit("\n/* \(instruction) */")
        self.load(instruction.source, into: reg)
        self.emit("negl \(reg)")
        self.store(reg, to: instruction.target)
    }

    private func handle(_ instruction: LogicalNegationInstruction) {
        let reg = X86Register.r8.with(.byte)

        self.emit("\n/* \(instruction) */")
        self.load(instruction.source, into: reg)
        self.emit("notb \(reg)")
        self.store(reg, to: instruction.target)
    }



    // MARK: - Spilling

    /// pseudoregister -> frame offset
    private var table: [Pseudoregister: Int] = [:]
    private var lastWrittenWidth: [Pseudoregister: RegisterWidth] = [:]
    private var currentOffset = 0

    @discardableResult
    private func reserveStackSlot(for pseudoregister: Pseudoregister) -> Int {
        if let offset = self.table[pseudoregister] {
            return offset
        }
        else {
            self.currentOffset -= 8
            self.table[pseudoregister] = self.currentOffset
            return self.currentOffset
        }
    }




    // MARK: - Pseudomoves

    private func store(_ register: RegisterValue<X86Register>, to value: Result<Pseudoregister>) {
        switch value {
        case .register(let value):
            self.store(register, to: value)
        case .memory(let value):
            self.store(register, to: value)
        }
    }

    private func store(_ register: RegisterValue<X86Register>, to value: RegisterValue<Pseudoregister>) {
        self.precondition(register.width == value.width, "value width mismatch")

        if let prev = self.lastWrittenWidth[value.register] {
            self.precondition(value.width == prev, "cannot write pseudoreg \(value.register) (prev written as \(prev)) as \(value.width)")
        }

        let offset = self.reserveStackSlot(for: value.register)
        let address = MemoryAddress.relative(base: .basePointer, offset: offset)

        self.move(register, to: address, comment: "write register to pseudoregister \(value)")

        self.lastWrittenWidth[value.register] = register.width
    }

    private func store(_ register: RegisterValue<X86Register>, to value: MemoryValue<Pseudoregister>) {
        self.precondition(register.width == value.width, "value width mismatch")

        let address: MemoryAddress<X86Register>

        switch value.address {
        case .relative(base: let base, offset: let offset):
            let base_reg = X86Register.r12.with(base.width)

            self.precondition(register != base_reg, "")

            self.loadPseudoregister(base, into: base_reg)

            address = MemoryAddress.relative(base: base_reg, offset: offset)
        case .indexed(base: let base , index: let index, scale: let scale, offset: let offset):
            let address_width = max(base.width, index.width)
            let base_reg = X86Register.r12.with(address_width)
            let index_reg = X86Register.r13.with(address_width)

            self.precondition(register != base_reg && register != index_reg, "")

            self.loadPseudoregister(base, extendInto: base_reg)
            self.loadPseudoregister(index, extendInto: index_reg)

            address = MemoryAddress.indexed(base: base_reg, index: index_reg, scale: scale, offset: offset)
        }

        self.move(register, to: address, comment: "write register to pseudomemory \(value)")
    }

    private func load(_ value: Argument<Pseudoregister>, into register: RegisterValue<X86Register>) {
        switch value {
        case .constant(let value):
            self.loadConstant(value, into: register)
        case .register(let pseudoregister):
            self.loadPseudoregister(pseudoregister, into: register)
        case .memory(let address):
            self.loadMemory(address, into: register)
        }
    }

    private func loadConstant(_ value: ConstantValue, into register: RegisterValue<X86Register>) {
        self.precondition(value.width == register.width, "value width mismatch")

        self.move(value.value, to: register, comment: "write constant \(value) to register")
    }

    private func loadPseudoregister(_ value: RegisterValue<Pseudoregister>, into register: RegisterValue<X86Register>) {
        self.precondition(value.width == register.width, "value width mismatch")

        if let prev = self.lastWrittenWidth[value.register] {
            self.precondition(value.width == prev, "cannot read pseudoreg \(value.register) (prev written as \(prev)) as \(value.width)")
        }

        let offset = self.reserveStackSlot(for: value.register)
        let address = MemoryAddress.relative(base: .basePointer, offset: offset)

        self.move(address, to: register, comment: "write pseudoregister \(value) to register")
    }

    private func loadPseudoregister(_ value: RegisterValue<Pseudoregister>, extendInto register: RegisterValue<X86Register>) {
        self.precondition(value.width <= register.width, "value width mismatch")

        self.loadPseudoregister(value, into: register.with(value.width))

        if value.width < register.width {
            self.emit("movsx \(register.with(value.width)), \(register)", comment: "sign-extend pseudoregister \(value)")
        }
    }

    private func loadMemory(_ value: MemoryValue<Pseudoregister>, into register: RegisterValue<X86Register>) {
        self.precondition(value.width == register.width, "value width mismatch")

        let address: MemoryAddress<X86Register>

        switch value.address {
        case .relative(base: let base, offset: let offset):
            let base_reg = X86Register.r8.with(base.width)

            self.loadPseudoregister(base, into: base_reg)

            address = MemoryAddress.relative(base: base_reg, offset: offset)
        case .indexed(base: let base, index: let index, scale: let scale, offset: let offset):
            let address_width = max(base.width, index.width)
            let base_reg = X86Register.r8.with(address_width)
            let index_reg = X86Register.r9.with(address_width)

            self.loadPseudoregister(base, extendInto: base_reg)
            self.loadPseudoregister(index, extendInto: index_reg)

            address = MemoryAddress.indexed(base: base_reg, index: index_reg, scale: scale, offset: offset)
        }

        self.move(address, to: register, comment: "read pseudoregister \(value) from memory")
    }


    // MARK: - x86 Moves

    private func move(_ register: RegisterValue<X86Register>, to other: RegisterValue<X86Register>, comment: String? = nil) {
        self.precondition(register.width == other.width, "register width mismatch")

        self.move(register.width, from: register.description, to: other.description, comment: comment)
    }

    private func move(_ register: RegisterValue<X86Register>, to address: MemoryAddress<X86Register>, comment: String? = nil) {
        self.move(register.width, from: register.description, to: address.description, comment: comment)
    }

    private func move(_ address: MemoryAddress<X86Register>, to register: RegisterValue<X86Register>, comment: String? = nil) {
        self.move(register.width, from: address.description, to: register.description, comment: comment)
    }

    private func move(_ constant: Int, to register: RegisterValue<X86Register>, comment: String? = nil) {
        self.move(register.width, from: "$\(constant)", to: register.description, comment: comment)
    }

    private func move(_ constant: Int, to address: MemoryAddress<X86Register>, width: RegisterWidth, comment: String? = nil) {
        self.move(width, from: "$\(constant)", to: address.description, comment: comment)
    }

    private func move(_ amount: RegisterWidth, from source: String, to target: String, comment: String? = nil) {
        switch amount {
        case .byte:
            self.emit("movb \(source), \(target)", comment: comment)
        case .word:
            self.emit("movw \(source), \(target)", comment: comment)
        case .double:
            self.emit("movl \(source), \(target)", comment: comment)
        case .quad:
            self.emit("movq \(source), \(target)", comment: comment)
        }
    }
}
