//
//  Instructions.swift
//  Molki
//
//  Created by Christian Schnorr on 14.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift



public protocol InstructionProtocol: CustomStringConvertible {
    var arguments: [Argument<Pseudoregister>] { get }
    var results: [Result<Pseudoregister>] { get }
}

extension InstructionProtocol {
    public var pseudoregisters: (read: Set<Pseudoregister>, written: Set<Pseudoregister>) {
        var read: Set<Pseudoregister> = []
        var written: Set<Pseudoregister> = []

        for argument in self.arguments {
            switch argument {
            case .constant:
                break
            case .register(let value):
                read.insert(value.register)
            case .memory(let value):
                read.formUnion(value.registers)
            }
        }

        for result in self.results {
            switch result {
            case .register(let value):
                written.insert(value.register)
            case .memory(let value):
                read.formUnion(value.registers)
            }
        }

        return (read: read, written: written)
    }
}

public enum Instruction: InstructionProtocol {
    case labelInstruction(LabelInstruction)
    case jumpInstruction(JumpInstruction)
    case callInstruction(CallInstruction)
    case moveInstruction(MoveInstruction)
    case comparisonInstruction(ComparisonInstruction)
    case additionInstruction(AdditionInstruction)
    case subtractionInstruction(SubtractionInstruction)
    case multiplicationInstruction(MultiplicationInstruction)
    case divisionInstruction(DivisionInstruction)
    case numericNegationInstruction(NumericNegationInstruction)
    case logicalNegationInstruction(LogicalNegationInstruction)

    public var arguments: [Argument<Pseudoregister>] {
        return self.rawInstruction.arguments
    }

    public var results: [Result<Pseudoregister>] {
        return self.rawInstruction.results
    }

    public var description: String {
        return self.rawInstruction.description
    }

    public var rawInstruction: InstructionProtocol {
        switch self {
        case .labelInstruction(let instruction):
            return instruction
        case .jumpInstruction(let instruction):
            return instruction
        case .callInstruction(let instruction):
            return instruction
        case .moveInstruction(let instruction):
            return instruction
        case .comparisonInstruction(let instruction):
            return instruction
        case .additionInstruction(let instruction):
            return instruction
        case .subtractionInstruction(let instruction):
            return instruction
        case .multiplicationInstruction(let instruction):
            return instruction
        case .divisionInstruction(let instruction):
            return instruction
        case .logicalNegationInstruction(let instruction):
            return instruction
        case .numericNegationInstruction(let instruction):
            return instruction
        }
    }
}




public class LabelInstruction: InstructionProtocol {
    public init(name: String) {
        self.name = name
    }

    private(set) public var name: String

    public var arguments: [Argument<Pseudoregister>] {
        return []
    }

    public var results: [Result<Pseudoregister>] {
        return []
    }

    public var description: String {
        return "\(self.name):"
    }
}

// jmp, jl, jle, jg, jge, je, jne
public class JumpInstruction: InstructionProtocol {
    public init(target: String, condition: JumpCondition) {
        self.target = target
        self.condition = condition
    }

    private(set) public var target: String
    private(set) public var condition: JumpCondition

    public var arguments: [Argument<Pseudoregister>] {
        return []
    }

    public var results: [Result<Pseudoregister>] {
        return []
    }

    public var description: String {
        return "\(self.condition.rawValue) \(self.target)"
    }
}

public enum JumpCondition: String {
    case unconditional = "jmp"
    case lessThan = "jl"
    case lessThanOrEqualTo = "jle"
    case greaterThan = "jg"
    case greaterThanOrEqualTo = "jge"
    case equalTo = "je"
    case notEqualTo = "jne"
}

// call
// Syntax: call f [ a | b | c | ... ] -> z
// Semantic: z = f(a, b, c, ...)
public class CallInstruction: InstructionProtocol {
    public init(target: String, arguments: [Argument<Pseudoregister>], result: Result<Pseudoregister>?) {
        self.target = target
        self.arguments = arguments
        self.result = result
    }

