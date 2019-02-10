//
//  Lexer.swift
//  Molki
//
//  Created by Christian Schnorr on 24.12.18.
//  Copyright © 2018 Christian Schnorr. All rights reserved.
//

import Swift


public class Lexer: TokenProvider {

    // MARK: - Initialization

    public init(path: String, text: String) {
        self.path = path
        self.characters = Array(text)

        self.currentRange = 0..<0

        self.currentTokenLineStartIndex = 0
        self.currentTokenLineNumber = 1
    }


    // MARK: - State

    private let path: String
    private let characters: [Character]

    // current token range
    private var currentRange: Range<Int>

    private var currentTokenLineStartIndex: Int
    private var currentTokenLineNumber: Int

    private var currentCharacter: Character? {
        if self.characters.indices.contains(self.currentRange.upperBound) {
            return self.characters[self.currentRange.upperBound]
        }
        else {
            return nil
        }
    }

    private var hasReachedEndOfInput: Bool {
        return self.currentCharacter == nil
    }


    // MARK: - Moving Through Input

    private func advance() {
        let lowerBound = self.currentRange.lowerBound
        let upperBound = self.characters.index(after: self.currentRange.upperBound)

        self.currentRange = lowerBound..<upperBound
    }

    private func advance(while predicate: (Character) -> Bool) throws {
        while let currentCharacter = self.currentCharacter, predicate(currentCharacter) {
            self.advance()
        }
    }

    private func consume(_ text: String) throws {
        for character in text {
            if character == self.currentCharacter {
                self.advance()
            }
            else if let currentCharacter = self.currentCharacter {
                throw LexerError.unexpectedCharacter(found: currentCharacter, expected: character)
            }
            else {
                throw LexerError.unexpectedEndOfInput(expected: character)
            }
        }
    }

    private func flushCurrentRange() {
        let flushed = self.characters[self.currentRange]

        if let index = flushed.lastIndex(where: { $0.isNewline }) {
            self.currentTokenLineStartIndex = flushed.index(after: index)
            self.currentTokenLineNumber += flushed.count(where: { $0.isNewline })
        }

        let lowerBound = self.currentRange.upperBound
        let upperBound = self.currentRange.upperBound

        self.currentRange = lowerBound..<upperBound
    }


    // MARK: - Extracting Tokens

    public func nextToken() throws -> Token? {
        while !self.hasReachedEndOfInput {
            if let token = try self.nextTokenOrWhitespace() {
                return token
            }
        }

        return nil
    }

    private func nextTokenOrWhitespace() throws -> Token? {
        guard let currentCharacter = self.currentCharacter else {
            return nil
        }

        if currentCharacter.isNumeric {
            try self.advance(while: { $0.isNumeric })

            return self.buildToken(.integer)
        }
        else if currentCharacter.isAlphanumeric {
            try self.advance(while: { $0.isAlphanumeric || $0 == "$" })

            return self.buildToken(.identifier)
        }
        else if currentCharacter.isWhitespace {
            try self.advance(while: { $0.isWhitespace })
            self.flushCurrentRange()

            return nil
        }
        else if currentCharacter == "(" {
            try self.consume("(")

            return self.buildToken(.openingParenthesis)
        }
        else if currentCharacter == ")" {
            try self.consume(")")

            return self.buildToken(.closingParenthesis)
        }
        else if currentCharacter == "[" {
            try self.consume("[")

            return self.buildToken(.openingBracket)
        }
        else if currentCharacter == "]" {
            try self.consume("]")

            return self.buildToken(.closingBracket)
        }
        else if currentCharacter == "," {
            try self.consume(",")

            return self.buildToken(.comma)
        }
        else if currentCharacter == "|" {
            try self.consume("|")

            return self.buildToken(.pipe)
        }
        else if currentCharacter == "-" {
            try self.consume("-")

            if self.currentCharacter == ">" {
                try self.consume(">")

                return self.buildToken(.arrow)
            }
            else {
                return self.buildToken(.minus)
            }
        }
        else if currentCharacter == "." {
            try self.consume(".")

            return self.buildToken(.period)
        }
        else if currentCharacter == ":" {
            try self.consume(":")

            return self.buildToken(.colon)
        }
        else if currentCharacter == "$" {
            try self.consume("$")

            return self.buildToken(.dollar)
        }
        else if currentCharacter == "%" {
            try self.consume("%@")

            return self.buildToken(.pseudoregister)
        }
        else {
            throw LexerError.illegalCharacter(currentCharacter)
        }
    }

    private func buildToken(_ type: TokenType) -> Token {
        precondition(!self.currentRange.isEmpty)

        let payload = String(self.characters[self.currentRange])

        let range = self.currentRange
        let substring = self.characters.slice(from: self.currentTokenLineStartIndex, toFirstWhere: { $0.isNewline })
        let context = TokenContext(substring: substring, range: range)

        let line = self.currentTokenLineNumber
        let column = substring.distance(from: substring.startIndex, to: range.lowerBound) + 1
        let location = TokenLocation(path: self.path, line: line, column: column)

        self.flushCurrentRange()

        return Token(type: type, payload: payload, location: location, context: context)
    }
}
