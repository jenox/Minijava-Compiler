//
//  Misc.swift
//  Molki
//
//  Created by Christian Schnorr on 04.01.19.
//  Copyright © 2019 Christian Schnorr. All rights reserved.
//

import Foundation


extension Collection {
    public func slice(from index: Index, toFirstWhere predicate: (Element) -> Bool) -> SubSequence {
        let lowerBound = index
        var upperBound = index

        while upperBound < self.endIndex, !predicate(self[upperBound]) {
            upperBound = self.index(after: upperBound)
        }

        return self[lowerBound..<upperBound]
    }
}

extension BidirectionalCollection {
    #if swift(>=4.2)
    #else
    public func lastIndex(where predicate: (Element) throws -> Bool) rethrows -> Index? {
        var index = self.endIndex

        while index != self.startIndex {
            self.formIndex(before: &index)

            if try predicate(self[index]) {
                return index
            }
        }

        return nil
    }
    #endif
}

extension Collection where Element: Equatable {
    public func count(where predicate: (Element) throws -> Bool) rethrows -> Int {
        return try self.reduce(0, { $0 + (try predicate($1) ? 1 : 0) })
    }
}

extension Int {
    public func floored(toMultipleOf divisor: Int) -> Int {
        precondition(divisor >= 0)

        if self > 0 {
            return self / divisor * divisor
        }
        else {
            return (self - divisor + 1) / divisor * divisor
        }
    }

    public func ceiled(toMultipleOf divisor: Int) -> Int {
        precondition(divisor >= 0)

        if self > 0 {
            return (self + divisor - 1) / divisor * divisor
        }
        else {
            return self / divisor * divisor
        }
    }

    public func isMultiple(of other: Int) -> Bool {
        // Nothing but zero is a multiple of zero.
        if other == 0 { return self == 0 }
        // Special case to avoid overflow on .min / -1 for signed types.
        if Int.isSigned && other == -1 { return true }
        // Having handled those special cases, this is safe.
        return self % other == 0
    }
}
