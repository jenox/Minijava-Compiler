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
