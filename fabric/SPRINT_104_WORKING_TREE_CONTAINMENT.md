# Sprint 104 — Working-tree containment (hygiene)

## Reason for this sprint

**Sprint 103** (**`fbdab7d8`**) committed only the **leaderboard** template work. The clone still reported **modified** tracked files (including **build/release-sensitive** paths) plus **untracked** docs — risking accidental commits or “haunted” diffs before the next Path A menu sprint. This sprint **does not** change gameplay, commands, or menus.

## Part A — Working-tree inventory (at sprint start)

Commands run:

```text
git status --short
git diff --stat
git diff --name-only
git ls-files --others --exclude-standard
```

### Modified tracked files (before resolution)

| File | Status |
|------|--------|
| `.gitignore` | M |
| `README.md` | M |
| `fabric/MENU_AND_COMMANDS_PORT.md` | M |
| `fabric/PARITY_AUDIT_LEDGER_PHASE1.md` | M |
| `fabric/PARITY_COMPLETION_ROADMAP.md` | M |
| `fabric/build.gradle.kts` | M |
| `fabric/src/main/java/.../FabricLeaderboardMenuGui.java` | M |
| `fabric/src/main/resources/fabric.mod.json` | M |
| `fabric/src/test/java/.../FabricMenuGuiTranslatableScanTest.java` | M |
| `slate-fabric/build.gradle.kts` | M |
| `slate-fabric/src/main/java/.../GraniteTemplateActionNames.java` | M |
| `slate-fabric/src/main/java/.../GraniteTemplateItems.java` | M |
| `slate-fabric/src/test/java/.../GraniteTemplateActionNamesTest.java` | M |
| `slate-fabric/src/test/java/.../GraniteTemplateItemsTest.java` | M |

### Untracked files (before resolution)

| Path |
|------|
| `docs/architecture/granitelib-acceptance-checklist.txt` |
| `docs/architecture/granitelib-dependency-cutover-results.md` |
| `docs/architecture/granitelib-examinable-packaging-fix.md` |
| `docs/architecture/granitelib-java-bedrock-smoke-results.md` |
| `docs/architecture/granitelib-manual-acceptance-matrix.md` |
| `docs/architecture/granitelib-no-jij-transition-results.md` |
| `docs/architecture/granitelib-release-prep-faq.md` |
| `docs/architecture/granitelib-runtime-smoke-transition-results.md` |
| `docs/architecture/granitelib-strict-server-transition-smoke-results.md` |
| `docs/handoffs/2026-07-11-granitelib-sprint-90-standalone-metadata-build-output.md` |
| `docs/handoffs/2026-07-12-granitelib-sprint-91-runtime-smoke-transition.md` |
| `docs/handoffs/2026-07-13-granitelib-sprint-92-strict-server-transition-smoke.md` |
| `docs/handoffs/2026-07-14-granitelib-sprint-93-examinable-packaging-fix.md` |
| `docs/handoffs/2026-07-15-granitelib-sprint-94-no-jij-transition-build.md` |
| `docs/handoffs/2026-07-16-granitelib-sprint-95-dependency-cutover.md` |
| `docs/handoffs/2026-07-17-granitelib-sprint-96-java-bedrock-smoke.md` |
| `docs/handoffs/2026-07-18-granitelib-sprint-97-manual-acceptance-pass.md` |
| `docs/handoffs/2026-07-19-granitelib-sprint-98-release-prep-faq.md` |
| `fabric/MOD_COMMANDS.txt` |
| `fabric/SPRINT_SUMMARY_SPEC.md` |

## Part B — Classification table

