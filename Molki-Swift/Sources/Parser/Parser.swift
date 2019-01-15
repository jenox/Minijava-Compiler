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

    private func lookahead(_ types: Set<TokenType>) throws -> Bool {
        guard let token = try self.token(atOffset: 0) else { return false }
        guard types.contains(token.type) else { return false }

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
        else {
            let token = try self.consume(.identifier)
            let opcode = token.payload

            switch opcode {
            case "jmp", "jl", "jle", "jg", "jge", "je", "jne":
                let condition = JumpCondition(rawValue: opcode)!
                let target = try self.consume(.identifier).payload

                let instruction = JumpInstruction(target: target, condition: condition)

                return .jumpInstruction(instruction)
            case "call":
                let target = try self.consume(.identifier).payload
                var arguments: [Argument<Pseudoregister>] = []
                var result: Result<Pseudoregister>? = nil

                if try self.lookahead(.openingBracket) {
                    try self.consume(.openingBracket)

                    if try self.lookahead([.dollar, .pseudoregister, .integer, .minus]) {
                        arguments.append(try self.parseArgument())
                    }

                    while try self.lookahead(.pipe) {
                        try self.consume(.pipe)
                        arguments.append(try self.parseArgument())
                    }

                    try self.consume(.closingBracket)
                }

                if try self.lookahead(.arrow) {
                    try self.consume(.arrow)
                    result = try self.parseResult()
                }

                let instruction = CallInstruction(target: target, arguments: arguments, result: result)

                return .callInstruction(instruction)
            case "movb", "movw", "movl", "movq":
                let width: RegisterWidth

                switch opcode {
                case "movb": width = .byte
                case "movw": width = .word
                case "movl": width = .double
                case "movq": width = .quad
                default: fatalError()
                }

                let source = try self.parseArgument(width)
                try self.consume(.arrow)
                let target = try self.parseResult(width)

                let instruction = MoveInstruction(width: width, source: source, target: target)

                return .moveInstruction(instruction)
            case "cmpb", "cmpw", "cmpl", "cmpq":
                let width: RegisterWidth

                switch opcode {
                case "cmpb": width = .byte
                case "cmpw": width = .word
                case "cmpl": width = .double
                case "cmpq": width = .quad
                default: fatalError()
                }

                try self.consume(.openingBracket)
                let lhs = try self.parseArgument(width)
                try self.consume(.pipe)
                let rhs = try self.parseArgument(width)
                try self.consume(.closingBracket)

                let instruction = ComparisonInstruction(width: width, lhs: lhs, rhs: rhs)

                return .comparisonInstruction(instruction)
            case "addl":
                try self.consume(.openingBracket)
                let augend = try self.parseArgument(.double)
                try self.consume(.pipe)
                let addend = try self.parseArgument(.double)
                try self.consume(.closingBracket)
                try self.consume(.arrow)
                let sum = try self.parseResult(.double)

                let instruction = AdditionInstruction(augend: augend, addend: addend, sum: sum)

                return .additionInstruction(instruction)
            case "subl":
                try self.consume(.openingBracket)
                let minuend = try self.parseArgument(.double)
                try self.consume(.pipe)
                let subtrahend = try self.parseArgument(.double)
                try self.consume(.closingBracket)
                try self.consume(.arrow)
                let difference = try self.parseResult(.double)

                let instruction = SubtractionInstruction(minuend: minuend, subtrahend: subtrahend, difference: difference)

                return .subtractionInstruction(instruction)
            case "mull":
                try self.consume(.openingBracket)
                let multiplicand = try self.parseArgument(.double)
                try self.consume(.pipe)
                let multiplier = try self.parseArgument(.double)
                try self.consume(.closingBracket)
                try self.consume(.arrow)
                let product = try self.parseResult(.double)

                let instruction = MultiplicationInstruction(multiplicand: multiplicand, multiplier: multiplier, product: product)

                return .multiplicationInstruction(instruction)
            case "divl":
                try self.consume(.openingBracket)
                let dividend = try self.parseArgument(.double)
                try self.consume(.pipe)
                let divisor = try self.parseArgument(.double)
                try self.consume(.closingBracket)
                try self.consume(.arrow)
                try self.consume(.openingBracket)
                let quotient = try self.parseResult(.double)
                try self.consume(.pipe)
                let remainder = try self.parseResult(.double)
                try self.consume(.closingBracket)

                let instruction = DivisionInstruction(dividend: dividend, divisor: divisor, quotient: quotient, remainder: remainder)

                return .divisionInstruction(instruction)
            case "negl":
                let source = try self.parseArgument(.double)
                try self.consume(.arrow)
                let target = try self.parseResult(.double)

                let instruction = NumericNegationInstruction(source: source, target: target)

                return .numericNegationInstruction(instruction)
            case "notb":
                let source = try self.parseArgument(.byte)
                try self.consume(.arrow)
                let target = try self.parseResult(.byte)

                let instruction = LogicalNegationInstruction(source: source, target: target)

                return .logicalNegationInstruction(instruction)
            default:
                throw ParserError.unrecognizedOpcode(opcode, token: token)
            }
        }
    }

    private func parseArgument(_ width: RegisterWidth? = nil) throws -> Argument<Pseudoregister> {
        if try self.lookahead(.dollar) {
            return .constant(try self.parseConstantValue(width))
        }
        else if try self.lookahead(.pseudoregister) {
            return .register(try self.parseRegisterValue(width))
        }
        else {
            return .memory(try self.parseMemoryValue(width))
        }
    }

    private func parseResult(_ width: RegisterWidth? = nil) throws -> Result<Pseudoregister> {
        if try self.lookahead(.pseudoregister) {
            return .register(try self.parseRegisterValue(width))
        }
        else {
            return .memory(try self.parseMemoryValue(width))
        }
    }

    private func parseConstantValue(_ expected: RegisterWidth? = nil) throws -> ConstantValue {
        let location = try self.consume(.dollar)

        let value = try self.parseInteger()
        let width = try self.parseValueWidth(expected, at: location)

        return ConstantValue(value: value, width: width)
    }

    private func parseRegisterValue(_ expected: RegisterWidth? = nil) throws -> RegisterValue<Pseudoregister> {
        let location = try self.token(atOffset: 0)

        let register = try self.parseRegister()
        let width = try self.parseValueWidth(expected, at: location!)

        return RegisterValue(register: register, width: width)
    }

    private func parseMemoryValue(_ expected: RegisterWidth? = nil) throws -> MemoryValue<Pseudoregister> {
        let location = try self.token(atOffset: 0)

        let address = try self.parseMemoryAddress()
        let width = try self.parseValueWidth(expected, at: location!)

        return MemoryValue(address: address, width: width)
    }

    private func parseValueWidth(_ expected: RegisterWidth? = nil, at location: Token) throws -> RegisterWidth {
        let width: RegisterWidth

        if let currentToken = try self.token(atOffset: 0), currentToken.type == .identifier {
            switch currentToken.payload {
            case "l":
                try self.consume(.identifier)
                width = .byte
            case "w":
                try self.consume(.identifier)
                width = .word
            case "d":
                try self.consume(.identifier)
                width = .double
            default:
                width = .quad
            }
        }
        else {
            width = .quad
        }

        if let expected = expected, width != expected {
            throw ParserError.incompatibleValueWidth(width, expected: expected, location: location)
        }

        return width
    }

    private func parseMemoryAddress() throws -> MemoryAddress<Pseudoregister> {
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

    private func parseRegister() throws -> Pseudoregister {
        try self.consume(.pseudoregister)

        if try self.lookahead(.integer) {
            let text = try self.consume(.integer).payload
            let number = Int(text)!

            return .numbered(number)
        }
        else if try self.lookahead(.dollar) {
            try self.consume(.dollar)

            return .reserved
        }
        else {
            throw try self.unexpectedToken(context: "Register")
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
