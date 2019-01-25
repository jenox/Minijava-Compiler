//
//  Function.swift
//  Molki
//
//  Created by Christian Schnorr on 14.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct Function: CustomStringConvertible {
    public var name: String
    public var parameterWidths: [RegisterWidth]
    public var returnValueWidth: RegisterWidth?
    public var basicBlocks: [BasicBlock]

    public var instructions: [Instruction] {
        return self.basicBlocks.flatMap({ $0.instructions })
    }

    public var description: String {
        var description = ".function \(self.name)"

        if let first = self.parameterWidths.first {
            description += " [ \(self.description(of: first))"

            for other in self.parameterWidths.dropFirst() {
                description += " | \(self.description(of: other))"
            }

            description += " ]"
        }

        if let returnValueWidth = self.returnValueWidth {
            description += " -> \(self.description(of: returnValueWidth))"
        }

        for instruction in self.instructions {
            if case .labelInstruction = instruction {
                description += "\n" + instruction.description
            }
            else {
                description += "\n    " + instruction.description
            }
        }

        return description + "\n.endfunction"
    }

    private func description(of width: RegisterWidth) -> String {
        switch width {
        case .byte: return "b"
        case .word: return "w"
        case .double: return "l"
        case .quad: return "q"
        }
    }
}

public class BasicBlock {
    public init(instructions: [Instruction]) {
        precondition(!instructions.isEmpty, "Basic block must not be empty")

        if let index = instructions.firstIndex(where: { $0.rawInstruction is LabelInstruction }) {
            precondition(index == instructions.indices.first, "Label instruction must be first in basic block")
        }

        if let index = instructions.firstIndex(where: { $0.rawInstruction is JumpInstruction }) {
            precondition(index == instructions.indices.last, "Jump instruction must be last in basic block")
        }

        self.instructions = instructions
    }

    private(set) public var instructions: [Instruction]

    public func removeInstruction(at index: Int) {
        self.instructions.remove(at: index)
    }

    public var name: String? {
        return (self.instructions.first?.rawInstruction as? LabelInstruction)?.name
    }
}

extension BasicBlock: Equatable, Hashable {
    public static func == (lhs: BasicBlock, rhs: BasicBlock) -> Bool {
        return lhs === rhs
    }

    public func hash(into hasher: inout Hasher) {
        ObjectIdentifier(self).hash(into: &hasher)
    }
}
