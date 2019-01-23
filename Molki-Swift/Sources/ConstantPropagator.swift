//
//  ConstantFolder.swift
//  Molki
//
//  Created by Christian Schnorr on 22.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public class ConstantPropagator {
    public init(function: Function) {
        self.function = function
    }

    private let function: Function

    public func propagate() {
        let writers = self.getInstructionsWritingPseudoregisters()

        var substitutionsToBeMade: [Pseudoregister: Argument<Pseudoregister>] = [:]

        for (pseudoregister, instructions) in writers {
            guard pseudoregister != .reserved else { continue }
            guard instructions.count == 1, let instruction = instructions.first else { continue }

            switch instruction {
            case .moveInstruction(let moveInstruction):
                substitutionsToBeMade[pseudoregister] = moveInstruction.source
            default:
                break
            }
        }

        guard !substitutionsToBeMade.isEmpty else {
            return
        }

        for block in self.function.basicBlocks {
            for instruction in block.instructions {
                let reads = instruction.pseudoregisters.read

                for (pseudoregister, argument) in substitutionsToBeMade where reads.contains(pseudoregister) {
                    instruction.substitute(pseudoregister, with: argument)
                }
            }
        }
    }

    private func getInstructionsWritingPseudoregisters() -> [Pseudoregister: Set<Instruction>] {
        var writers: [Pseudoregister: Set<Instruction>] = [:]

        for instruction in self.function.instructions {
            for pseudoregister in instruction.pseudoregisters.written {
                writers[pseudoregister, default: []].insert(instruction)
            }
        }

        return writers
    }
}
