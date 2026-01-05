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

import static org.junit.jupiter.api.Assertions.*;

class IntervalTest {

    @Test
    void testEncloses() {
        Interval<Integer, Void> interval = new Interval<>(0, 5);
        Interval<Integer, Void> interval1 = new Interval<>(0, 10);
        Interval<Integer, Void> interval2 = new Interval<>(5, 10);
        Interval<Integer, Void> interval3 = new Interval<>(0, 11);

        assertTrue(interval.encloses(interval));
        assertTrue(interval1.encloses(interval));
        assertFalse(interval.encloses(interval2));
        assertTrue(interval1.encloses(interval2));
        assertFalse(interval1.encloses(interval3));
    }

    @Test
    void testContains() {
        Interval<Integer, Void> interval = new Interval<>(0, 5);
        assertTrue(interval.contains(0));
        assertTrue(interval.contains(5));
        assertTrue(interval.contains(3));
        assertFalse(interval.contains(-1));
        assertFalse(interval.contains(6));
    }

    @Test
    void testIntersects() {
        Interval<Integer, Void> interval = new Interval<>(0, 5);
        Interval<Integer, Void> interval1 = new Interval<>(0, 10);
        Interval<Integer, Void> interval2 = new Interval<>(5, 10);
        Interval<Integer, Void> interval3 = new Interval<>(0, 11);
        Interval<Integer, Void> interval5 = new Interval<>(6, 12);

        assertTrue(interval.intersects(interval1));
        assertTrue(interval.intersects(interval2));
        assertTrue(interval.intersects(interval3));
        assertFalse(interval.intersects(interval5));
    }

    @Test
    void testIntersectsFullyEnclosed() {
        // This was the bug case: [2,5] fully inside [0,10]
        Interval<Integer, Void> outer = new Interval<>(0, 10);
        Interval<Integer, Void> inner = new Interval<>(2, 5);

        assertTrue(inner.intersects(outer));
        assertTrue(outer.intersects(inner));
    }

    @Test
    void testIntersectsEdgeCases() {
        Interval<Integer, Void> a = new Interval<>(0, 5);
        Interval<Integer, Void> b = new Interval<>(5, 10);  // touching at point
        Interval<Integer, Void> c = new Interval<>(6, 10);  // no overlap

        assertTrue(a.intersects(b));   // touching counts as intersecting
        assertTrue(b.intersects(a));
        assertFalse(a.intersects(c));
        assertFalse(c.intersects(a));
    }

    @Test
    void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Interval<>(5, 0));
        assertThrows(NullPointerException.class, () -> new Interval<>(null, 5));
        assertThrows(NullPointerException.class, () -> new Interval<>(0, null));
    }

    @Test
    void testPointInterval() {
        Interval<Integer, Void> point = new Interval<>(5, 5);
        assertTrue(point.contains(5));
        assertFalse(point.contains(4));
        assertFalse(point.contains(6));
    }

    @Test
    void testWithData() {
        Interval<Integer, String> interval = new Interval<>(0, 10, "test data");
        assertEquals("test data", interval.getData());
        assertEquals(0, interval.getMin());
        assertEquals(10, interval.getMax());
    }

    @Test
    void testEqualsAndHashCode() {
        Interval<Integer, String> a = new Interval<>(0, 10, "data");
        Interval<Integer, String> b = new Interval<>(0, 10, "data");
        Interval<Integer, String> c = new Interval<>(0, 10, "other");
        Interval<Integer, String> d = new Interval<>(0, 11, "data");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, d);
    }

    @Test
    void testToString() {
        Interval<Integer, String> interval = new Interval<>(0, 10, "data");
        assertEquals("[0, 10] -> data", interval.toString());

        Interval<Integer, Void> noData = new Interval<>(0, 10);
        assertEquals("[0, 10]", noData.toString());
    }
}
