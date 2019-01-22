//
//  Operands.swift
//  Molki
//
//  Created by Christian Schnorr on 14.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public enum Argument<RegisterType: Register>: CustomStringConvertible {
    case constant(ConstantValue)
    case register(RegisterValue<RegisterType>)
    case memory(MemoryValue<RegisterType>)

    public var width: RegisterWidth {
        switch self {
        case .constant(let value):
            return value.width
        case .register(let value):
            return value.width
        case .memory(let value):
            return value.width
        }
    }

    public mutating func substitute(_ register: RegisterType, with constant: Int) {
        switch self {
        case .constant:
            break
        case .register(let value):
            if value.register == register {
                self = .constant(ConstantValue(value: constant, width: value.width))
            }
        case .memory(let value):
            switch value.address {
            case .relative:
                break
            case .indexed(base: let base, index: let index, scale: let scale, offset: let offset):
                if index.register == register {
                    let address = MemoryAddress.relative(base: base, offset: offset + scale * constant)
                    let value = MemoryValue(address: address, width: value.width)

                    self = .memory(value)
                }
            }
        }
    }

    public var description: String {
        switch self {
        case .constant(let value):
            return value.description
        case .register(let value):
            return value.description
        case .memory(let value):
            return value.description
        }
    }
}

public enum Result<RegisterType: Register>: CustomStringConvertible {
    case register(RegisterValue<RegisterType>)
    case memory(MemoryValue<RegisterType>)

    public var width: RegisterWidth {
        switch self {
        case .register(let value):
            return value.width
        case .memory(let value):
            return value.width
        }
    }

    public var description: String {
        switch self {
        case .register(let value):
            return value.description
        case .memory(let value):
            return value.description
        }
    }
}
