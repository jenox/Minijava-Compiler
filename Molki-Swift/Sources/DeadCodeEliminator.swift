//
//  DeadCodeEliminator.swift
//  Molki
//
//  Created by Christian Schnorr on 22.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public class DeadCodeEliminator {
    public init(function: Function) {
        self.function = function
    }

    private let function: Function

    public func eliminateDeadCode() {
        let readers = self.getInstructionsReadingPseudoregisters()

        for block in self.function.basicBlocks {
            for (index, instruction) in block.instructions.enumerated().reversed() {
                guard !instruction.hasEffectsOtherThanWritingPseudoregisters else { continue }

                let writes = instruction.pseudoregisters.written

                // if it writes ONLY pseudoregisters that are never read, is superfluous
                guard !writes.contains(.reserved) else { continue }
                guard writes.isDisjoint(with: readers.keys) else { continue }

                block.removeInstruction(at: index)
            }
        }
    }

    private func getInstructionsReadingPseudoregisters() -> [Pseudoregister: Set<Instruction>] {
        var readers: [Pseudoregister: Set<Instruction>] = [:]

        for instruction in self.function.instructions {
            for pseudoregister in instruction.pseudoregisters.read {
                readers[pseudoregister, default: []].insert(instruction)
            }
        }

        return readers
    }
}
