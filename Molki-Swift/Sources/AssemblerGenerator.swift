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

        // caller pushes arguments on stack, their offset is fixed
        for index in 0..<function.numberOfParameters {

            // 2 slots reserved:
            //  - return address
            //  - old base pointer? (pushq %rbp in prologue)
            self.table[.numbered(index)] = 16 + 8 * index
        }

        if function.hasReturnValue {
            self.reserveStackSlot(for: .reserved)
        }

        for instruction in function.instructions {
            self.handle(instruction)
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

        if function.hasReturnValue {
            print("movq \(self.table[.reserved]!)(%rbp), %rax")
        }

        print("\n/* epilogue of function \(function.name) */")
        print("movq %rbp, %rsp")
        print("popq %rbp")
        print("ret")

        self.lines = lines
    }



    private var body: [String] = []
    public var lines: [String] = []

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

            // TODO: how do we know how many bytes to load if argument is memory?
            let register = X86Register.r8.with(argument.width!)

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

            // TODO: how do we know how many bytes to write if result is memory?
            let register = X86Register.rax.with(result.width!)
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
        self.emit("imull \(src), \(dest)") // TODO: imul or mul?
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

    func store(_ register: RegisterValue<X86Register>, to value: Result<Pseudoregister>) {
        switch value {
        case .register(let value):
            self.store(register, to: value)
        case .memory(let value):
            self.store(register, to: value)
        }
    }

    func store(_ register: RegisterValue<X86Register>, to value: RegisterValue<Pseudoregister>) {
        precondition(register.width == value.width, "register width mismatch")

        let offset = self.reserveStackSlot(for: value.register)

        switch register.width {
        case .byte: self.emit("movb \(register), \(offset)(%rbp)", comment: "write pseudoregister \(value)")
        case .word: self.emit("movw \(register), \(offset)(%rbp)", comment: "write pseudoregister \(value)")
        case .double: self.emit("movl \(register), \(offset)(%rbp)", comment: "write pseudoregister \(value)")
        case .quad: self.emit("movq \(register), \(offset)(%rbp)", comment: "write pseudoregister \(value)")
        }

        self.lastWrittenWidth[value.register] = register.width
    }

    func store(_ register: RegisterValue<X86Register>, to value: MemoryValue<Pseudoregister>) {
        switch value {
        case .relative(base: let base, offset: let offset):
            let base_reg = X86Register.r12.with(base.width)
            precondition(register != base_reg)

            self.loadPseudoregister(base, into: base_reg)

            switch register.width {
            case .byte:
                self.emit("movb \(register), \(offset)(\(base_reg))")
            case .word:
                self.emit("movw \(register), \(offset)(\(base_reg))")
            case .double:
                self.emit("movl \(register), \(offset)(\(base_reg))")
            case .quad:
                self.emit("movq \(register), \(offset)(\(base_reg))")
            }
        case .indexed(base: let base , index: let index, scale: let scale, offset: let offset):
            let base_reg = X86Register.r12.with(base.width)
            let index_reg = X86Register.r13.with(index.width)
            precondition(register != base_reg && register != index_reg)

            // TODO: x86 does not allow different widths for base and index, but we need it for array access

            self.loadPseudoregister(base, into: base_reg)
            self.loadPseudoregister(index, into: index_reg)

            switch register.width {
            case .byte:
                self.emit("movb \(register), \(offset)(\(base_reg), \(index_reg), \(scale))")
            case .word:
                self.emit("movw \(register), \(offset)(\(base_reg), \(index_reg), \(scale))")
            case .double:
                self.emit("movl \(register), \(offset)(\(base_reg), \(index_reg), \(scale))")
            case .quad:
                self.emit("movq \(register), \(offset)(\(base_reg), \(index_reg), \(scale))")
            }
        }
    }

    func load(_ value: Argument<Pseudoregister>, into register: RegisterValue<X86Register>) {
        switch value {
        case .constant(let value):
            self.loadConstant(value, into: register)
        case .register(let pseudoregister):
            self.loadPseudoregister(pseudoregister, into: register)
        case .memory(let address):
            self.loadMemory(address, into: register)
        }
    }

    func loadConstant(_ value: ConstantValue, into register: RegisterValue<X86Register>) {
        switch register.width {
        case .byte: self.emit("movb \(value), \(register)", comment: "load constant \(value)")
        case .word: self.emit("movw \(value), \(register)", comment: "load constant \(value)")
        case .double: self.emit("movl \(value), \(register)", comment: "load constant \(value)")
        case .quad: self.emit("movq \(value), \(register)", comment: "load constant \(value)")
        }
    }

    func loadPseudoregister(_ value: RegisterValue<Pseudoregister>, into register: RegisterValue<X86Register>) {
        precondition(self.table[value.register] != nil, "attempting to load uninitialized pseudoregister")
        precondition(register.width == value.width, "register width mismatch")

        if let writtenWidth = self.lastWrittenWidth[value.register] {
            precondition(writtenWidth == register.width, "pseudoreg was previously assigned \(writtenWidth), now attempting to load as \(register.width)")
        }
        else {
            print("missing information about pseudoreg width: is \(value.register) argument?")
        }

        let offset = self.reserveStackSlot(for: value.register)

        switch register.width {
        case .byte: self.emit("movb \(offset)(%rbp), \(register)", comment: "read pseudoregister \(value)")
        case .word: self.emit("movw \(offset)(%rbp), \(register)", comment: "read pseudoregister \(value)")
        case .double: self.emit("movl \(offset)(%rbp), \(register)", comment: "read pseudoregister \(value)")
        case .quad: self.emit("movq \(offset)(%rbp), \(register)", comment: "read pseudoregister \(value)")
        }
    }

    func loadMemory(_ address: MemoryValue<Pseudoregister>, into register: RegisterValue<X86Register>) {
        switch address {
        case .relative(base: let base, offset: let offset):
            let base_reg = X86Register.r8.with(base.width)

            self.loadPseudoregister(base, into: base_reg)

            switch register.width {
            case .byte:
                self.emit("movb \(offset)(\(base_reg)), \(register)")
            case .word:
                self.emit("movw \(offset)(\(base_reg)), \(register)")
            case .double:
                self.emit("movl \(offset)(\(base_reg)), \(register)")
            case .quad:
                self.emit("movq \(offset)(\(base_reg)), \(register)")
            }
        case .indexed(base: let base, index: let index, scale: let scale, offset: let offset):
            let base_reg = X86Register.r8.with(base.width)
            let index_reg = X86Register.r9.with(index.width)

            // TODO: x86 does not allow different widths for base and index, but we need it for array access

            self.loadPseudoregister(base, into: base_reg)
            self.loadPseudoregister(index, into: index_reg)

            switch register.width {
            case .byte:
                self.emit("movb \(offset)(\(base_reg), \(index_reg), \(scale)), \(register)")
            case .word:
                self.emit("movl \(offset)(\(base_reg), \(index_reg), \(scale)), \(register)")
            case .double:
                self.emit("movw \(offset)(\(base_reg), \(index_reg), \(scale)), \(register)")
            case .quad:
                self.emit("movq \(offset)(\(base_reg), \(index_reg), \(scale)), \(register)")
            }
        }
    }
}
