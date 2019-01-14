//
//  TokenType.swift
//  Molki
//
//  Created by Christian Schnorr on 04.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public enum TokenType {
    case identifier
    case integer
    case openingParenthesis // (
    case closingParenthesis // )
    case openingBracket // [
    case closingBracket // ]
    case comma // ,
    case pipe // |
    case arrow // ->
    case period // .
    case colon // :
    case dollar // $
    case minus // -
    case pseudoregister // %@
}
