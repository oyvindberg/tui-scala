package jatatui.examples.demo2;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;

/// Theme palette for the demo2 example. Mirrors `apps/demo2/src/theme.rs`.
///
/// Hard-coded as a singleton constant — upstream uses `pub const THEME: Theme = ...` so the values
/// are determined at compile time. Since [Style] is a Java record (not constexpr), [#THEME] is a
/// `public static final` initialized once.
public final class Theme {

  /// Tied to the bordered title-bar background.
  public final Style root;

  /// Style for content panels.
  public final Style content;

  /// Style for the application title in the top-left corner.
  public final Style appTitle;

  /// Style for the tabs in the title bar.
  public final Style tabs;

  /// Style for the highlighted (selected) tab.
  public final Style tabsSelected;

  /// Style for borders rendered with the borders color.
  public final Style borders;

  /// Style for description text panels.
  public final Style description;

  /// Style for description titles.
  public final Style descriptionTitle;

  /// Theme bits for the bottom key-binding bar.
  public final KeyBinding keyBinding;

  /// Theme bits for the Ratatui logo (unused by the current widget set, kept for parity).
  public final Logo logo;

  /// Theme bits for the email tab.
  public final Email email;

  /// Theme bits for the traceroute tab.
  public final Traceroute traceroute;

  /// Theme bits for the recipe tab.
  public final Recipe recipe;

  private Theme(
      Style root,
      Style content,
      Style appTitle,
      Style tabs,
      Style tabsSelected,
      Style borders,
      Style description,
      Style descriptionTitle,
      KeyBinding keyBinding,
      Logo logo,
      Email email,
      Traceroute traceroute,
      Recipe recipe) {
    this.root = root;
    this.content = content;
    this.appTitle = appTitle;
    this.tabs = tabs;
    this.tabsSelected = tabsSelected;
    this.borders = borders;
    this.description = description;
    this.descriptionTitle = descriptionTitle;
    this.keyBinding = keyBinding;
    this.logo = logo;
    this.email = email;
    this.traceroute = traceroute;
    this.recipe = recipe;
  }

  /// Key-binding theme record.
  public record KeyBinding(Style key, Style description) {}

  /// Logo theme record.
  public record Logo(Color ratEye, Color ratEyeAlt) {}

  /// Email tab theme record.
  public record Email(
      Style tabs,
      Style tabsSelected,
      Style inbox,
      Style item,
      Style selectedItem,
      Style header,
      Style headerValue,
      Style body) {}

  /// Traceroute tab theme record.
  public record Traceroute(Style header, Style selected, Style ping, Map map) {}

  /// Map sub-theme record (inside Traceroute).
  public record Map(
      Style style,
      Color color,
      Color path,
      Color source,
      Color destination,
      Color backgroundColor) {}

  /// Recipe tab theme record.
  public record Recipe(Style ingredients, Style ingredientsHeader) {}

  // ---- Color constants (same as upstream) ----

  private static final Color DARK_BLUE = new Color.Rgb(16, 24, 48);
  private static final Color LIGHT_BLUE = new Color.Rgb(64, 96, 192);
  private static final Color LIGHT_YELLOW = new Color.Rgb(192, 192, 96);
  private static final Color LIGHT_GREEN = new Color.Rgb(64, 192, 96);
  private static final Color LIGHT_RED = new Color.Rgb(192, 96, 96);
  private static final Color RED = new Color.Rgb(215, 0, 0);
  private static final Color BLACK = new Color.Rgb(8, 8, 8); // not really black, often #080808
  private static final Color DARK_GRAY = new Color.Rgb(68, 68, 68);
  private static final Color MID_GRAY = new Color.Rgb(128, 128, 128);
  private static final Color LIGHT_GRAY = new Color.Rgb(188, 188, 188);
  private static final Color WHITE =
      new Color.Rgb(238, 238, 238); // not really white, often #eeeeee

  /// The single global theme used everywhere in demo2. Mirrors `pub const THEME` upstream.
  public static final Theme THEME =
      new Theme(
          /* root */ Style.empty().withBg(DARK_BLUE),
          /* content */ Style.empty().withBg(DARK_BLUE).withFg(LIGHT_GRAY),
          /* appTitle */ Style.empty()
              .withFg(WHITE)
              .withBg(DARK_BLUE)
              .withAddModifier(Modifier.BOLD),
          /* tabs */ Style.empty().withFg(MID_GRAY).withBg(DARK_BLUE),
          /* tabsSelected */ Style.empty()
              .withFg(WHITE)
              .withBg(DARK_BLUE)
              .withAddModifier(Modifier.BOLD)
              .withAddModifier(Modifier.REVERSED),
          /* borders */ Style.empty().withFg(LIGHT_GRAY),
          /* description */ Style.empty().withFg(LIGHT_GRAY).withBg(DARK_BLUE),
          /* descriptionTitle */ Style.empty().withFg(LIGHT_GRAY).withAddModifier(Modifier.BOLD),
          /* keyBinding */ new KeyBinding(
              Style.empty().withFg(BLACK).withBg(DARK_GRAY),
              Style.empty().withFg(DARK_GRAY).withBg(BLACK)),
          /* logo */ new Logo(BLACK, RED),
          /* email */ new Email(
              Style.empty().withFg(MID_GRAY).withBg(DARK_BLUE),
              Style.empty().withFg(WHITE).withBg(DARK_BLUE).withAddModifier(Modifier.BOLD),
              Style.empty().withBg(DARK_BLUE).withFg(LIGHT_GRAY),
              Style.empty().withFg(LIGHT_GRAY),
              Style.empty().withFg(LIGHT_YELLOW),
              Style.empty().withAddModifier(Modifier.BOLD),
              Style.empty().withFg(LIGHT_GRAY),
              Style.empty().withBg(DARK_BLUE).withFg(LIGHT_GRAY)),
          /* traceroute */ new Traceroute(
              Style.empty()
                  .withBg(DARK_BLUE)
                  .withAddModifier(Modifier.BOLD)
                  .withAddModifier(Modifier.UNDERLINED),
              Style.empty().withFg(LIGHT_YELLOW),
              Style.empty().withFg(WHITE),
              new Map(
                  Style.empty().withBg(DARK_BLUE),
                  LIGHT_GRAY,
                  LIGHT_BLUE,
                  LIGHT_GREEN,
                  LIGHT_RED,
                  DARK_BLUE)),
          /* recipe */ new Recipe(
              Style.empty().withBg(DARK_BLUE).withFg(LIGHT_GRAY),
              Style.empty().withAddModifier(Modifier.BOLD).withAddModifier(Modifier.UNDERLINED)));
}
