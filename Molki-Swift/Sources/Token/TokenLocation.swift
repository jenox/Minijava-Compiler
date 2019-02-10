//
//  TokenLocation.swift
//  Molki
//
//  Created by Christian Schnorr on 04.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct TokenLocation: CustomStringConvertible {
    public var path: String
    public var line: Int
    public var column: Int

    public var description: String {
        return "\(self.path):\(self.line):\(self.column)"
    }
}
