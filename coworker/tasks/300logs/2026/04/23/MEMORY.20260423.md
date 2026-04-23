## 2026-04-24

- Completed `decouple-plainwebpage-from-gwebpage`: replaced the accidental `GoraWebPage` copy with a storage-agnostic `PlainWebPage` data model that stores native Kotlin/JDK fields, added snapshot converters from `WebPage`, and covered the new behavior with focused persist-module tests. Lesson learned: this class had only been renamed before, so the safest fix was to make it truly plain and detached rather than preserving the old wrapper API around `GWebPage`.
