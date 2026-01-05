/*
 * Copyright (c) 2011, 2025 Benjamin Jacob Coverston
 * All rights reserved.
 * BSD-2-Clause License
 */

package com.coversb.collections.benchmark;

import com.coversb.collections.primitive.LongIntervalTree;
import com.coversb.collections.primitive.LongIntervalTreeBuilder;
import com.coversb.collections.primitive.SearchResultBuffer;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for LongIntervalTree.
 *
 * <p>Run with: {@code java -jar target/benchmarks.jar}
 *
 * <p>Or quick run: {@code mvn test-compile exec:java -Dexec.mainClass=org.openjdk.jmh.Main}
 *
 * <h2>Benchmark Methodology</h2>
 * <ul>
 *   <li>Warmup: 3 iterations of 1 second each</li>
 *   <li>Measurement: 5 iterations of 1 second each</li>
 *   <li>Forks: 2 (to average out JVM variance)</li>
 *   <li>Mode: Throughput (ops/sec) and Average Time</li>
 * </ul>
 *
 * @author Benjamin Coverston
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
public class LongIntervalTreeBenchmark {

    // ========================================================================
    // Benchmark Parameters
    // ========================================================================

    @Param({"1000", "10000", "100000", "1000000"})
    private int intervalCount;

    @Param({"10", "100", "1000"})
    private int queryWidth;

    // ========================================================================
    // State
    // ========================================================================

    private LongIntervalTree<Integer> primitiveTree;
    private com.coversb.collections.IntervalTree<Long, Integer> genericTree;

    private long[] queryMins;
    private long[] queryMaxs;
    private int queryIndex;

    private SearchResultBuffer<Integer> resultBuffer;

    // For generic tree comparison
    private List<com.coversb.collections.Interval<Long, Integer>> genericIntervals;

    @Setup(Level.Trial)
    public void setup() {
        final Random random = new Random(42); // Deterministic for reproducibility

        // Build primitive tree
        final LongIntervalTreeBuilder<Integer> builder =
            new LongIntervalTreeBuilder<>(intervalCount);

        final long range = intervalCount * 100L;

        for (int i = 0; i < intervalCount; i++) {
            final long start = random.nextLong(range);
            final long width = random.nextLong(1000) + 1;
            builder.add(start, start + width, i);
        }

        primitiveTree = builder.build();

        // Build generic tree for comparison
        genericIntervals = new ArrayList<>(intervalCount);
        final Random random2 = new Random(42); // Same seed

        for (int i = 0; i < intervalCount; i++) {
            final long start = random2.nextLong(range);
            final long width = random2.nextLong(1000) + 1;
            genericIntervals.add(new com.coversb.collections.Interval<>(start, start + width, i));
        }

        genericTree = new com.coversb.collections.IntervalTree<>(genericIntervals);

        // Pre-generate queries
        final int numQueries = 10000;
        queryMins = new long[numQueries];
        queryMaxs = new long[numQueries];

        final Random random3 = new Random(123);
        for (int i = 0; i < numQueries; i++) {
            final long start = random3.nextLong(range);
            queryMins[i] = start;
            queryMaxs[i] = start + queryWidth;
        }

        queryIndex = 0;

        // Pre-allocate result buffer
        resultBuffer = new SearchResultBuffer<>(1000);
    }

    // ========================================================================
    // Primitive Tree Benchmarks
    // ========================================================================

    /**
     * Benchmark: Zero-allocation search with callback.
     */
    @Benchmark
    public int primitiveSearchCallback(final Blackhole bh) {
        final int idx = queryIndex++ % queryMins.length;
        final int[] count = {0};

        primitiveTree.search(queryMins[idx], queryMaxs[idx], (min, max, data) -> {
            bh.consume(data);
            count[0]++;
            return true;
        });

        return count[0];
    }

    /**
     * Benchmark: Search with pre-allocated buffer.
     */
    @Benchmark
    public int primitiveSearchBuffer() {
        final int idx = queryIndex++ % queryMins.length;
        resultBuffer.clear();

        primitiveTree.search(queryMins[idx], queryMaxs[idx], resultBuffer::add);

        return resultBuffer.size();
    }

    /**
     * Benchmark: Point query (common use case for timestamp lookups).
     */
    @Benchmark
    public int primitivePointSearch(final Blackhole bh) {
        final int idx = queryIndex++ % queryMins.length;
        final int[] count = {0};

        primitiveTree.searchPoint(queryMins[idx], (min, max, data) -> {
            bh.consume(data);
            count[0]++;
            return true;
        });

        return count[0];
    }

    // ========================================================================
    // Generic Tree Benchmarks (for comparison)
    // ========================================================================

    /**
     * Benchmark: Generic tree search (allocates ArrayList per call).
     */
    @Benchmark
    public int genericSearch(final Blackhole bh) {
        final int idx = queryIndex++ % queryMins.length;

        // This allocates a new ArrayList and Interval every call
        final List<Integer> results = genericTree.search(
            new com.coversb.collections.Interval<>(queryMins[idx], queryMaxs[idx]));

        bh.consume(results);
        return results.size();
    }

    /**
     * Benchmark: Generic tree point search.
     */
    @Benchmark
    public int genericPointSearch(final Blackhole bh) {
        final int idx = queryIndex++ % queryMins.length;

        // This allocates a new ArrayList and Interval every call
        final List<Integer> results = genericTree.searchPoint(queryMins[idx]);

        bh.consume(results);
        return results.size();
    }

    // ========================================================================
    // Construction Benchmarks
    // ========================================================================

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Measurement(iterations = 10)
    @Fork(1)
    public LongIntervalTree<Integer> primitiveConstruction() {
        final LongIntervalTreeBuilder<Integer> builder =
            new LongIntervalTreeBuilder<>(intervalCount);

        final Random random = new Random(42);
        final long range = intervalCount * 100L;

        for (int i = 0; i < intervalCount; i++) {
            final long start = random.nextLong(range);
            final long width = random.nextLong(1000) + 1;
            builder.add(start, start + width, i);
        }

        return builder.build();
    }

    // ========================================================================
    // Main method for running benchmarks
    // ========================================================================

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(LongIntervalTreeBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(2)
            .measurementIterations(3)
            // Quick run with smaller params
            .param("intervalCount", "10000")
            .param("queryWidth", "100")
            .build();

        new Runner(opt).run();
    }
}
