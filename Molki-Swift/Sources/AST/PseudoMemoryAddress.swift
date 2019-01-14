//
//  PseudoMemoryAddress.swift
//  Molki
//
//  Created by Christian Schnorr on 05.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public enum MemoryAddress: CustomStringConvertible {
    case relative(base: RegisterValue, offset: Int)
    case indexed(base: RegisterValue, index: RegisterValue, scale: Int, offset: Int)

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
