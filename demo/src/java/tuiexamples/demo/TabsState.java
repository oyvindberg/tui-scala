package tuiexamples.demo;

public final class TabsState {
  public final String[] titles;
  public int index;

  public TabsState(String[] titles) {
    this.titles = titles;
    this.index = 0;
  }

  public void next() {
    index = (index + 1) % titles.length;
  }

  public void previous() {
    if (index > 0) {
      index -= 1;
    } else {
      index = titles.length - 1;
    }
  }
}
