package jatatui.examples.demo2.tabs;

import static jatatui.examples.demo2.Theme.THEME;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.examples.demo2.Colors;
import jatatui.widgets.Clear;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.Padding;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.paragraph.Wrap;
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

/// Mirrors `apps/demo2/src/tabs/recipe.rs`.
///
/// The "Recipe" tab. Shows the Ratatouille recipe steps on the left and an ingredients table on
/// the right, with a scrollbar tracking the selected ingredient.
public final class RecipeTab {

  /// One ingredient. Mirrors the upstream `struct Ingredient`.
  public record Ingredient(String quantity, String name) {

    /// Number of newline-separated lines in `name`. Used as the row height.
    public int height() {
      // Mirrors `self.name.lines().count()` — count newlines + 1, but a trailing newline doesn't
      // add a line (see Rust `str::lines()` semantics).
      if (name.isEmpty()) return 0;
      int count = 1;
      for (int i = 0; i < name.length() - 1; i++) {
        if (name.charAt(i) == '\n') count++;
      }
      // If the string ends with `\n`, Rust's `lines()` drops the trailing empty line. We do too
      // by not counting the final char as a line break.
      if (name.charAt(name.length() - 1) == '\n') {
        // count was already incremented for previous \n at len-1; nothing to do.
      }
      return count;
    }
  }

  /// Static recipe steps shown on the left.
  /// https://www.realsimple.com/food-recipes/browse-all-recipes/ratatouille
  public static final List<RecipeStep> RECIPE =
      List.of(
          new RecipeStep(
              "Step 1: ",
              "Over medium-low heat, add the oil to a large skillet with the onion, garlic,"
                  + " and bay leaf, stirring occasionally, until the onion has softened."),
          new RecipeStep(
              "Step 2: ",
              "Add the eggplant and cook, stirring occasionally, for 8 minutes or until the"
                  + " eggplant has softened. Stir in the zucchini, red bell pepper, tomatoes,"
                  + " and salt, and cook over medium heat, stirring occasionally, for 5 to 7"
                  + " minutes or until the vegetables are tender. Stir in the basil and few"
                  + " grinds of pepper to taste."));

  /// One recipe step (label + body).
  public record RecipeStep(String step, String text) {}

  /// Static ingredient list shown on the right.
  public static final List<Ingredient> INGREDIENTS =
      List.of(
          new Ingredient("4 tbsp", "olive oil"),
          new Ingredient("1", "onion thinly sliced"),
          new Ingredient("4", "cloves garlic\npeeled and sliced"),
          new Ingredient("1", "small bay leaf"),
          new Ingredient("1", "small eggplant cut\ninto 1/2 inch cubes"),
          new Ingredient("1", "small zucchini halved\nlengthwise and cut\ninto thin slices"),
          new Ingredient("1", "red bell pepper cut\ninto slivers"),
          new Ingredient("4", "plum tomatoes\ncoarsely chopped"),
          new Ingredient("1 tsp", "kosher salt"),
          new Ingredient("1/4 cup", "shredded fresh basil\nleaves"),
          new Ingredient("", "freshly ground black\npepper"));

  private int rowIndex;

  private RecipeTab() {
    this.rowIndex = 0;
  }

  /// Constructs a new [RecipeTab] with default state.
  public static RecipeTab defaultTab() {
    return new RecipeTab();
  }

  /// Select the previous ingredient (with wrap around).
  public void prev() {
    int n = INGREDIENTS.size();
    rowIndex = (rowIndex + n - 1) % n;
  }

  /// Select the next ingredient (with wrap around).
  public void next() {
    int n = INGREDIENTS.size();
    rowIndex = (rowIndex + 1) % n;
  }

  /// Render this tab into the given area of the buffer. Mirrors `impl Widget for RecipeTab`.
  public void render(Rect outerArea, Buffer buf) {
    new Colors.RgbSwatch().render(outerArea, buf);
    Rect area = outerArea.inner(new Margin(2, 1));
    Clear.instance().render(area, buf);
    Block.empty()
        .withTitle(
            Line.from(
                Span.styled(
                    "Ratatouille Recipe",
                    Style.empty()
                        .withAddModifier(Modifier.BOLD)
                        .withFg(jatatui.core.style.Color.WHITE))))
        .withTitleAlignment(HorizontalAlignment.Center)
        .withStyle(THEME.content)
        .withPadding(new Padding(1, 1, 2, 1))
        .render(area, buf);

    Rect scrollbarArea =
        new Rect(area.x(), area.y() + 2, area.width(), Math.max(0, area.height() - 3));
    renderScrollbar(rowIndex, scrollbarArea, buf);

    Rect inner = area.inner(new Margin(2, 1));
    Layout layout = Layout.horizontal(new Constraint.Length(44), new Constraint.Min(0));
    Rect[] split = inner.layout(layout, 2);
    Rect recipe = split[0];
    Rect ingredients = split[1];

    renderRecipe(recipe, buf);
    renderIngredients(rowIndex, ingredients, buf);
  }

  private static void renderRecipe(Rect area, Buffer buf) {
    List<Line> lines = new ArrayList<>(RECIPE.size());
    for (RecipeStep s : RECIPE) {
      lines.add(
          Line.fromSpans(
              List.of(
                  Span.styled(
                      s.step(),
                      Style.empty()
                          .withFg(jatatui.core.style.Color.WHITE)
                          .withAddModifier(Modifier.BOLD)),
                  Span.styled(s.text(), Style.empty().withFg(jatatui.core.style.Color.GRAY)))));
    }
    Paragraph.of(lines)
        .withWrap(new Wrap(true))
        .withBlock(Block.empty().withPadding(new Padding(0, 1, 0, 0)))
        .render(area, buf);
  }

  private static void renderIngredients(int selectedRow, Rect area, Buffer buf) {
    TableState state = new TableState().withSelected(selectedRow);
    List<Row> rows = new ArrayList<>(INGREDIENTS.size());
    for (Ingredient i : INGREDIENTS) {
      rows.add(
          new Row(
              List.of(TableCell.of(i.quantity()), TableCell.of(i.name())),
              i.height(),
              0,
              0,
              Style.empty()));
    }
    var theme = THEME.recipe;
    Table.of(rows, List.of(new Constraint.Length(7), new Constraint.Length(30)))
        .withBlock(Block.empty().withStyle(theme.ingredients()))
        .withHeader(
            new Row(
                List.of(TableCell.of("Qty"), TableCell.of("Ingredient")),
                1,
                0,
                0,
                theme.ingredientsHeader()))
        .withRowHighlightStyle(Style.empty().withFg(jatatui.core.style.Color.LIGHT_YELLOW))
        .render(area, buf, state);
  }

  private static void renderScrollbar(int position, Rect area, Buffer buf) {
    ScrollbarState state =
        ScrollbarState.empty()
            .withContentLength(INGREDIENTS.size())
            .withViewportContentLength(6)
            .withPosition(position);
    Scrollbar.of(ScrollbarOrientation.VerticalRight)
        .withBeginSymbol(Optional.empty())
        .withEndSymbol(Optional.empty())
        .withTrackSymbol(Optional.empty())
        .withThumbSymbol("▐")
        .render(area, buf, state);
  }
}
