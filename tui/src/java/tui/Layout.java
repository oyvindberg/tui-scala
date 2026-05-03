package tui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tui.cassowary.CassowaryOps;
import tui.cassowary.Constraint;
import tui.cassowary.Either;
import tui.cassowary.Expression;
import tui.cassowary.Solver;
import tui.cassowary.Strength;
import tui.cassowary.Variable;
import tui.cassowary.VariableChange;
import tui.cassowary.WeightedRelation;
import tui.internal.Ranges;

public final class Layout {
  public final Direction direction;
  public final Margin margin;
  public final tui.Constraint[] constraints;
  public final boolean expandToFill;

  public Layout(
      Direction direction,
      Margin margin,
      tui.Constraint[] constraints,
      boolean expandToFill) {
    this.direction = direction;
    this.margin = margin;
    this.constraints = constraints;
    this.expandToFill = expandToFill;
  }

  public Rect[] split(Rect area) {
    CacheKey key = new CacheKey(area, this);
    Rect[] cached = LAYOUT_CACHE.get(key);
    if (cached != null) return cached;
    Rect[] computed = Layout.split(area, this);
    LAYOUT_CACHE.put(key, computed);
    return computed;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Layout other)) return false;
    if (direction != other.direction) return false;
    if (!margin.equals(other.margin)) return false;
    if (expandToFill != other.expandToFill) return false;
    if (constraints.length != other.constraints.length) return false;
    for (int i = 0; i < constraints.length; i++) {
      if (!constraints[i].equals(other.constraints[i])) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = direction.hashCode();
    h = 31 * h + margin.hashCode();
    h = 31 * h + Boolean.hashCode(expandToFill);
    for (tui.Constraint c : constraints) {
      h = 31 * h + c.hashCode();
    }
    return h;
  }

  public record CacheKey(Rect area, Layout layout) {}

  public static final Map<CacheKey, Rect[]> LAYOUT_CACHE = new HashMap<>();

  /// A container used by the solver inside split
  public static final class Element {
    public final Variable x;
    public final Variable y;
    public final Variable width;
    public final Variable height;

    public Element() {
      this.x = Variable.create();
      this.y = Variable.create();
      this.width = Variable.create();
      this.height = Variable.create();
    }

    public Variable left() {
      return x;
    }

    public Variable top() {
      return y;
    }

    public Expression right() {
      return CassowaryOps.add(x, width);
    }

    public Expression bottom() {
      return CassowaryOps.add(y, height);
    }
  }

  public static Rect[] split(Rect area, Layout layout) {
    Solver solver = new Solver();
    Map<Variable, IntPair> vars = new HashMap<>();
    Element[] elements = new Element[layout.constraints.length];
    for (int i = 0; i < elements.length; i++) elements[i] = new Element();
    Rect[] results = new Rect[layout.constraints.length];
    for (int i = 0; i < results.length; i++) results[i] = Rect.DEFAULT;

    Rect destArea = area.inner(layout.margin);
    Ranges.range(
        0,
        elements.length,
        i -> {
          Element e = elements[i];
          vars.put(e.x, new IntPair(i, 0));
          vars.put(e.y, new IntPair(i, 1));
          vars.put(e.width, new IntPair(i, 2));
          vars.put(e.height, new IntPair(i, 3));
        });

    java.util.ArrayList<Constraint> ccs = new java.util.ArrayList<>();
    ccs.ensureCapacity(elements.length * 4 + layout.constraints.length * 6);

    for (Element elt : elements) {
      ccs.add(CassowaryOps.constraint(elt.width, WeightedRelation.GE(Strength.REQUIRED), 0.0));
      ccs.add(CassowaryOps.constraint(elt.height, WeightedRelation.GE(Strength.REQUIRED), 0.0));
      ccs.add(
          CassowaryOps.constraint(
              elt.left(), WeightedRelation.GE(Strength.REQUIRED), (double) destArea.left()));
      ccs.add(
          CassowaryOps.constraint(
              elt.top(), WeightedRelation.GE(Strength.REQUIRED), (double) destArea.top()));
      ccs.add(
          CassowaryOps.constraint(
              elt.right(), WeightedRelation.LE(Strength.REQUIRED), (double) destArea.right()));
      ccs.add(
          CassowaryOps.constraint(
              elt.bottom(), WeightedRelation.LE(Strength.REQUIRED), (double) destArea.bottom()));
    }

    if (elements.length > 0) {
      Element first = elements[0];
      Constraint c =
          switch (layout.direction) {
            case Horizontal ->
                CassowaryOps.constraint(
                    first.left(),
                    WeightedRelation.EQ(Strength.REQUIRED),
                    (double) destArea.left());
            case Vertical ->
                CassowaryOps.constraint(
                    first.top(),
                    WeightedRelation.EQ(Strength.REQUIRED),
                    (double) destArea.top());
          };
      ccs.add(c);
    }

    if (layout.expandToFill && elements.length > 0) {
      Element last = elements[elements.length - 1];
      Constraint c =
          switch (layout.direction) {
            case Horizontal ->
                CassowaryOps.constraint(
                    last.right(),
                    WeightedRelation.EQ(Strength.REQUIRED),
                    (double) destArea.right());
            case Vertical ->
                CassowaryOps.constraint(
                    last.bottom(),
                    WeightedRelation.EQ(Strength.REQUIRED),
                    (double) destArea.bottom());
          };
      ccs.add(c);
    }

    switch (layout.direction) {
      case Horizontal -> {
        for (int j = 0; j + 1 < elements.length; j++) {
          Element one = elements[j];
          Element two = elements[j + 1];
          ccs.add(
              CassowaryOps.constraint(
                  CassowaryOps.add(one.x, one.width),
                  WeightedRelation.EQ(Strength.REQUIRED),
                  two.x));
        }

        Ranges.range(
            0,
            layout.constraints.length,
            i -> {
              ccs.add(
                  CassowaryOps.constraint(
                      elements[i].y,
                      WeightedRelation.EQ(Strength.REQUIRED),
                      (double) destArea.y()));
              ccs.add(
                  CassowaryOps.constraint(
                      elements[i].height,
                      WeightedRelation.EQ(Strength.REQUIRED),
                      (double) destArea.height()));
              tui.Constraint size = layout.constraints[i];
              Constraint built =
                  switch (size) {
                    case tui.Constraint.Length l ->
                        CassowaryOps.constraint(
                            elements[i].width,
                            WeightedRelation.EQ(Strength.MEDIUM),
                            (double) l.l());
                    case tui.Constraint.Percentage p ->
                        CassowaryOps.constraint(
                            elements[i].width,
                            WeightedRelation.EQ(Strength.MEDIUM),
                            ((double) (p.p() * destArea.width())) / 100.0);
                    case tui.Constraint.Ratio r ->
                        CassowaryOps.constraint(
                            elements[i].width,
                            WeightedRelation.EQ(Strength.MEDIUM),
                            (double) destArea.width() * (double) r.num() / (double) r.den());
                    case tui.Constraint.Min m ->
                        CassowaryOps.constraint(
                            elements[i].width, WeightedRelation.GE(Strength.MEDIUM), (double) m.m());
                    case tui.Constraint.Max m ->
                        CassowaryOps.constraint(
                            elements[i].width, WeightedRelation.LE(Strength.MEDIUM), (double) m.m());
                  };
              ccs.add(built);
              switch (size) {
                case tui.Constraint.Min m ->
                    ccs.add(
                        CassowaryOps.constraint(
                            elements[i].width,
                            WeightedRelation.EQ(Strength.WEAK),
                            (double) m.m()));
                case tui.Constraint.Max m ->
                    ccs.add(
                        CassowaryOps.constraint(
                            elements[i].width,
                            WeightedRelation.EQ(Strength.WEAK),
                            (double) m.m()));
                default -> {}
              }
            });
      }
      case Vertical -> {
        for (int j = 0; j + 1 < elements.length; j++) {
          Element one = elements[j];
          Element two = elements[j + 1];
          ccs.add(
              CassowaryOps.constraint(
                  CassowaryOps.add(one.y, one.height),
                  WeightedRelation.EQ(Strength.REQUIRED),
                  two.y));
        }
        Ranges.range(
            0,
            layout.constraints.length,
            i -> {
              ccs.add(
                  CassowaryOps.constraint(
                      elements[i].x,
                      WeightedRelation.EQ(Strength.REQUIRED),
                      (double) destArea.x()));
              ccs.add(
                  CassowaryOps.constraint(
                      elements[i].width,
                      WeightedRelation.EQ(Strength.REQUIRED),
                      (double) destArea.width()));
              tui.Constraint size = layout.constraints[i];
              Constraint built =
                  switch (size) {
                    case tui.Constraint.Length l ->
                        CassowaryOps.constraint(
                            elements[i].height,
                            WeightedRelation.EQ(Strength.MEDIUM),
                            (double) l.l());
                    case tui.Constraint.Percentage p ->
                        CassowaryOps.constraint(
                            elements[i].height,
                            WeightedRelation.EQ(Strength.MEDIUM),
                            ((double) (p.p() * destArea.height())) / 100.0);
                    case tui.Constraint.Ratio r ->
                        CassowaryOps.constraint(
                            elements[i].height,
                            WeightedRelation.EQ(Strength.MEDIUM),
                            (double) destArea.height() * (double) r.num() / (double) r.den());
                    case tui.Constraint.Min m ->
                        CassowaryOps.constraint(
                            elements[i].height,
                            WeightedRelation.GE(Strength.MEDIUM),
                            (double) m.m());
                    case tui.Constraint.Max m ->
                        CassowaryOps.constraint(
                            elements[i].height,
                            WeightedRelation.LE(Strength.MEDIUM),
                            (double) m.m());
                  };
              ccs.add(built);
              switch (size) {
                case tui.Constraint.Min m ->
                    ccs.add(
                        CassowaryOps.constraint(
                            elements[i].height,
                            WeightedRelation.EQ(Strength.WEAK),
                            (double) m.m()));
                case tui.Constraint.Max m ->
                    ccs.add(
                        CassowaryOps.constraint(
                            elements[i].height,
                            WeightedRelation.EQ(Strength.WEAK),
                            (double) m.m()));
                default -> {}
              }
            });
      }
    }

    Either<tui.cassowary.AddConstraintError, Void> result = solver.addConstraints(ccs);
    if (result instanceof Either.Left<tui.cassowary.AddConstraintError, Void> l) {
      throw new RuntimeException("Error while adding constraints: " + l.value());
    }

    for (VariableChange vc : solver.fetchChanges()) {
      Variable v = vc.variable();
      double value0 = vc.value();
      IntPair pair = vars.get(v);
      int index = pair.first();
      int attr = pair.second();
      int value = Math.max(0, (int) value0);
      Rect rect = results[index];
      switch (attr) {
        case 0 -> results[index] = new Rect(value, rect.y(), rect.width(), rect.height());
        case 1 -> results[index] = new Rect(rect.x(), value, rect.width(), rect.height());
        case 2 -> results[index] = new Rect(rect.x(), rect.y(), value, rect.height());
        case 3 -> results[index] = new Rect(rect.x(), rect.y(), rect.width(), value);
        default -> {}
      }
    }

    if (layout.expandToFill) {
      // Fix imprecision by extending the last item a bit if necessary
      if (results.length > 0) {
        int lastIdx = results.length - 1;
        Rect last = results[lastIdx];
        Rect updated;
        switch (layout.direction) {
          case Horizontal ->
              updated = new Rect(last.x(), last.y(), last.width(), destArea.bottom() - last.y());
          case Vertical ->
              updated = new Rect(last.x(), last.y(), destArea.right() - last.x(), last.height());
          default -> updated = last;
        }
        results[lastIdx] = updated;
      }
    }
    return results;
  }

  private record IntPair(int first, int second) {}
}
