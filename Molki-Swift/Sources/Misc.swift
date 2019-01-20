//
//  Misc.swift
//  Molki
//
//  Created by Christian Schnorr on 04.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Foundation


extension Character {
    public var isAlphanumeric: Bool {
        return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789".contains(self)
    }

    public var isAlpha: Bool {
        return self.isAlphanumeric && !self.isNumeric
    }

    public var isNumeric: Bool {
        return "0123456789".contains(self)
    }

    public var isWhitespace: Bool {
        return " \t\n\r\r\n".contains(self)
    }

    public var isNewline: Bool {
        return "\n\r\r\n".contains(self)
    }
}

extension Collection {
    #if swift(>=4.2)
    #else
    public func allSatisfy(_ predicate: (Element) throws -> Bool) rethrows -> Bool {
        return try !self.contains(where: { try !predicate($0) })
    }
    #endif
}

extension CharacterSet {
    public func contains(_ character: Character) -> Bool {
        return character.unicodeScalars.allSatisfy({ self.contains($0) })
    }
}

extension String {
    func line(at range: Range<String.Index>) -> Substring {
        var substring = self[self.lineRange(for: range)]

        while let first = substring.first, CharacterSet.newlines.contains(first) {
            substring = substring.dropFirst()
        }

        while let last = substring.last, CharacterSet.newlines.contains(last) {
            substring = substring.dropLast()
        }

        return substring
    }
}

extension Collection where Element: Equatable {
    public func count(where predicate: (Element) throws -> Bool) rethrows -> Int {
        return try self.reduce(0, { $0 + (try predicate($1) ? 1 : 0) })
    }
}

extension Int {
    public func floored(toMultipleOf divisor: Int) -> Int {
        precondition(divisor >= 0)

        if self > 0 {
            return self / divisor * divisor
        }
        else {
            return (self - divisor + 1) / divisor * divisor
        }
    }

    public func ceiled(toMultipleOf divisor: Int) -> Int {
        precondition(divisor >= 0)

        if self > 0 {
            return (self + divisor - 1) / divisor * divisor
        }
        else {
            return self / divisor * divisor
        }
    }

    public func isMultiple(of other: Int) -> Bool {
        // Nothing but zero is a multiple of zero.
        if other == 0 { return self == 0 }
        // Special case to avoid overflow on .min / -1 for signed types.
        if Int.isSigned && other == -1 { return true }
        // Having handled those special cases, this is safe.
        return self % other == 0
    }
}
