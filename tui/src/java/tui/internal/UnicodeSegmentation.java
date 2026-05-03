package tui.internal;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import tui.Grapheme;

public final class UnicodeSegmentation {
  private UnicodeSegmentation() {}

  public static Grapheme[] graphemes(String str, boolean isExtended) {
    return graphemes(str, isExtended, Locale.getDefault());
  }

  public static Grapheme[] graphemes(String str, boolean isExtended, Locale locale) {
    List<Grapheme> out = new ArrayList<>();
    BreakIterator boundary = BreakIterator.getCharacterInstance(locale);
    boundary.setText(str);

    int start = boundary.first();
    int end = boundary.next();
    while (end != BreakIterator.DONE) {
      String chunk = str.substring(start, end);
      out.add(new Grapheme(chunk));
      start = end;
      end = boundary.next();
    }
    return out.toArray(new Grapheme[0]);
  }
}
