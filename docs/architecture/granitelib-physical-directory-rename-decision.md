# Decision: Physical directory for `:granitelib-fabric` (`slate-fabric/` vs `granitelib-fabric/`)

| Field | Value |
|-------|--------|
| **Status** | **Proposed** (product/maintainer sign-off) |
| **Date** | 2026-07-03 (Sprint 82) |
| **Scope** | On-disk folder that backs Gradle project **`:granitelib-fabric`** |
| **Related** | [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) (Stage 5) · [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) (Sprint 83) · [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md) · [Sprint 82 handoff](../handoffs/2026-07-03-granitelib-sprint-82-directory-rename-decision.md) |

---

## Context

After **Sprint 77–81**, **product identity** is already clear in the places that affect consumers and bytecode:

- **Gradle** project path: **`:granitelib-fabric`**
- **Nested JAR** / verify: **`granitelib-fabric-*.jar`**
- **Java** package: **`dev.granitelib.fabric`**
- **Bootstrap** class: **`GraniteFabricBootstrap`**

The **filesystem** folder is still named **`slate-fabric/`**, wired with `project(":granitelib-fabric").projectDir = file("slate-fabric")` in **`settings.gradle.kts`**.

This document answers: **Is renaming the folder to `granitelib-fabric/` worth the churn now, later, or never?**

---

## Audit: where `slate-fabric` / `granitelib-fabric` appear (Sprint 82)

| Category | `slate-fabric` | `granitelib-fabric` (string) |
|----------|----------------|------------------------------|
| **Build-required** | **`settings.gradle.kts`:** `projectDir = file("slate-fabric")` (both full and `fabricOnly` include blocks). Module build file lives at **`slate-fabric/build.gradle.kts`** (path is the directory). | **`settings.gradle.kts`:** `include("granitelib-fabric")`. **`fabric/build.gradle.kts`:** `project(":granitelib-fabric", …)`, JiJ `include`, **`verifyFabricReleaseBundle`** `granitelib-fabric-*.jar`. **`slate-fabric/build.gradle.kts`:** `archiveBaseName` `granitelib-fabric`. |
| **Docs (current)** | Many `docs/architecture/*`, handoffs, embed contract, verification — describe “on-disk `slate-fabric/`”. | Same docs: module id and nested JAR name. |
| **Docs (historical)** | Older handoffs (e.g. Sprint 77/78/80) frozen in time. | Same. |
| **Changelog** | Indirect (2.3.19/2.3.20 entries refer to module, not folder name in path). | Yes (module / JAR wording). |
| **CI / scripts** | **No** matches under **`.github/`** for `slate-fabric` in this audit. | Task names use `:granitelib-fabric` in docs. |
| **Generated** | N/A for path string. | Build outputs use artifact name from `archiveBaseName`. |

**If the directory is renamed to `granitelib-fabric/`**, a minimal implementation would touch at least:

1. **`git mv slate-fabric granitelib-fabric`** (preserve history with `git mv` or explicit rename).
2. **`settings.gradle.kts`:** both `projectDir = file("slate-fabric")` → `file("granitelib-fabric")`.
3. **`docs/**`** including architecture + handoffs: replace path examples where they mean the **current** tree (keep historical PDFs/snapshots if the team keeps them immutable).
4. **Grep** for `slate-fabric` again (including `fabric/MENU_AND_COMMANDS_PORT.md` and any wiki if present later).

**Do not** confuse: the **artifact** is already **`granitelib-fabric-*.jar`**; renaming the **folder** does not change that name unless `archiveBaseName` is also edited (not required for directory alignment).

---

## Options considered

### Option A — Keep `slate-fabric/` indefinitely

| Pros | Cons |
|------|------|
| No filesystem or `projectDir` churn | Top-level folder name still says “Slate” |
| `projectDir` mapping already works | Possible contributor confusion vs `dev.granitelib` code |
| No broken local bookmarks / IDE roots | “Breadcrumb” mismatch until explained in docs |

