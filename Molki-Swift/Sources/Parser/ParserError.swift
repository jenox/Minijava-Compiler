//
//  ParserError.swift
//  Molki
//
//  Created by Christian Schnorr on 08.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public enum ParserError: Error, CustomStringConvertible {
    case unexpectedTokenType(Token, expected: TokenType)
    case unexpectedTokenPayload(Token, expected: String)
    case unexpectedToken(Token, context: String)
    case unexpectedEOFInsteadOfToken(expected: TokenType)
    case unexpectedEOF(context: String)
    case expectedEOF(found: Token)
    case integerOutOfRange(Token)

    public var description: String {
        switch self {
        case .unexpectedTokenType(let token, expected: let expected):
            return "Found token of type “\(token.type)” when expecting token of type “\(expected)”"
        case .unexpectedTokenPayload(let token, expected: let expected):
            return "Found token with payload “\(token.payload)” when expecting token with payload “\(expected)”"
        case .unexpectedToken(let token, context: let context):
            return "Unexpected token of type “\(token.type)” in context “\(context)”"
        case .unexpectedEOFInsteadOfToken(expected: let expected):
            return "Found EOF when expecting token of type “\(expected)”"
        case .unexpectedEOF(context: let context):
            return "Unexpected EOF in context “\(context)”"
        case .expectedEOF(found: let token):
            return "Expected EOF but found token of type “\(token.type)”"
        case .integerOutOfRange(_):
            return "Integer out of range"
        }
    }

    public var parserDefinedError: Token? {
        switch self {
        case .unexpectedTokenType(let token, expected: _):
            return token
        case .unexpectedTokenPayload(let token, expected: _):
            return token
        case .unexpectedToken(let token, context: _):
            return token
        case .unexpectedEOFInsteadOfToken(expected: _):
            return nil
        case .unexpectedEOF(context: _):
            return nil
        case .expectedEOF(found: let token):
            return token
        case .integerOutOfRange(let token):
            return token
        }
    }
}
