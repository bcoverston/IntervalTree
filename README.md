# IntervalTree

An interval tree implementation for overlap queries.

## Why This Exists

This library started in 2011 as a straightforward interval tree for finding overlapping ranges. In 2025, it was modernized with two goals:

1. **Fix bugs and update to modern Java** — The original code had a few issues and used outdated patterns. Now targets Java 17+ with proper generics and no external dependencies.

2. **Add a primitive-specialized variant** — The generic `IntervalTree<C, T>` boxes values and allocates on every search. For workloads where search latency matters (timestamps, scheduling, HPC), the `LongIntervalTree<T>` stores intervals in contiguous arrays and allocates nothing during queries.

## Usage

### Generic (any Comparable type)

```java
List<Interval<Integer, String>> intervals = List.of(
    new Interval<>(0, 10, "first"),
    new Interval<>(5, 15, "second"),
    new Interval<>(20, 30, "third")
);

IntervalTree<Integer, String> tree = new IntervalTree<>(intervals);
List<String> results = tree.search(new Interval<>(8, 12));
// results: ["first", "second"]
```

### Primitive (long bounds, zero-allocation search)

```java
LongIntervalTree<String> tree = new LongIntervalTreeBuilder<String>()
    .add(0, 10, "first")
    .add(5, 15, "second")
    .add(20, 30, "third")
    .build();

// Zero-allocation search via callback
tree.search(8, 12, (min, max, data) -> {
    System.out.println(data);
    return true; // continue searching
});

// Or collect into a reusable buffer
SearchResultBuffer<String> buffer = new SearchResultBuffer<>(100);
tree.search(8, 12, buffer::add);
```

## Performance

Both implementations have O(log n + k) search time where n = intervals, k = results.

The primitive variant avoids allocation in the search path. Intervals are stored in contiguous arrays (16 bytes per interval), and results are delivered via callback or pre-allocated buffer.

## Thread Safety

Both implementations are immutable after construction. Safe for concurrent reads without synchronization.

## Building

```bash
mvn clean test        # Run tests
mvn package           # Build JAR
java -jar target/benchmarks.jar  # Run JMH benchmarks
```

## License

BSD-2-Clause