    private(set) public var target: String
    private(set) public var arguments: [Argument<Pseudoregister>]
    private(set) public var result: Result<Pseudoregister>?

    public var results: [Result<Pseudoregister>] {
        return self.result.flatMap({ [$0] }) ?? []
    }

    public var description: String {
        var description = "call \(self.target)"

        if !self.arguments.isEmpty {
            description += " [ "
            description += self.arguments.map({ "\($0)" }).joined(separator: " | ")
            description += " ]"
        }

        if let result = self.result {
            description += " -> \(result)"
        }

        return description
    }
}

// mov
// Syntax: movx a -> b
// Semantic: b := a
public class MoveInstruction: InstructionProtocol {
    public init(width: RegisterWidth, source: Argument<Pseudoregister>, target: Result<Pseudoregister>) {
        self.width = width
        self.source = source
        self.target = target
    }

    private(set) public var width: RegisterWidth
    private(set) public var source: Argument<Pseudoregister>
    private(set) public var target: Result<Pseudoregister>

    public var arguments: [Argument<Pseudoregister>] {
        return [self.source]
    }

    public var results: [Result<Pseudoregister>] {
        return [self.target]
    }

    public var description: String {
        switch self.width {
        case .byte: return "movb \(self.source) -> \(self.target)"
        case .word: return "movw \(self.source) -> \(self.target)"
        case .double: return "movl \(self.source) -> \(self.target)"
        case .quad: return "movq \(self.source) -> \(self.target)"
        }
    }
}

// Synax: cmpx [ a | b ]
// Semantic: flags := a ? b
public class ComparisonInstruction: InstructionProtocol {
    public init(width: RegisterWidth, lhs: Argument<Pseudoregister>, rhs: Argument<Pseudoregister>) {
        self.width = width
        self.lhs = lhs
        self.rhs = rhs
    }

    private(set) public var width: RegisterWidth
    private(set) public var lhs: Argument<Pseudoregister>
    private(set) public var rhs: Argument<Pseudoregister>

    public var arguments: [Argument<Pseudoregister>] {
        return [self.lhs, self.rhs]
    }

    public var results: [Result<Pseudoregister>] {
        return []
    }

    public var description: String {
        switch self.width {
        case .byte: return "cmpb [ \(self.lhs) | \(self.rhs) ]"
        case .word: return "cmpw [ \(self.lhs) | \(self.rhs) ]"
        case .double: return "cmpl [ \(self.lhs) | \(self.rhs) ]"
        case .quad: return "cmpq [ \(self.lhs) | \(self.rhs) ]"
        }
    }
}

// add
// Syntax: add [ a | b ] -> c
// Semantic: c := a + b
public class AdditionInstruction: InstructionProtocol {
    public init(augend: Argument<Pseudoregister>, addend: Argument<Pseudoregister>, sum: Result<Pseudoregister>) {
        self.augend = augend
        self.addend = addend
        self.sum = sum
    }

    private(set) public var augend: Argument<Pseudoregister>
    private(set) public var addend: Argument<Pseudoregister>
    private(set) public var sum: Result<Pseudoregister>

    public var arguments: [Argument<Pseudoregister>] {
        return [self.augend, self.addend]
    }

    public var results: [Result<Pseudoregister>] {
        return [self.sum]
    }

    public var description: String {
        return "addl [ \(self.augend) | \(self.addend) ] -> \(self.sum)"
    }
}

// sub
// Syntax: sub [ a | b ] -> c
// Semantic: c := a - b
public class SubtractionInstruction: InstructionProtocol {
    public init(minuend: Argument<Pseudoregister>, subtrahend: Argument<Pseudoregister>, difference: Result<Pseudoregister>) {
        self.minuend = minuend
        self.subtrahend = subtrahend
        self.difference = difference
    }

