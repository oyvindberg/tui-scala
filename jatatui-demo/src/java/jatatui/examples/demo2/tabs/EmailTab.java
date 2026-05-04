package jatatui.examples.demo2.tabs;

import static jatatui.examples.demo2.Theme.THEME;

import jatatui.core.buffer.Buffer;
import jatatui.core.internal.Wcwidth;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.examples.demo2.Colors;
import jatatui.examples.demo2.Theme;
import jatatui.widgets.Borders;
import jatatui.widgets.Clear;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.BorderType;
import jatatui.widgets.block.Padding;
import jatatui.widgets.list.List;
import jatatui.widgets.list.ListItem;
import jatatui.widgets.list.ListState;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.scrollbar.Scrollbar;
import jatatui.widgets.scrollbar.ScrollbarState;
import jatatui.widgets.tabs.Tabs;
import java.util.ArrayList;
import java.util.Optional;

/// Mirrors `apps/demo2/src/tabs/email.rs`.
///
/// The "Email" tab. Shows an inbox list with a scrollbar and the body of the selected email.
public final class EmailTab {

  /// One email entry. Mirrors the upstream `struct Email`.
  public record Email(String from, String subject, String body) {}

  /// Hard-coded list of emails. Mirrors `EMAILS` upstream.
  public static final java.util.List<Email> EMAILS =
      java.util.List.of(
          new Email("Alice <alice@example.com>", "Hello", "Hi Bob,\nHow are you?\n\nAlice"),
          new Email(
              "Bob <bob@example.com>", "Re: Hello", "Hi Alice,\nI'm fine, thanks!\n\nBob"),
          new Email(
              "Charlie <charlie@example.com>",
              "Re: Hello",
              "Hi Alice,\nI'm fine, thanks!\n\nCharlie"),
          new Email(
              "Dave <dave@example.com>",
              "Re: Hello (STOP REPLYING TO ALL)",
              "Hi Everyone,\nPlease stop replying to all.\n\nDave"),
          new Email(
              "Eve <eve@example.com>",
              "Re: Hello (STOP REPLYING TO ALL)",
              "Hi Everyone,\nI'm reading all your emails.\n\nEve"));

  private int rowIndex;

  private EmailTab() {
    this.rowIndex = 0;
  }

  /// Constructs a new [EmailTab] with default state.
  public static EmailTab defaultTab() {
    return new EmailTab();
  }

  /// Select the previous email (with wrap around).
  public void prev() {
    int n = EMAILS.size();
    rowIndex = (rowIndex + n - 1) % n;
  }

  /// Select the next email (with wrap around).
  public void next() {
    int n = EMAILS.size();
    rowIndex = (rowIndex + 1) % n;
  }

  /// Render this tab into the given area of the buffer. Mirrors `impl Widget for EmailTab`.
  public void render(Rect outerArea, Buffer buf) {
    new Colors.RgbSwatch().render(outerArea, buf);
    Rect area = outerArea.inner(new Margin(2, 1));
    Clear.instance().render(area, buf);
    Layout layout = Layout.vertical(new Constraint.Length(5), new Constraint.Min(0));
    Rect[] split = area.layout(layout, 2);
    Rect inbox = split[0];
    Rect emailArea = split[1];
    renderInbox(rowIndex, inbox, buf);
    renderEmail(rowIndex, emailArea, buf);
  }

  private static void renderInbox(int selectedIndex, Rect area, Buffer buf) {
    Layout layout = Layout.vertical(new Constraint.Length(1), new Constraint.Min(0));
    Rect[] split = area.layout(layout, 2);
    Rect tabsArea = split[0];
    Rect inbox = split[1];
    Theme.Email theme = THEME.email;
    Tabs.ofStrings(" Inbox ", " Sent ", " Drafts ")
        .withStyle(theme.tabs())
        .withHighlightStyle(theme.tabsSelected())
        .withSelected(0)
        .withDivider("")
        .render(tabsArea, buf);

    String highlightSymbol = ">>";
    int fromWidth = 0;
    for (Email e : EMAILS) {
      int w = Wcwidth.width(e.from());
      if (w > fromWidth) fromWidth = w;
    }
    java.util.List<ListItem> items = new ArrayList<>(EMAILS.size());
    for (Email e : EMAILS) {
      String fromPadded = padRight(e.from(), fromWidth);
      Line line =
          Line.fromSpans(
              java.util.List.of(Span.from(fromPadded), Span.from(" "), Span.from(e.subject())));
      items.add(ListItem.of(line));
    }
    ListState state = ListState.empty().withSelected(Optional.of(selectedIndex));
    List.of(items)
        .withStyle(theme.inbox())
        .withHighlightStyle(theme.selectedItem())
        .withHighlightSymbol(highlightSymbol)
        .render(inbox, buf, state);
    ScrollbarState scrollbarState =
        ScrollbarState.empty().withContentLength(EMAILS.size()).withPosition(selectedIndex);
    Scrollbar.empty()
        .withBeginSymbol(Optional.empty())
        .withEndSymbol(Optional.empty())
        .withTrackSymbol(Optional.empty())
        .withThumbSymbol("▐")
        .render(inbox, buf, scrollbarState);
  }

  private static void renderEmail(int selectedIndex, Rect area, Buffer buf) {
    Theme.Email theme = THEME.email;
    Optional<Email> emailOpt =
        (selectedIndex >= 0 && selectedIndex < EMAILS.size())
            ? Optional.of(EMAILS.get(selectedIndex))
            : Optional.empty();
    Block block =
        Block.empty()
            .withStyle(theme.body())
            .withPadding(new Padding(2, 2, 0, 0))
            .withBorders(Borders.TOP)
            .withBorderType(BorderType.Thick);
    Rect inner = block.inner(area);
    block.render(area, buf);
    if (emailOpt.isPresent()) {
      Email email = emailOpt.get();
      Layout layout = Layout.vertical(new Constraint.Length(3), new Constraint.Min(0));
      Rect[] split = inner.layout(layout, 2);
      Rect headersArea = split[0];
      Rect bodyArea = split[1];
      java.util.List<Line> headers =
          java.util.List.of(
              Line.fromSpans(
                  java.util.List.of(
                      Span.styled("From: ", theme.header()),
                      Span.styled(email.from(), theme.headerValue()))),
              Line.fromSpans(
                  java.util.List.of(
                      Span.styled("Subject: ", theme.header()),
                      Span.styled(email.subject(), theme.headerValue()))),
              Line.from(repeat("-", inner.width()))
                  .withStyle(Style.empty().withAddModifier(Modifier.DIM)));
      Paragraph.of(headers).withStyle(theme.body()).render(headersArea, buf);
      java.util.List<Line> body = new ArrayList<>();
      for (String l : email.body().split("\n", -1)) {
        body.add(Line.from(l));
      }
      Paragraph.of(body).withStyle(theme.body()).render(bodyArea, buf);
    } else {
      Paragraph.of("No email selected").render(inner, buf);
    }
  }

  private static String padRight(String s, int width) {
    int w = Wcwidth.width(s);
    if (w >= width) return s;
    StringBuilder sb = new StringBuilder(s);
    for (int i = w; i < width; i++) {
      sb.append(' ');
    }
    return sb.toString();
  }

  private static String repeat(String s, int n) {
    if (n <= 0) return "";
    StringBuilder sb = new StringBuilder(s.length() * n);
    for (int i = 0; i < n; i++) {
      sb.append(s);
    }
    return sb.toString();
  }
}
