//
//  Register.swift
//  Molki
//
//  Created by Christian Schnorr on 14.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public enum RegisterWidth: Int, Comparable {
    case byte = 1
    case word = 2
    case double = 4
    case quad = 8

    public static func < (lhs: RegisterWidth, rhs: RegisterWidth) -> Bool {
        return lhs.rawValue < rhs.rawValue
    }
}

public protocol Register: Hashable {
    func name(for width: RegisterWidth) -> String
}

extension Register {
    public func with(_ width: RegisterWidth) -> RegisterValue<Self> {
        return RegisterValue(register: self, width: width)
    }
}

public enum Pseudoregister: Register, CustomStringConvertible {
    case regular(Int)
    case phi(Int)
    case reserved

    public func name(for width: RegisterWidth) -> String {
        switch width {
        case .byte: return "%\(self.basename)l"
        case .word: return "%\(self.basename)w"
        case .double: return "%\(self.basename)d"
        case .quad: return "%\(self.basename)"
        }
    }

    private var basename: String {
        switch self {
        case .regular(let number):
            return "@\(number)"
        case .phi(let number):
            return "@-\(number)"
        case .reserved:
            return "@$"
        }
    }

    public var description: String {
        switch self {
        case .regular(let number):
            return "\(number)"
        case .phi(let number):
            return "phi(\(number))"
        case .reserved:
            return "retval"
        }
    }

    public enum Kind {
        case regular
        case phi
        case reserved
    }

    public var kind: Kind {
        switch self {
        case .regular: return .regular
        case .phi: return .phi
        case .reserved: return .reserved
        }
    }
}

public enum X86Register: Register {
    case rax, rbx, rcx, rdx
    case rsi, rdi, rsp, rbp
    case r8, r9, r10, r11 // caller-saved
    case r12, r13, r14, r15 // callee-saved

    private var basename: String {
        switch self {
        case .rax: return "a"
        case .rbx: return "b"
        case .rcx: return "c"
        case .rdx: return "d"
        case .rsi: return "si"
        case .rdi: return "di"
        case .rsp: return "sp"
        case .rbp: return "bp"
        case .r8: return "r8"
        case .r9: return "r9"
        case .r10: return "r10"
        case .r11: return "r11"
        case .r12: return "r12"
        case .r13: return "r13"
        case .r14: return "r14"
        case .r15: return "r15"
        }
    }

    public func name(for width: RegisterWidth) -> String {
        switch self {
        case .rax, .rbx, .rcx, .rdx:
            switch width {
            case .byte: return "%\(self.basename)l"
            case .word: return "%\(self.basename)x"
            case .double: return "%e\(self.basename)x"
            case .quad: return "%r\(self.basename)x"
            }
        case .rsi, .rdi, .rsp, .rbp:
            switch width {
            case .byte: return "%\(self.basename)l"
            case .word: return "%\(self.basename)"
            case .double: return "%e\(self.basename)"
            case .quad: return "%r\(self.basename)"
            }
        case .r8, .r9, .r10, .r11, .r12, .r13, .r14, .r15:
            switch width {
            case .byte: return "%\(self.basename)b"
            case .word: return "%\(self.basename)w"
            case .double: return "%\(self.basename)d"
            case .quad: return "%\(self.basename)"
            }
        }
    }
}
