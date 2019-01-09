//
//  LexerError.swift
//  Molki
//
//  Created by Christian Schnorr on 05.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public enum LexerError: Error {
    case unexpectedCharacter(found: Character, expected: Character)
    case unexpectedEndOfInput(expected: Character)
    case illegalCharacter(Character)
}
