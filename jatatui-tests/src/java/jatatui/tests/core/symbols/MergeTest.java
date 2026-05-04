package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.symbols.Merge.MergeStrategy;
import org.junit.jupiter.api.Test;

public class MergeTest {

  @Test
  public void replace_merge_strategy() {
    MergeStrategy strategy = MergeStrategy.Replace;
    String[] symbols = {
      "─", "━", "│", "┃", "┄", "┅", "┆", "┇", "┈", "┉", "┊", "┋", "┌", "┍", "┎", "┏", "┐",
      "┑", "┒", "┓", "└", "┕", "┖", "┗", "┘", "┙", "┚", "┛", "├", "┝", "┞", "┟", "┠", "┡",
      "┢", "┣", "┤", "┥", "┦", "┧", "┨", "┩", "┪", "┫", "┬", "┭", "┮", "┯", "┰", "┱", "┲",
      "┳", "┴", "┵", "┶", "┷", "┸", "┹", "┺", "┻", "┼", "┽", "┾", "┿", "╀", "╁", "╂", "╃",
      "╄", "╅", "╆", "╇", "╈", "╉", "╊", "╋", "╌", "╍", "╎", "╏", "═", "║", "╒", "╓", "╔",
      "╕", "╖", "╗", "╘", "╙", "╚", "╛", "╜", "╝", "╞", "╟", "╠", "╡", "╢", "╣", "╤", "╥",
      "╦", "╧", "╨", "╩", "╪", "╫", "╬", "╭", "╮", "╯", "╰", "╴", "╵", "╶", "╷", "╸", "╹",
      "╺", "╻", "╼", "╽", "╾", "╿", " ", "a", "b",
    };

    for (String a : symbols) {
      for (String b : symbols) {
        assertEquals(b, strategy.merge(a, b));
      }
    }
  }

  @Test
  public void exact_merge_strategy() {
    MergeStrategy s = MergeStrategy.Exact;
    assertEquals("─", s.merge("┆", "─"));
    assertEquals("┆", s.merge("┏", "┆"));
    assertEquals("┉", s.merge("╎", "┉"));
    assertEquals("┉", s.merge("╎", "┉"));
    assertEquals("┋", s.merge("┋", "┋"));
    assertEquals("┌", s.merge("╷", "╶"));
    assertEquals("┌", s.merge("╭", "┌"));
    assertEquals("┝", s.merge("│", "┕"));
    assertEquals("┝", s.merge("┏", "│"));
    assertEquals("┢", s.merge("│", "┏"));
    assertEquals("┢", s.merge("╽", "┕"));
    assertEquals("┼", s.merge("│", "─"));
    assertEquals("┼", s.merge("┘", "┌"));
    assertEquals("┿", s.merge("┵", "┝"));
    assertEquals("┿", s.merge("│", "━"));
    assertEquals("╞", s.merge("┵", "╞"));
    assertEquals(" ", s.merge(" ", "╠"));
    assertEquals(" ", s.merge("╠", " "));
    assertEquals("╧", s.merge("╎", "╧"));
    assertEquals("╪", s.merge("╛", "╒"));
    assertEquals("╪", s.merge("│", "═"));
    assertEquals("╪", s.merge("╤", "╧"));
    assertEquals("╪", s.merge("╡", "╞"));
    assertEquals("╭", s.merge("┌", "╭"));
    assertEquals("╭", s.merge("┘", "╭"));
    assertEquals("a", s.merge("┌", "a"));
    assertEquals("a", s.merge("a", "╭"));
    assertEquals("b", s.merge("a", "b"));
  }

  @Test
  public void fuzzy_merge_strategy() {
    MergeStrategy s = MergeStrategy.Fuzzy;
    assertEquals("─", s.merge("┄", "╴"));
    assertEquals("┆", s.merge("│", "┆"));
    assertEquals(" ", s.merge(" ", "┉"));
    assertEquals("┋", s.merge("┋", "┋"));
    assertEquals("┌", s.merge("╷", "╶"));
    assertEquals("┌", s.merge("╭", "┌"));
    assertEquals("┝", s.merge("│", "┕"));
    assertEquals("┝", s.merge("┏", "│"));
    assertEquals("┝", s.merge("┏", "┆"));
    assertEquals("┢", s.merge("│", "┏"));
    assertEquals("┢", s.merge("╽", "┕"));
    assertEquals("┼", s.merge("│", "─"));
    assertEquals("┼", s.merge("┆", "─"));
    assertEquals("┼", s.merge("┘", "┌"));
    assertEquals("┼", s.merge("┘", "╭"));
    assertEquals("┿", s.merge("╎", "┉"));
    assertEquals(" ", s.merge(" ", "╠"));
    assertEquals(" ", s.merge("╠", " "));
    assertEquals("╪", s.merge("┵", "╞"));
    assertEquals("╪", s.merge("╛", "╒"));
    assertEquals("╪", s.merge("│", "═"));
    assertEquals("╪", s.merge("╤", "╧"));
    assertEquals("╪", s.merge("╡", "╞"));
    assertEquals("╪", s.merge("╎", "╧"));
    assertEquals("╭", s.merge("┌", "╭"));
    assertEquals("a", s.merge("┌", "a"));
    assertEquals("a", s.merge("a", "╭"));
    assertEquals("b", s.merge("a", "b"));
  }

  @Test
  public void merge_examples() {
    assertEquals("━", MergeStrategy.Replace.merge("│", "━"));
    assertEquals("┼", MergeStrategy.Exact.merge("│", "─"));
    assertEquals("╬", MergeStrategy.Fuzzy.merge("┘", "╔"));
  }
}
