//
//  Operands.swift
//  Molki
//
//  Created by Christian Schnorr on 14.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct RegisterValue<RegisterType: Register>: Equatable, CustomStringConvertible {
    public var register: RegisterType
    public var width: RegisterWidth

    public func with(_ width: RegisterWidth) -> RegisterValue<RegisterType> {
        return RegisterValue(register: self.register, width: width)
    }

    public mutating func substitute(_ register: RegisterType, with argument: Argument<RegisterType>) {
        guard self.register == register else { return }

        switch argument {
        case .constant:
            break
        case .register(let value):
            self.register = value.register
        case .memory:
            break
        }
    }

    public var description: String {
        return self.register.name(for: self.width)
    }
}

public extension RegisterValue where RegisterType == X86Register {
    public static let basePointer = X86Register.rbp.with(.quad)
    public static let stackPointer = X86Register.rsp.with(.quad)
}

public struct ConstantValue: Equatable, CustomStringConvertible {
    public var value: Int
    public var width: RegisterWidth

    public var description: String {
        switch self.width {
        case .byte: return "$\(self.value)l"
        case .word: return "$\(self.value)w"
        case .double: return "$\(self.value)d"
        case .quad: return "$\(self.value)"
        }
    }
}

public struct MemoryValue<RegisterType: Register>: Equatable, CustomStringConvertible {
    public var address: MemoryAddress<RegisterType>
    public var width: RegisterWidth

    public var registers: Set<RegisterType> {
        return self.address.registers
    }

    public var description: String {
        switch self.width {
        case .byte: return "\(self.address)l"
        case .word: return "\(self.address)w"
        case .double: return "\(self.address)d"
        case .quad: return "\(self.address)"
        }
    }
}

public enum MemoryAddress<RegisterType: Register>: Equatable, CustomStringConvertible {
    case relative(base: RegisterValue<RegisterType>, offset: Int)
    case indexed(base: RegisterValue<RegisterType>, index: RegisterValue<RegisterType>, scale: Int, offset: Int)

    public var registers: Set<RegisterType> {
        switch self {
        case .relative(base: let base, offset: _):
            return [base.register]
        case .indexed(base: let base, index: let index, scale: _, offset: _):
            return [base.register, index.register]
        }
    }

    public mutating func substitute(_ register: RegisterType, with argument: Argument<RegisterType>) {
        switch self {
        case .relative(base: var base, offset: let offset):
            base.substitute(register, with: argument)

            self = .relative(base: base, offset: offset)
        case .indexed(base: var base, index: var index, scale: let scale, offset: let offset):
            base.substitute(register, with: argument)
            index.substitute(register, with: argument)

            if index.register == register, case .constant(let value) = argument, let offset = Int32(exactly: offset + scale * value.value) {
                self = MemoryAddress.relative(base: base, offset: Int(offset))
            }
            else {
                self = MemoryAddress.indexed(base: base, index: index, scale: scale, offset: offset)
            }
        }
    }

    public var description: String {
        switch self {
        case .relative(base: let base, offset: let offset):
            if offset != 0 {
                return "\(offset)(\(base))"
            }
            else {
                return "(\(base))"
            }
        case .indexed(base: let base, index: let index, scale: let scale, offset: let offset):
            var description = ""
            if offset != 0 {
                description += "\(offset)"
            }
            description += "(\(base),\(index)"
            if scale != 1 {
                description += ",\(scale)"
            }
            description += ")"

            return description
        }
    }
}
