package jatatui.examples.demo;

import java.util.concurrent.ThreadLocalRandom;

/// A random unsigned-long signal in `[lower, upper)`.
///
/// Mirrors `examples/apps/demo/src/app.rs` — `RandomSignal` (impl Iterator). The Rust version uses
/// `rand::distr::Uniform` + `ThreadRng`. Here we use [ThreadLocalRandom#nextLong(long, long)].
public final class RandomSignal {

  private final long lower;
  private final long upper;

  /// Creates a new [RandomSignal] producing values in `[lower, upper)`.
  public RandomSignal(long lower, long upper) {
    if (lower >= upper) {
      throw new IllegalArgumentException("invalid range: " + lower + " >= " + upper);
    }
    this.lower = lower;
    this.upper = upper;
  }

  /// Returns the next random `long` in `[lower, upper)`.
  public long next() {
    return ThreadLocalRandom.current().nextLong(lower, upper);
  }
}
