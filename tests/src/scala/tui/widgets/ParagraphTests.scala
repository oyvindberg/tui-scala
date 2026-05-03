package tui
package widgets

class ParagraphTests extends TuiTest {
  test("it_does_not_panic_if_max_is_zero") {
    val widget = SparklineWidget.empty().withData(Array(0, 0, 0))
    val area = new Rect(0, 0, 3, 1)
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
        val text = Text.from(Spans.nostyle(SAMPLE_STRING))
        val paragraph = ParagraphWidget
          .empty(text)
          .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
          .withWrap(new ParagraphWidget.Wrap(true))
          .withAlignment(alignment)
        f.renderWidget(paragraph, f.size);
      }
      assertBuffer(backend, expected)
    }

    test_case(
      Alignment.Left,
      Buffer.withLines(
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
    test_case(
      Alignment.Right,
      Buffer.withLines(
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
    test_case(
      Alignment.Center,
      Buffer.withLines(
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
  }

  test("widgets_paragraph_renders_double_width_graphemes") {
    val backend = TestBackend(10, 10)
    val terminal = Terminal.init(backend)

    val s = "コンピュータ上で文字を扱う場合、典型的には文字による通信を行う場合にその両端点では、"
    terminal.draw { f =>
      val paragraph = ParagraphWidget
        .empty(Text.nostyle(s))
        .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
        .withWrap(new ParagraphWidget.Wrap(true))
      f.renderWidget(paragraph, f.size);
    }

    val expected = Buffer.withLines(
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
    assertBuffer(backend, expected)
  }

  test("widgets_paragraph_renders_mixed_width_graphemes") {
    val backend = TestBackend(10, 7)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val text = Text.nostyle("aコンピュータ上で文字を扱う場合、")
      val paragraph = ParagraphWidget
        .empty(text)
        .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
        .withWrap(new ParagraphWidget.Wrap(true))
      f.renderWidget(paragraph, f.size);
    }

    val expected = Buffer.withLines(
      // The internal width is 8 so only 4 slots for double-width characters.
      "┌────────┐",
      "│aコンピ │", // Here we have 1 latin character so only 3 double-width ones can fit.
      "│ュータ上│",
      "│で文字を│",
      "│扱う場合│",
      "│、      │",
      "└────────┘"
    )
    assertBuffer(backend, expected)
  }

  test("widgets_paragraph_can_wrap_with_a_trailing_nbsp") {
    val nbsp = " "
    val line = Text.fromSpans(Span.nostyle("NBSP"), Span.nostyle(nbsp))
    val backend = TestBackend(20, 3)
    val terminal = Terminal.init(backend)
    val expected = Buffer.withLines(
      "┌──────────────────┐",
      "│NBSP              │",
      "└──────────────────┘"
    )
    terminal.draw { f =>
      val paragraph = ParagraphWidget
        .empty(line)
        .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
      f.renderWidget(paragraph, f.size);
    }
    assertBuffer(backend, expected)
  }

  test("widgets_paragraph_can_scroll_horizontally") {
    val test_case = (alignment: Alignment, scroll: ParagraphWidget.Scroll, expected: Buffer) => {
      val backend = TestBackend(20, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val text = Text.nostyle("段落现在可以水平滚动了！\nParagraph can scroll horizontally!\nShort line")
        val paragraph = ParagraphWidget
          .empty(text)
          .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
          .withAlignment(alignment)
          .withScroll(scroll)
        f.renderWidget(paragraph, f.size);
      }
      assertBuffer(backend, expected)
    }

    test_case(
      Alignment.Left,
      new ParagraphWidget.Scroll(0, 7),
      Buffer.withLines(
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
    // only support Alignment.Left
    test_case(
      Alignment.Right,
      new ParagraphWidget.Scroll(0, 7),
      Buffer.withLines(
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
  }
}
