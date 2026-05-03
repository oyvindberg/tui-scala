package tui;

public interface Backend {
  void draw(BufferUpdate[] content);

  void hideCursor();

  void showCursor();

  Position getCursor();

  void setCursor(int x, int y);

  void clear();

  Rect size();

  void flush();
}
