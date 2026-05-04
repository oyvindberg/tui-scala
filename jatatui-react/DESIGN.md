# jatatui-react — design sketch

A React-style API layer over `jatatui-widgets`. Not wired into the build yet; this directory is a sketch for review.

## Stance

Pure React semantics. Components are functions of props that return Elements. Hooks (`useState`, `useRef`, `useEffect`, `useFocus`) for local state, side effects, and focus. Element tree is produced fresh every render and committed straight to the buffer — no virtual-DOM diffing because it isn't worth it for terminal-sized buffers.

What we DO get from React:
- Components as pure functions of props
- Hooks for local state, refs, effects, focus
- Composition by nesting elements
- Per-region event handlers attached during render
- Proper unmount semantics (sweep untouched fibers, run cleanups)

What we DON'T get:
- Virtual-DOM diffing / reconciliation (overkill for terminal)
- JSX (Java has no equivalent; static-method composition stands in)

## Decisions taken

| Question | Answer |
|---|---|
| **Re-render cadence** | Strictly event-driven. `setState` flips a dirty bit; runner re-renders on next loop iteration. No frame timer. Animations would need a `useFrame` hook later — out of scope for now. |
| **Focus model** | Ink's. `useFocus({id?, autoFocus?})` registers in render order, Tab/Shift-Tab cycles, explicit `id` lets app code `focus(id)` imperatively. See `FocusManager.java`. |
| **Effect cleanup / unmount** | After every render, sweep the hook store: any fiber that wasn't touched this frame is "unmounted" — run its pending `useEffect` cleanups and drop its state. Same algorithm React's commit phase uses. See `RenderContext.HookStore.sweep()`. |
| **Async / data fetching** | Stock `useState + useEffect + CompletableFuture` composition. Sugar like `useFuture(supplier) → LoadingState<T>` is a 20-line layer on top, not in the core. |
| **Children typing** | Yes. `Element` is a sealed interface; built-in records implement it; user code returns it from components. Typed sub-children (e.g., `Tabs.Tab` only valid inside `Tabs`) are plain records, not Elements. |

## Borrowed from typr-3 — and what's changed

| Borrowed | Changed |
|---|---|
| Component-as-render-function | Now wrapped as `Component(Function<RenderContext, Element>)` — one specific kind of `Element`. Mirrors React's "function component as element type". |
| Hook-stack with index counter | `(parent-fiber, key)` integer pair via `Fiber.java`, not `componentStack.mkString("/")`. No collisions, no per-hook string allocation. |
| Per-area event registry | Adds focus tracking + key-event routing scoped by the focused fiber. |
| `Box` / `Column` / `Row` / `Text` / `Button` shapes | Built on jatatui's `Layout`/`Constraint` solver. typr-3's hand-rolled `area.x + i*childWidth` math doesn't honor min/max constraints. |

Dropped: 1418-LOC `Elements` (start with ~10 essentials and grow per use case); `Integration` global singleton (`ReactApp` instance owns its own state); string-keyed `StateStore`.

## Cross-references

- tui-scala issue [#12](https://github.com/oyvindberg/tui-scala/issues/12) — "Investigate building support for events into widgets"
- tui-scala issue [#41](https://github.com/oyvindberg/tui-scala/issues/41) + [PR #50](https://github.com/oyvindberg/tui-scala/pull/50) — "Make Layout a widget" (foundation for nestable composition)
- typr-3 [`tui-react/`](file:///Users/oyvind/pr/typr-3/tui-react) — Scala 3 prior art
- [Ink](https://github.com/vadimdemedes/ink) — React-for-terminal in JS; closest relative; we copy its focus model.

## File map

- `Element.java` — sealed interface + built-in records (`Box`, `Text`, `Paragraph_`, `Button`, `Column`, `Row`, `Sized`, `Tabs`, `Tabs.Tab`, `ForEach`, `When`, `IfElse`, `Empty`, `WidgetWrap`). Containers (`Box`, `Column`, `Row`) take `Flex` / `Spacing` / `Margin` and feed jatatui-core's [`Layout`](../jatatui-core/src/java/jatatui/core/layout/Layout.java) solver directly. Per-child constraints come from wrapping any element in `Sized(Constraint, Element)`.
- `Component.java` — function-component case
- `Memo.java` — `React.memo` equivalent (deps-keyed)
- `PureComponent.java` — record-props auto-memoized component
- `Components.java` — static factories: `box`, `text`, `column`, `row`, `tabs`, `tab`, plus per-child sizing helpers `length(7, child)`, `fill(1, child)`, `min`, `max`, `percent`, `ratio`, `sized`. Memoization helpers `memo(deps, body)`, `pureComponent(props, body)`, `deps(...)`.
- `RenderContext.java` — hooks (`useState`, `useRef`, `useEffect`, `useFocus`), `renderChild`, event-registration helpers, `HookStore.memoCache`, `HookStore.memoOrCompute`, `HookStore.sweep`
- `Fiber.java` — `(parent, key)` identity for hook lookup. `toPath()` for devtools-style debugging.
- `FocusManager.java` — Ink-style focus
- `EventRegistry.java` — per-frame click/scroll/key handler list, dispatcher
- `EventKind.java` — enum
- `ReactApp.java` — event-driven loop, routes Crossterm events, runs sweep+commit
- `examples/CounterExample.java` — counter (stateful + focusable + key-bound) and a todo list (record-props pure component) inside a `column` with explicit `length(9, ...)` / `fill(1, ...)` sizing

## Still open

1. **`useFocus` ↔ Fiber mapping**: `FocusManager` knows the focused id (string) but `EventRegistry` keys handlers by Fiber. The current sketch routes all key handlers as global. Need `(id → Fiber)` registered alongside `useFocus` so onKey only fires when the *registering* fiber is focused.
2. **`forEach` row height**: currently fixed per-call (`rowHeight=1` default). Need a flexible variant where each child produces an Element of unknown height (maybe via min-height constraint).
3. **`useState` semantics on initial render**: `Supplier<T>` initial is invoked at most once per fiber per mount. That's right. But `set()` from inside an effect during render — should that bypass the dirty flag and be applied synchronously, or schedule for next tick? React batches; we should too.
4. **`useFuture` sugar**: needs design; should it auto-cancel on unmount? React Query's pattern (`{ data, isLoading, error }`) is the obvious target.
5. **DevTools-ish introspection**: `Element` is a tree; could expose a `dump()` for golden-file testing of "what would this render".
