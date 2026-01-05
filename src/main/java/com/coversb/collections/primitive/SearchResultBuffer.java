/*
 * Copyright (c) 2011, 2025 Benjamin Jacob Coverston
 * All rights reserved.
 * BSD-2-Clause License
 */

package com.coversb.collections.primitive;

import java.util.Arrays;

/**
 * Pre-allocated buffer for collecting search results without allocation.
 *
 * <p>Use this class when you need to collect results into arrays rather than
 * using callbacks. Allocate once and reuse across many searches.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Allocate once
 * SearchResultBuffer<String> buffer = new SearchResultBuffer<>(1000);
 *
 * // Reuse for many searches
 * for (Query query : queries) {
 *     buffer.clear();
 *     tree.search(query.min, query.max, buffer::add);
 *     processResults(buffer);
 * }
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>Not thread-safe. Each thread should have its own buffer.
 *
 * @param <T> the type of data in the intervals
 * @author Benjamin Coverston
 */
public final class SearchResultBuffer<T> {

    private long[] mins;
    private long[] maxs;
    private Object[] data;
    private int size;
    private final int maxCapacity;

    /**
     * Creates a buffer with the specified capacity.
     *
     * @param capacity maximum number of results
     */
    public SearchResultBuffer(final int capacity) {
        this.mins = new long[capacity];
        this.maxs = new long[capacity];
        this.data = new Object[capacity];
        this.size = 0;
        this.maxCapacity = capacity;
    }

    /**
     * Adds a result to the buffer.
     *
     * <p>Designed to be used as a method reference: {@code buffer::add}
     *
     * @param min interval minimum
     * @param max interval maximum
     * @param value interval data
     * @return true if added, false if buffer is full
     */
    public boolean add(final long min, final long max, final T value) {
        if (size >= maxCapacity) {
            return false;
        }

        mins[size] = min;
        maxs[size] = max;
        data[size] = value;
        size++;
        return true;
    }

    /**
     * Clears the buffer for reuse.
     *
     * <p><b>Note:</b> Does not null out data references.
     * Call {@link #clearFully()} if you need to release references for GC.
     */
    public void clear() {
        size = 0;
    }

    /**
     * Clears the buffer and nulls out data references.
     *
     * <p>Use this if you need to release references for garbage collection.
     */
    public void clearFully() {
        Arrays.fill(data, 0, size, null);
        size = 0;
    }

    /**
     * Returns the number of results in the buffer.
     *
     * @return result count
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if the buffer is empty.
     *
     * @return true if no results
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns true if the buffer is full.
     *
     * @return true if at capacity
     */
    public boolean isFull() {
        return size >= maxCapacity;
    }

    /**
     * Returns the minimum bound at the given index.
     *
     * @param index result index
     * @return minimum bound
     */
    public long getMin(final int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return mins[index];
    }

    /**
     * Returns the maximum bound at the given index.
     *
     * @param index result index
     * @return maximum bound
     */
    public long getMax(final int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return maxs[index];
    }

    /**
     * Returns the data at the given index.
     *
     * @param index result index
     * @return interval data
     */
    @SuppressWarnings("unchecked")
    public T getData(final int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return (T) data[index];
    }

    /**
     * Provides direct access to the mins array.
     *
     * <p><b>Warning:</b> Returns the internal array. Do not modify.
     * Only valid up to {@link #size()} elements.
     *
     * @return internal mins array
     */
    public long[] getMinsArray() {
        return mins;
    }

    /**
     * Provides direct access to the maxs array.
     *
     * <p><b>Warning:</b> Returns the internal array. Do not modify.
     * Only valid up to {@link #size()} elements.
     *
     * @return internal maxs array
     */
    public long[] getMaxsArray() {
        return maxs;
    }

    /**
     * Provides direct access to the data array.
     *
     * <p><b>Warning:</b> Returns the internal array. Do not modify.
     * Only valid up to {@link #size()} elements.
     *
     * @return internal data array
     */
    public Object[] getDataArray() {
        return data;
    }

    /**
     * Iterates over all results.
     *
     * @param consumer callback for each result
     */
    @SuppressWarnings("unchecked")
    public void forEach(final LongIntervalTree.ResultConsumer<T> consumer) {
        for (int i = 0; i < size; i++) {
            if (!consumer.accept(mins[i], maxs[i], (T) data[i])) {
                break;
            }
        }
    }
}
