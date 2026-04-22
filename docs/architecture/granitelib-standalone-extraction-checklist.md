# GraniteLib — toward a standalone library (checklist, **upstream**)

**Purpose:** Map what would be needed for GraniteLib to ship **outside** the DiscountAuraSkills mod, **without** promising dates. **This file is not a build contract.**

**Prerequisite (conceptual):** [granitelib-v1-embed-contract.md](granitelib-v1-embed-contract.md).

## Upstream `main` **today** (Sprint 77+)

- **One** library Gradle module: **`:granitelib-fabric`**, on-disk path **`slate-fabric/`**, JiJ’d into the remapped mod with nested **`granitelib-fabric-*.jar`**, **`projectVersion` 2.3.20**+.
- **Not present:** **`slate-platform/`**, **`:granitelib-platform`**. Any checklist line that assumed two modules is **fork / future** until that code lands.

## Future / fork (optional second module)

If **`slate-platform/`** is added: mirror the Sprint 77 pattern — **`include("granitelib-platform")` + `projectDir`**, `fabric` `implementation` + JiJ, **`verifyFabricReleaseBundle`** for `granitelib-platform-*.jar`. Validation matrices from two-module spikes apply **only** then.

## Build / product items (unchanged in spirit, shortened)

- [ ] **Distribution:** [granitelib-distribution-adr.md](granitelib-distribution-adr.md) (Milestone D — **Proposed:** **JiJ now, external later**; implementation deferred).
- [x] **First** package / bootstrap rename — [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) / [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md) — **`dev.granitelib.fabric`**, **`GraniteFabricBootstrap`** (stages 1–3 for current tiny tree).  
- [ ] **Further** renames and subpackages as the port grows (same plan; re-audit on merge).  
- [ ] **API** surface / SemVer (embed contract + ADR).
- [ ] **CI** for one-way `:fabric` → lib edges.

Cross-link: [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md), [granitelib-package-name-adr.md](granitelib-package-name-adr.md), [granitelib-distribution-adr.md](granitelib-distribution-adr.md), [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md).

*Sprint 78: stub replaced with upstream–fork split. Sprint 79: distribution ADR (Milestone D). Sprint 80: package/public class rename plan. Sprint 81: first package + class rename in code.*
