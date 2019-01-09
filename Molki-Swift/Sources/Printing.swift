//
//  Printing.swift
//  Molki
//
//  Created by Christian Schnorr on 04.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Darwin


extension UnsafeMutablePointer: TextOutputStream where Pointee == FILE {
    public mutating func write(_ string: String) {
        fputs(string, self)
    }
}
