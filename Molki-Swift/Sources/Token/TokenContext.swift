//
//  TokenContext.swift
//  Molki
//
//  Created by Christian Schnorr on 04.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct TokenContext: CustomStringConvertible {
    public var substring: ArraySlice<Character>
    public var range: Range<Int>

    public var description: String {
        let offset = self.substring.distance(from: self.substring.startIndex, to: self.range.lowerBound)
        let length = self.substring.distance(from: self.range.lowerBound, to: self.range.upperBound)

        let head = String(repeating: " ", count: offset) + "^"
        let tail = String(repeating: "~", count: max(length - 1, 0))

        return self.substring + "\n" + head + tail
    }
}
