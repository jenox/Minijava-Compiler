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

    public func matches(_ width: RegisterWidth) -> Bool {
        switch self {
        case .constant(_):
            return true
        case .register(let value):
            return value.width == width
        case .memory(_):
            return true
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

    public func matches(_ width: RegisterWidth) -> Bool {
        switch self {
        case .register(let value):
            return value.width == width
        case .memory(_):
            return true
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
