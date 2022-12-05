package tui.crossterm;

import java.util.Optional;

public class CrosstermJni {
    static {
        try {
            NativeLoader.load("crossterm");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public native void flush();

    public native boolean poll(Duration timeout);

    public native Event read();

    public native Xy terminalSize();

    public native Xy cursorPosition();

    public native void disableRawMode();

    public native void enableRawMode();

    public native void enqueueCursorDisableBlinking();

    public native void enqueueCursorEnableBlinking();

    public native void enqueueCursorHide();

    public native void enqueueCursorMoveDown(int num_rows);

    public native void enqueueCursorMoveLeft(int num_cols);

    public native void enqueueCursorMoveRight(int num_cols);

    public native void enqueueCursorMoveTo(int x, int y);

    public native void enqueueCursorMoveToColumn(int col);

    public native void enqueueCursorMoveToNextLine(int num_lines);

    public native void enqueueCursorMoveToPreviousLine(int num_lines);

    public native void enqueueCursorMoveToRow(int row);

    public native void enqueueCursorMoveUp(int num_rows);

    public native void enqueueCursorRestorePosition();

    public native void enqueueCursorSavePosition();

    public native void enqueueCursorShow();

    public native void enqueueEventDisableBracketedPaste();

    public native void enqueueEventDisableFocusChange();

    public native void enqueueEventDisableMouseCapture();

    public native void enqueueEventEnableBracketedPaste();

    public native void enqueueEventEnableFocusChange();

    public native void enqueueEventEnableMouseCapture();

    public native void enqueueEventPopKeyboardEnhancementFlags();

    public native void enqueueStyleResetColor();

    public native void enqueueTerminalDisableLineWrap();

    public native void enqueueTerminalEnableLineWrap();

    public native void enqueueTerminalEnterAlternateScreen();

    public native void enqueueTerminalLeaveAlternateScreen();

    public native void enqueueTerminalScrollDown(int value);

    public native void enqueueTerminalScrollUp(int value);

    public native void enqueueTerminalSetSize(int x, int y);

    public native void enqueueTerminalClear(ClearType value);

    public native void enqueueEventPushKeyboardEnhancementFlags(KeyboardEnhancementFlags value);

    public native void enqueueCursorSetCursorShape(CursorShape value);

    public native void enqueueStyleSetAttribute(Attribute attribute);

    public native void enqueueStyleSetAttributes(Attributes attributes);

    public native void enqueueStyleSetBackgroundColor(Color color);

    public native void enqueueStyleSetColors(Optional<Color> foreground, Optional<Color> background);

    public native void enqueueStyleSetForegroundColor(Color color);

    public native void enqueueStyleSetStyle(
            /// The foreground color.
            Optional<Color> foreground_color,
            /// The background color.
            Optional<Color> background_color,
            /// The underline color.
            Optional<Color> underline_color,
            /// List of attributes.
            Attributes attributes
    );

    public native void enqueueStylePrint(String value);
}
