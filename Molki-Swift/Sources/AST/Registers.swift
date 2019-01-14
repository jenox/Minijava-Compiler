//
//  Register.swift
//  Molki
//
//  Created by Christian Schnorr on 14.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public enum RegisterWidth {
    case byte
    case word
    case double
    case quad
}

public protocol Register {
    func name(for width: RegisterWidth) -> String
}

public enum Pseudoregister: Register {
    case numbered(Int)
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
        case .numbered(let number):
            return "@\(number)"
        case .reserved:
            return "@r0"
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
