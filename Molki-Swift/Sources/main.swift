//
//  main.swift
//  Molki
//
//  Created by Christian Schnorr on 23.12.18.
//  Copyright © 2018 Christian Schnorr. All rights reserved.
//

import Foundation


do {
    let sourceURL: URL
    let targetURL: URL

    if CommandLine.arguments.count >= 2 {
        sourceURL = URL(fileURLWithPath: CommandLine.arguments[1])

        if CommandLine.arguments.count >= 3 {
            targetURL = URL(fileURLWithPath: CommandLine.arguments[2])
        }
        else {
            targetURL = URL(fileURLWithPath: "a.out.s")
        }
    }
    else {
        fatalError("Not enough arguments, using sample input file.")
    }

    print("Input file:", sourceURL.path)
    print("Output file:", targetURL.path)

    let input = String(data: try Data(contentsOf: sourceURL), encoding: .utf8)!
    let lexer = Lexer(path: sourceURL.lastPathComponent, text: input)
    let parser = Parser(tokenProvider: lexer)

    var lines: [String] = []

    for function in try parser.parseFunctions() {
        ConstantFolder(function: function).fold(untilConverged: true)

//        FunctionValidator(function: function).validate()

        let generator = AssemblerGenerator(function: function)
        lines.append(contentsOf: generator.lines)
    }

    lines.append("")

    let data = lines.joined(separator: "\n").data(using: .utf8)!
    try! data.write(to: targetURL)
}
catch let error as LexerError {
    print("Lexer error:", error)
    exit(1)
}
catch let error as ParserError {
    print("Parser error:", error)
    if let token = error.parserDefinedError {
        print(token.context)
    }
    exit(1)
}
catch let error {
    print("Unknown error:", error)
    exit(1)
}
