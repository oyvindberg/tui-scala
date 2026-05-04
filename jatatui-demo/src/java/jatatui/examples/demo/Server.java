package jatatui.examples.demo;

/// A fake server entry shown on the second tab of the demo.
///
/// Mirrors `examples/apps/demo/src/app.rs` — `Server&lt;'a&gt;`. `coords` is `(latitude, longitude)`
/// in degrees.
public record Server(String name, String location, double latitude, double longitude, String status) {

  /// Convenience factory matching upstream's struct literal field order
  /// (`coords: (latitude, longitude)`).
  public static Server of(String name, String location, double latitude, double longitude, String status) {
    return new Server(name, location, latitude, longitude, status);
  }
}
