//
//  Parser.swift
//  Molki
//
//  Created by Christian Schnorr on 29.12.18.
//  Copyright © 2018 Christian Schnorr. All rights reserved.
//

import Swift


public class Parser {
    public init(tokenProvider: TokenProvider) {
        self.tokenProvider = tokenProvider
    }

    private let tokenProvider: TokenProvider

    private var bufferedTokens: [Token] = []
    private var hasReceivedEndOfInputFromLexer: Bool = false

    private func token(atOffset offset: Int) throws -> Token? {
        while self.bufferedTokens.count <= offset, !self.hasReceivedEndOfInputFromLexer {
            if let token = try self.tokenProvider.nextToken() {
                self.bufferedTokens.append(token)
            }
            else {
                self.hasReceivedEndOfInputFromLexer = true
            }
        }

        if offset < self.bufferedTokens.count {
            return self.bufferedTokens[offset]
        }
        else {
            return nil
        }
    }

    private func lookahead(_ types: TokenType...) throws -> Bool {
        for (index, type) in types.enumerated() {
            guard let token = try self.token(atOffset: index) else { return false }
            guard token.type == type else { return false }
        }

        return true
    }

    @discardableResult
    private func consume(_ type: TokenType, text: String? = nil) throws -> Token {
        guard let currentToken = try self.token(atOffset: 0) else {
            throw ParserError.unexpectedEOFInsteadOfToken(expected: type)
        }

        guard currentToken.type == type else {
            throw ParserError.unexpectedTokenType(currentToken, expected: type)
        }

        if let text = text {
            guard currentToken.payload == text else {
                throw ParserError.unexpectedTokenPayload(currentToken, expected: text)
            }
        }

        self.bufferedTokens.removeFirst()

        return currentToken
    }


    // MARK: - Parsing

    public func parseFunctions() throws -> [Function] {
        var functions: [Function] = []

        while try self.lookahead(.period) {
            functions.append(try self.parseFunction())
        }

        if let token = try self.token(atOffset: 0) {
            throw ParserError.expectedEOF(found: token)
        }

        return functions
    }

    private func parseFunction() throws -> Function {
        try self.consume(.period)
        try self.consume(.identifier, text: "function")

        let name = try self.consume(.identifier).payload

        let numberOfParameters = try self.parseInteger(0...)
        let numberOfReturnValues = try self.parseInteger(0...1)
        let hasReturnValue = numberOfReturnValues > 0
        var instructions: [Instruction] = []

        while try self.lookahead(.identifier) {
            instructions.append(try self.parseInstruction())
        }

        try self.consume(.period)
        try self.consume(.identifier, text: "endfunction")

        return Function(name: name, numberOfParameters: numberOfParameters, hasReturnValue: hasReturnValue, instructions: instructions)
    }

    private func parseInstruction() throws -> Instruction {
        if try self.lookahead(.identifier, .colon) {
            let name = try self.consume(.identifier).payload
            try self.consume(.colon)

            let instruction = LabelInstruction(name: name)

            return .labelInstruction(instruction)
        }
        else if try self.lookahead(.identifier, .identifier) {
            let opcode = try self.consume(.identifier).payload

            if opcode == "call" {
                let name = try self.consume(.identifier).payload
                var arguments: [Value] = []

                try self.consume(.openingBracket)

                if try self.lookahead(.dollar) || self.lookahead(.pseudoregister) {
                    arguments.append(try self.parseValue())
                }

                while try self.lookahead(.pipe) {
                    try self.consume(.pipe)
                    arguments.append(try self.parseValue())
                }

                try self.consume(.closingBracket)

                if try self.lookahead(.arrow) {
                    try self.consume(.arrow)
                    let returnRegister = try self.parseValue()

                    let instruction = CallInstruction(name: name, arguments: arguments, returnValue: returnRegister)

                    return .callInstruction(instruction)
                }
                else {
                    let instruction = CallInstruction(name: name, arguments: arguments, returnValue: nil)

                    return .callInstruction(instruction)
                }
            }
            else {
                let target = try self.consume(.identifier).payload

                let instruction = JumpInstruction(operation: opcode, target: target)

                return .jumpInstruction(instruction)
            }
        }
        else {
            let opcode = try self.consume(.identifier).payload

            if try self.lookahead(.openingBracket) {
                try self.consume(.openingBracket)
                let first = try self.parseValue()
                try self.consume(.pipe)
                let second = try self.parseValue()
                try self.consume(.closingBracket)
                try self.consume(.arrow)

                if try self.lookahead(.openingBracket) {
                    try self.consume(.openingBracket)
                    let third = try self.parseValue()
                    try self.consume(.pipe)
                    let fourth = try self.parseValue()
                    try self.consume(.closingBracket)

                    let instruction = FourAddressCodeInstruction.init(operation: opcode, first: first, second: second, third: third, fourth: fourth)

                    return .fourAddressCodeInstruction(instruction)
                }
                else {
                    let third = try self.parseValue()

                    let instruction = ThreeAddressCodeInstruction.init(operation: opcode, first: first, second: second, third: third)

                    return .threeAddressCodeInstruction(instruction)
                }
            }
            else {
                let first = try self.parseValue()
                try self.consume(.comma)
                let second = try self.parseValue()

                let instruction = TwoAddressCodeInstruction(operation: opcode, first: first, second: second)

                return .twoAddressCodeInstruction(instruction)
            }
        }
    }

