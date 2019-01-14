//
//  PseudoValue.swift
//  Molki
//
//  Created by Christian Schnorr on 05.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public enum Value: CustomStringConvertible {
    case constant(Int)
    case register(RegisterValue)
    case memory(MemoryAddress)

    public var description: String {
        switch self {
        case .constant(let value):
            return "$\(value)"
        case .register(let register):
            return "\(register)"
        case .memory(let address):
            return "\(address)"
        }
    }
}
