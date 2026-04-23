## Through 2026-04-23

- `2026-04-23`: Completed the `decouple-plainwebpage-from-gwebpage` task by turning `PlainWebPage` into a storage-agnostic snapshot/data model with native fields instead of a renamed `GWebPage` wrapper, and added tests to lock in detachment from Gora-specific state. The key lesson was to favor a real plain model over keeping compatibility with an implementation detail that had no active callers.
