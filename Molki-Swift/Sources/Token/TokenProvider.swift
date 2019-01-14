//
//  TokenProvider.swift
//  Molki
//
//  Created by Christian Schnorr on 04.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public protocol TokenProvider {
    func nextToken() throws -> Token?
}
