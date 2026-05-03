package tui;

import java.util.ArrayList;
import java.util.Optional;
import tui.crossterm.Attribute;
import tui.crossterm.Command;
import tui.crossterm.CrosstermJni;

public final class CrosstermBackend implements Backend {
  private final CrosstermJni buffer;

  public CrosstermBackend(CrosstermJni buffer) {
    this.buffer = buffer;
  }

  @Override
  public void flush() {
    buffer.flush();
  }

  @Override
  public void draw(BufferUpdate[] content) {
    Color fg = Color.Reset;
    Color bg = Color.Reset;
    Modifier modifier = Modifier.EMPTY;
    Optional<Position> lastPos = Optional.empty();
    ArrayList<Command> commands = new ArrayList<>();

    for (BufferUpdate update : content) {
      int x = update.x();
      int y = update.y();
      Cell cell = update.cell();
      // Move the cursor if the previous location was not (x - 1, y)
      boolean shouldMove;
      if (lastPos.isPresent()) {
        Position lp = lastPos.get();
        shouldMove = !(x == lp.x() + 1 && y == lp.y());
      } else {
        shouldMove = true;
      }
      if (shouldMove) {
        commands.add(new Command.MoveTo(x, y));
      }
      lastPos = Optional.of(new Position(x, y));

      if (!cell.modifier.equals(modifier)) {
        ModifierDiff diff = new ModifierDiff(modifier, cell.modifier);
        diff.queue(commands);
        modifier = cell.modifier;
      }
      if (!cell.fg.equals(fg)) {
        tui.crossterm.Color color = from(cell.fg);
        commands.add(new Command.SetForegroundColor(color));
        fg = cell.fg;
      }
      if (!cell.bg.equals(bg)) {
        tui.crossterm.Color color = from(cell.bg);
        commands.add(new Command.SetBackgroundColor(color));
        bg = cell.bg;
      }
      commands.add(new Command.Print(cell.symbol.str));
    }
    commands.add(new Command.SetForegroundColor(new tui.crossterm.Color.Reset()));
    commands.add(new Command.SetBackgroundColor(new tui.crossterm.Color.Reset()));
    commands.add(new Command.SetAttribute(Attribute.Reset));

    buffer.enqueue(commands);
  }

  @Override
  public void hideCursor() {
    buffer.execute(new Command.Hide());
  }

  @Override
  public void showCursor() {
    buffer.execute(new Command.Show());
  }

  @Override
  public Position getCursor() {
    tui.crossterm.Xy xy = buffer.cursorPosition();
    return new Position(xy.x(), xy.y());
  }

  @Override
  public void setCursor(int x, int y) {
    buffer.execute(new Command.MoveTo(x, y));
  }

  @Override
  public void clear() {
    buffer.execute(new Command.Clear(tui.crossterm.ClearType.All));
  }

  @Override
  public Rect size() {
    tui.crossterm.Xy xy = buffer.terminalSize();
    return new Rect(0, 0, xy.x(), xy.y());
  }

  public static tui.crossterm.Color from(Color color) {
    return switch (color) {
      case Color.Reset r -> new tui.crossterm.Color.Reset();
      case Color.Black b -> new tui.crossterm.Color.Black();
      case Color.Red r -> new tui.crossterm.Color.DarkRed();
      case Color.Green g -> new tui.crossterm.Color.DarkGreen();
      case Color.Yellow y -> new tui.crossterm.Color.DarkYellow();
      case Color.Blue b -> new tui.crossterm.Color.DarkBlue();
      case Color.Magenta m -> new tui.crossterm.Color.DarkMagenta();
      case Color.Cyan c -> new tui.crossterm.Color.DarkCyan();
      case Color.Gray g -> new tui.crossterm.Color.Grey();
      case Color.DarkGray dg -> new tui.crossterm.Color.DarkGrey();
      case Color.LightRed lr -> new tui.crossterm.Color.Red();
      case Color.LightGreen lg -> new tui.crossterm.Color.Green();
      case Color.LightBlue lb -> new tui.crossterm.Color.Blue();
      case Color.LightYellow ly -> new tui.crossterm.Color.Yellow();
      case Color.LightMagenta lm -> new tui.crossterm.Color.Magenta();
      case Color.LightCyan lc -> new tui.crossterm.Color.Cyan();
      case Color.White w -> new tui.crossterm.Color.White();
      case Color.Indexed i -> new tui.crossterm.Color.AnsiValue(i.index() & 0xff);
      case Color.Rgb rgb -> new tui.crossterm.Color.Rgb(rgb.r(), rgb.g(), rgb.b());
    };
  }

  public record ModifierDiff(Modifier from, Modifier to) {
    public void queue(ArrayList<Command> commands) {
      Modifier removed = from.minus(to);
      if (removed.contains(Modifier.REVERSED)) {
        commands.add(new Command.SetAttribute(Attribute.NoReverse));
      }
      if (removed.contains(Modifier.BOLD)) {
        commands.add(new Command.SetAttribute(Attribute.NormalIntensity));
        if (to.contains(Modifier.DIM)) {
          commands.add(new Command.SetAttribute(Attribute.Dim));
        }
      }
      if (removed.contains(Modifier.ITALIC)) {
        commands.add(new Command.SetAttribute(Attribute.NoItalic));
      }
      if (removed.contains(Modifier.UNDERLINED)) {
        commands.add(new Command.SetAttribute(Attribute.NoUnderline));
      }
      if (removed.contains(Modifier.DIM)) {
        commands.add(new Command.SetAttribute(Attribute.NormalIntensity));
      }
      if (removed.contains(Modifier.CROSSED_OUT)) {
        commands.add(new Command.SetAttribute(Attribute.NotCrossedOut));
      }
      if (removed.contains(Modifier.SLOW_BLINK) || removed.contains(Modifier.RAPID_BLINK)) {
        commands.add(new Command.SetAttribute(Attribute.NoBlink));
      }

      Modifier added = to.minus(from);
      if (added.contains(Modifier.REVERSED)) {
        commands.add(new Command.SetAttribute(Attribute.Reverse));
      }
      if (added.contains(Modifier.BOLD)) {
        commands.add(new Command.SetAttribute(Attribute.Bold));
      }
      if (added.contains(Modifier.ITALIC)) {
        commands.add(new Command.SetAttribute(Attribute.Italic));
      }
      if (added.contains(Modifier.UNDERLINED)) {
        commands.add(new Command.SetAttribute(Attribute.Underlined));
      }
      if (added.contains(Modifier.DIM)) {
        commands.add(new Command.SetAttribute(Attribute.Dim));
      }
      if (added.contains(Modifier.CROSSED_OUT)) {
        commands.add(new Command.SetAttribute(Attribute.CrossedOut));
      }
      if (added.contains(Modifier.SLOW_BLINK)) {
        commands.add(new Command.SetAttribute(Attribute.SlowBlink));
      }
      if (added.contains(Modifier.RAPID_BLINK)) {
        commands.add(new Command.SetAttribute(Attribute.RapidBlink));
      }
    }
  }
}
