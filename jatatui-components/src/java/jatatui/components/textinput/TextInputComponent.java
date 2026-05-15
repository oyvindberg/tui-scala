package jatatui.components.textinput;

import static jatatui.react.Components.ANY_CHAR;
import static jatatui.react.Components.component;

import jatatui.react.Element;
import jatatui.react.RenderContext;
import jatatui.react.State;
import jatatui.widgets.input.TextInput;
import jatatui.widgets.input.TextInput.TextResult;
import tui.crossterm.KeyCode;

/// React-style single-line text input.
///
/// Controlled — parent owns `value`, supplies `onChange`. Cursor position is internal
/// (`useState`); resets only when the component unmounts.
///
/// Key handling: when focused, registers handlers for printable chars (insert), Backspace,
/// Delete, Left/Right, Home/End. Enter and Esc are only registered if `onSubmit`/`onCancel` are
/// present — otherwise they bubble (so app-level shortcuts and the Esc-to-quit fallback still
/// work). Ctrl/Alt-modified chars also bubble: they're shortcuts, not input.
///
/// Each handled key calls `e.stopPropagation()` so editing doesn't bubble up to parents.
public final class TextInputComponent {
  private TextInputComponent() {}

  public static Element of(TextInputProps props) {
    return component(
        ctx -> {
          State<Integer> cursorState = ctx.useState(() -> props.value().length());

          // Clamp cursor to value length (e.g. parent set a shorter value).
          int cursor = Math.max(0, Math.min(props.value().length(), cursorState.get()));
          if (cursor != cursorState.get()) cursorState.set(cursor);

          boolean focused = ctx.useFocus(props.focusId(), props.autoFocus());

          if (focused) {
            registerKeyHandlers(ctx, props, cursorState);
          }

          // Click-to-focus: clicking anywhere in the input's area imperatively focuses it.
          // Only when focusId is set (otherwise there's no id to focus by) and the click would
          // actually change focus (no-op when already focused).
          if (props.focusOnClick() && !focused) {
            props.focusId().ifPresent(id -> ctx.onClick(e -> ctx.focus(id)));
          }

          int finalCursor = cursor;
          boolean finalFocused = focused;
          jatatui.core.widgets.Widget adapter =
              (area, buf) -> {
                jatatui.core.layout.Rect inputArea = area;
                if (!props.title().isEmpty()) {
                  jatatui.core.style.Style borderStyle =
                      finalFocused ? props.focusedBorderStyle() : props.borderStyle();
                  jatatui.widgets.block.Block block =
                      jatatui.widgets.block.Block.empty()
                          .withTitle(jatatui.core.text.Line.from(" " + props.title() + " "))
                          .withTitleStyle(borderStyle)
                          .withBorders(jatatui.widgets.Borders.ALL)
                          .withBorderStyle(borderStyle);
                  block.render(area, buf);
                  inputArea = block.inner(area);
                }
                TextInput widget =
                    TextInput.of(props.value())
                        .withCursorPos(finalCursor)
                        .withFocused(finalFocused)
                        .withPlaceholder(props.placeholder())
                        .withStyle(props.style())
                        .withFocusedStyle(props.focusedStyle())
                        .withPlaceholderStyle(props.placeholderStyle())
                        .withCursorStyle(props.cursorStyle());
                widget.render(inputArea, buf);
              };
          return jatatui.react.Components.widget(adapter);
        });
  }

  private static void registerKeyHandlers(
      RenderContext ctx, TextInputProps props, State<Integer> cursorState) {
    // Printable char insertion. Ctrl/Alt-modified chars bubble — they're shortcuts, not input.
    ctx.onKey(
        ANY_CHAR,
        e -> {
          int mods = e.modifiers().bits();
          if ((mods & tui.crossterm.KeyModifiers.CONTROL) != 0
              || (mods & tui.crossterm.KeyModifiers.ALT) != 0) {
            return;
          }
          KeyCode.Char ch = (KeyCode.Char) e.code();
          int cursor = cursorState.get();
          TextResult r = TextInput.insertAt(props.value(), cursor, ch.c());
          props.onChange().accept(r.value());
          cursorState.set(r.cursorPos());
          e.stopPropagation();
        });

    ctx.onKey(
        new KeyCode.Backspace(),
        e -> {
          int cursor = cursorState.get();
          TextResult r = TextInput.backspaceAt(props.value(), cursor);
          props.onChange().accept(r.value());
          cursorState.set(r.cursorPos());
          e.stopPropagation();
        });

    ctx.onKey(
        new KeyCode.Delete(),
        e -> {
          int cursor = cursorState.get();
          TextResult r = TextInput.deleteAt(props.value(), cursor);
          props.onChange().accept(r.value());
          cursorState.set(r.cursorPos());
          e.stopPropagation();
        });

    ctx.onKey(
        new KeyCode.Left(),
        e -> {
          cursorState.set(TextInput.moveLeft(props.value(), cursorState.get()).cursorPos());
          e.stopPropagation();
        });

    ctx.onKey(
        new KeyCode.Right(),
        e -> {
          cursorState.set(TextInput.moveRight(props.value(), cursorState.get()).cursorPos());
          e.stopPropagation();
        });

    ctx.onKey(
        new KeyCode.Home(),
        e -> {
          cursorState.set(0);
          e.stopPropagation();
        });

    ctx.onKey(
        new KeyCode.End(),
        e -> {
          cursorState.set(props.value().length());
          e.stopPropagation();
        });

    // Enter / Esc only register if there's a handler — otherwise they bubble (app-level Enter
    // shortcuts, Esc-to-quit fallback in ReactApp).
    props
        .onSubmit()
        .ifPresent(
            cb ->
                ctx.onKey(
                    new KeyCode.Enter(),
                    e -> {
                      cb.run();
                      e.stopPropagation();
                    }));

    props
        .onCancel()
        .ifPresent(
            cb ->
                ctx.onKey(
                    new KeyCode.Esc(),
                    e -> {
                      cb.run();
                      e.stopPropagation();
                    }));
    // Tab / Shift-Tab / BackTab are handled by ReactApp directly, never reach here.
  }
}
