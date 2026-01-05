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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An interval tree for efficient overlap queries.
 * <p>
 * Given a set of intervals, this data structure supports finding all intervals
 * that overlap with a query interval in O(log n + m) time, where n is the number
 * of intervals and m is the number of results.
 *
 * @param <C> the type of the interval bounds (must be Comparable)
 * @param <T> the type of data associated with intervals
 */
public class IntervalTree<C extends Comparable<C>, T> {

    private final IntervalNode<C, T> root;

    /**
     * Constructs an interval tree from the given list of intervals.
     *
     * @param intervals the intervals to index
     */
    public IntervalTree(List<Interval<C, T>> intervals) {
        this.root = (intervals == null || intervals.isEmpty())
            ? null
            : new IntervalNode<>(intervals);
    }

    /**
     * Returns all intervals that overlap with the given search interval.
     *
     * @param searchInterval the interval to search for overlaps
     * @return list of data from overlapping intervals
     */
    public List<T> search(Interval<C, ?> searchInterval) {
        if (root == null || root.pivot == null) {
            return Collections.emptyList();
        }
        List<T> results = new ArrayList<>();
        searchInternal(root, searchInterval, results);
        return results;
    }

    /**
     * Returns all intervals that contain the given point.
     *
     * @param point the point to search for
     * @return list of data from intervals containing the point
     */
    public List<T> searchPoint(C point) {
        return search(new Interval<>(point, point));
    }

    /**
     * Returns all intervals in the tree.
     *
     * @return list of all intervals
     */
    public List<Interval<C, T>> getAllIntervals() {
        List<Interval<C, T>> results = new ArrayList<>();
        collectAllIntervals(root, results);
        return results;
    }

    private void collectAllIntervals(IntervalNode<C, T> node, List<Interval<C, T>> results) {
        if (node == null || node.pivot == null) {
            return;
        }
        results.addAll(node.sortedByMin);
        collectAllIntervals(node.left, results);
        collectAllIntervals(node.right, results);
    }

    private void searchInternal(IntervalNode<C, T> node, Interval<C, ?> searchInterval, List<T> results) {
        if (node == null || node.pivot == null) {
            return;
        }

        C pivot = node.pivot;
        C searchMin = searchInterval.getMin();
        C searchMax = searchInterval.getMax();

        // Case 1: Search interval contains the pivot point
        // All intervals at this node overlap, search both subtrees
        if (searchInterval.contains(pivot)) {
            for (Interval<C, T> interval : node.sortedByMin) {
                results.add(interval.getData());
            }
            searchInternal(node.left, searchInterval, results);
            searchInternal(node.right, searchInterval, results);
            return;
        }

        // Case 2: Pivot is to the left of the search interval
        // Check intervals by their max (sorted descending), search right subtree
        if (pivot.compareTo(searchMin) < 0) {
            for (Interval<C, T> interval : node.sortedByMax) {
                if (interval.getMax().compareTo(searchMin) >= 0) {
                    results.add(interval.getData());
                } else {
                    break;  // No more overlaps possible (sorted descending by max)
                }
            }
            searchInternal(node.right, searchInterval, results);
            return;
        }

        // Case 3: Pivot is to the right of the search interval
        // Check intervals by their min (sorted ascending), search left subtree
        if (pivot.compareTo(searchMax) > 0) {
            for (Interval<C, T> interval : node.sortedByMin) {
                if (interval.getMin().compareTo(searchMax) <= 0) {
                    results.add(interval.getData());
                } else {
                    break;  // No more overlaps possible (sorted ascending by min)
                }
            }
            searchInternal(node.left, searchInterval, results);
        }
    }
}
