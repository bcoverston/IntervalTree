/*
 * Copyright (c) 2011, 2025 Benjamin Jacob Coverston
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.coversb.collections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntervalNodeTest {

    @Test
    void testNodeConstruction() {
        List<Interval<Integer, String>> intervals = new ArrayList<>();
        intervals.add(new Interval<>(1, 2, "range1"));
        intervals.add(new Interval<>(2, 3, "range2"));

        IntervalNode<Integer, String> node = new IntervalNode<>(intervals);

        assertNotNull(node.pivot);
        assertEquals(2, node.pivot);  // median of {1, 2, 3} is 2
    }

    @Test
    void testNodePartitioning() {
        List<Interval<Integer, String>> intervals = new ArrayList<>();
        intervals.add(new Interval<>(1, 2, "a"));    // left of pivot
        intervals.add(new Interval<>(2, 3, "b"));    // left of pivot
        intervals.add(new Interval<>(3, 4, "c"));    // contains pivot
        intervals.add(new Interval<>(4, 5, "d"));    // contains pivot
        intervals.add(new Interval<>(6, 7, "e"));    // right of pivot

        IntervalNode<Integer, String> node = new IntervalNode<>(intervals);

        // Pivot should be 4 (median of {1,2,3,4,5,6,7})
        assertEquals(4, node.pivot);

        // Intervals containing pivot should be stored at node
        assertEquals(2, node.sortedByMin.size());
        assertEquals(2, node.sortedByMax.size());

        // Should have left and right children
        assertNotNull(node.left);
        assertNotNull(node.right);
    }

    @Test
    void testEmptyNode() {
        IntervalNode<Integer, String> node = new IntervalNode<>(new ArrayList<>());
        assertNull(node.pivot);
        assertTrue(node.sortedByMin.isEmpty());
        assertTrue(node.sortedByMax.isEmpty());
        assertNull(node.left);
        assertNull(node.right);
    }

    @Test
    void testSingleInterval() {
        List<Interval<Integer, String>> intervals = List.of(new Interval<>(5, 10, "only"));
        IntervalNode<Integer, String> node = new IntervalNode<>(intervals);

        assertNotNull(node.pivot);
        assertEquals(1, node.sortedByMin.size());
        assertNull(node.left);
        assertNull(node.right);
    }
}
