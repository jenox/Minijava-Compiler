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

    var pseudoregisters: Set<Pseudoregister> { get }
}

extension InstructionProtocol {
    public var pseudoregisters: Set<Pseudoregister> {
        var pseudoregisters: Set<Pseudoregister> = []
        pseudoregisters.formUnion(self.arguments.flatMap({ $0.registers }))
        pseudoregisters.formUnion(self.results.flatMap({ $0.registers }))

        return pseudoregisters
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




public struct LabelInstruction: InstructionProtocol {
    public var name: String

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
public struct JumpInstruction: InstructionProtocol {
    public var target: String
    public var condition: JumpCondition

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
public struct CallInstruction: InstructionProtocol {
    public var target: String
    public var arguments: [Argument<Pseudoregister>]
    public var result: Result<Pseudoregister>?

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
public struct MoveInstruction: InstructionProtocol {
    public var width: RegisterWidth
    public var source: Argument<Pseudoregister>
    public var target: Result<Pseudoregister>

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
public struct ComparisonInstruction: InstructionProtocol {
    public var width: RegisterWidth
    public var lhs: Argument<Pseudoregister>
    public var rhs: Argument<Pseudoregister>

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
public struct AdditionInstruction: InstructionProtocol {
    public var augend: Argument<Pseudoregister>
    public var addend: Argument<Pseudoregister>
    public var sum: Result<Pseudoregister>

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
public struct SubtractionInstruction: InstructionProtocol {
    public var minuend: Argument<Pseudoregister>
    public var subtrahend: Argument<Pseudoregister>
    public var difference: Result<Pseudoregister>

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
public struct MultiplicationInstruction: InstructionProtocol {
    public var multiplicand: Argument<Pseudoregister>
    public var multiplier: Argument<Pseudoregister>
    public var product: Result<Pseudoregister>

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
public struct DivisionInstruction: InstructionProtocol {
    public var dividend: Argument<Pseudoregister>
    public var divisor: Argument<Pseudoregister>
    public var quotient: Result<Pseudoregister>
    public var remainder: Result<Pseudoregister>

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
public struct NumericNegationInstruction: InstructionProtocol {
    public var source: Argument<Pseudoregister>
    public var target: Result<Pseudoregister>

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
public struct LogicalNegationInstruction: InstructionProtocol {
    public var source: Argument<Pseudoregister>
    public var target: Result<Pseudoregister>

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
