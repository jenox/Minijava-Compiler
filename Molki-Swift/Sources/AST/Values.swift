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

    public var description: String {
        return self.register.name(for: self.width)
    }
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
