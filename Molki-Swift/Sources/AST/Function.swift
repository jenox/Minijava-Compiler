//
//  Function.swift
//  Molki
//
//  Created by Christian Schnorr on 14.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public struct Function {
    public var name: String
    public var parameterWidths: [RegisterWidth]
    public var returnValueWidth: RegisterWidth?
    public var instructions: [Instruction]
}
