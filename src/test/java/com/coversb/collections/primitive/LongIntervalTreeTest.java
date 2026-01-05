/*
 * Copyright (c) 2011, 2025 Benjamin Jacob Coverston
 * All rights reserved.
 * BSD-2-Clause License
 */

package com.coversb.collections.primitive;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link LongIntervalTree}.
 */
class LongIntervalTreeTest {

    @Test
    void testEmptyTree() {
        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>().build();

        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());

        final AtomicInteger count = new AtomicInteger(0);
        tree.search(0, 100, (min, max, data) -> {
            count.incrementAndGet();
            return true;
        });

        assertEquals(0, count.get());
    }

    @Test
    void testSingleInterval() {
        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
            .add(10, 20, "only")
            .build();

        assertEquals(1, tree.size());
        assertFalse(tree.isEmpty());

        // Query that hits
        final List<String> results = new ArrayList<>();
        tree.search(15, 15, (min, max, data) -> {
            results.add(data);
            return true;
        });
        assertEquals(1, results.size());
        assertEquals("only", results.get(0));

        // Query that misses (before)
        results.clear();
        tree.search(0, 9, (min, max, data) -> {
            results.add(data);
            return true;
        });
        assertEquals(0, results.size());

        // Query that misses (after)
        results.clear();
        tree.search(21, 100, (min, max, data) -> {
            results.add(data);
            return true;
        });
        assertEquals(0, results.size());
    }

    @Test
    void testMultipleIntervals() {
        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
            .add(0, 10, "a")
            .add(5, 15, "b")
            .add(20, 30, "c")
            .add(25, 35, "d")
            .build();

        assertEquals(4, tree.size());

        // Query overlapping a and b
        final List<String> results = new ArrayList<>();
        tree.search(8, 12, (min, max, data) -> {
            results.add(data);
            return true;
        });

        assertTrue(results.contains("a"));
        assertTrue(results.contains("b"));
        assertFalse(results.contains("c"));
        assertFalse(results.contains("d"));
    }

    @Test
    void testPointSearch() {
        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
            .add(0, 10, "first")
            .add(5, 15, "second")
            .add(10, 20, "third")
            .build();

        final List<String> results = new ArrayList<>();
        tree.searchPoint(10, (min, max, data) -> {
            results.add(data);
            return true;
        });

        // Point 10 is in all three intervals
        assertEquals(3, results.size());
        assertTrue(results.contains("first"));
        assertTrue(results.contains("second"));
        assertTrue(results.contains("third"));
    }

    @Test
    void testBoundaryConditions() {
        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
            .add(0, 10, "a")
            .add(10, 20, "b") // Touches a at 10
            .build();

        // Search exactly at boundary point
        final List<String> results = new ArrayList<>();
        tree.searchPoint(10, (min, max, data) -> {
            results.add(data);
            return true;
        });

        // Both intervals include point 10
        assertEquals(2, results.size());
    }

    @Test
    void testEarlyTermination() {
        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
            .add(0, 100, "a")
            .add(0, 100, "b")
            .add(0, 100, "c")
            .add(0, 100, "d")
            .build();

        final List<String> results = new ArrayList<>();
        tree.search(50, 50, (min, max, data) -> {
            results.add(data);
            return results.size() < 2; // Stop after 2
        });

        assertEquals(2, results.size());
    }

    @Test
    void testLargeTree() {
        final int size = 10_000;
        final LongIntervalTreeBuilder<Integer> builder = new LongIntervalTreeBuilder<>(size);

        for (int i = 0; i < size; i++) {
            builder.add(i * 10L, i * 10L + 5, i);
        }

        final LongIntervalTree<Integer> tree = builder.build();
        assertEquals(size, tree.size());

        // Query that should hit ~10 intervals
        final AtomicInteger count = new AtomicInteger(0);
        tree.search(500, 600, (min, max, data) -> {
            count.incrementAndGet();
            return true;
        });

        assertTrue(count.get() > 0);
        assertTrue(count.get() < 20); // Sanity check
    }

    @Test
    void testSearchResultBuffer() {
        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
            .add(0, 10, "a")
            .add(5, 15, "b")
            .add(10, 20, "c")
            .build();

        final SearchResultBuffer<String> buffer = new SearchResultBuffer<>(100);

        tree.search(8, 12, buffer::add);

        assertEquals(3, buffer.size());

        // Verify we can access results
        boolean foundA = false;
        boolean foundB = false;
        boolean foundC = false;

        for (int i = 0; i < buffer.size(); i++) {
            final String data = buffer.getData(i);
            if ("a".equals(data)) {
                foundA = true;
            }
            if ("b".equals(data)) {
                foundB = true;
            }
            if ("c".equals(data)) {
                foundC = true;
            }
        }

        assertTrue(foundA);
        assertTrue(foundB);
        assertTrue(foundC);

        // Test reuse
        buffer.clear();
        assertEquals(0, buffer.size());

        tree.search(100, 200, buffer::add);
        assertEquals(0, buffer.size()); // No hits
    }

    @Test
    void testMemoryEstimation() {
        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
            .add(0, 10, "a")
            .add(5, 15, "b")
            .build();

        final long memoryBytes = tree.estimateMemoryBytes();
        assertTrue(memoryBytes > 0);
        assertTrue(memoryBytes < 10_000); // Sanity check for small tree
    }

    @Test
    void testNegativeIntervals() {
        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
            .add(-100, -50, "negative")
            .add(-25, 25, "spanning")
            .add(50, 100, "positive")
            .build();

        final List<String> results = new ArrayList<>();

        // Query in negative range
        tree.search(-75, -60, (min, max, data) -> {
            results.add(data);
            return true;
        });
        assertEquals(1, results.size());
        assertTrue(results.contains("negative"));

        // Query spanning zero
        results.clear();
        tree.search(-10, 10, (min, max, data) -> {
            results.add(data);
            return true;
        });
        assertEquals(1, results.size());
        assertTrue(results.contains("spanning"));
    }

    @Test
    void testLongMinMax() {
        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
            .add(Long.MIN_VALUE, Long.MIN_VALUE + 100, "min")
            .add(Long.MAX_VALUE - 100, Long.MAX_VALUE, "max")
            .add(-1, 1, "zero")
            .build();

        final List<String> results = new ArrayList<>();

        tree.searchPoint(Long.MIN_VALUE, (min, max, data) -> {
            results.add(data);
            return true;
        });
        assertEquals(1, results.size());
        assertTrue(results.contains("min"));

        results.clear();
        tree.searchPoint(Long.MAX_VALUE, (min, max, data) -> {
            results.add(data);
            return true;
        });
        assertEquals(1, results.size());
        assertTrue(results.contains("max"));
    }

    @Test
    void testTimestampUseCase() {
        // Simulate scheduling/time-range queries
        final long hour = 3600_000L; // milliseconds
        final long day = 24 * hour;
        final long baseTime = 1704067200000L; // 2024-01-01 00:00:00 UTC

        final LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
            .add(baseTime, baseTime + 2 * hour, "meeting1")
            .add(baseTime + hour, baseTime + 3 * hour, "meeting2")
            .add(baseTime + 4 * hour, baseTime + 5 * hour, "meeting3")
            .add(baseTime + day, baseTime + day + hour, "tomorrow")
            .build();

        // Query: what's happening at hour 1.5?
        final List<String> results = new ArrayList<>();
        final long queryTime = baseTime + (long) (1.5 * hour);

        tree.searchPoint(queryTime, (min, max, data) -> {
            results.add(data);
            return true;
        });

        assertEquals(2, results.size());
        assertTrue(results.contains("meeting1"));
        assertTrue(results.contains("meeting2"));
    }
}
