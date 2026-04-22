# ADR: GraniteLib package, module, and identity naming

| Field | Value |
|-------|--------|
| **Status** | **Partial / upstream** (not a full product-wide ADR on `main` yet) |
| **This repo (upstream `main`, post–Sprint 77)** | See table below |
| **Detail** | [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md) · [granitelib-distribution-adr.md](granitelib-distribution-adr.md) · [granitelib-standalone-dependency-identity-adr.md](granitelib-standalone-dependency-identity-adr.md) (Sprint 88) · [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) · [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md) · [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) · [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) · [granitelib-branding-metadata-cleanup-plan.md](granitelib-branding-metadata-cleanup-plan.md) (Sprint 85–88) · [Sprint 77 handoff](../handoffs/2026-06-28-granitelib-sprint-77-module-rename-implementation.md) · [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md) |

## Accepted on **this** repository (Sprint 77)

- **Gradle module ID** **`:granitelib-fabric`** with **`projectDir` → `slate-fabric/`** (directory name unchanged).
- **Artifact / nested JAR** name prefix for that library: **`granitelib-fabric-*.jar`** in the remapped mod JiJ.
- **No** `slate-fabric` Gradle `include` — replaced by the above.

## Accepted (Sprint 81) — **`:granitelib-fabric` Java surface**

- **Package root (Fabric):** **`dev.granitelib.fabric`** (replaces `dev.aurelium.slate.fabric` for the current stub tree).
- **Bootstrap class:** **`GraniteFabricBootstrap`** (replaces `SlateFabricBootstrap`).

**Still not decided here:** `Maven` coordinates, subpackages as the port grows. **On-disk folder** `slate-fabric/`: [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md) — **Proposed: defer** physical rename to `granitelib-fabric/` (Sprint 82).

## N/A on upstream **for now**

- **`:granitelib-platform`**, until a **`slate-platform/`** subproject exists and is included in `settings.gradle.kts`. *Fork/planning branches may already document two modules; that is not current upstream truth.*

## Still open (everywhere, unless decided elsewhere)

- **Deeper** `dev.granitelib.…` subpackages and additional public types as the port lands — [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md).
- **Maven `group` /** publish coordinates.
- **Further** public class branding (new types, not the Sprint 81 bootstrap) when the API surface grows.
- **Physical** rename of `slate-fabric/` — optional **Stage 5**; **default timing:** defer per [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md).
- **JiJ vs external** — [granitelib-distribution-adr.md](granitelib-distribution-adr.md) (Milestone D) + [granitelib-standalone-dependency-identity-adr.md](granitelib-standalone-dependency-identity-adr.md) (Sprint 88: **proposed** Fabric mod **`id` `granitelib`**, **external** as **near-term** **goal**; **implementation** Sprint 89+).
- **License / notice** — [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) (Sprint 83 audit checklist); **full** sign-off before shipping a public “GraniteLib” artifact or stripping **Slate**/**AuraSkills** **attribution** in **vendor** / **legal** text.

## Fork vs upstream (wording to reuse)

- **Fork / full planning** may describe **`:granitelib-fabric` + `:granitelib-platform`**, a two-nested-JAR verify matrix, and more nested JARs — that applies **only** to branches/repos that have **both** subprojects.  
- **Upstream** today: **one** GraniteLib Gradle module, **`:granitelib-fabric`**.

*Sprint 78: reconciled for honest upstream snapshot. Sprint 79: distribution ADR (Milestone D) added. Sprint 80: package / public class rename plan added. Sprint 81: `dev.granitelib.fabric` + `GraniteFabricBootstrap` landed. Sprint 82: physical directory rename decision (default defer). Sprint 83: license/notice review plan. Sprint 84: draft root NOTICE / attribution strategy (no file at repo root). Sprint 85: branding & metadata cleanup plan (product-facing vs legal surfaces; docs only). Sprint 88: [standalone dependency identity ADR](granitelib-standalone-dependency-identity-adr.md) — **proposed** Loader **`id`** for standalone lib.*
