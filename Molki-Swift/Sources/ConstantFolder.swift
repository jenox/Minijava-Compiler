//
//  ConstantFolder.swift
//  Molki
//
//  Created by Christian Schnorr on 22.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public class ConstantFolder {
    public init(function: Function) {
        self.function = function
    }

    private let function: Function

    public func fold(untilConverged: Bool = false) {
        var stop = false

        repeat {
            stop = !(self.foldAndReportIfChangeWasMade() && untilConverged)
        }
            while !stop
    }

    private func foldAndReportIfChangeWasMade() -> Bool {
        let writers = self.getInstructionsWritingPseudoregisters()

        var instructionsToBeRemoved: Set<Instruction> = []
        var substitutionsToBeMade: [Pseudoregister: Int] = [:]

        for (pseudoregister, instructions) in writers {
            guard pseudoregister != .reserved else { continue }
            guard instructions.count == 1, let instruction = instructions.first else { continue }

            switch instruction {
            case .moveInstruction(let moveInstruction):
                if case .constant(let constant) = moveInstruction.source {
                    instructionsToBeRemoved.insert(instruction)
                    substitutionsToBeMade[pseudoregister] = constant.value
                }
                break
            default:
                break
            }
        }

        assert(instructionsToBeRemoved.count == substitutionsToBeMade.count)

        guard !instructionsToBeRemoved.isEmpty else {
            return false
        }

        print("[INFO] Folding \(instructionsToBeRemoved.count) constant moves in \(self.function.name)")

        for block in self.function.basicBlocks {
            for (index, instruction) in block.instructions.enumerated().reversed() {
                if instructionsToBeRemoved.contains(instruction) {
                    block.removeInstruction(at: index)
                }
                else {
                    let reads = instruction.pseudoregisters.read

                    for (pseudoregister, constant) in substitutionsToBeMade where reads.contains(pseudoregister) {
                        instruction.substitute(pseudoregister, with: constant)
                    }
                }
            }
        }

        return true
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
