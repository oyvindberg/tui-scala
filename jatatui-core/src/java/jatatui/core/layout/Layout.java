package jatatui.core.layout;

import static jatatui.core.layout.solver.KasuariOps.constraint;
import static jatatui.core.layout.solver.KasuariOps.mul;
import static jatatui.core.layout.solver.KasuariOps.sub;
import static jatatui.core.layout.solver.WeightedRelation.EQ;
import static jatatui.core.layout.solver.WeightedRelation.GE;
import static jatatui.core.layout.solver.WeightedRelation.LE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jatatui.core.layout.solver.AddConstraintError;
import jatatui.core.layout.solver.Either;
import jatatui.core.layout.solver.Expression;
import jatatui.core.layout.solver.KasuariOps;
import jatatui.core.layout.solver.Solver;
import jatatui.core.layout.solver.Strength;
import jatatui.core.layout.solver.Variable;
import jatatui.core.layout.solver.VariableChange;

/// The primary layout engine for dividing terminal space using constraints and direction.
///
/// A layout is a set of constraints that can be applied to a given area to split it into smaller
/// rectangular areas. This is the core building block for creating structured user interfaces in
/// terminal applications.
///
/// A layout is composed of:
/// - a direction (horizontal or vertical)
/// - a set of constraints (length, ratio, percentage, fill, min, max)
/// - a margin (horizontal and vertical), the space between the edge of the main area and the split
///   areas
/// - a flex option that controls space distribution
/// - a spacing option that controls gaps between segments
///
/// The algorithm used to compute the layout is based on the kasuari solver, a linear constraint
/// solver that computes positions and sizes to satisfy as many constraints as possible in order of
/// their priorities.
///
/// When the layout is computed, the result is cached in a thread-safe LRU cache, so that
/// subsequent calls with the same parameters are faster. The cache size can be configured using
/// [Layout#initCache(int)].
public final class Layout {
  /// Default size of the layout cache. See [Layout#initCache(int)].
  public static final int DEFAULT_CACHE_SIZE = 500;

  // Multiplier that decides floating point precision when rounding.
  private static final double FLOAT_PRECISION_MULTIPLIER = 100.0;

  private final Direction direction;
  private final List<Constraint> constraints;
  private final Margin margin;
  private final Flex flex;
  private final Spacing spacing;

  private Layout(
      Direction direction,
      List<Constraint> constraints,
      Margin margin,
      Flex flex,
      Spacing spacing) {
    this.direction = direction;
    this.constraints = List.copyOf(constraints);
    this.margin = margin;
    this.flex = flex;
    this.spacing = spacing;
  }

  // --- Construction --------------------------------------------------------

  /// Creates a new layout with default values: vertical direction, no constraints, no margin,
  /// `Flex.Start`, and no spacing.
  public static Layout empty() {
    return new Layout(Direction.Vertical, List.of(), new Margin(0, 0), Flex.Start, Spacing.DEFAULT);
  }

  /// Creates a new layout with the given direction and constraints.
  public static Layout of(Direction direction, List<Constraint> constraints) {
    return new Layout(
        direction, constraints, new Margin(0, 0), Flex.Start, Spacing.DEFAULT);
  }

  /// Creates a new layout with the given direction and constraints.
  public static Layout of(Direction direction, Constraint... constraints) {
    return of(direction, Arrays.asList(constraints));
  }

  /// Creates a new vertical layout with the given constraints.
  public static Layout vertical(List<Constraint> constraints) {
    return of(Direction.Vertical, constraints);
  }

  /// Creates a new vertical layout with the given constraints.
  public static Layout vertical(Constraint... constraints) {
    return of(Direction.Vertical, Arrays.asList(constraints));
  }

  /// Creates a new horizontal layout with the given constraints.
  public static Layout horizontal(List<Constraint> constraints) {
    return of(Direction.Horizontal, constraints);
  }

  /// Creates a new horizontal layout with the given constraints.
  public static Layout horizontal(Constraint... constraints) {
    return of(Direction.Horizontal, Arrays.asList(constraints));
  }

  // --- Accessors -----------------------------------------------------------

  public Direction direction() {
    return direction;
  }

