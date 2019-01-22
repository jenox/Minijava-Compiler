//
//  Function.swift
//  Molki
//
//  Created by Christian Schnorr on 14.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct Function {
    public var name: String
    public var parameterWidths: [RegisterWidth]
    public var returnValueWidth: RegisterWidth?
    public var basicBlocks: [BasicBlock]

    public var instructions: [Instruction] {
        return self.basicBlocks.flatMap({ $0.instructions })
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
