//
//  CallInstruction.swift
//  Molki
//
//  Created by Christian Schnorr on 05.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct CallInstruction: CustomStringConvertible {
    public var name: String
    public var arguments: [Value]
    public var returnValue: Value?

    public var description: String {
        var description = "call \(self.name) ["

        if self.arguments.isEmpty {
            description += "]"
        }
        else {
            description += " " + self.arguments.map({ $0.description }).joined(separator: " | ") + " ]"
        }

        if let returnValue = self.returnValue {
            description += " -> \(returnValue)"
        }

        return description
    }
}