  public List<Constraint> constraints() {
    return constraints;
  }

  public Margin margin() {
    return margin;
  }

  public Flex flex() {
    return flex;
  }

  public Spacing spacing() {
    return spacing;
  }

  // --- Builder methods -----------------------------------------------------

  /// Returns a copy of this layout with the given direction.
  public Layout withDirection(Direction direction) {
    return new Layout(direction, constraints, margin, flex, spacing);
  }

  /// Returns a copy of this layout with the given constraints.
  public Layout withConstraints(List<Constraint> constraints) {
    return new Layout(direction, constraints, margin, flex, spacing);
  }

  /// Returns a copy of this layout with the given constraints.
  public Layout withConstraints(Constraint... constraints) {
    return withConstraints(Arrays.asList(constraints));
  }

  /// Returns a copy of this layout with both horizontal and vertical margin set to `margin`.
  public Layout withMargin(int margin) {
    return new Layout(direction, constraints, new Margin(margin, margin), flex, spacing);
  }

  /// Returns a copy of this layout with the given horizontal margin (vertical preserved).
  public Layout withHorizontalMargin(int horizontal) {
    return new Layout(
        direction, constraints, new Margin(horizontal, margin.vertical()), flex, spacing);
  }

  /// Returns a copy of this layout with the given vertical margin (horizontal preserved).
  public Layout withVerticalMargin(int vertical) {
    return new Layout(
        direction, constraints, new Margin(margin.horizontal(), vertical), flex, spacing);
  }

  /// Returns a copy of this layout with the given flex.
  public Layout withFlex(Flex flex) {
    return new Layout(direction, constraints, margin, flex, spacing);
  }

  /// Returns a copy of this layout with the given spacing.
  public Layout withSpacing(Spacing spacing) {
    return new Layout(direction, constraints, margin, flex, spacing);
  }

  /// Returns a copy of this layout with the given spacing in cells. Negative values become
  /// `Spacing.Overlap`.
  public Layout withSpacing(int cells) {
    return withSpacing(Spacing.fromSigned(cells));
  }

