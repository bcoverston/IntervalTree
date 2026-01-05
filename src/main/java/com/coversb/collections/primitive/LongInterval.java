/*
 * Copyright (c) 2011, 2025 Benjamin Jacob Coverston
 * All rights reserved.
 * BSD-2-Clause License
 */

package com.coversb.collections.primitive;

/**
 * A value type representing an interval with long bounds.
 *
 * <p>This class is designed for zero-allocation access patterns. Rather than
 * creating interval objects, use the static factory methods or reuse instances.
 *
 * <p><b>Memory Layout:</b> 16 bytes (2 × 8-byte longs).
 *
 * <p><b>Thread Safety:</b> Immutable, safe for concurrent access.
 *
 * @author Benjamin Coverston
 */
public final class LongInterval {

    /** Minimum bound (inclusive). */
    public final long min;

    /** Maximum bound (inclusive). */
    public final long max;

    /**
     * Creates an interval with the specified bounds.
     *
     * @param min the minimum bound (inclusive)
     * @param max the maximum bound (inclusive)
     * @throws IllegalArgumentException if min > max
     */
    public LongInterval(final long min, final long max) {
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") must be <= max (" + max + ")");
        }
        this.min = min;
        this.max = max;
    }

    /**
     * Returns true if this interval contains the given point.
     *
     * @param point the point to test
     * @return true if min <= point <= max
     */
    public boolean contains(final long point) {
        return (min <= point) & (point <= max);
    }

    /**
     * Returns true if this interval intersects the given interval.
     *
     * <p>Two intervals [a,b] and [c,d] intersect iff a <= d AND b >= c.
     *
     * @param other the interval to test
     * @return true if intervals overlap
     */
    public boolean intersects(final LongInterval other) {
        return (this.min <= other.max) & (this.max >= other.min);
    }

    /**
     * Returns true if this interval intersects [otherMin, otherMax].
     *
     * <p>Primitive version avoiding object creation.
     *
     * @param otherMin the other interval's minimum
     * @param otherMax the other interval's maximum
     * @return true if intervals overlap
     */
    public boolean intersects(final long otherMin, final long otherMax) {
        return (this.min <= otherMax) & (this.max >= otherMin);
    }

    /**
     * Returns true if this interval completely encloses the other.
     *
     * @param other the interval to test
     * @return true if this.min <= other.min AND this.max >= other.max
     */
    public boolean encloses(final LongInterval other) {
        return (this.min <= other.min) & (this.max >= other.max);
    }

    /**
     * Returns the width of this interval.
     *
     * @return max - min
     */
    public long width() {
        return max - min;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LongInterval)) {
            return false;
        }
        final LongInterval other = (LongInterval) obj;
        return (this.min == other.min) & (this.max == other.max);
    }

    @Override
    public int hashCode() {
        long h = min * 31L + max;
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        return (int) h;
    }

    @Override
    public String toString() {
        return "[" + min + ", " + max + "]";
    }
}
