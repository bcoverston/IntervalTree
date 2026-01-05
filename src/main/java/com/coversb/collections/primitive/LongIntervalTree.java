/*
 * Copyright (c) 2011, 2025 Benjamin Jacob Coverston
 * All rights reserved.
 * BSD-2-Clause License
 */

package com.coversb.collections.primitive;

import java.util.Arrays;

/**
 * An interval tree using primitive long intervals stored in contiguous arrays.
 *
 * <h2>Design</h2>
 * <ul>
 *   <li><b>Contiguous Storage:</b> Interval data stored in arrays.
 *       Each interval occupies 16 bytes (2 longs).</li>
 *   <li><b>Zero Allocation:</b> Search operations allocate nothing. Results
 *       written to pre-allocated buffers or delivered via callbacks.</li>
 *   <li><b>Primitive Types:</b> No boxing. Comparisons are direct long comparisons,
 *       not Comparable.compareTo() virtual calls.</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>This class is <b>effectively immutable</b> after construction. Safe for
 * concurrent reads from multiple threads without synchronization.
 *
 * @param <T> the type of data associated with each interval
 * @author Benjamin Coverston
 */
public final class LongIntervalTree<T> {

    // ========================================================================
    // Node structure - each node is a segment of arrays
    // ========================================================================

    /**
     * For each node: pivot value.
     */
    private final long[] nodePivots;

    /**
     * For each node: start index in nodeIntervalIndices for intervals sorted by min.
     */
    private final int[] nodeMinStart;

    /**
     * For each node: start index in nodeIntervalIndices for intervals sorted by max.
     */
    private final int[] nodeMaxStart;

    /**
     * For each node: number of intervals at this node.
     */
    private final int[] nodeIntervalCount;

    /**
     * For each node: left child index, or -1 if none.
     */
    private final int[] nodeLeft;

    /**
     * For each node: right child index, or -1 if none.
     */
    private final int[] nodeRight;

    /**
     * Flattened array of interval indices for all nodes.
     * Each node has two segments: sorted by min, then sorted by max.
     */
    private final int[] nodeIntervalIndices;

    /**
     * Interval bounds: [min0, max0, min1, max1, ...].
     */
    private final long[] intervalBounds;

    /**
     * Associated data for each interval.
     */
    private final Object[] intervalData;

    /**
     * Number of intervals.
     */
    private final int intervalCount;

    /**
     * Number of nodes.
     */
    private final int nodeCount;

    private static final int NULL_NODE = -1;

    // ========================================================================
    // Construction
    // ========================================================================

    /**
     * Constructs an interval tree from parallel arrays.
     *
     * @param mins array of minimum bounds
     * @param maxs array of maximum bounds
     * @param data array of associated data
     */
    @SuppressWarnings("unchecked")
    public LongIntervalTree(final long[] mins, final long[] maxs, final T[] data) {
        if (mins.length != maxs.length || mins.length != data.length) {
            throw new IllegalArgumentException("Array lengths must match");
        }

        this.intervalCount = mins.length;

        if (intervalCount == 0) {
            this.intervalBounds = new long[0];
            this.intervalData = new Object[0];
            this.nodePivots = new long[0];
            this.nodeMinStart = new int[0];
            this.nodeMaxStart = new int[0];
            this.nodeIntervalCount = new int[0];
            this.nodeLeft = new int[0];
            this.nodeRight = new int[0];
            this.nodeIntervalIndices = new int[0];
            this.nodeCount = 0;
            return;
        }

        // Pack intervals
        this.intervalBounds = new long[intervalCount * 2];
        this.intervalData = new Object[intervalCount];

        for (int i = 0; i < intervalCount; i++) {
            if (mins[i] > maxs[i]) {
                throw new IllegalArgumentException(
                    "min[" + i + "]=" + mins[i] + " > max[" + i + "]=" + maxs[i]);
            }
            intervalBounds[i * 2] = mins[i];
            intervalBounds[i * 2 + 1] = maxs[i];
            intervalData[i] = data[i];
        }

        // Build tree
        final TreeBuilder builder = new TreeBuilder(intervalCount);
        final int[] allIndices = new int[intervalCount];
        for (int i = 0; i < intervalCount; i++) {
            allIndices[i] = i;
        }

        builder.buildNode(allIndices, 0, intervalCount);

        this.nodeCount = builder.nodeCount;
        this.nodePivots = Arrays.copyOf(builder.nodePivots, nodeCount);
        this.nodeMinStart = Arrays.copyOf(builder.nodeMinStart, nodeCount);
        this.nodeMaxStart = Arrays.copyOf(builder.nodeMaxStart, nodeCount);
        this.nodeIntervalCount = Arrays.copyOf(builder.nodeIntervalCount, nodeCount);
        this.nodeLeft = Arrays.copyOf(builder.nodeLeft, nodeCount);
        this.nodeRight = Arrays.copyOf(builder.nodeRight, nodeCount);
        this.nodeIntervalIndices = Arrays.copyOf(builder.intervalIndices, builder.intervalIndexPos);
    }

