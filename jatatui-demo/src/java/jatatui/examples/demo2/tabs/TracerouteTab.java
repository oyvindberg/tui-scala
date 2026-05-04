package jatatui.examples.demo2.tabs;

import static jatatui.examples.demo2.Theme.THEME;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.symbols.Marker;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.examples.demo2.Colors;
import jatatui.widgets.Clear;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.Padding;
import jatatui.widgets.canvas.Canvas;
import jatatui.widgets.canvas.Coord;
import jatatui.widgets.canvas.Map;
import jatatui.widgets.canvas.MapResolution;
import jatatui.widgets.canvas.Points;
import jatatui.widgets.scrollbar.Scrollbar;
import jatatui.widgets.scrollbar.ScrollbarOrientation;
import jatatui.widgets.scrollbar.ScrollbarState;
import jatatui.widgets.table.Row;
import jatatui.widgets.table.Table;
import jatatui.widgets.table.TableCell;
import jatatui.widgets.table.TableState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// Mirrors `apps/demo2/src/tabs/traceroute.rs`.
///
/// The "Traceroute" tab. Shows a hop table on the upper-left, a ping sparkline on the lower-left,
/// and a canvas-rendered map of Australia on the right (the only continent with hops).
public final class TracerouteTab {

  /// One traceroute hop. Mirrors the upstream `struct Hop`.
  public record Hop(String host, String address, Coord location) {

    public static Hop of(String name, String address, Coord location) {
      return new Hop(name, address, location);
    }
  }

  // ---- Locations (degrees) ----
  private static final Coord CANBERRA = new Coord(149.1, -35.3);
  private static final Coord SYDNEY = new Coord(151.1, -33.9);
  private static final Coord MELBOURNE = new Coord(144.9, -37.8);
  private static final Coord PERTH = new Coord(115.9, -31.9);
  private static final Coord DARWIN = new Coord(130.8, -12.4);
  private static final Coord BRISBANE = new Coord(153.0, -27.5);
  private static final Coord ADELAIDE = new Coord(138.6, -34.9);

  /// Hard-coded list of hops. Mirrors `HOPS` upstream. Locations are made up.
  public static final List<Hop> HOPS =
      List.of(
          Hop.of("home", "127.0.0.1", CANBERRA),
          Hop.of("bad.horse", "162.252.205.130", SYDNEY),
          Hop.of("bad.horse", "162.252.205.131", MELBOURNE),
          Hop.of("bad.horse", "162.252.205.132", BRISBANE),
          Hop.of("bad.horse", "162.252.205.133", SYDNEY),
          Hop.of("he.rides.across.the.nation", "162.252.205.134", PERTH),
          Hop.of("the.thoroughbred.of.sin", "162.252.205.135", DARWIN),
          Hop.of("he.got.the.application", "162.252.205.136", BRISBANE),
          Hop.of("that.you.just.sent.in", "162.252.205.137", ADELAIDE),
          Hop.of("it.needs.evaluation", "162.252.205.138", DARWIN),
          Hop.of("so.let.the.games.begin", "162.252.205.139", PERTH),
          Hop.of("a.heinous.crime", "162.252.205.140", BRISBANE),
          Hop.of("a.show.of.force", "162.252.205.141", CANBERRA),
          Hop.of("a.murder.would.be.nice.of.course", "162.252.205.142", PERTH),
          Hop.of("bad.horse", "162.252.205.143", MELBOURNE),
          Hop.of("bad.horse", "162.252.205.144", DARWIN),
          Hop.of("bad.horse", "162.252.205.145", MELBOURNE),
          Hop.of("he-s.bad", "162.252.205.146", PERTH),
          Hop.of("the.evil.league.of.evil", "162.252.205.147", BRISBANE),
          Hop.of("is.watching.so.beware", "162.252.205.148", DARWIN),
          Hop.of("the.grade.that.you.receive", "162.252.205.149", PERTH),
          Hop.of("will.be.your.last.we.swear", "162.252.205.150", ADELAIDE),
          Hop.of("so.make.the.bad.horse.gleeful", "162.252.205.151", SYDNEY),
          Hop.of("or.he-ll.make.you.his.mare", "162.252.205.152", MELBOURNE),
          Hop.of("o_o", "162.252.205.153", BRISBANE),
          Hop.of("you-re.saddled.up", "162.252.205.154", DARWIN),
          Hop.of("there-s.no.recourse", "162.252.205.155", PERTH),
          Hop.of("it-s.hi-ho.silver", "162.252.205.156", SYDNEY),
          Hop.of("signed.bad.horse", "162.252.205.157", CANBERRA));

  private int rowIndex;

  private TracerouteTab() {
    this.rowIndex = 0;
  }

  /// Constructs a new [TracerouteTab] with default state.
  public static TracerouteTab defaultTab() {
    return new TracerouteTab();
  }

  /// Select the previous hop (with wrap around).
  public void prevRow() {
    int n = HOPS.size();
    rowIndex = (rowIndex + n - 1) % n;
  }

  /// Select the next hop (with wrap around).
  public void nextRow() {
    int n = HOPS.size();
    rowIndex = (rowIndex + 1) % n;
  }

