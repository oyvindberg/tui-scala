package jatatui.components.search;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/// IntelliJ-style fuzzy matching: each char of `query` must appear in `target` in order (a
/// subsequence), and matches on a word boundary score much higher than mid-word matches.
///
/// **Word boundary** covers:
///   - first char of the string
///   - first char after a separator (`_`, `.`, `-`, `/`, space)
///   - an uppercase letter following a lowercase one (camelCase)
///   - a digit following a letter (or vice versa)
///
/// Examples:
///   - `"cn"` matches `"customer_name"` and `"customerName"` (word-start hits each side)
///   - `"sc"` matches `"sales.customer"` (s and c word starts)
///   - `"cust"` matches `"customer"` (one word-start + three intra-word chars)
///
/// Case-insensitive. Returns [Optional#empty] when no subsequence is possible; otherwise a
/// score where larger = better.
///
/// Plugs directly into [jatatui.components.picker.PickerProps.Filter]: build an [Indexed] list
/// once when the corpus is known, then call [#rank] per keystroke.
public final class FuzzyMatch {
  private FuzzyMatch() {}

  private static final int WORD_START_BONUS = 10;
  private static final int IN_WORD_BONUS = 1;

  /// Indices of word starts in `s` per the rules above.
  public static Set<Integer> wordStarts(String s) {
    Set<Integer> out = new HashSet<>();
    int n = s.length();
    for (int i = 0; i < n; i++) {
      char c = s.charAt(i);
      if (!Character.isLetterOrDigit(c)) continue;
      boolean isStart;
      if (i == 0) {
        isStart = true;
      } else {
        char prev = s.charAt(i - 1);
        boolean prevSep = !Character.isLetterOrDigit(prev);
        boolean camel = Character.isUpperCase(c) && Character.isLowerCase(prev);
        boolean ld =
            (Character.isDigit(c) && Character.isLetter(prev))
                || (Character.isLetter(c) && Character.isDigit(prev));
        isStart = prevSep || camel || ld;
      }
      if (isStart) out.add(i);
    }
    return out;
  }

  /// Convenience entry point — computes [#wordStarts] on the fly. Use [#scoreWith] when the
  /// same target gets scored many times (search-as-you-type).
  public static Optional<Integer> score(String query, String target) {
    return scoreWith(query, target, target.toLowerCase(), wordStarts(target));
  }

  /// Hot-path scorer: caller supplies the precomputed lowercase target + word starts. Empty
  /// query returns a tiny negative score proportional to target length, so short items sort
  /// first when no query has been typed.
  public static Optional<Integer> scoreWith(
      String query, String target, String targetLower, Set<Integer> ws) {
    if (query.isEmpty()) return Optional.of(-target.length());
    String q = query.toLowerCase();
    int tn = targetLower.length();
    int qn = q.length();

    int qi = 0;
    int ti = 0;
    int s = 0;
    while (ti < tn && qi < qn) {
      if (targetLower.charAt(ti) == q.charAt(qi)) {
        s += ws.contains(ti) ? WORD_START_BONUS : IN_WORD_BONUS;
        qi++;
      }
      ti++;
    }
    if (qi == qn) return Optional.of(s - target.length() / 4);
    return Optional.empty();
  }

  /// Pre-indexed item: payload + precomputed label / labelLower / wordStarts. Build the list
  /// once when the corpus is known; reuse across every keystroke.
  public record Indexed<A>(A item, String label, String labelLower, Set<Integer> wordStarts) {}

  public static <A> List<Indexed<A>> index(List<A> items, Function<A, String> label) {
    return items.stream()
        .map(
            a -> {
              String l = label.apply(a);
              return new Indexed<>(a, l, l.toLowerCase(), wordStarts(l));
            })
        .toList();
  }

  /// Filter + rank pre-indexed items. Empty query returns every item in original order.
  public static <A> List<A> rank(String query, List<Indexed<A>> indexed) {
    if (query.isEmpty()) return indexed.stream().map(Indexed::item).toList();
    record Scored<A>(A item, int score) {}
    return indexed.stream()
        .flatMap(
            i ->
                scoreWith(query, i.label(), i.labelLower(), i.wordStarts()).stream()
                    .map(s -> new Scored<>(i.item(), s)))
        .sorted((a, b) -> Integer.compare(b.score(), a.score()))
        .map(Scored::item)
        .toList();
  }
}