    private func parseRegister() throws -> Register {
        try self.consume(.pseudoregister)

        if try self.lookahead(.integer) {
            let text = try self.consume(.integer).payload
            let number = Int(text)!

            return .identified(number)
        }
        else if try self.lookahead(.identifier) {
            try self.consume(.identifier, text: "r0")

            return .returnValue
        }
        else {
            throw try self.unexpectedToken(context: "Register")
        }
    }

    private func parseRegisterValue() throws -> RegisterValue {
        let register = try self.parseRegister()

        if let currentToken = try self.token(atOffset: 0), currentToken.type == .identifier {
            switch currentToken.payload {
            case "l":
                try self.consume(.identifier)
                return RegisterValue(register: register, width: .byte)
            case "w":
                try self.consume(.identifier)
                return RegisterValue(register: register, width: .word)
            case "d":
                try self.consume(.identifier)
                return RegisterValue(register: register, width: .double)
            default:
                break
            }
        }

        return RegisterValue(register: register, width: .quad)
    }

    private func parseMemoryAddress() throws -> MemoryAddress {
        let offset: Int

        if try self.lookahead(.minus) || self.lookahead(.integer) {
            offset = try self.parseInteger()
        }
        else {
            offset = 0
        }

        try self.consume(.openingParenthesis)

        let base = try self.parseRegisterValue()

        if try self.lookahead(.comma) {
            try self.consume(.comma)
            let index = try self.parseRegisterValue()
            let scale: Int

            if try self.lookahead(.comma) {
                try self.consume(.comma)
                scale = try self.parseInteger(1...)
            }
            else {
                scale = 1
            }

            try self.consume(.closingParenthesis)

            return .indexed(base: base, index: index, scale: scale, offset: offset)
        }
        else {
            try self.consume(.closingParenthesis)

            return .relative(base: base, offset: offset)
        }
    }

    private func parseValue() throws -> Value {
        if try self.lookahead(.dollar) {
            try self.consume(.dollar)
            return .constant(try self.parseInteger())
        }
        else if try self.lookahead(.pseudoregister) {
            return .register(try self.parseRegisterValue())
        }
        else {
            return .memory(try self.parseMemoryAddress())
        }
    }

    private func parseInteger() throws -> Int {
        return try self.parseInteger(Int.min...Int.max)
    }

    private func parseInteger<R>(_ range: R) throws -> Int where R: RangeExpression, R.Bound == Int {
        let token: Token
        let value: Int

        if try self.lookahead(.minus) {
            try self.consume(.minus)

            token = try self.consume(.integer)
            value = Int("-" + token.payload)!
        }
        else if try self.lookahead(.integer) {
            token = try self.consume(.integer)
            value = Int(token.payload)!
        }
        else {
            throw try self.unexpectedToken(context: "Integer")
        }

        guard range.contains(value) else {
            throw ParserError.integerOutOfRange(token)
        }

        return value
    }

    private func unexpectedToken(context: String) throws -> ParserError {
        if let token = try self.token(atOffset: 0) {
            return .unexpectedToken(token, context: context)
        }
        else {
            return .unexpectedEOF(context: context)
        }
    }
}
