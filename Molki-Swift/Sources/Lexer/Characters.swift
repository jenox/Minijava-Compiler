//
//  Characters.swift
//  Molki
//
//  Created by Christian Schnorr on 20.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Foundation


extension Character {
    private static let alphanumerics = Set("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789")
    private static let numerics = Set("0123456789")
    private static let whitespaces = Set(" \t\n\r\r\n")
    private static let newlines = Set("\n\r\r\n")

    public var isAlphanumeric: Bool {
        return Character.alphanumerics.contains(self)
    }

    public var isNumeric: Bool {
        return Character.numerics.contains(self)
    }

    public var isWhitespace: Bool {
        return Character.whitespaces.contains(self)
    }

    public var isNewline: Bool {
        return Character.newlines.contains(self)
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
