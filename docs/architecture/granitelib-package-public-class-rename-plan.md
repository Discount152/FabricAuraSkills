# GraniteLib — package / public class rename plan (implementation blueprint)

| Field | Value |
|-------|--------|
| **Type** | Implementation blueprint; **Sprint 81** executed the first Stages 1–3 for the current stub. |
| **Date** | 2026-07-01 (Sprint 80 plan); **2026-07-02 (Sprint 81 first rename)** |
| **Scope** | [FabricAuraSkills](https://github.com/Discount152/FabricAuraSkills) `main`-shaped tree; **`:granitelib-fabric`** + consumers |
| **Prereq** | [granitelib-package-name-adr.md](granitelib-package-name-adr.md) · [granitelib-distribution-adr.md](granitelib-distribution-adr.md) (JiJ now, external later) · [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md) (Sprint 82) · [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) (Sprint 83) · [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) (Sprint 84) · [Sprint 79 handoff](../handoffs/2026-06-30-granitelib-sprint-79-distribution-adr.md) · [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md) |

---

## 1. Purpose

Turn the **partial** package/identity ADR into a **sequenced, reviewable** plan so a **future implementation milestone** can rename Java packages and public types **without** a single giant refactor (the “caffeinated raccoon with a refactor button” failure mode).

This document is the **blueprint**; re-audit inventories after **large merges**; future types follow the same stages.

### 2.1 After Sprint 81 (current snapshot)

| Dimension | **Upstream** |
|-----------|-------------|
| **Gradle module** | **`:granitelib-fabric`** |
| **Directory** | **`slate-fabric/`** (unchanged) |
| **`slate-fabric/src/main/java`** | **1** file, package **`dev.granitelib.fabric`**, class **`GraniteFabricBootstrap`**. |
| **Prior (historical)** | `dev.aurelium.slate.fabric` / `SlateFabricBootstrap` — **removed in Sprint 81** (see [Changelog.md](../../Changelog.md) **2.3.20**). |
| **`slate-fabric/src/test/java`** | **Not present** — no tests in module yet. |
| **Consumer imports** | No first-party code outside `slate-fabric` required updates (no call sites) — **re-run** `rg` when consumers appear. |

### 2.2 Before Sprint 81 (archived, Sprint 80 audit)

| Dimension | **Was true** |
|-----------|-------------|
| **Package** | `dev.aurelium.slate.fabric` |
| **Class** | `SlateFabricBootstrap` |
| **Consumer imports** | None outside the module. |

**Reference (not in `:granitelib-fabric` source):** `vendor/Slate` holds **upstream Bukkit Slate** as a submodule; **out of scope** for GraniteLib’s first package pass unless the team explicitly subsumes that tree (different license/attribution story). Bukkit / API code under `dev.aurelium.auraskills` is the **mod product** — renames there are a **larger** product decision, not required to move with the first `slate-fabric` package change.

---

## 3. Target naming state

| Item | **Direction** |
|------|----------------|
| **Adopted root (Sprint 81) for `slate-fabric` bootstrap** | **`dev.granitelib.fabric`**. |
| **Further** subpackages (e.g. `…menu`, `…menu.render`) | Add when the port lands; not locked here. |
| **Avoid for new public API** in GraniteLib | `dev.aurelium.slate.…` for **new** first-party code in this module; **vendor** `dev.aurelium.slate` in `vendor/Slate` stays until a separate policy. |
| **Sibling modules** | **`dev.aurelium.slate.platform…`** N/A on upstream (no `slate-platform/`). |

| Previous | **Now (Sprint 81)** |
|----------|---------------------|
| `dev.aurelium.slate.fabric` | `dev.granitelib.fabric` |

Re-split subpackages (e.g. `…fabric.menu`, `…fabric.menu.render`) when the code exists — **not** invented in this inventory.

---

## 4. Package rename inventory (first-party `:granitelib-fabric` only)

**Source:** `slate-fabric/src/main/java` and `slate-fabric/src/test/java` (test **absent**).

| Package root (current) | File count (main) | Status |
|------------------------|--------------------|--------|
| `dev.granitelib.fabric` | 1 | **Landed (Sprint 81)**. |

**Not listed yet:** `…menu`, `…platform` under `slate-fabric` — re-run inventory when new packages appear.

---

## 5. Public class rename inventory (Slate / AuraSkills lineage)

Audit query: public top-level types under `slate-fabric` whose names suggest **Slate** or **AuraSkills** (or that will be **consumed** by `fabric` / other modules).

| Type | Found? | Future name (suggested) | Scope | Stage | Risk |
|------|--------|-------------------------|--------|--------|------|
| `GraniteFabricBootstrap` (was `SlateFabricBootstrap`) | **Current** | — | Public bootstrap | **Landed (Sprint 81)** | — |
| `SlateFabricMenuRegistry` | **N/A** (not in tree) | e.g. `GraniteFabricMenuRegistry` if introduced | public-ish | TBD | — |
| `SlatePortManifest` | **N/A** | TBD | public-ish | TBD | — |
| `FabricSlateGroupSlotMath` | **N/A** | e.g. `GraniteGroupSlotMath` or `FabricMenuGroupSlotMath` | public-ish / util | TBD | — |
| `SlateMenuRuntime` | **N/A** | e.g. `GraniteMenuRuntime` | public-ish | TBD | — |
| `AuraSkillsPackagedMenuIds` | **N/A** (not under `slate-fabric`) | e.g. mod-owned `…PackagedMenuIds` vs Discount branding — **product** call | TBD | TBD | **High** if mod surface |

**Rule:** Re-audit with `rg` / IDE symbol search on the branch you ship; do **not** treat this table as exhaustive for other branches.

---

## 6. Internal class rename inventory

| Finding | Recommendation |
|--------|----------------|
| **No** non-public top-level types under `slate-fabric` in this snapshot. | When helpers appear, default: rename in **same** stage as the **public** surface of that subpackage, or **one stage later** if they are package-private to a single package. |
| `vendor/Slate` | Treat as **vendor** — renames are **separate** (license, diff noise). |

---

## 7. Directory rename stance

| Question | **Recommendation** |
|----------|---------------------|
| Rename **`slate-fabric/`** → `granitelib-fabric/`? | **Sprint 82 decision (Proposed default):** **defer** — see [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md). **Gradle** already exposes **`:granitelib-fabric`**; physical rename is **cosmetic + churn** unless product wants breadcrumb alignment. |
| **Later** (optional “filesystem cleanup” sprint) | **Stage 5** — if accepted: **`git mv` + `settings.gradle.kts` `projectDir` + doc path sweep**; one mechanical PR, no logic change. |

---

## 8. Migration staging (recommended)

Avoid one “rename everything” PR. Suggested order:

| Stage | What | Rationale |
|------|------|-----------|
| **1** | **Package renames** inside **`slate-fabric`** `main` (and add `test` when it exists) only; update **internal** relative imports. | Smallest surface; `:granitelib-fabric` compiles. |
| **2** | Update **`:fabric`** (and any other module) **imports** of moved packages. | One-way dependency: consumers catch up after the lib. |
| **3** | **Public class renames** (e.g. `SlateFabricBootstrap` → `GraniteFabricBootstrap`) where names are part of **API** story. | Clear git history; can pair with 1+2 in one PR if the tree is still tiny. |
| **4** | **Docs / handoffs / Javadoc** / ADR “accepted” table — no `fabric.mod.json` **unless** product wants visible IDs. | Comms and contributor truth. |
| **5** | **Optional** physical **`slate-fabric/`** directory rename. | Isolated, low value until team cares. |
| **6** | **License / notice** review and **metadata** (Modrinth/Maven) per [granitelib-distribution-adr.md](granitelib-distribution-adr.md) **before** external publish. | Gate for “toolbox out of backpack.” |

**Sprint 81:** Stages **1–3** completed in one pass for the **one-class** tree (`dev.granitelib.fabric` + `GraniteFabricBootstrap`); **Stage 4** (this doc, ADR, [Changelog.md](../../Changelog.md)) in the same release.

**Merge strategy (future):** For larger trees, use separate commits (package, consumers, public class renames) for reviewability.

**Anti-pattern:** Renaming `vendor/Slate` and `slate-fabric` and `dev.aurelium.auraskills.fabric` in one PR.

---

## 9. Validation matrix (run after any rename implementation)

| Step | Command | Expected |
|------|---------|----------|
| 1 | `.\gradlew.bat :granitelib-fabric:test` | `BUILD SUCCESSFUL` (may be `NO-SOURCE` if no tests) |
| 2 | `.\gradlew.bat :fabric:test` | `BUILD SUCCESSFUL` |
| 3 | `.\gradlew.bat :fabric:build -PfabricOnly=true` | `BUILD SUCCESSFUL`; **`verifyFabricReleaseBundle: OK`**; nested **`granitelib-fabric-*.jar` present** |
| 4 | `.\gradlew.bat :fabric:dependencies --configuration compileClasspath` | Includes **`project :granitelib-fabric`**, not the reverse from lib |
| 5 | `.\gradlew.bat :granitelib-fabric:dependencies --configuration compileClasspath` | **No** `project :fabric` on compile classpath |

**Do not** run **`:granitelib-platform`** tasks — **module absent** on upstream.

---

## 10. Risk register

| Risk | Mitigation |
|------|------------|
| **IDE / bulk refactor** mistakes wrong module | Limit search scope to `slate-fabric/` (and then consumer dirs); review diff by module. |
| **Missed** generated code / docs / links | Grep for old package string and `Slate` in **docs**; CI optional: `rg` gate in a follow-up. |
| **Package-private** access break after split packages | Minimize new package boundaries in one move; or use bridge package-private in same `package` until split stabilizes. |
| **ServiceLoader** / `META-INF` / resource paths | N/A for current stub; re-check if SPI or `fabric.mod.json` `entrypoints` use class names. |
| **Tests** with old imports | Add tests with new packages as code lands. |
| **Docs** still say “Slate” for GraniteLib | Update architecture docs in Stage 4; keep **attribution** to Archy-X Slate where the doc refers to the **vendor** submodule. |
| **License** line removed during rename | Never drop copyright headers; re-run notice review in Stage 6. |
| **Root package** not final | Revisit ADR; avoid publishing **external** JARs until [granitelib-distribution-adr.md](granitelib-distribution-adr.md) prerequisites met. |

---

## 11. Non-goals (Sprint 80 doc sprint; Sprint 81 code sprint was separate)

- Sprint 80: **no** code edits when only the plan was added.  
- **No** `vendor/Slate` rewrite as part of GraniteLib module renames.  

---

## 12. Recommended Sprint 83+

- **Re-audit** this inventory when a branch adds **new** `slate-fabric` / **on-disk** `granitelib-fabric` source; apply **Stages 1–3** for new Slate-branded public types.  
- **Physical directory (Stage 5):** [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md) (Sprint 82) — default **defer**; **execute** only after sign-off.  
- **Stage 6** license/notice and external story when [granitelib-distribution-adr.md](granitelib-distribution-adr.md) prereqs are met — use [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) and [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) before stripping **Slate**/**vendor** **attribution** in comments or `vendor/`; **add** root `NOTICE` only after [§10 review](granitelib-notice-attribution-strategy.md#10-review-checklist-before-any-root-notice-is-added).

## See also

- [Sprint 80 handoff](../handoffs/2026-07-01-granitelib-sprint-80-package-public-class-rename-plan.md) (historical plan)  
- [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md) (landed renames + validation)  
- [Sprint 82 handoff](../handoffs/2026-07-03-granitelib-sprint-82-directory-rename-decision.md) (directory decision + audit)  
- [Sprint 83 handoff](../handoffs/2026-07-04-granitelib-sprint-83-license-notice-review-plan.md) (license/notice plan)  
- [Sprint 84 handoff](../handoffs/2026-07-05-granitelib-sprint-84-notice-attribution-strategy.md) (draft `NOTICE` strategy)

*Sprint 80: plan. Sprint 81: first code rename + 2.3.20. Sprint 82: physical directory decision (default defer). Sprint 83: license/notice review plan. Sprint 84: draft NOTICE/attribution strategy (no root file).*
