package tui
package widgets

import tui.backend.test.TestBackend
import tui.buffer.Buffer
import tui.layout.{Alignment, Rect}
import tui.terminal.Terminal
import tui.text.{Span, Spans, Text}
import tui.widgets.paragraph.{Paragraph, Wrap}

class paragraphTests extends TuiTest {
  test("it_does_not_panic_if_max_is_zero") {
    val widget = Sparkline(data = Array(0, 0, 0))
    val area = Rect(0, 0, 3, 1)
    val buffer = Buffer.empty(area)
    widget.render(area, buffer)
  }

  val SAMPLE_STRING =
    "The library is based on the principle of immediate rendering with intermediate buffers. This means that at each new frame you should build all widgets that are supposed to be part of the UI. While providing a great flexibility for rich and interactive UI, this may introduce overhead for highly dynamic content."

  test("widgets_paragraph_can_wrap_its_content") {
    val test_case = (alignment: Alignment, expected: Buffer) => {
      val backend = TestBackend(20, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val text = Array(Spans.from(SAMPLE_STRING))
        val paragraph = Paragraph(
          text = Text(text),
          block = Some(Block(borders = Borders.ALL)),
          alignment = alignment,
          wrap = Some(Wrap(trim = true))
        )
        f.render_widget(paragraph, f.size());
      }
      assert_buffer(backend, expected)
    }

    test_case(
      Alignment.Left,
      Buffer.with_lines(
        Array(
          "┌──────────────────┐",
          "│The library is    │",
          "│based on the      │",
          "│principle of      │",
          "│immediate         │",
          "│rendering with    │",
          "│intermediate      │",
          "│buffers. This     │",
          "│means that at each│",
          "└──────────────────┘"
        )
      )
    )
    test_case(
      Alignment.Right,
      Buffer.with_lines(
        Array(
          "┌──────────────────┐",
          "│    The library is│",
          "│      based on the│",
          "│      principle of│",
          "│         immediate│",
          "│    rendering with│",
          "│      intermediate│",
          "│     buffers. This│",
          "│means that at each│",
          "└──────────────────┘"
        )
      )
    )
    test_case(
      Alignment.Center,
      Buffer.with_lines(
        Array(
          "┌──────────────────┐",
          "│  The library is  │",
          "│   based on the   │",
          "│   principle of   │",
          "│     immediate    │",
          "│  rendering with  │",
          "│   intermediate   │",
          "│   buffers. This  │",
          "│means that at each│",
          "└──────────────────┘"
        )
      )
    )
  }

  test("widgets_paragraph_renders_double_width_graphemes") {
    val backend = TestBackend(width = 10, height = 10)
    val terminal = Terminal.init(backend)

    val s = "コンピュータ上で文字を扱う場合、典型的には文字による通信を行う場合にその両端点では、"
    terminal.draw { f =>
      val text = Array(Spans.from(s))
      val paragraph = Paragraph(text = Text(text), block = Some(Block(borders = Borders.ALL)), wrap = Some(Wrap(trim = true)))
      f.render_widget(paragraph, f.size());
    }

    val expected = Buffer.with_lines(
      Array(
        "┌────────┐",
        "│コンピュ│",
        "│ータ上で│",
        "│文字を扱│",
        "│う場合、│",
        "│典型的に│",
        "│は文字に│",
        "│よる通信│",
        "│を行う場│",
        "└────────┘"
      )
    )
    assert_buffer(backend, expected)
  }

  test("widgets_paragraph_renders_mixed_width_graphemes") {
    val backend = TestBackend(10, 7)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val text = Array(Spans.from("aコンピュータ上で文字を扱う場合、"))
      val paragraph = Paragraph(text = Text(text), block = Some(Block(borders = Borders.ALL)), wrap = Some(Wrap(trim = true)))
      f.render_widget(paragraph, f.size());
    }

    val expected = Buffer.with_lines(
      Array(
        // The internal width is 8 so only 4 slots for double-width characters.
        "┌────────┐",
        "│aコンピ │", // Here we have 1 latin character so only 3 double-width ones can fit.
        "│ュータ上│",
        "│で文字を│",
        "│扱う場合│",
        "│、      │",
        "└────────┘"
      )
    )
    assert_buffer(backend, expected)
  }

  test("widgets_paragraph_can_wrap_with_a_trailing_nbsp") {
    val nbsp = "\u00a0"
    val line = Spans.from(Array(Span.raw("NBSP"), Span.raw(nbsp)))
    val backend = TestBackend(20, 3)
    val terminal = Terminal.init(backend)
    val expected = Buffer.with_lines(
      Array(
        "┌──────────────────┐",
        "│NBSP\u00a0             │",
        "└──────────────────┘"
      )
    )
    terminal.draw { f =>
      val paragraph = Paragraph(text = Text(Array(line)), block = Some(Block(borders = Borders.ALL)))
      f.render_widget(paragraph, f.size());
    }
    assert_buffer(backend, expected)
  }

  test("widgets_paragraph_can_scroll_horizontally") {
    val test_case = (alignment: Alignment, scroll: (Int, Int), expected: Buffer) => {
      val backend = TestBackend(20, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val text = Text.raw("段落现在可以水平滚动了！\nParagraph can scroll horizontally!\nShort line")
        val paragraph = Paragraph(text = text, block = Some(Block(borders = Borders.ALL)), alignment = alignment, scroll = scroll)
        f.render_widget(paragraph, f.size());
      }
      assert_buffer(backend, expected)
    }

    test_case(
      Alignment.Left,
      (0, 7),
      Buffer.with_lines(
        Array(
          "┌──────────────────┐",
          "│在可以水平滚动了！│",
          "│ph can scroll hori│",
          "│ine               │",
          "│                  │",
          "│                  │",
          "│                  │",
          "│                  │",
          "│                  │",
          "└──────────────────┘"
        )
      )
    )
    // only support Alignment.Left
    test_case(
      Alignment.Right,
      (0, 7),
      Buffer.with_lines(
        Array(
          "┌──────────────────┐",
          "│段落现在可以水平滚│",
          "│Paragraph can scro│",
          "│        Short line│",
          "│                  │",
          "│                  │",
          "│                  │",
          "│                  │",
          "│                  │",
          "└──────────────────┘"
        )
      )
    )
  }
}
