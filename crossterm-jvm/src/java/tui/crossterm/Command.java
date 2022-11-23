package tui.crossterm;

import java.util.List;
import java.util.Optional;

public sealed interface Command
        permits Command.MoveTo,
        Command.MoveToNextLine,
        Command.MoveToPreviousLine,
        Command.MoveToColumn,
        Command.MoveToRow,
        Command.MoveUp,
        Command.MoveRight,
        Command.MoveDown,
        Command.MoveLeft,
        Command.SavePosition,
        Command.RestorePosition,
        Command.Hide,
        Command.Show,
        Command.EnableBlinking,
        Command.DisableBlinking,
        Command.SetCursorShape,
        Command.EnableMouseCapture,
        Command.DisableMouseCapture,
        Command.PushKeyboardEnhancementFlags,
        Command.PopKeyboardEnhancementFlags,
        Command.EnableFocusChange,
        Command.DisableFocusChange,
        Command.EnableBracketedPaste,
        Command.DisableBracketedPaste,
        Command.SetForegroundColor,
        Command.SetBackgroundColor,
        Command.SetUnderlineColor,
        Command.SetColors,
        Command.SetAttribute,
        Command.SetAttributes,
        Command.SetStyle,
        Command.ResetColor,
        Command.DisableLineWrap,
        Command.EnableLineWrap,
        Command.EnterAlternateScreen,
        Command.LeaveAlternateScreen,
        Command.ScrollUp,
        Command.ScrollDown,
        Command.Clear,
        Command.SetSize {
    /// A command that moves the terminal cursor to the given position (column, row).
    /// * Top left cell is represented as `0,0`.
    record MoveTo(int x, int y) implements Command {
    }

    /// A command that moves the terminal cursor down the given number of lines,
    /// and moves it to the first column.
    /// * This command is 1 based, meaning `MoveToNextLine(1)` moves to the next line.
    /// * Most terminals default 0 argument to 1.
    record MoveToNextLine(int num_lines) implements Command {
    }

    /// A command that moves the terminal cursor up the given number of lines,
    /// and moves it to the first column.
    ///
    /// * This command is 1 based, meaning `MoveToPreviousLine(1)` moves to the previous line.
    /// * Most terminals default 0 argument to 1.
    record MoveToPreviousLine(int num_lines) implements Command {
    }

    /// A command that moves the terminal cursor to the given column on the current row.
    /// * This command is 0 based, meaning 0 is the leftmost column.
    record MoveToColumn(int column) implements Command {
    }

    /// A command that moves the terminal cursor to the given row on the current column.
    /// * This command is 0 based, meaning 0 is the topmost row.
    record MoveToRow(int row) implements Command {
    }

    /// A command that moves the terminal cursor a given number of rows up.
    /// * This command is 1 based, meaning `MoveUp(1)` moves the cursor up one cell.
    /// * Most terminals default 0 argument to 1.
    record MoveUp(int num_rows) implements Command {
    }

    /// A command that moves the terminal cursor a given number of columns to the right.
    /// * This command is 1 based, meaning `MoveRight(1)` moves the cursor right one cell.
    /// * Most terminals default 0 argument to 1.
    record MoveRight(int num_columns) implements Command {
    }

    /// A command that moves the terminal cursor a given number of rows down.
    /// * This command is 1 based, meaning `MoveDown(1)` moves the cursor down one cell.
    /// * Most terminals default 0 argument to 1.
    record MoveDown(int num_rows) implements Command {
    }

    /// A command that moves the terminal cursor a given number of columns to the left.
    /// * This command is 1 based, meaning `MoveLeft(1)` moves the cursor left one cell.
    /// * Most terminals default 0 argument to 1.
    record MoveLeft(int num_columns) implements Command {
    }

    /// A command that saves the current terminal cursor position.
    /// - The cursor position is stored globally.
    record SavePosition() implements Command {
    }

    /// A command that restores the saved terminal cursor position.
    /// - The cursor position is stored globally.
    record RestorePosition() implements Command {
    }

    /// A command that hides the terminal cursor.
    record Hide() implements Command {
    }

    /// A command that shows the terminal cursor.
    record Show() implements Command {
    }

    /// A command that enables blinking of the terminal cursor.
    /// - Windows versions lower than Windows 10 do not support this functionality.
    record EnableBlinking() implements Command {
    }

    /// A command that disables blinking of the terminal cursor.
    /// - Windows versions lower than Windows 10 do not support this functionality.
    record DisableBlinking() implements Command {
    }

    /// A command that sets the shape of the cursor
    record SetCursorShape(CursorShape cursor_shape) implements Command {
    }

    /// A command that enables mouse event capturing.
    ///
    /// Mouse events can be captured with [read](./fn.read.html)/[poll](./fn.poll.html).
    record EnableMouseCapture() implements Command {
    }

    /// A command that disables mouse event capturing.
    ///
    /// Mouse events can be captured with [read](./fn.read.html)/[poll](./fn.poll.html).
    record DisableMouseCapture() implements Command {
    }

    /// A command that enables the [kitty keyboard protocol](https://sw.kovidgoyal.net/kitty/keyboard-protocol/), which adds extra information to keyboard events and removes ambiguity for modifier keys.
    ///
    /// It should be paired with [`PopKeyboardEnhancementFlags`] at the end of execution.
    ///
    /// Note that, currently, only the following support this protocol:
    /// * [kitty terminal](https://sw.kovidgoyal.net/kitty/)
    /// * [foot terminal](https://codeberg.org/dnkl/foot/issues/319)
    /// * [WezTerm terminal](https://wezfurlong.org/wezterm/config/lua/config/enable_kitty_keyboard.html)
    /// * [notcurses library](https://github.com/dankamongmen/notcurses/issues/2131)
    /// * [neovim text editor](https://github.com/neovim/neovim/pull/18181)
    /// * [kakoune text editor](https://github.com/mawww/kakoune/issues/4103)
    /// * [dte text editor](https://gitlab.com/craigbarnes/dte/-/issues/138)
    record PushKeyboardEnhancementFlags(KeyboardEnhancementFlags flags) implements Command {
    }

    /// A command that disables extra kinds of keyboard events.
    ///
    /// Specifically, it pops one level of keyboard enhancement flags.
    ///
    /// See [`PushKeyboardEnhancementFlags`] and <https://sw.kovidgoyal.net/kitty/keyboard-protocol/> for more information.
    record PopKeyboardEnhancementFlags() implements Command {
    }

    /// A command that enables focus event emission.
    ///
    /// It should be paired with [`DisableFocusChange`] at the end of execution.
    ///
    /// Focus events can be captured with [read](./fn.read.html)/[poll](./fn.poll.html).
    record EnableFocusChange() implements Command {
    }

    /// A command that disables focus event emission.
    record DisableFocusChange() implements Command {
    }

    /// A command that enables [bracketed paste mode](https://en.wikipedia.org/wiki/Bracketed-paste).
    ///
    /// It should be paired with [`DisableBracketedPaste`] at the end of execution.
    ///
    /// This is not supported in older Windows terminals without
    /// [virtual terminal sequences](https://docs.microsoft.com/en-us/windows/console/console-virtual-terminal-sequences).
    record EnableBracketedPaste() implements Command {
    }

    /// A command that disables bracketed paste mode.
    record DisableBracketedPaste() implements Command {
    }

    /// A command that sets the the foreground color.
    record SetForegroundColor(Color color) implements Command {
    }

    /// A command that sets the the background color.
    record SetBackgroundColor(Color color) implements Command {
    }

    /// A command that sets the the underline color.
    record SetUnderlineColor(Color color) implements Command {
    }

    /// A command that optionally sets the foreground and/or background color.
    record SetColors(Optional<Color> foreground, Optional<Color> background) implements Command {
    }

    /// A command that sets an attribute.
    record SetAttribute(Attribute attribute) implements Command {
    }

    /// A command that sets several attributes.
    record SetAttributes(List<Attribute> attributes) implements Command {
    }

    /// A command that sets a style (colors and attributes).
    record SetStyle(
            /// The foreground color.
            Optional<Color> foreground_color,
            /// The background color.
            Optional<Color> background_color,
            /// The underline color.
            Optional<Color> underline_color,
            /// List of attributes.
            List<Attribute> attributes
    ) implements Command {
    }

    /// A command that resets the colors back to default.
    record ResetColor() implements Command {
    }

    /// Disables line wrapping.
    record DisableLineWrap() implements Command {
    }

    /// Enable line wrapping.
    record EnableLineWrap() implements Command {
    }

    /// A command that switches to alternate screen.
    /// * Use [LeaveAlternateScreen](./struct.LeaveAlternateScreen.html) command to leave the entered alternate screen.
    record EnterAlternateScreen() implements Command {
    }

    /// A command that switches back to the main screen.
    /// * Use [EnterAlternateScreen](./struct.EnterAlternateScreen.html) to enter the alternate screen.
    record LeaveAlternateScreen() implements Command {
    }

    /// A command that scrolls the terminal screen a given number of rows up.
    record ScrollUp(int num_rows) implements Command {
    }

    /// A command that scrolls the terminal screen a given number of rows down.
    record ScrollDown(int num_rows) implements Command {
    }

    /// A command that clears the terminal screen buffer.
    record Clear(ClearType clear_type) implements Command {
    }

    /// A command that sets the terminal buffer size `(columns, rows)`.
    record SetSize(int columns, int rows) implements Command {
    }
}
