//
//  PseudoRegisterValue.swift
//  Molki
//
//  Created by Christian Schnorr on 05.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct RegisterValue: CustomStringConvertible {
    public var register: Register
    public var width: Width

    public var description: String {
        switch self.width {
        case .byte: return "\(self.register)l" // lower?
        case .word: return "\(self.register)w" // word?
        case .double: return "\(self.register)d" // double?
        case .quad: return "\(self.register)"
        }
    }
}

public enum Width {
    case byte // 8 bit
    case word // 16 bit
    case double // 32 bit
    case quad // 64 bit
}