| File | Status | Category | Decision | Reason |
|------|--------|----------|----------|--------|
| `.gitignore` | was M | **C → resolved** | **Refresh index** (`git add -u`); **no blob diff** vs `HEAD` | `git hash-object` = `HEAD` blob; `git diff-files` showed racy/stale index stat (second SHA `0000…`); not a real edit. |
| `README.md` | was M | **C → resolved** | Same | Identical to **HEAD**; phantom **M**. |
| `fabric/build.gradle.kts` | was M | **C → resolved** | Same | Identical to **HEAD**; **two-JAR** / **`embedGraniteJij`** policy unchanged. |
| `fabric.mod.json` | was M | **C → resolved** | Same | **`depends.granitelib`** intact; identical to **HEAD**. |
| `slate-fabric/build.gradle.kts` | was M | **C → resolved** | Same | Identical to **HEAD**. |
| `fabric/MENU_AND_COMMANDS_PORT.md` | was M | **C → resolved** | Same | Content already merged in **Sprint 103** commit; working tree matched **HEAD**. |
| `fabric/PARITY_AUDIT_LEDGER_PHASE1.md` | was M | **C → resolved** | Same | Idem. |
| `fabric/PARITY_COMPLETION_ROADMAP.md` | was M then **updated** | **A** | **Commit** in Sprint 104 | Sprint 104 paragraph added (**this sprint**). |
| `FabricLeaderboardMenuGui.java` | was M | **C → resolved** | Index refresh | Matches **HEAD** (**Sprint 103**). |
| `FabricMenuGuiTranslatableScanTest.java` | was M | **C → resolved** | Index refresh | Matches **HEAD**. |
| `GraniteTemplate*.java` / tests | was M | **C → resolved** | Index refresh | Matches **HEAD** (**Sprint 103**). |
| `docs/architecture/*` (granitelib-*) | untracked | **D** | **Stay untracked** (or **owner commit** later) | Local **GraniteLib transition / smoke** notes; **not** part of Sprint 103 product surface. |
| `docs/handoffs/2026-07-*granitelib*` | untracked | **D** | **Stay untracked** | Agent/handoff-style captures; optional future **`docs/`** commit by owner. |
| `fabric/MOD_COMMANDS.txt` | untracked | **D** | **Stay untracked** | User-facing command list draft; may drift from **`COMMAND_PARITY_AUDIT`**; not release-critical. |
| `fabric/SPRINT_SUMMARY_SPEC.md` | untracked | **D** | **Stay untracked** | Internal sprint **spec** template; useful but **not** required in `main` unless adopted team-wide. |
| **`fabric/PARITY_BACKLOG.md`** | edited | **A** | **Commit** | Sprint 104 row. |
| **`fabric/SPRINT_104_WORKING_TREE_CONTAINMENT.md`** | new | **A** | **Commit** | This manual. |

**Resolution command for phantom tracked `M`:** `git add -u` (updates index to match worktree; **empty staged diff** vs `HEAD` when blobs already matched).

## Part C — Packaging policy guardrail (on `HEAD` / `fbdab7d8`)

Verified on committed sources (**not** relying on phantom edits):

| Check | Result |
|-------|--------|
| Canonical install | **Two JARs:** **FabricAuraSkills** (`auraskills`) + **granitelib-fabric** (`granitelib`). |
| `fabric.mod.json` | **`depends.granitelib`** present (`>=0.1.0-alpha.2`). |
| `fabric/build.gradle.kts` | Default **`embedGraniteJij=false`**; **`compileOnly`** GraniteLib unless **`-PembedGraniteJij=true`**; **`verifyFabricReleaseBundle`** rejects nested **`granitelib-fabric-*.jar`** when **not** embedding. |
| `README.md` | States **two-mod** install; JiJ **opt-in** / non-canonical. |

**Conclusion:** No local drift was needed to “fix” packaging; policy on **HEAD** matches **Sprint 93** two-JAR story.

## Part D — Optional clone-local noise reduction

These paths are **not** added to repo **`.gitignore`** (would affect all clones). To hide **D** files from `git status` **on this machine only**, append the list to **`.git/info/exclude`** (template in **Appendix**). **This clone:** the block was **appended** during Sprint 104 so **`git status`** is clean after the doc commit. Other clones: copy the appendix or leave files visible.

