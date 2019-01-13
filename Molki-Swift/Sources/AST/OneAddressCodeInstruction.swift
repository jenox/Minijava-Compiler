//
//  OneAddressCodeInstruction.swift
//  Molki
//
//  Created by Christian Schnorr on 13.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct OneAddressCodeInstruction: CustomStringConvertible {
    public var operation: String
    public var first: Value

    public var description: String {
        return "\(self.operation) \(self.first)"
    }
}
