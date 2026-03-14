Compressed version under 3000 chars:

## Daily Memory - 2026-03-14

- **DOM fidelity map:** Added KDoc to `DOMStateBuilder.kt` clarifying `MergedDOMTree -> TinyDOMTreeNode -> MicroDOMTreeNode`. Key lesson: explicitly document where prompt-facing compaction loses DOM/AX fidelity.

- **Fast regression baseline:** `DOMStateBuilderTest` passed before/after changes and remains the smallest dependable check for DOM serialization and aria snapshot work.

- **Nano-tree range pruning fix:** Expanded `MicroToNanoTreeHelperTest` for overlapping leaves, open/closed bounds, merged seen-chunks, invalid intervals, and full-range consistency. Root cause: `toNanoTreeInRangeRecursive()` pruned leaf children before recursion, deleting valid prompt nodes. Fix: recurse first, then prune only empty placeholder nano nodes. Lesson: range tests must include true leaf descendants; pre-recursion pruning can remove exactly what prompts need.

- **Canonical full-range path:** Reviewed `toNanoTree()`, `toNanoTreeInRange()`, and `toNanoTreeUnfiltered()`, then made the sentinel full-range (`0.0..1_000_000.0`) delegate to the unfiltered path. Lesson: if “full range” means unfiltered, both must share one code path or drift will drop geometry-less / zero-height / sentinel-exceeding nodes.

- **Best focused validation pair:** `MicroToNanoTreeHelperTest` + `DOMStateBuilderTest` is the efficient regression suite for DOM serialization changes. In PowerShell, quote comma-separated Surefire class lists as one `-Dtest=...` value.

- **Aria rendering moved earlier:** Added `AriaSnapshotRenderer`, made `DOMState.ariaSnapshot` prefer `EnhancedDOMTreeNode`, extracted shared YAML helpers, and kept `AriaSnapshotForNanoDOMTreeRenderer` deprecated for compatibility. Lesson: fidelity-sensitive rendering should happen before lossy enhanced -> micro -> nano compaction, or keep access to the richer tree.

- **Playwright aria parity fixes:** Updated Kotlin formatting so `[cursor=pointer]` appears only on the highest rendered ref in a pointer subtree, collapsed unnamed generic wrappers, filtered noisy default AX props, derived pointer hints from real snapshot/style data, and promoted title/description into fallback names for titled generic nodes. Lesson: parity depends on recursive formatting state, AX-noise filtering, and preserving meaningful fallback names.

- **Timeout skip hardening:** In `UniversalProxyParserTest.kt`, only known `ChatModelException` timeout messages become JUnit assumption skips; everything else is rethrown. Lesson: guard the known remote-timeout path only.

- **Surefire/E2E behavior:** Focused `UniversalProxyParserTest` ran 0 tests until rerun with `-DrunE2ETests=true`, then skipped locally due to missing LLM config. Lesson: `@Tag("RequiresServer")` still needs `-DrunE2ETests=true` in focused Surefire runs.

- **Wrapper/tooling updates:** Updated `bin/test.ps1`, `bin/test.sh`, and `bin/README.md` to remove deleted `kotlin-sdk` / `python-sdk` modes, keep `nodejs-sdk` mapped to `sdks/browser4-cli`, fail clearly on removed modes, and always pass `-P=-examples` to avoid the missing `browser4/browser4-spa` module. `bash -n ./bin/test.sh` passed after restoring LF endings.

- **E2E renderer loop:** Added `AriaSnapshotRendererE2ETest` in `pulsar-it-tests` using a real Spring-served page plus nested frames. Normalize dynamic refs in assertions instead of comparing raw IDs/indentation. Reliable loop: `pulsar-browser` unit test -> local install -> focused `pulsar-it-tests` E2E with `-DrunE2ETests=true`.

- **Scrollable snapshot cleanup:** Re-ran `SnapshotServiceIsScrollableTest`; issue was brittle expectations, not `CDPSnapshotService`. Updated test to assert only stable guarantees: JS fixture setup, node presence/rects, `overflow:hidden == false`, and `includeScrollAnalysis=false == null`. Lesson: keep DOM snapshot tests aligned to supported guarantees; positive generic-container scrollability remains postponed.

- **Memory policy:** Verified before appending; never rewrite earlier monthly entries. Confirmed March memory already contained the `2026-03-13` rollup, so no backfill was needed.


- **ARIA invalid-node filtering:** Updated `AriaSnapshotRenderer` to skip comment nodes plus `script`/`style` elements before recursion while continuing to drop blank text through normalization, and added `renderIgnoresInvalidAriaSnapshotNodes` in `DOMStateBuilderTest` to lock the behavior down. Outcome: focused validation with `.\mvnw.cmd -f pulsar-core\pulsar-browser\pom.xml "-Dtest=DOMStateBuilderTest#renderIgnoresInvalidAriaSnapshotNodes" -D"surefire.failIfNoSpecifiedTests=false" test` passed. Lesson: aria snapshot rendering should prune obviously non-accessible structural nodes before child traversal so snapshots stay cleaner and the renderer avoids wasted work.

- **Monthly memory check:** Verified `coworker/tasks/300logs/2026/03/MEMORY.202603.md` already contains the `2026-03-13` rollup, so no March backfill was needed for this task.
