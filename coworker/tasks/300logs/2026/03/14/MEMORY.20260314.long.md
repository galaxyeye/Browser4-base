Here’s a compressed version under 3000 characters:

## Daily Memory - 2026-03-14

- **Clarified DOM node-model pipeline:** Added KDoc in `DOMStateBuilder.kt` documenting the transformation chain: `MergedDOMTree` = rich merged DOM/AX/snapshot model, `TinyDOMTreeNode` = builder-stage pruned/display-aware form, `MicroDOMTreeNode` = compact agent-facing output. **Learning:** The compaction boundary is the right place to explain why rich internal nodes become minimal prompt nodes.

- **Used focused validation for DOM serialization changes:** Re-ran `.\mvnw.cmd -f pulsar-core\pulsar-browser\pom.xml -Dtest=DOMStateBuilderTest -D"surefire.failIfNoSpecifiedTests=false" test` before/after nearby changes; it passed both times. **Learning:** `DOMStateBuilderTest` is the smallest reliable safety check for DOM serialization / aria snapshot work.

- **Fixed nano-tree range pruning with stronger regression coverage:** Expanded `MicroToNanoTreeHelperTest` for overlapping leaf inclusion, open-start/closed-end semantics, merged seen-chunk behavior, invalid intervals, and full-range consistency. Root cause: `toNanoTreeInRangeRecursive()` filtered leaf children too early and dropped valid prompt-facing nodes. Fix: recurse into in-range children first, then prune only empty placeholder nano nodes. **Learning:** Range tests must include real leaf descendants because pre-recursion filtering can remove exactly the compact nodes the prompt needs.

- **Aligned full-range and unfiltered traversal:** Reviewed `MicroDOMTreeNode.toNanoTree()`, `toNanoTreeInRange()`, and `toNanoTreeUnfiltered()`, then changed `MicroToNanoTreeHelper.toNanoTreeInRange()` so canonical full range (`0.0..1_000_000.0`) delegates to the unfiltered path. This removed drift where range traversal dropped geometry-less nodes, zero-height nodes at `y == 0`, or nodes beyond the sentinel cutoff. **Learning:** If “full range” should equal unfiltered behavior, both should share the same code path.

- **Validated serializer fixes with the right focused pair:** Ran `.\mvnw.cmd -f pulsar-core\pulsar-browser\pom.xml "-Dtest=MicroToNanoTreeHelperTest,DOMStateBuilderTest" -D"surefire.failIfNoSpecifiedTests=false" test`; focused passes increased as expected. **Learning:** `MicroToNanoTreeHelperTest` + `DOMStateBuilderTest` is an efficient validation pair for DOM serialization work. In PowerShell, quote comma-separated Surefire class lists as one `-Dtest=...` value.

- **Implemented direct aria snapshot rendering from the enhanced tree:** Added `AriaSnapshotRenderer`, routed `DOMState.ariaSnapshot` to prefer `EnhancedDOMTreeNode`, extracted shared YAML formatting helpers, and deprecated `AriaSnapshotForNanoDOMTreeRenderer` for compatibility. This preserved AX metadata like description and autocomplete-style/non-default properties that were lost during enhanced -> micro -> nano compaction. **Learning:** Fidelity-sensitive rendering should happen before a lossy compaction boundary, or retain a direct reference to the richer tree.

- **Fixed memory-note formatting guidance:** Confirmed validation preserved description/autocomplete-style AX metadata. **Learning:** When appending Markdown via PowerShell here-strings, avoid unescaped backticks or use escape-aware construction.

- **Made flaky LLM-backed proxy test timeout-skippable:** Updated `UniversalProxyParserTest.kt` so `testParseuniversalproxyWithNonStandardProxy()` catches `ChatModelException` and converts known timeout messages into a JUnit assumption skip, while rethrowing non-timeout failures. **Learning:** Guard only the known remote-timeout path instead of weakening the whole test.

- **Validated with correct Surefire profile awareness:** A focused run of `UniversalProxyParserTest` executed 0 tests until rerun with `-DrunE2ETests=true`, after which the class ran and skipped locally because no LLM was configured. **Learning:** In this repo, `@Tag("RequiresServer")` tests need `-DrunE2ETests=true` for focused Surefire runs to be meaningful.

- **Kept coworker memory append-only:** Daily memory was appended without retro-editing monthly history. Verified `coworker\tasks\300logs\2026\03\MEMORY.202603.md` already contained the `2026-03-13` rollup, so no backfill was needed. **Learning:** Verify first, then append; never rewrite prior month entries.


- **Aligned Kotlin aria snapshot cursor formatting with the TypeScript reference:** Updated `AriaSnapshotFormatting.kt` so `[cursor=pointer]` is emitted only for the highest rendered ref in a pointer-bearing subtree, matching `ariaSnapshot.ts` behavior instead of repeating the marker on nested interactive descendants. Added a focused regression in `DOMStateBuilderTest.kt` covering an interactive container with an interactive child, and re-ran `./mvnw -f pulsar-core/pulsar-browser/pom.xml -Dtest=DOMStateBuilderTest -D"surefire.failIfNoSpecifiedTests=false" test` successfully. **Learning:** When mirroring Playwright aria snapshot output, recursive formatting state matters just as much as node metadata—ancestor-emitted cursor markers must suppress descendant duplicates to keep snapshots semantically aligned.
