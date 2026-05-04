package jatatui.widgets.canvas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// World-map outline data ported from `submodules/ratatui/ratatui-widgets/src/canvas/world.rs`.
///
/// The two large `[Point]` arrays from upstream are stored as classpath text resources rather
/// than baked into Java source — `world_low.txt` (1166 points) and `world_high.txt` (5125
/// points). Each line contains an `x y` pair (space-separated decimal floats). Resources are
/// parsed lazily on first access and cached.
public final class WorldMap {

  private WorldMap() {}

  private static final String RESOURCE_DIR = "jatatui/widgets/canvas/";

  private static volatile Optional<Coord[]> cachedLow = Optional.empty();
  private static volatile Optional<Coord[]> cachedHigh = Optional.empty();

  /// Returns the low-resolution world map data (1166 points).
  public static Coord[] low() {
    Optional<Coord[]> cached = cachedLow;
    if (cached.isPresent()) return cached.get();
    synchronized (WorldMap.class) {
      if (cachedLow.isPresent()) return cachedLow.get();
      Coord[] arr = loadResource(RESOURCE_DIR + "world_low.txt");
      cachedLow = Optional.of(arr);
      return arr;
    }
  }

  /// Returns the high-resolution world map data (5125 points).
  public static Coord[] high() {
    Optional<Coord[]> cached = cachedHigh;
    if (cached.isPresent()) return cached.get();
    synchronized (WorldMap.class) {
      if (cachedHigh.isPresent()) return cachedHigh.get();
      Coord[] arr = loadResource(RESOURCE_DIR + "world_high.txt");
      cachedHigh = Optional.of(arr);
      return arr;
    }
  }

  private static Coord[] loadResource(String path) {
    ClassLoader cl = WorldMap.class.getClassLoader();
    try (InputStream in = cl.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException("Missing classpath resource: " + path);
      }
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        List<Coord> coords = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty()) continue;
          // Lines look like: "-163.7128  -78.5956" — split on any whitespace.
          int spaceIdx = -1;
          for (int i = 0; i < line.length(); i++) {
            if (Character.isWhitespace(line.charAt(i))) {
              spaceIdx = i;
              break;
            }
          }
          if (spaceIdx < 0) {
            throw new IllegalStateException(
                "Invalid line in " + path + ": '" + line + "' — missing whitespace separator");
          }
          String xStr = line.substring(0, spaceIdx);
          String yStr = line.substring(spaceIdx + 1).trim();
          double x = Double.parseDouble(xStr);
          double y = Double.parseDouble(yStr);
          coords.add(new Coord(x, y));
        }
        return coords.toArray(new Coord[0]);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
