## Daily Memory - 2026-03-14

- **Clarified the DOMStateBuilder node-model pipeline:** Added KDoc in `pulsar-core\pulsar-browser\src\main\kotlin\ai\platon\browser4\driver\chrome\dom\DOMStateBuilder.kt` documenting the transformation chain: `DOMTreeNodeEx` as the rich merged DOM/AX/snapshot model, `TinyDOMTreeNode` as the builder-stage pruned/display-aware form, and `MicroDOMTreeNode` as the compact agent-facing output. **Learning:** The transformation boundary is the clearest place to explain why rich internal nodes become compact prompt-facing ones.

- **Used the smallest reliable validation scope for DOM serialization docs:** Ran `.\mvnw.cmd -f pulsar-core\pulsar-browser\pom.xml -Dtest=DOMStateBuilderTest -D"surefire.failIfNoSpecifiedTests=false" test` before and after the KDoc change; both passed (`12` tests). **Learning:** Even for docs-only changes near DOM serialization, `DOMStateBuilderTest` is the right focused safety check.

- **Kept coworker memory append-only:** Added the daily memory entry and treated monthly sync as an explicit gate: if yesterday’s rollup is missing, append a new cumulative month-to-date update; otherwise leave the monthly file unchanged. Later verification showed the `2026-03-13` rollup already existed, so only the daily log needed updating. **Learning:** Never retro-edit prior month entries; verify first, then append only if needed.

- **Added `toNanoTreeInRange` regression coverage and fixed dropped leaf nodes:** Expanded `MicroToNanoTreeHelperTest` to cover overlapping leaf inclusion, open-start/closed-end semantics, merged seen-chunk behavior, and invalid intervals. The tests exposed that `MicroToNanoTreeHelper.toNanoTreeInRangeRecursive()` filtered leaf children too early, removing valid prompt-facing nodes. The fix now recurses into in-range children first, then prunes only empty placeholder nano nodes. **Learning:** Tree-range tests must include real leaf descendants, because pre-recursion filtering can erase exactly the compact nodes the prompt needs.

- **Validated the nano-range fix with focused browser-module tests:** Used `DOMStateBuilderTest` as baseline, then ran `.\mvnw.cmd -f pulsar-core\pulsar-browser\pom.xml "-Dtest=MicroToNanoTreeHelperTest,DOMStateBuilderTest" -D"surefire.failIfNoSpecifiedTests=false" test`; final pass was `16` tests. **Learning:** In Windows PowerShell, quote comma-separated Surefire class lists as one `-Dtest=...` value so PowerShell does not split them.

- **Aligned canonical full-range nano-tree generation with unfiltered traversal:** Reviewed `MicroDOMTreeNode.toNanoTree()`, `toNanoTreeInRange()`, and `toNanoTreeUnfiltered()`, then changed `MicroToNanoTreeHelper.toNanoTreeInRange()` so the canonical full-range request (`0.0..1_000_000.0`) delegates directly to the unfiltered path. This removed mismatches where the range path dropped geometry-less nodes, zero-height nodes at the open start boundary (`y == 0`), or nodes beyond the sentinel cutoff while unfiltered traversal kept them. **Learning:** If a “full range” API is intended to match unfiltered behavior, it should share the same code path rather than approximate completeness with geometric filters.

- **Added consistency regression coverage:** Extended `MicroToNanoTreeHelperTest` with an equality check asserting that `toNanoTreeInRange(0.0, 1_000_000.0)`, `toNanoTree()`, and `toNanoTreeUnfiltered()` return the same `NanoDOMTree` for geometry-less nodes, a zero-height boundary node, and a node below the sentinel end range. **Learning:** Boundary and geometry-free nodes are where filtered and unfiltered serializers most often drift.

- **Validated the final serializer alignment:** Re-ran `.\mvnw.cmd -f pulsar-core\pulsar-browser\pom.xml "-Dtest=MicroToNanoTreeHelperTest,DOMStateBuilderTest" -D"surefire.failIfNoSpecifiedTests=false" test`; targeted suite passed (`17` tests). **Learning:** For DOM serialization work here, `MicroToNanoTreeHelperTest` plus `DOMStateBuilderTest` is an efficient focused validation pair.



- **Implemented direct aria snapshot rendering from EnhancedDOMTreeNode:** Added AriaSnapshotRenderer, routed DOMState.ariaSnapshot to prefer the enhanced tree when available, extracted shared YAML formatting helpers, and deprecated AriaSnapshotForNanoDOMTreeRenderer for backward compatibility. The new path preserves AX metadata such as description and non-default AX properties that were previously lost during enhanced -> micro -> nano conversion. **Learning:** When a compact serialization pipeline intentionally filters attributes, any fidelity-sensitive rendering should happen before that compaction boundary or keep a direct reference to the richer source tree.

- **Validated the direct renderer with focused browser-module tests:** Added regression coverage proving direct rendering keeps description/utocomplete-style AX metadata while the legacy nano renderer still omits it, then ran .\mvnw.cmd -f pulsar-core\pulsar-browser\pom.xml "-Dtest=DOMStateBuilderTest" -D"surefire.failIfNoSpecifiedTests=false" test successfully (13 tests). **Learning:** DOMStateBuilderTest is a good focused safety net for aria snapshot renderer changes because it exercises both the enhanced-tree build path and the prompt-facing snapshot output.

- **Memory note correction:** The previous validation bullet refers to description/utocomplete-style AX metadata. **Learning:** When appending Markdown containing backticks from PowerShell here-strings, prefer plain text or escape-aware construction so the memory log stays readable.


- **Memory note clarification:** The validation coverage preserved description and autocomplete-style AX metadata. **Learning:** Avoid Markdown backticks when appending text through PowerShell strings unless they are escaped explicitly.

