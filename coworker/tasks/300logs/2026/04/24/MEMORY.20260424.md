## 2026-04-24

- Completed `remove-pagemodel-from-codebase`: deleted the `PageModel` wrapper, replaced its behavior with `GPageModel`/`WebPage` extension helpers in `pulsar-persist`, updated persist and skeleton call sites to the storage-specific API, and renamed the persist tests to cover the new extension-based path. Lesson learned: when a wrapper only mirrors a generated record, extension functions keep the ergonomics while removing the redundant domain type cleanly.
