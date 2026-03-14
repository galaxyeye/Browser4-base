## Daily Memory - 2026-03-14

- **DOM fidelity boundaries:** Documented `MergedDOMTree -> TinyDOMTreeNode -> MicroDOMTreeNode` in `DOMStateBuilder.kt` and clarified where prompt-oriented compaction drops DOM/AX fidelity. Structural lesson: fidelity-sensitive logic must run before lossy compaction or keep access to richer trees.

- **Best fast regression checks:** `DOMStateBuilderTest` stayed the smallest reliable validation for DOM serialization / aria snapshot work. Best focused pair for related changes: `MicroToNanoTreeHelperTest` + `DOMStateBuilderTest`. In PowerShell, quote comma-separated Surefire class lists as a single `-Dtest=...`.

- **Nano-tree range pruning fix:** `toNanoTreeInRangeRecursive()` was pruning leaf children too early, deleting valid prompt nodes. Fixed by recursing first, then pruning only empty placeholder nano nodes. Expanded tests for overlapping leaves, bounds, seen-chunk merges, invalid intervals, and full-range consistency. Lesson: pre-recursion pruning can erase required prompt content.

- **Canonical full-range behavior:** Reviewed `toNanoTree()`, `toNanoTreeInRange()`, and `toNanoTreeUnfiltered()`, then made sentinel full-range (`0.0..1_000_000.0`) delegate to the unfiltered path. Lesson: if “full range” means unfiltered, both must share one code path or drift will drop geometry-less / zero-height / sentinel-exceeding nodes.

- **Earlier aria rendering:** Added `AriaSnapshotRenderer`, made `DOMState.ariaSnapshot` prefer `EnhancedDOMTreeNode`, extracted shared YAML helpers, and kept `AriaSnapshotForNanoDOMTreeRenderer` deprecated for compatibility. Lesson: render aria from richer trees whenever output fidelity matters.

- **Playwright aria parity:** Improved formatting so `[cursor=pointer]` appears only at the highest rendered ref in a pointer subtree, collapsed unnamed generic wrappers, filtered default AX noise, derived pointer hints from real snapshot/style data, and promoted title/description into fallback names for titled generic nodes. Lesson: parity depends on recursive formatting state, AX-noise filtering, and meaningful fallback naming.

- **Invalid aria node filtering:** `AriaSnapshotRenderer` now skips comment, `script`, and `style` nodes before recursion while still dropping blank text after normalization; added focused coverage. Lesson: prune obviously non-accessible structure early to keep snapshots clean.

- **Nano aria semantic-role fix:** Investigated why `domState.serializableTree.toNanoTree().ariaSnapshot` downgraded semantic controls like `button` to `generic` while `domState.ariaSnapshot` stayed correct. Root cause: `DOMStateBuilder` intentionally removes duplicate compacted `role` attrs when they match native HTML semantics, but legacy `NanoAriaSnapshotRenderer` relied only on flattened attrs and did not recover implicit roles. Fix: preserve compacted Nano shape, but infer native roles when explicit `role` is absent (`button`, `a[href]`, `input` variants, `textarea`, `select`, `summary`, `img`, `option`). Added regression coverage reproducing `button -> generic`. Lesson: duplicate-role elision is safe only if downstream renderers can recover native semantics from element type.

- **Timeout skip hardening:** In `UniversalProxyParserTest.kt`, only known `ChatModelException` timeout messages become JUnit assumption skips; all other failures are rethrown. Lesson: guard only the known remote-timeout path.

- **Surefire / E2E behavior:** Focused `UniversalProxyParserTest` ran 0 tests until rerun with `-DrunE2ETests=true`, then skipped locally due to missing LLM config. Lesson: `@Tag("RequiresServer")` still needs `-DrunE2ETests=true` in focused Surefire runs.

- **Wrapper/tooling updates:** Updated `bin/test.ps1`, `bin/test.sh`, and `bin/README.md` to remove deleted `kotlin-sdk` / `python-sdk` modes, keep `nodejs-sdk` mapped to `sdks/browser4-cli`, fail clearly on removed modes, and always pass `-P=-examples` to avoid the missing `browser4/browser4-spa` module. `bash -n ./bin/test.sh` passed after restoring LF endings.

- **E2E renderer loop:** Added `AriaSnapshotRendererE2ETest` in `pulsar-it-tests` using a real Spring-served page with nested frames. Normalize dynamic refs in assertions instead of comparing raw IDs/indentation. Reliable loop: pulsar-browser unit test -> local install -> focused `pulsar-it-tests` E2E with `-DrunE2ETests=true`.

- **Scrollable snapshot cleanup:** Re-ran `SnapshotServiceIsScrollableTest`; brittleness was in expectations, not `CDPSnapshotService`. Updated assertions to stable guarantees only. Lesson: DOM snapshot tests should track supported guarantees, not optimistic generic-container scrollability.

- **Memory policy:** Verified before appending; never rewrite earlier monthly entries. March memory already contained the `2026-03-13` rollup, so no backfill was needed.


- **Coworker dotfile-ignore hardening:** Updated the shared coworker file filter so dotfiles and files nested under dot-directories are always ignored, not just .gitkeep. Routed coworker.ps1 queue scans through the shared helper so draft/created/review/approved/pushed logging and processing all skip hidden queue artifacts consistently. Validation: parsed the edited PowerShell scripts and ran a focused temp-directory check covering 	ask.md, .env, .gitkeep, and .git\HEAD. Lesson: for queue scanners, ignore rules must examine ancestor path segments too, otherwise files inside hidden directories can still leak through recursive scans.

- **Memory sync check:** Verified coworker\tasks\300logs\2026\03\MEMORY.202603.md already contains the 2026-03-13 rollup, so no monthly backfill was needed before appending today's entry.
- **Correction:** The temp-directory validation for the coworker dotfile-ignore change covered the files `task.md`, `.env`, `.gitkeep`, and `.git\HEAD`.
