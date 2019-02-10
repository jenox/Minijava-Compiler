//
//  Token.swift
//  Molki
//
//  Created by Christian Schnorr on 24.12.18.
//  Copyright © 2018 Christian Schnorr. All rights reserved.
//

import Swift


public struct Token {
    public init(type: TokenType, payload: String, location: TokenLocation, context: TokenContext) {
        self.type = type
        self.payload = payload
        self.location = location
        self.context = context
    }

    public var type: TokenType
    public var payload: String
    public var location: TokenLocation
    public var context: TokenContext
}
