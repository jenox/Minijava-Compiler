//
//  StringReader.swift
//  Molki
//
//  Created by Christian Schnorr on 04.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public class StringReader: BufferedReader {
    public init(string: String) {
        let lines = string.components(separatedBy: .newlines)

        if let last = lines.last, last.isEmpty {
            self.lines = Array(lines.dropLast())
        }
        else {
            self.lines = lines
        }
    }

    private var currentIndex = 0
    private let lines: [String]

    public func readLine() throws -> String? {
        guard self.lines.indices.contains(self.currentIndex + 1) else {
            return nil
        }

        let line = self.lines[self.currentIndex]

        self.currentIndex += 1

        return line
    }
}
