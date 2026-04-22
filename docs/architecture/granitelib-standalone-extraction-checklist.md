# GraniteLib — toward a standalone library (checklist, **upstream**)

**Purpose:** Map what would be needed for GraniteLib to ship **outside** the DiscountAuraSkills mod, **without** promising dates. **This file is not a build contract.**

**Prerequisite (conceptual):** [granitelib-v1-embed-contract.md](granitelib-v1-embed-contract.md).

## Upstream `main` **today** (Sprint 77+)

- **One** library Gradle module: **`:granitelib-fabric`**, on-disk path **`slate-fabric/`** (or future **`granitelib-fabric/`** if [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md) is accepted later), JiJ’d into the remapped mod with nested **`granitelib-fabric-*.jar`**, **`projectVersion` 2.3.22**+.
- **Not present:** **`slate-platform/`**, **`:granitelib-platform`**. Any checklist line that assumed two modules is **fork / future** until that code lands.

## Future / fork (optional second module)

If **`slate-platform/`** is added: mirror the Sprint 77 pattern — **`include("granitelib-platform")` + `projectDir`**, `fabric` `implementation` + JiJ, **`verifyFabricReleaseBundle`** for `granitelib-platform-*.jar`. Validation matrices from two-module spikes apply **only** then.

## Build / product items (unchanged in spirit, shortened)

- [ ] **Distribution / standalone identity + build plan:** [granitelib-distribution-adr.md](granitelib-distribution-adr.md) (Milestone D) + [granitelib-standalone-dependency-identity-adr.md](granitelib-standalone-dependency-identity-adr.md) (Sprint 88) + [granitelib-standalone-build-metadata-plan.md](granitelib-standalone-build-metadata-plan.md) (Sprint 89 — **where** `fabric.mod.json` lives, **version** **split**, **JiJ** / **`depends`**, **validation**). **Implementation** **Sprint 90+**.
- [x] **First** package / bootstrap rename — [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) / [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md) — **`dev.granitelib.fabric`**, **`GraniteFabricBootstrap`** (stages 1–3 for current tiny tree).  
- [ ] **Further** renames and subpackages as the port grows (same plan; re-audit on merge).  
- [ ] **API** surface / SemVer (embed contract + ADR).
- [ ] **License / notice** — [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) (Sprint 83) + [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) (Sprint 84 draft `NOTICE` **strategy**; **add** real `NOTICE` only after sign-off).  
- [ ] **Branding / product metadata** (separate from legal) — [granitelib-branding-metadata-cleanup-plan.md](granitelib-branding-metadata-cleanup-plan.md) (Sprint 85 inventory; implementation **after** sign-off, coordinated with [granitelib-distribution-adr.md](granitelib-distribution-adr.md) for external story).  
- [ ] **CI** for one-way `:fabric` → lib edges.

Cross-link: [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md), [granitelib-package-name-adr.md](granitelib-package-name-adr.md), [granitelib-distribution-adr.md](granitelib-distribution-adr.md), [granitelib-standalone-dependency-identity-adr.md](granitelib-standalone-dependency-identity-adr.md), [granitelib-standalone-build-metadata-plan.md](granitelib-standalone-build-metadata-plan.md), [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md), [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md), [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md), [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md), [granitelib-branding-metadata-cleanup-plan.md](granitelib-branding-metadata-cleanup-plan.md).

*Sprint 78: stub replaced with upstream–fork split. Sprint 79: distribution ADR (Milestone D). Sprint 80: package/public class rename plan. Sprint 81: first package + class rename in code. Sprint 82: physical directory decision (default defer). Sprint 83: license/notice review plan. Sprint 84: draft NOTICE / attribution strategy (no root file). Sprint 85: branding & metadata cleanup plan (docs only, no product file edits). Sprint 86–87: README, Gradle, `fabric.mod.json` (non-`id`/`license`) branding. Sprint 88: standalone **dependency identity** ADR. Sprint 89: [standalone build/metadata plan](granitelib-standalone-build-metadata-plan.md) (planning only).*
