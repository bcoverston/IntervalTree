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

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents an interval with a minimum and maximum bound, optionally carrying data.
 *
 * @param <C> the type of the interval bounds (must be Comparable)
 * @param <T> the type of data associated with this interval
 */
public class Interval<C extends Comparable<C>, T> {

    private final C min;
    private final C max;
    private final T data;

    public Interval(C min, C max) {
        this(min, max, null);
    }

    public Interval(C min, C max, T data) {
        Objects.requireNonNull(min, "min cannot be null");
        Objects.requireNonNull(max, "max cannot be null");
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min must be <= max");
        }
        this.min = min;
        this.max = max;
        this.data = data;
    }

    public C getMin() {
        return min;
    }

    public C getMax() {
        return max;
    }

    public T getData() {
        return data;
    }

    /**
     * Returns true if this interval completely encloses the given interval.
     */
    public boolean encloses(Interval<C, ?> interval) {
        return this.min.compareTo(interval.min) <= 0
            && this.max.compareTo(interval.max) >= 0;
    }

    /**
     * Returns true if this interval contains the given point.
     */
    public boolean contains(C point) {
        return this.min.compareTo(point) <= 0
            && this.max.compareTo(point) >= 0;
    }

    /**
     * Returns true if this interval intersects (overlaps) with the given interval.
     */
    public boolean intersects(Interval<C, ?> interval) {
        // Two intervals [a,b] and [c,d] intersect iff a <= d AND b >= c
        return this.min.compareTo(interval.max) <= 0
            && this.max.compareTo(interval.min) >= 0;
    }

    /**
     * Comparator that orders intervals by their minimum bound (ascending).
     */
    public static <C extends Comparable<C>, T> Comparator<Interval<C, T>> minOrdering() {
        return Comparator.comparing(Interval::getMin);
    }

    /**
     * Comparator that orders intervals by their maximum bound (ascending).
     */
    public static <C extends Comparable<C>, T> Comparator<Interval<C, T>> maxOrdering() {
        return Comparator.comparing(Interval::getMax);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interval<?, ?> interval)) return false;
        return min.equals(interval.min)
            && max.equals(interval.max)
            && Objects.equals(data, interval.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, data);
    }

    @Override
    public String toString() {
        return "[" + min + ", " + max + "]" + (data != null ? " -> " + data : "");
    }
}