  /// Render this tab into the given area of the buffer. Mirrors `impl Widget for TracerouteTab`.
  public void render(Rect outerArea, Buffer buf) {
    new Colors.RgbSwatch().render(outerArea, buf);
    Rect area = outerArea.inner(new Margin(2, 1));
    Clear.instance().render(area, buf);
    Block.empty().withStyle(THEME.content).render(area, buf);
    Layout horizontal =
        Layout.horizontal(new Constraint.Ratio(1, 2), new Constraint.Ratio(1, 2));
    Layout vertical = Layout.vertical(new Constraint.Min(0), new Constraint.Length(3));
    Rect[] horiz = area.layout(horizontal, 2);
    Rect left = horiz[0];
    Rect map = horiz[1];
    Rect[] vert = left.layout(vertical, 2);
    Rect hops = vert[0];
    Rect pings = vert[1];

    renderHops(rowIndex, hops, buf);
    renderPing(rowIndex, pings, buf);
    renderMap(rowIndex, map, buf);
  }

  private static void renderHops(int selectedRow, Rect area, Buffer buf) {
    TableState state = new TableState().withSelected(selectedRow);
    List<Row> rows = new ArrayList<>(HOPS.size());
    for (Hop hop : HOPS) {
      rows.add(Row.ofStrings(hop.host(), hop.address()));
    }
    Block block =
        Block.empty()
            .withPadding(new Padding(1, 1, 1, 1))
            .withTitleAlignment(HorizontalAlignment.Center)
            .withTitle(
                Line.from(
                    Span.styled(
                        "Traceroute bad.horse",
                        Style.empty().withAddModifier(Modifier.BOLD).withFg(Color.WHITE))));
    Table.of(rows, List.of(new Constraint.Max(100), new Constraint.Length(15)))
        .withHeader(Row.ofStrings("Host", "Address").withStyle(THEME.traceroute.header()))
        .withRowHighlightStyle(THEME.traceroute.selected())
        .withBlock(block)
        .render(area, buf, state);
    ScrollbarState scrollbarState =
        ScrollbarState.empty().withContentLength(HOPS.size()).withPosition(selectedRow);
    Rect scrollArea =
        new Rect(
            area.x(),
            area.y() + 3,
            area.width() + 1,
            Math.max(0, area.height() - 4));
    Scrollbar.of(ScrollbarOrientation.VerticalLeft)
        .withBeginSymbol(Optional.empty())
        .withEndSymbol(Optional.empty())
        .withTrackSymbol(Optional.empty())
        .withThumbSymbol("▌")
        .render(scrollArea, buf, scrollbarState);
  }

  /// Renders the ping sparkline. Mirrors `pub fn render_ping`.
  public static void renderPing(int progress, Rect area, Buffer buf) {
    long[] base = {
      8, 8, 8, 8, 7, 7, 7, 6, 6, 5, 4, 3, 3, 2, 2, 1, 1, 1, 2, 2, 3, 4, 5, 6, 7, 7, 8, 8, 8, 7,
      7, 6, 5, 4, 3, 2, 1, 1, 1, 1, 1, 2, 4, 6, 7, 8, 8, 8, 8, 6, 4, 2, 1, 1, 1, 1, 2, 2, 2, 3,
      3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7
    };
    long[] data = rotateLeft(base, progress % base.length);
    jatatui.widgets.sparkline.Sparkline.empty()
        .withBlock(
            Block.empty()
                .withTitle("Ping")
                .withTitleAlignment(HorizontalAlignment.Center)
                .withBorderType(jatatui.widgets.block.BorderType.Thick))
        .withDataLongs(data)
        .withStyle(THEME.traceroute.ping())
        .render(area, buf);
  }

  private static void renderMap(int selectedRow, Rect area, Buffer buf) {
    var theme = THEME.traceroute.map();
    final Optional<HopPair> path =
        (selectedRow >= 0 && selectedRow + 1 < HOPS.size())
            ? Optional.of(new HopPair(HOPS.get(selectedRow), HOPS.get(selectedRow + 1)))
            : Optional.empty();
    Map mapShape = new Map(MapResolution.High, theme.color());
    Canvas.empty()
        .withBackgroundColor(theme.backgroundColor())
        .withBlock(Block.empty().withPadding(new Padding(1, 0, 1, 0)).withStyle(theme.style()))
        .withMarker(Marker.HalfBlock)
        // picked to show Australia for the demo as it's the most interesting part of the map
        .withXBounds(new double[] {112.0, 155.0})
        .withYBounds(new double[] {-46.0, -11.0})
        .withPaintFn(
            ctx -> {
              ctx.draw(mapShape);
              if (path.isPresent()) {
                HopPair p = path.get();
                ctx.draw(
                    jatatui.widgets.canvas.Line.of(
                        p.first().location().x(),
                        p.first().location().y(),
                        p.second().location().x(),
                        p.second().location().y(),
                        theme.path()));
                ctx.draw(new Points(new Coord[] {p.first().location()}, theme.source()));
                ctx.draw(new Points(new Coord[] {p.second().location()}, theme.destination()));
              }
            })
        .render(area, buf);
  }

  /// Two adjacent hops on the path. Replaces upstream's `(&Hop, &Hop)` tuple.
  private record HopPair(Hop first, Hop second) {}

  private static long[] rotateLeft(long[] arr, int n) {
    int len = arr.length;
    if (len == 0) return arr;
    int k = ((n % len) + len) % len;
    long[] out = new long[len];
    for (int i = 0; i < len; i++) {
      out[i] = arr[(i + k) % len];
    }
    return out;
  }
}