### Option B — Rename now (`slate-fabric/` → `granitelib-fabric/`)

| Pros | Cons |
|------|------|
| Tree matches **`:granitelib-fabric`** naming | Mechanical churn across docs; git diff noise |
| Removes one visible Slate string at repo root | Cursor/IDE index may need re-open; personal scripts may break |
| Slightly simpler mental model for new contributors | Low **functional** gain: Gradle/module/package already aligned |

### Option C — **Defer** until more code lands or a **public-breadcrumb / pre-release** pass

| Pros | Cons |
|------|------|
| Avoids churn while the port is still small / fast-moving | Folder name stays historical longer |
| Can bundle with **license/notice**, **docs site**, **external** story | Same confusion risk until then |
| Lets **Sprint 81** package identity “settle” first | — |

---

## Recommendation

**Option C — Defer** the physical directory rename **unless** an audit finds **concrete** path pain (broken automation, org policy “path must match module,” or contributor tickets).

**Rationale:**

- **`:granitelib-fabric`**, **`dev.granitelib.fabric`**, and **`granitelib-fabric-*.jar`** already establish **product and build** identity.
- The **folder** name is **cosmetic** for builds: only **`settings.gradle.kts`** and doc paths must change; no runtime or JiJ behavior improvement.
- Higher-value follow-ups: **license/notice**, **external** distribution prep per [granitelib-distribution-adr.md](granitelib-distribution-adr.md), **more GraniteLib code** and subpackages — then revisit one **mechanical** rename PR or bundle with a **breadcrumb cleanup** milestone.

**If “never” (Option A):** keep documenting that **`projectDir` → `slate-fabric/`** is a **historical** folder name, like keeping an old street address after rebranding the store.

---

## Implementation plan **if** accepted later (do not execute in Sprint 82)

1. **Branch** off `main`; notify contributors with local clones.
2. **`git mv slate-fabric granitelib-fabric`**
3. **`settings.gradle.kts`:** update **both** `file("slate-fabric")` → `file("granitelib-fabric")` (and any comment that says `slate-fabric/`).
4. **Docs / handoffs / README** (as applicable): path strings to **`granitelib-fabric/...`**
5. **Stale search:** `rg "slate-fabric" --glob '!**/historical/**'` (or project convention); fix **current** references.
6. **Validation matrix** (same as rename plan):  
   `.\gradlew.bat :granitelib-fabric:test` · `:fabric:test` · `:fabric:build -PfabricOnly=true`  
   + `verifyFabricReleaseBundle: OK` · dependency edges unchanged.
7. **Single** focused PR; **no** Java package edits in the same change unless required (prefer **directory-only** diff).

**Rollback:** `git revert` the merge commit or reverse `git mv`.

---

## Non-goals (Sprint 82)

- **No** directory rename, **no** `settings.gradle.kts` edit, **no** Java, YAML, or metadata in the sprint that **only** adds this decision + doc links.

## Risks (of doing the rename in a **future** sprint)

| Risk | Mitigation |
|------|------------|
| Missed doc path in wiki/submodule | Re-run `rg slate-fabric` on full tree |
| Contributors with old path in notes | Changelog + short dev note |
| Merging with long-lived branch that touched `slate-fabric/` | Rebase/merge carefully; `git mv` can conflict |

## Validation matrix (no change required after Sprint 82 docs-only)

After **any future** implement sprint that renames the folder, run the matrix in [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md#9-validation-matrix-run-after-any-rename-implementation). Until then, current **2.3.21** build expectations apply.

## Recommended follow-ups (post–Sprint 82)

- **Sprint 83 (done, docs):** [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) — **license/notice** audit plan before **breadcrumb** or **external** work.  
- **Later:** **If** the team **accepts** a **physical** rename: **execute** the implementation plan in one PR (after sign-off), or **bundle** with a **breadcrumb / pre-release** epic.  
- **Or:** **GraniteLib** feature port; **re-run** the license plan before **public** **Maven** publish.

*Sprint 82: decision + audit only.*
