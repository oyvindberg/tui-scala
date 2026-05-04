package jatatui.react;

/// Identity for a single hook slot — `(owning fiber, hook-call-index)`. Package-private; created
/// only by [RenderContext.useState] / `useRef` / `useEffect`.
record HookKey(Fiber fiber, int index) {}
