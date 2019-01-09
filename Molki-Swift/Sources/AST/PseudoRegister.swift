//
//  PseudoRegister.swift
//  Molki
//
//  Created by Christian Schnorr on 05.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public enum Register: Equatable, Hashable, CustomStringConvertible {
    case identified(Int)
    case returnValue

    public var description: String {
        switch self {
        case .identified(let number):
            return "%@\(number)"
        case .returnValue:
            return "%@r0"
        }
    }
}
