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
import java.util.TreeSet;

/**
 * A node in the interval tree, storing intervals that contain the pivot point.
 *
 * @param <C> the type of the interval bounds (must be Comparable)
 * @param <T> the type of data associated with intervals
 */
class IntervalNode<C extends Comparable<C>, T> {

    final C pivot;
    final List<Interval<C, T>> sortedByMin;  // intervals at this node, sorted by min ascending
    final List<Interval<C, T>> sortedByMax;  // intervals at this node, sorted by max descending
    final IntervalNode<C, T> left;
    final IntervalNode<C, T> right;

    IntervalNode(List<Interval<C, T>> intervals) {
        if (intervals == null || intervals.isEmpty()) {
            this.pivot = null;
            this.sortedByMin = Collections.emptyList();
            this.sortedByMax = Collections.emptyList();
            this.left = null;
            this.right = null;
            return;
        }

        this.pivot = findMedianEndpoint(intervals);

        // Partition intervals into: left of pivot, containing pivot, right of pivot
        List<Interval<C, T>> containing = new ArrayList<>();
        List<Interval<C, T>> leftIntervals = new ArrayList<>();
        List<Interval<C, T>> rightIntervals = new ArrayList<>();

        for (Interval<C, T> interval : intervals) {
            if (interval.getMax().compareTo(pivot) < 0) {
                leftIntervals.add(interval);
            } else if (interval.getMin().compareTo(pivot) > 0) {
                rightIntervals.add(interval);
            } else {
                containing.add(interval);
            }
        }

        // Sort containing intervals two ways for efficient search
        this.sortedByMin = containing.stream()
            .sorted(Interval.minOrdering())
            .toList();

        this.sortedByMax = containing.stream()
            .sorted(Interval.<C, T>maxOrdering().reversed())
            .toList();

        // Recursively build subtrees
        this.left = leftIntervals.isEmpty() ? null : new IntervalNode<>(leftIntervals);
        this.right = rightIntervals.isEmpty() ? null : new IntervalNode<>(rightIntervals);
    }

    /**
     * Finds the median endpoint from all interval endpoints.
     */
    private C findMedianEndpoint(List<Interval<C, T>> intervals) {
        TreeSet<C> endpoints = new TreeSet<>();
        for (Interval<C, T> interval : intervals) {
            endpoints.add(interval.getMin());
            endpoints.add(interval.getMax());
        }

        int medianIndex = endpoints.size() / 2;
        int i = 0;
        for (C endpoint : endpoints) {
            if (i == medianIndex) {
                return endpoint;
            }
            i++;
        }
        return endpoints.first();
    }
}
