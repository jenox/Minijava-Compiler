//
//  AssemblerGenerator.swift
//  Molki
//
//  Created by Christian Schnorr on 31.12.18.
//  Copyright © 2018 Christian Schnorr. All rights reserved.
//

import Foundation


/*
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
            self.table[.identified(index)] = 16 + 8 * index
        }

        if function.hasReturnValue {
            self.reserveStackSlot(for: .returnValue)
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
            print("movq \(self.table[.returnValue]!)(%rbp), %rax")
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
        case .oneAddressCodeInstruction(let instruction):
            self.handle(instruction)
        case .twoAddressCodeInstruction(let instruction):
            self.handle(instruction)
        case .threeAddressCodeInstruction(let instruction):
            self.handle(instruction)
        case .fourAddressCodeInstruction(let instruction):
            self.handle(instruction)
        case .jumpInstruction(let instruction):
            self.handle(instruction)
        case .callInstruction(let instruction):
            self.handle(instruction)
        case .labelInstruction(let instruction):
            self.handle(instruction)
        }
    }

    private func handle(_ instruction: OneAddressCodeInstruction) {
        self.emit("\n/* \(instruction) */")
        self.load(instruction.first, into: "%r8")
        self.emit("\(instruction.operation) %r8")
        self.store("%r8", to: instruction.first)
    }

    private func handle(_ instruction: TwoAddressCodeInstruction) {
        if instruction.operation.hasPrefix("cmp") || instruction.operation.hasPrefix("test") {
            self.emit("\n/* \(instruction) */")
            self.load(instruction.first, into: "%r8")
            self.load(instruction.second, into: "%r9")
            self.emit("\(instruction.operation) %r8, %r9")
        }
        else if instruction.operation.hasPrefix("mov") {
            self.emit("\n/* \(instruction) */")
            self.load(instruction.first, into: "%r8")
            self.store("%r8", to: instruction.second)
        }
        else {
            self.emit("\n/* \(instruction) */")
            self.emit("/* warning: unrecognized two address code instruction “\(instruction.operation)” */")
            self.load(instruction.first, into: "%r8")
            self.load(instruction.second, into: "%r9")
            self.emit("\(instruction.operation) %r8, %r9")
            self.store("%r8", to: instruction.first)
            self.store("%r9", to: instruction.second)
        }
    }

    private func handle(_ instruction: ThreeAddressCodeInstruction) {
        self.emit("\n/* \(instruction) */")
        self.load(instruction.first, into: "%r8")
        self.load(instruction.second, into: "%r9")
        self.emit("\(instruction.operation) %r8, %r9")
        self.store("%r9", to: instruction.third)
    }

    private func handle(_ instruction: FourAddressCodeInstruction) {
        precondition(["div", "idiv"].contains(instruction.operation))

        self.emit("\n/* \(instruction) */")
        self.load(instruction.first, into: "%rax")
        self.load(instruction.second, into: "%rbx")
        self.emit("cltd")
        self.emit("\(instruction.operation) %rbx")
        self.store("%rax", to: instruction.third)
        self.store("%rdx", to: instruction.fourth)
    }

    private func handle(_ instruction: JumpInstruction) {
        self.emit("\n/* \(instruction) */")
        self.emit("\(instruction.operation) \(instruction.target)")
    }

    private func handle(_ instruction: CallInstruction) {
        self.emit("\n/* \(instruction) */")

        let needsDummyArgument = instruction.arguments.count.isMultiple(of: 2)

        if needsDummyArgument {
            self.emit("pushq $4", comment: "dummy argument to align")
        }

        for (index, argument) in instruction.arguments.enumerated().reversed() {
            self.load(argument, into: "%r8")
            self.emit("pushq %r8", comment: "push argument #\(index) onto stack")
        }

        #if os(Linux)
        self.emit("callq \(instruction.name)")
        #else
        self.emit("callq \(instruction.name)")
        #endif

        for index in instruction.arguments.indices {
            self.emit("popq %rbx", comment: "pop argument #\(index) from stack")
        }

        if needsDummyArgument {
            self.emit("popq %rbx", comment: "pop dummy argument")
        }

        if let returnValue = instruction.returnValue {
            self.store("%rax", to: returnValue)
        }
    }

    private func handle(_ instruction: LabelInstruction) {
        self.emit("\n/* \(instruction) */")
        self.emit("\(instruction.name):")
    }





    // MARK: - Spilling

    /// pseudoregister -> frame offset
    private var table: [Register: Int] = [:]
    private var currentOffset = 0

    @discardableResult
    private func reserveStackSlot(for pseudoregister: Register) -> Int {
        if let offset = self.table[pseudoregister] {
            return offset
        }
        else {
            self.currentOffset -= 8
            self.table[pseudoregister] = self.currentOffset
            return self.currentOffset
        }
    }

    func store(_ register: String, to value: Value) {
        switch value {
        case .constant:
            fatalError("Cannot write to constant")
        case .register(let pseudoregister):
            self.store(register, to: pseudoregister)
        case .memory(let address):
            switch address {
            case .relative(base: let base, offset: let offset):
                precondition(register != "%r12")
                self.loadPseudoregister(base, into: "%r12")
                self.emit("movq \(register), \(offset)(%r12)", comment: "dereference \(base) + \(offset)")
            case .indexed(base: let base , index: let index, scale: let scale, offset: let offset):
                precondition(register != "%r12" && register != "%r13")
                self.loadPseudoregister(base, into: "%r12")
                self.loadPseudoregister(index, into: "%r13")
                self.emit("movq \(register), \(offset)(%r12, %r13, \(scale))")
            }
        }
    }

    func store(_ register: String, to value: RegisterValue) {
        //        self.emit("movq \(register), ")
        let offset = self.reserveStackSlot(for: value.register)

        switch value.width {
        case .byte: self.emit("movb \(register)l, \(offset)(%rbp)", comment: "write pseudoregister \(value)")
        case .word: self.emit("movw \(register)w, \(offset)(%rbp)", comment: "write pseudoregister \(value)")
        case .double: self.emit("movl \(register)d, \(offset)(%rbp)", comment: "write pseudoregister \(value)")
        case .quad: self.emit("movq \(register), \(offset)(%rbp)", comment: "write pseudoregister \(value)")
        }
    }

    func load(_ value: Value, into register: String) {
        switch value {
        case .constant(let value):
            self.loadConstant(value, into: register)
        case .register(let pseudoregister):
            self.loadPseudoregister(pseudoregister, into: register)
        case .memory(let address):
            self.loadMemory(address, into: register)
        }
    }

    func loadConstant(_ value: Int, into register: String) {
        precondition(register.hasPrefix("%"))

        // e.g. `movq $2, %r8`
        self.emit("movq $\(value), \(register)", comment: "load constant \(value)")
    }

    func loadPseudoregister(_ value: RegisterValue, into register: String) {
        precondition(register.hasPrefix("%"))

        // https://stackoverflow.com/questions/1898834/why-would-one-use-movl-1-eax-as-opposed-to-say-movb-1-eax
        // command and register suffixes
        // byte b l
        // word w w
        // double l d
        // quad q -

        if self.table[value.register] == nil {
            self.emit("/* warning: use of undeclared pseudoregister \(value.register) */")
        }

        let offset = self.reserveStackSlot(for: value.register)

        switch value.width {
        case .byte: self.emit("movb \(offset)(%rbp), \(register)l", comment: "read pseudoregister \(value)")
        case .word: self.emit("movw \(offset)(%rbp), \(register)w", comment: "read pseudoregister \(value)")
        case .double: self.emit("movl \(offset)(%rbp), \(register)d", comment: "read pseudoregister \(value)")
        case .quad: self.emit("movq \(offset)(%rbp), \(register)", comment: "read pseudoregister \(value)")
        }
    }

    func loadMemory(_ address: MemoryAddress, into register: String) {
        switch address {
        case .relative(base: let base, offset: let offset):
            self.loadPseudoregister(base, into: "%r8")
            self.emit("movq \(offset)(%r8), \(register)", comment: "dereference \(base) + \(offset)")
        case .indexed(base: let base, index: let index, scale: let scale, offset: let offset):
            self.loadPseudoregister(base, into: "%r8")
            self.loadPseudoregister(index, into: "%r9")
            self.emit("movq \(offset)(%r8, %r9, \(scale)), \(register)")
        }
    }
}
*/
