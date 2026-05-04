package jatatui.tests.widgets.clear;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.widgets.Clear;
import org.junit.jupiter.api.Test;

public class ClearTest {

  @Test
  public void render() {
    Buffer buffer =
        Buffer.withLines(
            "xxxxxxxxxxxxxxx",
            "xxxxxxxxxxxxxxx",
            "xxxxxxxxxxxxxxx",
            "xxxxxxxxxxxxxxx",
            "xxxxxxxxxxxxxxx",
            "xxxxxxxxxxxxxxx",
            "xxxxxxxxxxxxxxx");
    Clear clear = new Clear();
    clear.render(new Rect(1, 2, 3, 4), buffer);
    Buffer expected =
        Buffer.withLines(
            "xxxxxxxxxxxxxxx",
            "xxxxxxxxxxxxxxx",
            "x   xxxxxxxxxxx",
            "x   xxxxxxxxxxx",
            "x   xxxxxxxxxxx",
            "x   xxxxxxxxxxx",
            "xxxxxxxxxxxxxxx");
    assertBufferEq(buffer, expected);
  }
}
