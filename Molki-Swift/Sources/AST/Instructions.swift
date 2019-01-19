//
//  Instructions.swift
//  Molki
//
//  Created by Christian Schnorr on 14.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift



public enum Instruction: CustomStringConvertible {
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

    public var description: String {
        switch self {
        case .labelInstruction(let instruction):
            return instruction.description
        case .jumpInstruction(let instruction):
            return instruction.description
        case .callInstruction(let instruction):
            return instruction.description
        case .moveInstruction(let instruction):
            return instruction.description
        case .comparisonInstruction(let instruction):
            return instruction.description
        case .additionInstruction(let instruction):
            return instruction.description
        case .subtractionInstruction(let instruction):
            return instruction.description
        case .multiplicationInstruction(let instruction):
            return instruction.description
        case .divisionInstruction(let instruction):
            return instruction.description
        case .logicalNegationInstruction(let instruction):
            return instruction.description
        case .numericNegationInstruction(let instruction):
            return instruction.description
        }
    }
}


public struct LabelInstruction: CustomStringConvertible {
    public var name: String

    public var description: String {
        return "\(self.name):"
    }
}

// jmp, jl, jle, jg, jge, je, jne
public struct JumpInstruction: CustomStringConvertible {
    public var target: String
    public var condition: JumpCondition

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
public struct CallInstruction: CustomStringConvertible {
    public var target: String
    public var arguments: [Argument<Pseudoregister>]
    public var result: Result<Pseudoregister>?

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
public struct MoveInstruction: CustomStringConvertible {
    public var width: RegisterWidth
    public var source: Argument<Pseudoregister>
    public var target: Result<Pseudoregister>

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
public struct ComparisonInstruction: CustomStringConvertible {
    public var width: RegisterWidth
    public var lhs: Argument<Pseudoregister>
    public var rhs: Argument<Pseudoregister>

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
public struct AdditionInstruction: CustomStringConvertible {
    public var augend: Argument<Pseudoregister>
    public var addend: Argument<Pseudoregister>
    public var sum: Result<Pseudoregister>

    public var description: String {
        return "addl [ \(self.augend) | \(self.addend) ] -> \(self.sum)"
    }
}

// sub
// Syntax: sub [ a | b ] -> c
// Semantic: c := a - b
public struct SubtractionInstruction: CustomStringConvertible {
    public var minuend: Argument<Pseudoregister>
    public var subtrahend: Argument<Pseudoregister>
    public var difference: Result<Pseudoregister>

    public var description: String {
        return "subl [ \(self.minuend) | \(self.subtrahend) ] -> \(self.difference)"
    }
}

// imul
// Syntax: mul [ a | b ] -> c
// Semantic: c := a * b
public struct MultiplicationInstruction: CustomStringConvertible {
    public var multiplicand: Argument<Pseudoregister>
    public var multiplier: Argument<Pseudoregister>
    public var product: Result<Pseudoregister>

    public var description: String {
        return "mull [ \(self.multiplicand) | \(self.multiplier) ] -> \(self.product)"
    }
}

// idiv
// Syntax: div [ a | b ] -> [ c | d ]
// Semantic: c := a / b
// Semantic: d := a % b
public struct DivisionInstruction: CustomStringConvertible {
    public var dividend: Argument<Pseudoregister>
    public var divisor: Argument<Pseudoregister>
    public var quotient: Result<Pseudoregister>
    public var remainder: Result<Pseudoregister>

    public var description: String {
        return "divl [ \(self.dividend) | \(self.divisor) ] -> [ \(self.quotient) | \(self.remainder) ]"
    }
}



// neg + b/w/l/q
// twos complement
// Syntax: neg a
// Semantic: a := -a
// width is assumed to be 32bit
public struct NumericNegationInstruction: CustomStringConvertible {
    public var source: Argument<Pseudoregister>
    public var target: Result<Pseudoregister>

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
public struct LogicalNegationInstruction: CustomStringConvertible {
    public var source: Argument<Pseudoregister>
    public var target: Result<Pseudoregister>

    public var description: String {
        return "notb \(self.source) -> \(self.target)"
    }
}