    /**
     * Internal builder to construct tree structure.
     */
    private final class TreeBuilder {
        long[] nodePivots;
        int[] nodeMinStart;
        int[] nodeMaxStart;
        int[] nodeIntervalCount;
        int[] nodeLeft;
        int[] nodeRight;
        int[] intervalIndices;
        int nodeCount;
        int intervalIndexPos;

        TreeBuilder(final int capacity) {
            final int maxNodes = 2 * capacity;
            nodePivots = new long[maxNodes];
            nodeMinStart = new int[maxNodes];
            nodeMaxStart = new int[maxNodes];
            nodeIntervalCount = new int[maxNodes];
            nodeLeft = new int[maxNodes];
            nodeRight = new int[maxNodes];
            intervalIndices = new int[capacity * 4]; // 2x for min-sorted, 2x for max-sorted
            nodeCount = 0;
            intervalIndexPos = 0;
        }

        int buildNode(final int[] indices, final int start, final int end) {
            if (start >= end) {
                return NULL_NODE;
            }

            final int count = end - start;

            // Find median endpoint as pivot
            final long pivot = findMedianEndpoint(indices, start, end);

            // Partition intervals
            final int[] containing = new int[count];
            final int[] left = new int[count];
            final int[] right = new int[count];
            int containingCount = 0;
            int leftCount = 0;
            int rightCount = 0;

            for (int i = start; i < end; i++) {
                final int idx = indices[i];
                final long min = intervalBounds[idx * 2];
                final long max = intervalBounds[idx * 2 + 1];

                if (max < pivot) {
                    left[leftCount++] = idx;
                } else if (min > pivot) {
                    right[rightCount++] = idx;
                } else {
                    containing[containingCount++] = idx;
                }
            }

            // Allocate node
            final int nodeIdx = nodeCount++;
            nodePivots[nodeIdx] = pivot;
            nodeIntervalCount[nodeIdx] = containingCount;

            // Store intervals sorted by min (ascending)
            nodeMinStart[nodeIdx] = intervalIndexPos;
            sortByMin(containing, 0, containingCount - 1);
            for (int i = 0; i < containingCount; i++) {
                ensureIntervalIndicesCapacity();
                intervalIndices[intervalIndexPos++] = containing[i];
            }

            // Store intervals sorted by max (descending)
            nodeMaxStart[nodeIdx] = intervalIndexPos;
            sortByMaxDesc(containing, 0, containingCount - 1);
            for (int i = 0; i < containingCount; i++) {
                ensureIntervalIndicesCapacity();
                intervalIndices[intervalIndexPos++] = containing[i];
            }

            // Build children
            nodeLeft[nodeIdx] = (leftCount > 0) ? buildNode(left, 0, leftCount) : NULL_NODE;
            nodeRight[nodeIdx] = (rightCount > 0) ? buildNode(right, 0, rightCount) : NULL_NODE;

            return nodeIdx;
        }

        void ensureIntervalIndicesCapacity() {
            if (intervalIndexPos >= intervalIndices.length) {
                intervalIndices = Arrays.copyOf(intervalIndices, intervalIndices.length * 2);
            }
        }

        long findMedianEndpoint(final int[] indices, final int start, final int end) {
            final int count = (end - start) * 2;
            final long[] endpoints = new long[Math.min(count, 256)];
            int idx = 0;

            if (count <= 256) {
                for (int i = start; i < end; i++) {
                    final int intervalIdx = indices[i];
                    endpoints[idx++] = intervalBounds[intervalIdx * 2];
                    endpoints[idx++] = intervalBounds[intervalIdx * 2 + 1];
                }
            } else {
                // Sample for large ranges
                final int step = (end - start) / 128;
                for (int i = start; i < end && idx < 256; i += Math.max(1, step)) {
                    final int intervalIdx = indices[i];
                    endpoints[idx++] = intervalBounds[intervalIdx * 2];
                    if (idx < 256) {
                        endpoints[idx++] = intervalBounds[intervalIdx * 2 + 1];
                    }
                }
            }

            Arrays.sort(endpoints, 0, idx);
            return endpoints[idx / 2];
        }

        void sortByMin(final int[] arr, final int lo, final int hi) {
            if (lo >= hi) {
                return;
            }

            final int pivotIdx = arr[lo + (hi - lo) / 2];
            final long pivotVal = intervalBounds[pivotIdx * 2];

            int i = lo;
            int j = hi;

            while (i <= j) {
                while (intervalBounds[arr[i] * 2] < pivotVal) {
                    i++;
                }
                while (intervalBounds[arr[j] * 2] > pivotVal) {
                    j--;
                }
                if (i <= j) {
                    final int tmp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = tmp;
                    i++;
                    j--;
                }
            }

            sortByMin(arr, lo, j);
            sortByMin(arr, i, hi);
        }

        void sortByMaxDesc(final int[] arr, final int lo, final int hi) {
            if (lo >= hi) {
                return;
            }

            final int pivotIdx = arr[lo + (hi - lo) / 2];
            final long pivotVal = intervalBounds[pivotIdx * 2 + 1];

            int i = lo;
            int j = hi;

            while (i <= j) {
                while (intervalBounds[arr[i] * 2 + 1] > pivotVal) {
                    i++;
                }
                while (intervalBounds[arr[j] * 2 + 1] < pivotVal) {
                    j--;
                }
                if (i <= j) {
                    final int tmp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = tmp;
                    i++;
                    j--;
                }
            }

            sortByMaxDesc(arr, lo, j);
            sortByMaxDesc(arr, i, hi);
        }
    }

