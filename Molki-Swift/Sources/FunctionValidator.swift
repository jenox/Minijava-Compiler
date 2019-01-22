//
//  FunctionValidator.swift
//  Molki
//
//  Created by Christian Schnorr on 22.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


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
        let counter = ReadWriteCounter(function: self.function)

        if CommandLine.arguments.contains("--verbose") {
            counter.dump()
        }

        let readButNeverWrittenTo = counter.reads.subtracting(counter.writes).subtracting(counter.parameters)
        let writtenToButNeverRead = counter.writes.filter({ $0.kind != .reserved }).subtracting(counter.reads)

        if !readButNeverWrittenTo.isEmpty {
            print("[ERROR] Read but never written to:", readButNeverWrittenTo)
        }

        if !writtenToButNeverRead.isEmpty {
            print("[WARNING] Written to but never read:", writtenToButNeverRead)
        }

        let phisWrittenOnlyOnce = counter.writes.filter({ $0.kind == .phi && counter.numberOfWrites(for: $0) == 1 })
        let nonPhisWrittenMoreThanOnce = counter.writes.filter({ $0.kind != .phi && counter.numberOfWrites(for: $0) > 1 })

        if !phisWrittenOnlyOnce.isEmpty {
            print("[WARNING] Phis written only once:", phisWrittenOnlyOnce)
        }

        if !nonPhisWrittenMoreThanOnce.isEmpty {
            print("[ERROR] Non-Phis written more than once:", nonPhisWrittenMoreThanOnce)
        }

        let parametersWrittenAtAll = counter.writes.intersection(counter.parameters)
        let resultsReadAtAll = counter.reads.filter({ $0.kind == .reserved })

        if !parametersWrittenAtAll.isEmpty {
            print("[ERROR] Parameters were written:", parametersWrittenAtAll)
        }

        if !resultsReadAtAll.isEmpty {
            print("[ERROR] Results were read:", resultsReadAtAll)
        }
    }
}


struct ReadWriteCounter {
    public init(function: Function) {
        var numberOfReads: [Pseudoregister: Int] = [:]
        var numberOfWrites: [Pseudoregister: Int] = [:]

        for instruction in function.instructions {
            let pseudoregisters = instruction.pseudoregisters

            for register in pseudoregisters.read {
                numberOfReads[register, default: 0] += 1
            }

            for register in pseudoregisters.written {
                numberOfWrites[register, default: 0] += 1
            }
        }

        self.function = function
        self.numberOfReads = numberOfReads
        self.numberOfWrites = numberOfWrites
    }

    private let function: Function
    private let numberOfReads: [Pseudoregister: Int]
    private let numberOfWrites: [Pseudoregister: Int]


    public var parameters: Set<Pseudoregister> {
        return Set((0..<self.function.parameterWidths.count).map({ Pseudoregister.regular($0) }))
    }

    public var reads: Set<Pseudoregister> {
        return Set(self.numberOfReads.keys)
    }

    public var writes: Set<Pseudoregister> {
        return Set(self.numberOfWrites.keys)
    }

    public func numberOfReads(for register: Pseudoregister) -> Int {
        return self.numberOfReads[register] ?? 0
    }

    public func numberOfWrites(for register: Pseudoregister) -> Int {
        return self.numberOfWrites[register] ?? 0
    }

    public func dump() {
        let numberOfReadsToPseudoregisters = Dictionary(grouping: self.reads, by: { self.numberOfReads[$0] ?? 0 })
        let numberOfWritesToPseudoregisters = Dictionary(grouping: self.writes, by: { self.numberOfWrites[$0] ?? 0 })

        print("Reads:")
        for (key, value) in numberOfReadsToPseudoregisters.sorted(by: { $0.key < $1.key }) {
            print("- \(key): \(value)")
        }
        print("Writes:")
        for (key, value) in numberOfWritesToPseudoregisters.sorted(by: { $0.key < $1.key }) {
            print("- \(key): \(value)")
        }
    }
}
