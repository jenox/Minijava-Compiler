//
//  Instruction.swift
//  Molki
//
//  Created by Christian Schnorr on 05.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Swift


public enum Instruction {
    case twoAddressCodeInstruction(TwoAddressCodeInstruction)
    case threeAddressCodeInstruction(ThreeAddressCodeInstruction)
    case fourAddressCodeInstruction(FourAddressCodeInstruction)
    case jumpInstruction(JumpInstruction)
    case callInstruction(CallInstruction)
    case labelInstruction(LabelInstruction)
}
