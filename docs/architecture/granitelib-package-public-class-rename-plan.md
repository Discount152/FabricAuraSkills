# GraniteLib — package / public class rename plan (implementation blueprint)

| Field | Value |
|-------|--------|
| **Type** | Planning only — **no renames in the sprint that added this file** |
| **Date** | 2026-07-01 (Sprint 80) |
| **Scope** | [FabricAuraSkills](https://github.com/Discount152/FabricAuraSkills) `main`-shaped tree; **`:granitelib-fabric`** + consumers |
| **Prereq** | [granitelib-package-name-adr.md](granitelib-package-name-adr.md) · [granitelib-distribution-adr.md](granitelib-distribution-adr.md) (JiJ now, external later) · [Sprint 79 handoff](../handoffs/2026-06-30-granitelib-sprint-79-distribution-adr.md) |

---

## 1. Purpose

Turn the **partial** package/identity ADR into a **sequenced, reviewable** plan so a **future implementation milestone** can rename Java packages and public types **without** a single giant refactor (the “caffeinated raccoon with a refactor button” failure mode).

This document is the **blueprint**; the next sprint that **edits** `*.java` should follow these stages and re-audit inventories after large merges.

---

## 2. Current upstream naming state (audited, Sprint 80)

| Dimension | **Upstream `main` (this clone)** |
|-----------|-----------------------------------|
| **Gradle module** | **`:granitelib-fabric`** |
| **Directory** | **`slate-fabric/`** (name unchanged; `projectDir` mapping gives artifact identity) |
| **`slate-fabric/src/main/java`** | **1** file under **`dev.aurelium.slate.fabric`** (see inventory). |
| **`slate-fabric/src/test/java`** | **Not present** — no test sources under this module in this tree. |
| **Distribution** | **JiJ** embedded in FabricAuraSkills; [granitelib-distribution-adr.md](granitelib-distribution-adr.md) **Option C**. |
| **Public API** | **Embedded v1, evolving** — not a standalone SemVer library yet. |
| **Consumer imports** | No `import dev.aurelium.slate.fabric.…` found **outside** `slate-fabric/` in first-party `fabric/` / `api/` / `common/` (lib is a stub; [`:fabric` does not yet depend on it at source level in a way that would break on rename, until code references land). Re-run grep before any rename. |

**Reference (not in `:granitelib-fabric` source):** `vendor/Slate` holds **upstream Bukkit Slate** as a submodule; **out of scope** for GraniteLib’s first package pass unless the team explicitly subsumes that tree (different license/attribution story). Bukkit / API code under `dev.aurelium.auraskills` is the **mod product** — renames there are a **larger** product decision, not required to move with the first `slate-fabric` package change.

---

## 3. Target naming state (honest: not fully locked)

| Item | **Direction** |
|------|----------------|
| **Preferred package root (GraniteLib on Fabric)** | **`dev.granitelib.…`** (or **`org.…` / team domain** if legal/org policy picks otherwise — **not fully decided in Sprint 80**; confirm before first mass move). |
| **Avoid for new public API** | **`dev.aurelium.slate.…`** as the long-term **public** home for new GraniteLib types; “Slate” in **public** class names where it reads like product branding. |
| **Allow temporarily** | Historical packages under `slate-fabric` until a staged move completes. |
| **Sibling modules** | **`dev.aurelium.slate.platform…`** N/A on upstream (no `slate-platform/`) — any target tree is **future / fork** only. |

**Naming candidates (illustrative, not a commitment):**

| Current (illustrative) | Possible future |
|------------------------|-----------------|
| `dev.aurelium.slate.fabric` | `dev.granitelib.fabric` (or `dev.granitelib.fabric.bootstrap`, split by subpackage as the tree grows) |

Re-split subpackages (e.g. `…fabric.menu`, `…fabric.menu.render`) when the code exists — **not** invented in this inventory.

---

## 4. Package rename inventory (first-party `:granitelib-fabric` only)

**Source:** `slate-fabric/src/main/java` and `slate-fabric/src/test/java` (test directory **absent** here).

| Package root (current) | File count (main) | Suggested future root (illustrative) | Notes |
|------------------------|--------------------|--------------------------------------|--------|
| `dev.aurelium.slate.fabric` | 1 | `dev.granitelib.fabric` (or `dev.granitelib.fabric.bootstrap`) | Only package in module today. |

**Not listed:** `dev.aurelium.slate.menu`, `…platform` — **no** such packages under `slate-fabric/src` in this repository snapshot.

**When new files land** (e.g. from a feature branch), **re-run** this inventory before executing Stage 1.

---

## 5. Public class rename inventory (Slate / AuraSkills lineage)

Audit query: public top-level types under `slate-fabric` whose names suggest **Slate** or **AuraSkills** (or that will be **consumed** by `fabric` / other modules).

| Type | Found? | Future name (suggested) | Scope | Stage | Risk |
|------|--------|-------------------------|--------|--------|------|
| `SlateFabricBootstrap` | **Yes** | e.g. `GraniteFabricBootstrap` (or `GraniteLibFabricBootstrap`) | Public API entry / bootstrap | **Stage 3** (after package move + import fixes) or same PR as **Stage 1** if trivially small | Low today (no external callers) |
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
| Rename **`slate-fabric/`** → `granitelib-fabric/`? | **Not** in the **first** package/class rename implementation **unless** tooling or org policy **requires** path = package. **Gradle** already exposes **`:granitelib-fabric`**; physical rename is **cosmetic + churn** (IDE, git blame, backports). |
| **Later** (optional “filesystem cleanup” sprint) | **Stage 5** — after packages and call sites are stable, optionally align folder name with `projectDir` for contributor ergonomics, with a **single** mechanical commit and no logic change. |

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

**Merge strategy:** If Stage 1–3 stay **small** (current tree: **one** class), a **single** PR can still be *staged* in commits (package move commit, then class rename commit) for reviewability.

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

## 11. Non-goals (Sprint 80)

- **No** Java edits, renames, Gradle, YAML, or metadata in the sprint that **only** adds/updates this plan and handoff links.  
- **No** commitment to a specific `dev.granitelib` vs `org.*` final root without org sign-off.  
- **No** `vendor/Slate` rewrite as part of “Sprint 80” deliverable.

---

## 12. Recommended Sprint 81

**First execution slice (when product approves root + one bootstrap name):**

- Implement **Stages 1–3** for current **`slate-fabric` tree** (small: likely **one** class + any new files merged since this audit), then run the **Validation matrix** in one CI/local pass.  
- **Or** if the next merge adds **substantive** `slate-fabric` code from a long-lived branch, **Sprint 81 = re-audit inventories** in this file *then* apply the same stages.

**Parallel track (optional):** org/legal **package root** sign-off, or **license/notice** prep (Stage 6) before any public GraniteLib story grows.

## See also

- [Sprint 80 handoff](../handoffs/2026-07-01-granitelib-sprint-80-package-public-class-rename-plan.md)

*Sprint 80: plan only. Re-audit before implementation.*
