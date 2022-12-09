package tui.crossterm;

import java.util.Arrays;
import java.util.List;

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

    public native void enqueue(List<Command> commands);

    final public void enqueue(Command ...commands) {
        enqueue(java.util.Arrays.asList(commands));
    }

    public native void execute(List<Command> commands);

    final public void execute(Command ...commands) {
        execute(Arrays.asList(commands));
    }
}
