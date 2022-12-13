package tui

/// A grapheme associated to a style.
//#[derive(Debug, Clone, PartialEq, Eq)]
case class StyledGrapheme(
    symbol: Grapheme,
    style: Style
)
