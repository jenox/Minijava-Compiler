//
//  LabelInstruction.swift
//  Molki
//
//  Created by Christian Schnorr on 05.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct LabelInstruction: CustomStringConvertible {
    public var name: String

    public var description: String {
        return "\(self.name):"
    }
}
