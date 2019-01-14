//
//  JumpInstruction.swift
//  Molki
//
//  Created by Christian Schnorr on 05.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct JumpInstruction: CustomStringConvertible {
    public var operation: String
    public var target: String

    public var description: String {
        return "\(self.operation) \(self.target)"
    }
}
