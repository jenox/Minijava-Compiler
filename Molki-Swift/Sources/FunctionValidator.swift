//
//  FunctionValidator.swift
//  Molki
//
//  Created by Christian Schnorr on 22.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Foundation



public class FunctionValidator {

    public init(function: Function) {
        self.function = function
    }

    private let function: Function



    public func validate() {
        print("\nAnalyzing function \(self.function.name)")

        self.validateNumberOfReadsAndWrites()
    }

    private func validateNumberOfReadsAndWrites() {
        var reads: [Pseudoregister: Int] = [:]
        var writes: [Pseudoregister: Int] = [:]

        for instruction in self.function.instructions {
            let pseudoregisters = instruction.pseudoregisters

            for register in pseudoregisters.read {
                reads[register, default: 0] += 1
            }

            for register in pseudoregisters.written {
                writes[register, default: 0] += 1
            }
        }

        // Warning: Some phi pseudoregisters are only written once!

        if CommandLine.arguments.contains("--verbose") {
            print("Reads:")
            for (key, value) in Dictionary(grouping: reads.keys, by: { reads[$0]! }).sorted(by: { $0.key < $1.key }) {
                print("- \(key): \(value)")
            }
            print("Writes:")
            for (key, value) in Dictionary(grouping: writes.keys, by: { writes[$0]! }).sorted(by: { $0.key < $1.key }) {
                print("- \(key): \(value)")
            }
        }

        let parameters = (0..<self.function.parameterWidths.count).map({ Pseudoregister.regular($0) })
        let readButNeverWrittenTo = Set(reads.keys).subtracting(parameters).subtracting(writes.keys)
        let writtenToButNeverRead = Set(writes.keys).subtracting(reads.keys)

        if !readButNeverWrittenTo.isEmpty {
            print("[WARNING] Read but never writte to:", readButNeverWrittenTo)
        }

        if !writtenToButNeverRead.isEmpty {
            print("[WARNING] Written to but never read:", writtenToButNeverRead)
        }
    }
}
