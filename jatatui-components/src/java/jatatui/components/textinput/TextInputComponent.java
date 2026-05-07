package jatatui.components.textinput;

import static jatatui.react.Components.ANY_KEY;
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
/// Focus: opt-in via `focusId` / `autoFocus`. When focused, registers a single `ctx.onKey(ANY_KEY,
/// ...)` handler that dispatches based on the key code:
///   - printable char → insert at cursor
///   - Backspace → delete char before cursor
///   - Delete → delete char at cursor
///   - Left/Right → move cursor
///   - Home/End → jump to start/end
///   - Enter → onSubmit (if present), no insert
///   - Esc → onCancel (if present)
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

          int finalCursor = cursor;
          jatatui.core.widgets.Widget adapter =
              (area, buf) -> {
                TextInput widget =
                    TextInput.of(props.value())
                        .withCursorPos(finalCursor)
                        .withFocused(focused)
                        .withPlaceholder(props.placeholder())
                        .withStyle(props.style())
                        .withFocusedStyle(props.focusedStyle())
                        .withPlaceholderStyle(props.placeholderStyle())
                        .withCursorStyle(props.cursorStyle());
                widget.render(area, buf);
              };
          return jatatui.react.Components.widget(adapter);
        });
  }

  private static void registerKeyHandlers(
      RenderContext ctx, TextInputProps props, State<Integer> cursorState) {
    ctx.onKey(
        ANY_KEY,
        e -> {
          KeyCode code = e.code();
          int cursor = cursorState.get();
          String value = props.value();

          if (code instanceof KeyCode.Char ch) {
            TextResult r = TextInput.insertAt(value, cursor, ch.c());
            props.onChange().accept(r.value());
            cursorState.set(r.cursorPos());
            e.stopPropagation();
          } else if (code instanceof KeyCode.Backspace) {
            TextResult r = TextInput.backspaceAt(value, cursor);
            props.onChange().accept(r.value());
            cursorState.set(r.cursorPos());
            e.stopPropagation();
          } else if (code instanceof KeyCode.Delete) {
            TextResult r = TextInput.deleteAt(value, cursor);
            props.onChange().accept(r.value());
            cursorState.set(r.cursorPos());
            e.stopPropagation();
          } else if (code instanceof KeyCode.Left) {
            cursorState.set(TextInput.moveLeft(value, cursor).cursorPos());
            e.stopPropagation();
          } else if (code instanceof KeyCode.Right) {
            cursorState.set(TextInput.moveRight(value, cursor).cursorPos());
            e.stopPropagation();
          } else if (code instanceof KeyCode.Home) {
            cursorState.set(0);
            e.stopPropagation();
          } else if (code instanceof KeyCode.End) {
            cursorState.set(value.length());
            e.stopPropagation();
          } else if (code instanceof KeyCode.Enter) {
            props.onSubmit().ifPresent(Runnable::run);
            e.stopPropagation();
          } else if (code instanceof KeyCode.Esc) {
            props.onCancel().ifPresent(Runnable::run);
            // Esc isn't stopPropagation'd: ReactApp uses Esc to quit. If the parent wants Esc to
            // stay local (e.g. modal dismiss), they can intercept earlier.
          }
          // Tab / Shift-Tab / BackTab are handled by ReactApp directly, never reach here.
        });
  }
}
