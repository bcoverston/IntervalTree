/*
 * Copyright (c) 2011, 2025 Benjamin Jacob Coverston
 * All rights reserved.
 * BSD-2-Clause License
 */

package com.coversb.collections.primitive;

import java.util.Arrays;

/**
 * Builder for constructing {@link LongIntervalTree} instances.
 *
 * <p>This builder pre-allocates arrays to avoid repeated resizing during
 * construction. For best performance, provide an initial capacity estimate.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>(1000)
 *     .add(0, 100, "first")
 *     .add(50, 150, "second")
 *     .add(200, 300, "third")
 *     .build();
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>Not thread-safe. Use from a single thread during construction.
 *
 * @param <T> the type of data associated with intervals
 * @author Benjamin Coverston
 */
public final class LongIntervalTreeBuilder<T> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private long[] mins;
    private long[] maxs;
    private Object[] data;
    private int size;

    /**
     * Creates a builder with default initial capacity.
     */
    public LongIntervalTreeBuilder() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a builder with the specified initial capacity.
     *
     * @param initialCapacity expected number of intervals
     */
    public LongIntervalTreeBuilder(final int initialCapacity) {
        final int capacity = Math.max(initialCapacity, DEFAULT_CAPACITY);
        this.mins = new long[capacity];
        this.maxs = new long[capacity];
        this.data = new Object[capacity];
        this.size = 0;
    }

    /**
     * Adds an interval to the tree.
     *
     * @param min minimum bound (inclusive)
     * @param max maximum bound (inclusive)
     * @param value associated data
     * @return this builder for chaining
     * @throws IllegalArgumentException if min > max
     */
    public LongIntervalTreeBuilder<T> add(final long min, final long max, final T value) {
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") must be <= max (" + max + ")");
        }

        ensureCapacity(size + 1);

        mins[size] = min;
        maxs[size] = max;
        data[size] = value;
        size++;

        return this;
    }

    /**
     * Adds an interval without associated data.
     *
     * @param min minimum bound (inclusive)
     * @param max maximum bound (inclusive)
     * @return this builder for chaining
     */
    public LongIntervalTreeBuilder<T> add(final long min, final long max) {
        return add(min, max, null);
    }

    /**
     * Adds a point interval (min == max).
     *
     * @param point the point value
     * @param value associated data
     * @return this builder for chaining
     */
    public LongIntervalTreeBuilder<T> addPoint(final long point, final T value) {
        return add(point, point, value);
    }

    /**
     * Adds all intervals from another builder.
     *
     * @param other the other builder
     * @return this builder for chaining
     */
    public LongIntervalTreeBuilder<T> addAll(final LongIntervalTreeBuilder<T> other) {
        ensureCapacity(size + other.size);

        System.arraycopy(other.mins, 0, mins, size, other.size);
        System.arraycopy(other.maxs, 0, maxs, size, other.size);
        System.arraycopy(other.data, 0, data, size, other.size);
        size += other.size;

        return this;
    }

    /**
     * Adds intervals from parallel arrays.
     *
     * @param intervalMins array of minimum bounds
     * @param intervalMaxs array of maximum bounds
     * @param intervalData array of associated data
     * @return this builder for chaining
     */
    @SuppressWarnings("unchecked")
    public LongIntervalTreeBuilder<T> addAll(
            final long[] intervalMins,
            final long[] intervalMaxs,
            final T[] intervalData) {

        if (intervalMins.length != intervalMaxs.length
                || intervalMins.length != intervalData.length) {
            throw new IllegalArgumentException("Array lengths must match");
        }

        ensureCapacity(size + intervalMins.length);

        for (int i = 0; i < intervalMins.length; i++) {
            if (intervalMins[i] > intervalMaxs[i]) {
                throw new IllegalArgumentException(
                    "min[" + i + "]=" + intervalMins[i] + " > max[" + i + "]=" + intervalMaxs[i]);
            }
        }

        System.arraycopy(intervalMins, 0, mins, size, intervalMins.length);
        System.arraycopy(intervalMaxs, 0, maxs, size, intervalMins.length);
        System.arraycopy(intervalData, 0, data, size, intervalMins.length);
        size += intervalMins.length;

        return this;
    }

    /**
     * Returns the number of intervals added so far.
     *
     * @return current size
     */
    public int size() {
        return size;
    }

    /**
     * Clears the builder for reuse.
     *
     * @return this builder for chaining
     */
    public LongIntervalTreeBuilder<T> clear() {
        // Clear references to allow GC
        Arrays.fill(data, 0, size, null);
        size = 0;
        return this;
    }

    /**
     * Builds the interval tree.
     *
     * <p>This method creates trimmed copies of the internal arrays, so the
     * builder can be reused or discarded after calling build().
     *
     * @return the constructed interval tree
     */
    @SuppressWarnings("unchecked")
    public LongIntervalTree<T> build() {
        final long[] trimmedMins = Arrays.copyOf(mins, size);
        final long[] trimmedMaxs = Arrays.copyOf(maxs, size);
        final T[] trimmedData = (T[]) Arrays.copyOf(data, size);

        return new LongIntervalTree<>(trimmedMins, trimmedMaxs, trimmedData);
    }

    private void ensureCapacity(final int minCapacity) {
        if (minCapacity > mins.length) {
            grow(minCapacity);
        }
    }

    private void grow(final int minCapacity) {
        final int oldCapacity = mins.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1); // 1.5x growth

        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }

        if (newCapacity > MAX_ARRAY_SIZE) {
            newCapacity = hugeCapacity(minCapacity);
        }

        mins = Arrays.copyOf(mins, newCapacity);
        maxs = Arrays.copyOf(maxs, newCapacity);
        data = Arrays.copyOf(data, newCapacity);
    }

    private static int hugeCapacity(final int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError("Required capacity exceeds maximum");
        }
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }
}
