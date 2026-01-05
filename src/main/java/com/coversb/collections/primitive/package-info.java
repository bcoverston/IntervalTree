/*
 * Copyright (c) 2011, 2025 Benjamin Jacob Coverston
 * All rights reserved.
 * BSD-2-Clause License
 */

/**
 * Primitive-specialized interval tree implementations.
 *
 * <h2>Overview</h2>
 * <p>This package provides interval tree implementations using primitive long
 * values instead of boxed types. The design uses contiguous array storage,
 * avoids allocations in search paths, and eliminates boxing overhead.
 *
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link com.coversb.collections.primitive.LongIntervalTree} -
 *       Main interval tree for long-valued bounds (timestamps, IDs, etc.)</li>
 *   <li>{@link com.coversb.collections.primitive.LongIntervalTreeBuilder} -
 *       Builder for constructing trees</li>
 *   <li>{@link com.coversb.collections.primitive.LongInterval} -
 *       Flyweight interval value type</li>
 *   <li>{@link com.coversb.collections.primitive.SearchResultBuffer} -
 *       Pre-allocated buffer for collecting results</li>
 * </ul>
 *
 * <h2>Design Characteristics</h2>
 * <ol>
 *   <li><b>Zero Allocation in Search Path:</b> Search operations allocate nothing.
 *       Results are delivered via callbacks or written to pre-allocated buffers.</li>
 *   <li><b>Contiguous Storage:</b> Interval data stored in contiguous arrays.
 *       Each interval occupies 16 bytes (2 longs); four fit in a 64-byte cache line.</li>
 *   <li><b>No Boxing:</b> Primitive long values used throughout.
 *       No Comparable.compareTo() virtual dispatch.</li>
 *   <li><b>Immutability:</b> Trees are immutable after construction,
 *       enabling safe concurrent reads without synchronization.</li>
 * </ol>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Build tree (one-time cost)
 * LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>(10000)
 *     .add(startTime1, endTime1, "event1")
 *     .add(startTime2, endTime2, "event2")
 *     .build();
 *
 * // Zero-allocation search (hot path)
 * SearchResultBuffer<String> buffer = new SearchResultBuffer<>(100);
 * tree.search(queryStart, queryEnd, buffer::add);
 *
 * // Process results
 * for (int i = 0; i < buffer.size(); i++) {
 *     System.out.println(buffer.getData(i));
 * }
 * }</pre>
 *
 * <h2>Thread Safety &amp; Memory Visibility</h2>
 * <p>{@link com.coversb.collections.primitive.LongIntervalTree} is <b>effectively
 * immutable</b> after construction. All internal arrays are final fields,
 * providing safe publication guarantees per JLS §17.5 (final field semantics).
 *
 * <h3>Safe Publication</h3>
 * <p>Once the constructor completes and the tree reference is visible to other
 * threads (via any happens-before relationship), all tree state is guaranteed
 * visible. Common safe publication patterns:
 * <ul>
 *   <li>Storing the reference in a volatile field</li>
 *   <li>Storing the reference in a final field of a properly constructed object</li>
 *   <li>Storing via AtomicReference, ConcurrentHashMap, etc.</li>
 *   <li>Passing to another thread via a synchronized block or explicit lock</li>
 * </ul>
 *
 * <h3>Concurrent Search</h3>
 * <p>Multiple threads can safely search the same tree concurrently without
 * synchronization. Search operations are read-only and do not modify any
 * shared state. Each thread should use its own
 * {@link com.coversb.collections.primitive.SearchResultBuffer} if collecting
 * results into buffers.
 *
 * <h3>Thread-Local Resources</h3>
 * <p>In multi-threaded scenarios, use thread-local buffers to avoid contention:
 * <pre>{@code
 * // Allocate once per thread
 * private static final ThreadLocal<SearchResultBuffer<MyData>> BUFFER =
 *     ThreadLocal.withInitial(() -> new SearchResultBuffer<>(1000));
 *
 * // In hot path - no allocation
 * SearchResultBuffer<MyData> buffer = BUFFER.get();
 * buffer.clear();
 * tree.search(min, max, buffer::add);
 * }</pre>
 *
 * <h3>Memory Model</h3>
 * <ul>
 *   <li><b>No data races:</b> All shared state is final and read-only after construction</li>
 *   <li><b>Final field semantics:</b> Reads of final fields require no synchronization (JLS §17.5)</li>
 * </ul>
 *
 * <h2>Performance Characteristics</h2>
 * <table border="1">
 *   <tr><th>Operation</th><th>Time Complexity</th><th>Allocations</th></tr>
 *   <tr><td>Construction</td><td>O(n log n)</td><td>O(n)</td></tr>
 *   <tr><td>Search (callback)</td><td>O(log n + k)</td><td>0</td></tr>
 *   <tr><td>Search (buffer)</td><td>O(log n + k)</td><td>0</td></tr>
 *   <tr><td>Point query</td><td>O(log n + k)</td><td>0</td></tr>
 * </table>
 * <p>Where n = number of intervals, k = number of results.
 *
 * @author Benjamin Coverston
 */
package com.coversb.collections.primitive;
