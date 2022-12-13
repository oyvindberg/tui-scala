package tui
package widgets
package clear

import tui.internal.ranges

/// A widget to clear/reset a certain area to allow overdrawing (e.g. for popups).
///
/// This widget **cannot be used to clear the terminal on the first render** as `tui` assumes the
/// render area is empty. Use [`crate::Terminal::clear`] instead.
///
/// # Examples
///
/// ```
/// # use tui::widgets::{Clear, Block, Borders};
/// # use tui::layout::Rect;
/// # use tui::Frame;
/// # use tui::backend::Backend;
/// fn draw_on_clear<B: Backend>(f: &mut Frame<B>, area: Rect) {
///     let block = Block::default().title("Block").borders(Borders::ALL);
///     f.render_widget(Clear, area); // <- this will clear/reset the area first
///     f.render_widget(block, area); // now render the block widget
/// }
/// ```
///
/// # Popup Example
///
/// For a more complete example how to utilize `Clear` to realize popups see
/// the example `examples/popup.rs`
case object ClearWidget extends Widget {
  override def render(area: Rect, buf: Buffer): Unit =
    ranges.range(area.left, area.right) { x =>
      ranges.range(area.top, area.bottom) { y =>
        buf.get(x, y).reset()
      }
    }
}