  // --- equals / hashCode ---------------------------------------------------

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Layout other)) return false;
    return direction == other.direction
        && Objects.equals(constraints, other.constraints)
        && Objects.equals(margin, other.margin)
        && flex == other.flex
        && Objects.equals(spacing, other.spacing);
  }

  @Override
  public int hashCode() {
    return Objects.hash(direction, constraints, margin, flex, spacing);
  }

  @Override
  public String toString() {
    return "Layout{direction="
        + direction
        + ", constraints="
        + constraints
        + ", margin="
        + margin
        + ", flex="
        + flex
        + ", spacing="
        + spacing
        + '}';
  }

  // --- Cache ---------------------------------------------------------------

  private record CacheKey(Rect area, Layout layout) {}

  private static final Object CACHE_LOCK = new Object();
  private static int cacheCapacity = DEFAULT_CACHE_SIZE;
  private static LinkedHashMap<CacheKey, SplitResult> cache = newCache(DEFAULT_CACHE_SIZE);

  private static LinkedHashMap<CacheKey, SplitResult> newCache(int capacity) {
    return new LinkedHashMap<>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<CacheKey, SplitResult> eldest) {
        return size() > cacheCapacity;
      }
    };
  }

  /// Initialise an empty cache with the given size. Subsequent calls to [Layout#split(Rect)] and
  /// [Layout#splitWithSpacers(Rect)] use this cache.
  ///
  /// `cacheSize` must be positive.
  public static void initCache(int cacheSize) {
    if (cacheSize <= 0) {
      throw new IllegalArgumentException("cache size must be positive: " + cacheSize);
    }
    synchronized (CACHE_LOCK) {
      cacheCapacity = cacheSize;
      cache = newCache(cacheSize);
    }
  }

  /// Returns the configured cache capacity.
  public static int cacheCapacity() {
    synchronized (CACHE_LOCK) {
      return cacheCapacity;
    }
  }

  // --- Splitting -----------------------------------------------------------

  /// Result of [Layout#splitWithSpacers(Rect)] — the per-constraint segments and the spacers
  /// between them. There are always `constraints.size() + 1` spacers.
  public record SplitResult(Rect[] segments, Rect[] spacers) {
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SplitResult other)) return false;
      return Arrays.equals(segments, other.segments) && Arrays.equals(spacers, other.spacers);
    }

    @Override
    public int hashCode() {
      return 31 * Arrays.hashCode(segments) + Arrays.hashCode(spacers);
    }
  }

  /// Split `area` according to this layout, returning one rect per constraint.
  public Rect[] split(Rect area) {
    return splitWithSpacers(area).segments();
  }

  /// Split `area` according to this layout, returning a list (matches Rust `layout_vec`).
  public List<Rect> splitVec(Rect area) {
    return new ArrayList<>(Arrays.asList(split(area)));
  }

  /// Split `area` according to this layout, returning both segments and spacers.
  ///
  /// The number of spacers is `constraints.size() + 1`.
  public SplitResult splitWithSpacers(Rect area) {
    CacheKey key = new CacheKey(area, this);
    synchronized (CACHE_LOCK) {
      SplitResult cached = cache.get(key);
      if (cached != null) return cached;
    }
    SplitResult computed = trySplit(area).unwrap();
    synchronized (CACHE_LOCK) {
      cache.putIfAbsent(key, computed);
    }
    return computed;
  }

  /// Returns the segments split from `area`, requiring exactly `expected` segments. Throws an
  /// `IllegalArgumentException` if the number of constraints does not match.
  public Rect[] areas(Rect area, int expected) {
    Rect[] segs = split(area);
    if (segs.length != expected) {
      throw new IllegalArgumentException(
          "invalid number of rects: expected " + expected + ", found " + segs.length);
    }
    return segs;
  }

  /// Like [Layout#areas(Rect, int)] but returns an `Either.Left` instead of throwing if the
  /// segment count doesn't match.
  public Either<String, Rect[]> tryAreas(Rect area, int expected) {
    Rect[] segs = split(area);
    if (segs.length != expected) {
      return Either.left(
          "invalid number of rects: expected " + expected + ", found " + segs.length);
    }
    return Either.right(segs);
  }

  /// Returns the spacers between segments. There are always `constraints.size() + 1` spacers; this
  /// asserts that the count matches `expected`.
  public Rect[] spacers(Rect area, int expected) {
    Rect[] sp = splitWithSpacers(area).spacers();
    if (sp.length != expected) {
      throw new IllegalArgumentException(
          "invalid number of rects: expected " + expected + ", found " + sp.length);
    }
    return sp;
  }

  Either<AddConstraintError, SplitResult> trySplit(Rect area) {
    Solver solver = new Solver();

    Rect innerArea = area.inner(margin);
    double areaStart;
    double areaEnd;
    switch (direction) {
      case Horizontal:
        areaStart = innerArea.x() * FLOAT_PRECISION_MULTIPLIER;
        areaEnd = innerArea.right() * FLOAT_PRECISION_MULTIPLIER;
        break;
      case Vertical:
      default:
        areaStart = innerArea.y() * FLOAT_PRECISION_MULTIPLIER;
        areaEnd = innerArea.bottom() * FLOAT_PRECISION_MULTIPLIER;
        break;
    }

    int variableCount = constraints.size() * 2 + 2;
    Variable[] variables = new Variable[variableCount];
    for (int i = 0; i < variableCount; i++) {
      variables[i] = Variable.create();
    }

    // spacers: pairs (v0,v1), (v2,v3), ... — there will be constraints.size()+1 of them
    Element[] spacers = new Element[constraints.size() + 1];
    for (int i = 0; i < spacers.length; i++) {
      spacers[i] = new Element(variables[2 * i], variables[2 * i + 1]);
    }
    // segments: pairs (v1,v2), (v3,v4), ... — there will be constraints.size() of them
    Element[] segments = new Element[constraints.size()];
    for (int i = 0; i < segments.length; i++) {
      segments[i] = new Element(variables[2 * i + 1], variables[2 * i + 2]);
    }

    int spacingI16 =
        switch (spacing) {
          case Spacing.Space sp -> sp.cells();
          case Spacing.Overlap ov -> -ov.cells();
        };

    Element areaSize = new Element(variables[0], variables[variables.length - 1]);

    Either<AddConstraintError, Void> r;
    r = configureArea(solver, areaSize, areaStart, areaEnd);
    if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
    r = configureVariableInAreaConstraints(solver, variables, areaSize);
    if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
    r = configureVariableConstraints(solver, variables);
    if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
    r = configureFlexConstraints(solver, areaSize, spacers, flex, spacingI16);
    if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
    r = configureConstraints(solver, areaSize, segments, constraints, flex);
    if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
    r = configureFillConstraints(solver, segments, constraints, flex);
    if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());

    if (flex != Flex.Legacy) {
      for (int i = 0; i + 1 < segments.length; i++) {
        Either<AddConstraintError, Void> rr =
            solver.addConstraint(segments[i].hasSize(segments[i + 1], Strengths.ALL_SEGMENT_GROW));
        if (rr instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
      }
    }

    HashMap<Variable, Double> changes = new HashMap<>();
    for (VariableChange vc : solver.fetchChanges()) {
      changes.put(vc.variable(), vc.value());
    }

    Rect[] segmentRects = changesToRects(changes, segments, innerArea, direction);
    Rect[] spacerRects = changesToRects(changes, spacers, innerArea, direction);

    return Either.right(new SplitResult(segmentRects, spacerRects));
  }

  // --- Configuration helpers ----------------------------------------------

  private static Either<AddConstraintError, Void> configureArea(
      Solver solver, Element area, double areaStart, double areaEnd) {
    Either<AddConstraintError, Void> r =
        solver.addConstraint(constraint(area.start, EQ(Strength.REQUIRED), areaStart));
    if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
    r = solver.addConstraint(constraint(area.end, EQ(Strength.REQUIRED), areaEnd));
    if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
    return Either.unit();
  }

  private static Either<AddConstraintError, Void> configureVariableInAreaConstraints(
      Solver solver, Variable[] variables, Element area) {
    for (Variable v : variables) {
      Either<AddConstraintError, Void> r =
          solver.addConstraint(constraint(v, GE(Strength.REQUIRED), area.start));
      if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
      r = solver.addConstraint(constraint(v, LE(Strength.REQUIRED), area.end));
      if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
    }
    return Either.unit();
  }

  private static Either<AddConstraintError, Void> configureVariableConstraints(
      Solver solver, Variable[] variables) {
    // tuples of consecutive vars from variables.skip(1): (v1,v2), (v3,v4), ...
    for (int i = 1; i + 1 < variables.length; i += 2) {
      Either<AddConstraintError, Void> r =
          solver.addConstraint(constraint(variables[i], LE(Strength.REQUIRED), variables[i + 1]));
      if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
    }
    return Either.unit();
  }

  private static Either<AddConstraintError, Void> configureConstraints(
      Solver solver,
      Element area,
      Element[] segments,
      List<Constraint> constraints,
      Flex flex) {
    int n = Math.min(segments.length, constraints.size());
    for (int i = 0; i < n; i++) {
      Constraint constraint = constraints.get(i);
      Element segment = segments[i];
      Either<AddConstraintError, Void> r;
      switch (constraint) {
        case Constraint.Max max -> {
          r = solver.addConstraint(segment.hasMaxSize(max.v(), Strengths.MAX_SIZE_LE));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          r = solver.addConstraint(segment.hasIntSize(max.v(), Strengths.MAX_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        case Constraint.Min min -> {
          r = solver.addConstraint(segment.hasMinSize(min.v(), Strengths.MIN_SIZE_GE));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          if (flex == Flex.Legacy) {
            r = solver.addConstraint(segment.hasIntSize(min.v(), Strengths.MIN_SIZE_EQ));
            if (r instanceof Either.Left<AddConstraintError, Void> l)
              return Either.left(l.value());
          } else {
            r = solver.addConstraint(segment.hasSize(area, Strengths.FILL_GROW));
            if (r instanceof Either.Left<AddConstraintError, Void> l)
              return Either.left(l.value());
          }
        }
        case Constraint.Length length -> {
          r = solver.addConstraint(segment.hasIntSize(length.v(), Strengths.LENGTH_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        case Constraint.Percentage p -> {
          double size = area.size().constant; // start with constant; we'll multiply below
          // size = area.size() * p / 100
          Expression sizeExpr = mul(area.size(), p.v() / 100.0);
          r = solver.addConstraint(segment.hasSize(sizeExpr, Strengths.PERCENTAGE_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        case Constraint.Ratio ratio -> {
          int den = Math.max(ratio.denominator(), 1);
          Expression sizeExpr = mul(area.size(), (double) ratio.numerator() / (double) den);
          r = solver.addConstraint(segment.hasSize(sizeExpr, Strengths.RATIO_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        case Constraint.Fill f -> {
          r = solver.addConstraint(segment.hasSize(area, Strengths.FILL_GROW));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
      }
    }
    return Either.unit();
  }

  private static Either<AddConstraintError, Void> configureFlexConstraints(
      Solver solver, Element area, Element[] spacers, Flex flex, int spacing) {
    Element[] middle =
        spacers.length >= 2
            ? Arrays.copyOfRange(spacers, 1, spacers.length - 1)
            : new Element[0];
    double spacingF64 = spacing * FLOAT_PRECISION_MULTIPLIER;

    Either<AddConstraintError, Void> r;
    switch (flex) {
      case Legacy: {
        for (Element sp : middle) {
          r = solver.addConstraint(sp.hasSize(spacingF64, Strengths.SPACER_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        if (spacers.length >= 1) {
          Element first = spacers[0];
          Element last = spacers[spacers.length - 1];
          r = solver.addConstraint(first.isEmpty());
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          r = solver.addConstraint(last.isEmpty());
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        break;
      }
      case SpaceAround: {
        if (spacers.length <= 2) {
          // fallback to SpaceEvenly behavior
          for (int i = 0; i < spacers.length; i++) {
            for (int j = i + 1; j < spacers.length; j++) {
              r = solver.addConstraint(spacers[i].hasSize(spacers[j], Strengths.SPACER_SIZE_EQ));
              if (r instanceof Either.Left<AddConstraintError, Void> l)
                return Either.left(l.value());
            }
          }
          for (Element sp : spacers) {
            r = solver.addConstraint(sp.hasMinSize(spacing, Strengths.SPACER_SIZE_EQ));
            if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
            r = solver.addConstraint(sp.hasSize(area, Strengths.SPACE_GROW));
            if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          }
        } else {
          Element first = spacers[0];
          Element last = spacers[spacers.length - 1];
          // middle already excludes first and last
          for (int i = 0; i < middle.length; i++) {
            for (int j = i + 1; j < middle.length; j++) {
              r = solver.addConstraint(middle[i].hasSize(middle[j], Strengths.SPACER_SIZE_EQ));
              if (r instanceof Either.Left<AddConstraintError, Void> l)
                return Either.left(l.value());
            }
          }
          if (middle.length > 0) {
            Element firstMiddle = middle[0];
            r = solver.addConstraint(firstMiddle.hasDoubleSize(first, Strengths.SPACER_SIZE_EQ));
            if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
            r = solver.addConstraint(firstMiddle.hasDoubleSize(last, Strengths.SPACER_SIZE_EQ));
            if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          }
          for (Element sp : spacers) {
            r = solver.addConstraint(sp.hasMinSize(spacing, Strengths.SPACER_SIZE_EQ));
            if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
            r = solver.addConstraint(sp.hasSize(area, Strengths.SPACE_GROW));
            if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          }
        }
        break;
      }
      case SpaceEvenly: {
        for (int i = 0; i < spacers.length; i++) {
          for (int j = i + 1; j < spacers.length; j++) {
            r = solver.addConstraint(spacers[i].hasSize(spacers[j], Strengths.SPACER_SIZE_EQ));
            if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          }
        }
        for (Element sp : spacers) {
          r = solver.addConstraint(sp.hasMinSize(spacing, Strengths.SPACER_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          r = solver.addConstraint(sp.hasSize(area, Strengths.SPACE_GROW));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        break;
      }
      case SpaceBetween: {
        for (int i = 0; i < middle.length; i++) {
          for (int j = i + 1; j < middle.length; j++) {
            r =
                solver.addConstraint(
                    middle[i].hasSize(middle[j].size(), Strengths.SPACER_SIZE_EQ));
            if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          }
        }
        for (Element sp : middle) {
          r = solver.addConstraint(sp.hasMinSize(spacing, Strengths.SPACER_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          r = solver.addConstraint(sp.hasSize(area, Strengths.SPACE_GROW));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        if (spacers.length >= 1) {
          Element first = spacers[0];
          Element last = spacers[spacers.length - 1];
          r = solver.addConstraint(first.isEmpty());
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          r = solver.addConstraint(last.isEmpty());
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        break;
      }
      case Start: {
        for (Element sp : middle) {
          r = solver.addConstraint(sp.hasSize(spacingF64, Strengths.SPACER_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        if (spacers.length >= 1) {
          Element first = spacers[0];
          Element last = spacers[spacers.length - 1];
          r = solver.addConstraint(first.isEmpty());
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          r = solver.addConstraint(last.hasSize(area, Strengths.GROW));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        break;
      }
      case Center: {
        for (Element sp : middle) {
          r = solver.addConstraint(sp.hasSize(spacingF64, Strengths.SPACER_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        if (spacers.length >= 1) {
          Element first = spacers[0];
          Element last = spacers[spacers.length - 1];
          r = solver.addConstraint(first.hasSize(area, Strengths.GROW));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          r = solver.addConstraint(last.hasSize(area, Strengths.GROW));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          r = solver.addConstraint(first.hasSize(last, Strengths.SPACER_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        break;
      }
      case End: {
        for (Element sp : middle) {
          r = solver.addConstraint(sp.hasSize(spacingF64, Strengths.SPACER_SIZE_EQ));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        if (spacers.length >= 1) {
          Element first = spacers[0];
          Element last = spacers[spacers.length - 1];
          r = solver.addConstraint(last.isEmpty());
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
          r = solver.addConstraint(first.hasSize(area, Strengths.GROW));
          if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
        }
        break;
      }
    }
    return Either.unit();
  }

  private static Either<AddConstraintError, Void> configureFillConstraints(
      Solver solver, Element[] segments, List<Constraint> constraints, Flex flex) {
    // Build a list of (constraint, segment) pairs for Fill or (non-legacy + Min) constraints
    record CSPair(Constraint c, Element s) {}
    List<CSPair> picked = new ArrayList<>();
    int n = Math.min(segments.length, constraints.size());
    for (int i = 0; i < n; i++) {
      Constraint c = constraints.get(i);
      boolean isFill = c instanceof Constraint.Fill;
      boolean isMin = c instanceof Constraint.Min;
      if (isFill || (flex != Flex.Legacy && isMin)) {
        picked.add(new CSPair(c, segments[i]));
      }
    }
    // tuple_combinations: every unordered pair (i, j) with i < j
    for (int i = 0; i < picked.size(); i++) {
      for (int j = i + 1; j < picked.size(); j++) {
        CSPair left = picked.get(i);
        CSPair right = picked.get(j);
        double leftScalingFactor =
            switch (left.c()) {
              case Constraint.Fill f -> Math.max((double) f.v(), 1e-6);
              case Constraint.Min m -> 1.0;
              default -> throw new IllegalStateException("unreachable");
            };
        double rightScalingFactor =
            switch (right.c()) {
              case Constraint.Fill f -> Math.max((double) f.v(), 1e-6);
              case Constraint.Min m -> 1.0;
              default -> throw new IllegalStateException("unreachable");
            };
        Expression lhs = mul(left.s().size(), rightScalingFactor);
        Expression rhs = mul(right.s().size(), leftScalingFactor);
        Either<AddConstraintError, Void> r =
            solver.addConstraint(constraint(lhs, EQ(Strengths.GROW), rhs));
        if (r instanceof Either.Left<AddConstraintError, Void> l) return Either.left(l.value());
      }
    }
    return Either.unit();
  }

  private static Rect[] changesToRects(
      Map<Variable, Double> changes,
      Element[] elements,
      Rect area,
      Direction direction) {
    Rect[] out = new Rect[elements.length];
    for (int i = 0; i < elements.length; i++) {
      Element e = elements[i];
      double startD = changes.getOrDefault(e.start, 0.0);
      double endD = changes.getOrDefault(e.end, 0.0);
      int start = (int) Math.round(Math.round(startD) / FLOAT_PRECISION_MULTIPLIER);
      int end = (int) Math.round(Math.round(endD) / FLOAT_PRECISION_MULTIPLIER);
      int size = Math.max(0, end - start);
      out[i] =
          switch (direction) {
            case Horizontal -> new Rect(start, area.y(), size, area.height());
            case Vertical -> new Rect(area.x(), start, area.width(), size);
          };
    }
    return out;
  }

  // --- Internal Element helper --------------------------------------------

  /// A pair of Variables representing the start and end of a span — either a segment or a
  /// spacer in the layout solver.
  private static final class Element {
    final Variable start;
    final Variable end;

    Element(Variable start, Variable end) {
      this.start = start;
      this.end = end;
    }

    Expression size() {
      return sub(end, start);
    }

    jatatui.core.layout.solver.Constraint hasMaxSize(int size, Strength strength) {
      return constraint(size(), LE(strength), size * FLOAT_PRECISION_MULTIPLIER);
    }

    jatatui.core.layout.solver.Constraint hasMinSize(int size, Strength strength) {
      return constraint(size(), GE(strength), size * FLOAT_PRECISION_MULTIPLIER);
    }

    jatatui.core.layout.solver.Constraint hasIntSize(int size, Strength strength) {
      return constraint(size(), EQ(strength), size * FLOAT_PRECISION_MULTIPLIER);
    }

    jatatui.core.layout.solver.Constraint hasSize(double size, Strength strength) {
      return constraint(size(), EQ(strength), size);
    }

    jatatui.core.layout.solver.Constraint hasSize(Expression size, Strength strength) {
      return constraint(size(), EQ(strength), size);
    }

    jatatui.core.layout.solver.Constraint hasSize(Element other, Strength strength) {
      return constraint(size(), EQ(strength), other.size());
    }

    jatatui.core.layout.solver.Constraint hasDoubleSize(Element other, Strength strength) {
      return constraint(size(), EQ(strength), KasuariOps.mul(other.size(), 2.0));
    }

    jatatui.core.layout.solver.Constraint isEmpty() {
      return constraint(size(), EQ(Strength.REQUIRED.minus(Strength.WEAK)), 0.0);
    }
  }

  // --- Strength constants -------------------------------------------------

  /// Strength constants used by the layout solver to prioritise constraints.
  public static final class Strengths {
    private Strengths() {}

    /// The strength to apply to Spacers to ensure that their sizes are equal.
    public static final Strength SPACER_SIZE_EQ =
        new Strength(Strength.REQUIRED.value() / 10.0);

    /// The strength to apply to Min inequality constraints.
    public static final Strength MIN_SIZE_GE = Strength.STRONG.times(100.0);

    /// The strength to apply to Max inequality constraints.
    public static final Strength MAX_SIZE_LE = Strength.STRONG.times(100.0);

    /// The strength to apply to Length constraints.
    public static final Strength LENGTH_SIZE_EQ = Strength.STRONG.times(10.0);

    /// The strength to apply to Percentage constraints.
    public static final Strength PERCENTAGE_SIZE_EQ = Strength.STRONG;

    /// The strength to apply to Ratio constraints.
    public static final Strength RATIO_SIZE_EQ =
        new Strength(Strength.STRONG.value() / 10.0);

    /// The strength to apply to Min equality constraints.
    public static final Strength MIN_SIZE_EQ = Strength.MEDIUM.times(10.0);

    /// The strength to apply to Max equality constraints.
    public static final Strength MAX_SIZE_EQ = Strength.MEDIUM.times(10.0);

    /// The strength to apply to Fill growing constraints.
    public static final Strength FILL_GROW = Strength.MEDIUM;

    /// The strength to apply to growing constraints.
    public static final Strength GROW = new Strength(Strength.MEDIUM.value() / 10.0);

    /// The strength to apply to Spacer growing constraints.
    public static final Strength SPACE_GROW = Strength.WEAK.times(10.0);

    /// The strength to apply to growing the size of all segments equally.
    public static final Strength ALL_SEGMENT_GROW = Strength.WEAK;
  }
}
