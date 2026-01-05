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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntervalTreeTest {

    @Test
    void testSearch() {
        List<Interval<Integer, Void>> intervals = new ArrayList<>();

        intervals.add(new Interval<>(-300, -200));
        intervals.add(new Interval<>(-3, -2));
        intervals.add(new Interval<>(1, 2));
        intervals.add(new Interval<>(3, 6));
        intervals.add(new Interval<>(2, 4));
        intervals.add(new Interval<>(5, 7));
        intervals.add(new Interval<>(1, 3));
        intervals.add(new Interval<>(4, 6));
        intervals.add(new Interval<>(8, 9));
        intervals.add(new Interval<>(15, 20));
        intervals.add(new Interval<>(40, 50));
        intervals.add(new Interval<>(49, 60));

        IntervalTree<Integer, Void> tree = new IntervalTree<>(intervals);

        assertEquals(3, tree.search(new Interval<>(4, 4)).size());
        assertEquals(4, tree.search(new Interval<>(4, 5)).size());
        assertEquals(7, tree.search(new Interval<>(-1, 10)).size());
        assertEquals(0, tree.search(new Interval<>(-1, -1)).size());
        assertEquals(5, tree.search(new Interval<>(1, 4)).size());
        assertEquals(2, tree.search(new Interval<>(0, 1)).size());
        assertEquals(0, tree.search(new Interval<>(10, 12)).size());
    }

    @Test
    void testSearchWithData() {
        List<Interval<Integer, String>> intervals = new ArrayList<>();

        // Composers with their lifespans
        intervals.add(new Interval<>(1880, 1971, "Stravinsky"));
        intervals.add(new Interval<>(1874, 1951, "Schoenberg"));
        intervals.add(new Interval<>(1843, 1907, "Grieg"));
        intervals.add(new Interval<>(1779, 1828, "Schubert"));
        intervals.add(new Interval<>(1756, 1791, "Mozart"));
        intervals.add(new Interval<>(1585, 1672, "Schuetz"));

        IntervalTree<Integer, String> tree = new IntervalTree<>(intervals);

        // No composers alive between 1829-1842
        assertEquals(0, tree.search(new Interval<>(1829, 1842)).size());

        // Who was alive in 1907?
        List<String> alive1907 = tree.search(new Interval<>(1907, 1907));
        assertEquals(3, alive1907.size());
        assertTrue(alive1907.contains("Stravinsky"));
        assertTrue(alive1907.contains("Schoenberg"));
        assertTrue(alive1907.contains("Grieg"));

        // Who was alive 1780-1790?
        List<String> alive1780s = tree.search(new Interval<>(1780, 1790));
        assertEquals(2, alive1780s.size());
        assertTrue(alive1780s.contains("Schubert"));
        assertTrue(alive1780s.contains("Mozart"));
    }

    @Test
    void testSearchPoint() {
        List<Interval<Integer, String>> intervals = new ArrayList<>();
        intervals.add(new Interval<>(0, 10, "a"));
        intervals.add(new Interval<>(5, 15, "b"));
        intervals.add(new Interval<>(20, 30, "c"));

        IntervalTree<Integer, String> tree = new IntervalTree<>(intervals);

        List<String> at5 = tree.searchPoint(5);
        assertEquals(2, at5.size());
        assertTrue(at5.contains("a"));
        assertTrue(at5.contains("b"));

        List<String> at25 = tree.searchPoint(25);
        assertEquals(1, at25.size());
        assertTrue(at25.contains("c"));

        assertEquals(0, tree.searchPoint(100).size());
    }

    @Test
    void testEmptyTree() {
        IntervalTree<Integer, Void> emptyTree = new IntervalTree<>(Collections.emptyList());
        assertTrue(emptyTree.search(new Interval<>(0, 10)).isEmpty());

        IntervalTree<Integer, Void> nullTree = new IntervalTree<>(null);
        assertTrue(nullTree.search(new Interval<>(0, 10)).isEmpty());
    }

    @Test
    void testSingleInterval() {
        List<Interval<Integer, String>> intervals = List.of(new Interval<>(5, 10, "only"));
        IntervalTree<Integer, String> tree = new IntervalTree<>(intervals);

        assertEquals(1, tree.search(new Interval<>(7, 8)).size());
        assertEquals(1, tree.search(new Interval<>(0, 100)).size());
        assertEquals(0, tree.search(new Interval<>(0, 4)).size());
        assertEquals(0, tree.search(new Interval<>(11, 20)).size());
    }

    @Test
    void testGetAllIntervals() {
        List<Interval<Integer, String>> intervals = new ArrayList<>();
        intervals.add(new Interval<>(1, 5, "a"));
        intervals.add(new Interval<>(3, 8, "b"));
        intervals.add(new Interval<>(10, 15, "c"));

        IntervalTree<Integer, String> tree = new IntervalTree<>(intervals);

        List<Interval<Integer, String>> all = tree.getAllIntervals();
        assertEquals(3, all.size());
    }

    @Test
    void testOverlappingIntervalsAtBoundary() {
        List<Interval<Integer, String>> intervals = new ArrayList<>();
        intervals.add(new Interval<>(0, 5, "a"));
        intervals.add(new Interval<>(5, 10, "b"));  // touches a at point 5

        IntervalTree<Integer, String> tree = new IntervalTree<>(intervals);

        // Search exactly at boundary
        List<String> atBoundary = tree.search(new Interval<>(5, 5));
        assertEquals(2, atBoundary.size());
    }

    @Test
    void testWithStringBounds() {
        List<Interval<String, Integer>> intervals = new ArrayList<>();
        intervals.add(new Interval<>("a", "c", 1));
        intervals.add(new Interval<>("b", "d", 2));
        intervals.add(new Interval<>("e", "g", 3));

        IntervalTree<String, Integer> tree = new IntervalTree<>(intervals);

        List<Integer> results = tree.search(new Interval<>("b", "b"));
        assertEquals(2, results.size());
        assertTrue(results.contains(1));
        assertTrue(results.contains(2));
    }
}
