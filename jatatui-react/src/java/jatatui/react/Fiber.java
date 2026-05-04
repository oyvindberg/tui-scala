package jatatui.react;

import java.util.Objects;
import java.util.Optional;

/// Identity for a component instance across renders. Replaces typr-3's
// `componentStack.mkString("/")`
/// with an explicit `(parent, key)` pair — same shape React's reconciler uses.
///
/// `key` is either an `int` index (when a parent renders children in a list and the order is
// stable)
/// or a `String` (when the caller wants to keep state stable across reorders, like React's
/// `key="..."`).
public final class Fiber {
  private final Optional<Fiber> parent;
  private final Object key;
  private final int hash;

  public static Fiber root() {
    return new Fiber(Optional.empty(), "root");
  }

  public Fiber child(int index) {
    return new Fiber(Optional.of(this), index);
  }

  public Fiber child(String key) {
    return new Fiber(Optional.of(this), key);
  }

  private Fiber(Optional<Fiber> parent, Object key) {
    this.parent = parent;
    this.key = key;
    this.hash = Objects.hash(parent.map(p -> p.hash).orElse(0), key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Fiber other)) return false;
    return hash == other.hash && key.equals(other.key) && parent.equals(other.parent);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  /// Human-readable path for devtools / golden-file tests. Format: `"root/0/2/foo"` etc.
  public String toPath() {
    java.util.ArrayDeque<String> parts = new java.util.ArrayDeque<>();
    Fiber cur = this;
    while (true) {
      parts.addFirst(String.valueOf(cur.key));
      if (cur.parent.isEmpty()) break;
      cur = cur.parent.get();
    }
    return String.join("/", parts);
  }

  @Override
  public String toString() {
    return "Fiber(" + toPath() + ")";
  }
}
