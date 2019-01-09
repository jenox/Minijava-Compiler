//
//  TwoAddressCodeInstruction.swift
//  Molki
//
//  Created by Christian Schnorr on 05.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct TwoAddressCodeInstruction: CustomStringConvertible {
    public var operation: String
    public var first: Value
    public var second: Value

    public var description: String {
        return "\(self.operation) \(self.first) \(self.second)"
    }
}