    private(set) public var minuend: Argument<Pseudoregister>
    private(set) public var subtrahend: Argument<Pseudoregister>
    private(set) public var difference: Result<Pseudoregister>

    public var arguments: [Argument<Pseudoregister>] {
        return [self.minuend, self.subtrahend]
    }

    public var results: [Result<Pseudoregister>] {
        return [self.difference]
    }

    public var description: String {
        return "subl [ \(self.minuend) | \(self.subtrahend) ] -> \(self.difference)"
    }
}

// imul
// Syntax: mul [ a | b ] -> c
// Semantic: c := a * b
public class MultiplicationInstruction: InstructionProtocol {
    public init(multiplicand: Argument<Pseudoregister>, multiplier: Argument<Pseudoregister>, product: Result<Pseudoregister>) {
        self.multiplicand = multiplicand
        self.multiplier = multiplier
        self.product = product
    }

    private(set) public var multiplicand: Argument<Pseudoregister>
    private(set) public var multiplier: Argument<Pseudoregister>
    private(set) public var product: Result<Pseudoregister>

    public var arguments: [Argument<Pseudoregister>] {
        return [self.multiplicand, self.multiplier]
    }

    public var results: [Result<Pseudoregister>] {
        return [self.product]
    }

    public var description: String {
        return "mull [ \(self.multiplicand) | \(self.multiplier) ] -> \(self.product)"
    }
}

// idiv
// Syntax: div [ a | b ] -> [ c | d ]
// Semantic: c := a / b
// Semantic: d := a % b
public class DivisionInstruction: InstructionProtocol {
    public init(dividend: Argument<Pseudoregister>, divisor: Argument<Pseudoregister>, quotient: Result<Pseudoregister>, remainder: Result<Pseudoregister>) {
        self.dividend = dividend
        self.divisor = divisor
        self.quotient = quotient
        self.remainder = remainder
    }

    private(set) public var dividend: Argument<Pseudoregister>
    private(set) public var divisor: Argument<Pseudoregister>
    private(set) public var quotient: Result<Pseudoregister>
    private(set) public var remainder: Result<Pseudoregister>

    public var arguments: [Argument<Pseudoregister>] {
        return [self.dividend, self.divisor]
    }

    public var results: [Result<Pseudoregister>] {
        return [self.quotient, self.remainder]
    }

    public var description: String {
        return "divl [ \(self.dividend) | \(self.divisor) ] -> [ \(self.quotient) | \(self.remainder) ]"
    }
}



// neg + b/w/l/q
// twos complement
// Syntax: neg a
// Semantic: a := -a
// width is assumed to be 32bit
public class NumericNegationInstruction: InstructionProtocol {
    public init(source: Argument<Pseudoregister>, target: Result<Pseudoregister>) {
        self.source = source
        self.target = target
    }

    private(set) public var source: Argument<Pseudoregister>
    private(set) public var target: Result<Pseudoregister>

    public var arguments: [Argument<Pseudoregister>] {
        return [self.source]
    }

    public var results: [Result<Pseudoregister>] {
        return [self.target]
    }

    public var description: String {
        return "negl \(self.source) -> \(self.target)"
    }
}

// not, notb, notw, notl, notq
// not works with all register sizes
// not does not work with memory (ambiguous)
// ones complement
// width is assumbed to be 8bit
// Syntax: not -> a
// Semantic: a := !a
public class LogicalNegationInstruction: InstructionProtocol {
    public init(source: Argument<Pseudoregister>, target: Result<Pseudoregister>) {
        self.source = source
        self.target = target
    }

    private(set) public var source: Argument<Pseudoregister>
    private(set) public var target: Result<Pseudoregister>

    public var arguments: [Argument<Pseudoregister>] {
        return [self.source]
    }

    public var results: [Result<Pseudoregister>] {
        return [self.target]
    }

    public var description: String {
        return "notb \(self.source) -> \(self.target)"
    }
}