Remove lines if you later **`git add`** those docs.

## Part E — Files committed / reverted (summary)

| Outcome | Paths |
|---------|--------|
| **Committed** | **`fabric/SPRINT_104_WORKING_TREE_CONTAINMENT.md`**, **`fabric/PARITY_BACKLOG.md`**, **`fabric/PARITY_COMPLETION_ROADMAP.md`** (Sprint 104 notes). |
| **Reverted** | **None** (no real content rollback required). |
| **Resolved without commit** | Phantom **`M`** on listed tracked files via **`git add -u`**. |
| **Left untracked** | **`docs/architecture/granitelib-*`**, **`docs/handoffs/2026-07-*granitelib*`**, **`fabric/MOD_COMMANDS.txt`**, **`fabric/SPRINT_SUMMARY_SPEC.md`**. |

**Not updated (no change needed):** `fabric/MENU_AND_COMMANDS_PORT.md`, `fabric/PARITY_AUDIT_LEDGER_PHASE1.md`, root **`README.md`**, **`Changelog.md`**, **`gradle.properties`**.

## Part F — Build / verify

Run:

```bat
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

**Sprint 104 result (this repo, after hygiene):** **BUILD SUCCESSFUL**; **`verifyFabricReleaseBundle`**: **OK** (**15** nested jars in **`FabricAuraSkills-2.3.131.jar`**; **canonical** path: **no** nested **`granitelib-fabric-*.jar`** when **`embedGraniteJij`** is default **false**).

## Remaining local state (after Sprint 104)

- **Tracked:** clean vs **`HEAD`** after index refresh + Sprint 104 doc commit.
- **Untracked:** **D** list above — intentional unless owner commits.
- **Bedrock/Geyser:** still **deferred** / **not** tested.

## Recommended next sprint

- **Sprint 105 — template-backed `unclaimed_items` prototype**, or  
- **Sprint 105 — GraniteLib disk / config-folder template loading**

Only after this hygiene baseline so **build metadata** and **`git status`** stay trustworthy.

---

### Appendix — Example `.git/info/exclude` block (optional)

```gitignore
# Sprint 104 — local scratch (clone only; see fabric/SPRINT_104_WORKING_TREE_CONTAINMENT.md)
docs/architecture/granitelib-acceptance-checklist.txt
docs/architecture/granitelib-dependency-cutover-results.md
docs/architecture/granitelib-examinable-packaging-fix.md
docs/architecture/granitelib-java-bedrock-smoke-results.md
docs/architecture/granitelib-manual-acceptance-matrix.md
docs/architecture/granitelib-no-jij-transition-results.md
docs/architecture/granitelib-release-prep-faq.md
docs/architecture/granitelib-runtime-smoke-transition-results.md
docs/architecture/granitelib-strict-server-transition-smoke-results.md
docs/handoffs/2026-07-11-granitelib-sprint-90-standalone-metadata-build-output.md
docs/handoffs/2026-07-12-granitelib-sprint-91-runtime-smoke-transition.md
docs/handoffs/2026-07-13-granitelib-sprint-92-strict-server-transition-smoke.md
docs/handoffs/2026-07-14-granitelib-sprint-93-examinable-packaging-fix.md
docs/handoffs/2026-07-15-granitelib-sprint-94-no-jij-transition-build.md
docs/handoffs/2026-07-16-granitelib-sprint-95-dependency-cutover.md
docs/handoffs/2026-07-17-granitelib-sprint-96-java-bedrock-smoke.md
docs/handoffs/2026-07-18-granitelib-sprint-97-manual-acceptance-pass.md
docs/handoffs/2026-07-19-granitelib-sprint-98-release-prep-faq.md
fabric/MOD_COMMANDS.txt
fabric/SPRINT_SUMMARY_SPEC.md
```