    // ========================================================================
    // Search Operations
    // ========================================================================

    /**
     * Functional interface for receiving search results.
     */
    @FunctionalInterface
    public interface ResultConsumer<T> {
        /**
         * Called for each matching interval.
         *
         * @param min interval minimum
         * @param max interval maximum
         * @param data associated data
         * @return true to continue, false to stop
         */
        boolean accept(long min, long max, T data);
    }

    /**
     * Searches for all intervals overlapping [queryMin, queryMax].
     *
     * @param queryMin query minimum
     * @param queryMax query maximum
     * @param consumer callback for results
     * @return number of matches
     */
    public int search(final long queryMin, final long queryMax, final ResultConsumer<T> consumer) {
        if (nodeCount == 0) {
            return 0;
        }

        final int[] countHolder = {0};
        searchNode(0, queryMin, queryMax, consumer, countHolder);
        return countHolder[0];
    }

    @SuppressWarnings("unchecked")
    private boolean searchNode(
            final int nodeIdx,
            final long queryMin,
            final long queryMax,
            final ResultConsumer<T> consumer,
            final int[] countHolder) {

        if (nodeIdx == NULL_NODE) {
            return true;
        }

        final long pivot = nodePivots[nodeIdx];
        final int intervalCount = nodeIntervalCount[nodeIdx];
        final int minStart = nodeMinStart[nodeIdx];
        final int maxStart = nodeMaxStart[nodeIdx];
        final int leftChild = nodeLeft[nodeIdx];
        final int rightChild = nodeRight[nodeIdx];

        // Case 1: Query contains pivot - all intervals at node match, search both
        if (queryMin <= pivot && pivot <= queryMax) {
            for (int i = 0; i < intervalCount; i++) {
                final int idx = nodeIntervalIndices[minStart + i];
                final long min = intervalBounds[idx * 2];
                final long max = intervalBounds[idx * 2 + 1];
                countHolder[0]++;
                if (!consumer.accept(min, max, (T) intervalData[idx])) {
                    return false;
                }
            }

            if (!searchNode(leftChild, queryMin, queryMax, consumer, countHolder)) {
                return false;
            }
            return searchNode(rightChild, queryMin, queryMax, consumer, countHolder);
        }

        // Case 2: Pivot < queryMin - check by max (descending), go right
        if (pivot < queryMin) {
            for (int i = 0; i < intervalCount; i++) {
                final int idx = nodeIntervalIndices[maxStart + i];
                final long max = intervalBounds[idx * 2 + 1];

                if (max < queryMin) {
                    break; // Sorted descending, no more
                }

                final long min = intervalBounds[idx * 2];
                countHolder[0]++;
                if (!consumer.accept(min, max, (T) intervalData[idx])) {
                    return false;
                }
            }

            return searchNode(rightChild, queryMin, queryMax, consumer, countHolder);
        }

        // Case 3: Pivot > queryMax - check by min (ascending), go left
        for (int i = 0; i < intervalCount; i++) {
            final int idx = nodeIntervalIndices[minStart + i];
            final long min = intervalBounds[idx * 2];

            if (min > queryMax) {
                break; // Sorted ascending, no more
            }

            final long max = intervalBounds[idx * 2 + 1];
            countHolder[0]++;
            if (!consumer.accept(min, max, (T) intervalData[idx])) {
                return false;
            }
        }

        return searchNode(leftChild, queryMin, queryMax, consumer, countHolder);
    }

    /**
     * Point query - finds all intervals containing the point.
     */
    public int searchPoint(final long point, final ResultConsumer<T> consumer) {
        return search(point, point, consumer);
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    public int size() {
        return intervalCount;
    }

    public boolean isEmpty() {
        return intervalCount == 0;
    }

    public int nodeCount() {
        return nodeCount;
    }

    public long estimateMemoryBytes() {
        long bytes = 48; // Object header + fields

        bytes += 16 + (long) intervalBounds.length * 8;
        bytes += 16 + (long) intervalData.length * 8;
        bytes += 16 + (long) nodePivots.length * 8;
        bytes += 16 + (long) nodeMinStart.length * 4;
        bytes += 16 + (long) nodeMaxStart.length * 4;
        bytes += 16 + (long) nodeIntervalCount.length * 4;
        bytes += 16 + (long) nodeLeft.length * 4;
        bytes += 16 + (long) nodeRight.length * 4;
        bytes += 16 + (long) nodeIntervalIndices.length * 4;

        return bytes;
    }

    public long getMin(final int index) {
        return intervalBounds[index * 2];
    }

    public long getMax(final int index) {
        return intervalBounds[index * 2 + 1];
    }

    @SuppressWarnings("unchecked")
    public T getData(final int index) {
        return (T) intervalData[index];
    }
}
