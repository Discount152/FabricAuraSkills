# ADR: GraniteLib package, module, and identity naming

| Field | Value |
|-------|--------|
| **Status** | **Partial / upstream** (not a full product-wide ADR on `main` yet) |
| **This repo (upstream `main`, post–Sprint 77)** | See table below |
| **Detail** | [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md) · [granitelib-distribution-adr.md](granitelib-distribution-adr.md) · [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) · [Sprint 77 handoff](../handoffs/2026-06-28-granitelib-sprint-77-module-rename-implementation.md) |

## Accepted on **this** repository (Sprint 77)

- **Gradle module ID** **`:granitelib-fabric`** with **`projectDir` → `slate-fabric/`** (directory name unchanged).
- **Artifact / nested JAR** name prefix for that library: **`granitelib-fabric-*.jar`** in the remapped mod JiJ.
- **No** `slate-fabric` Gradle `include` — replaced by the above.

## N/A on upstream **for now**

- **`:granitelib-platform`**, until a **`slate-platform/`** subproject exists and is included in `settings.gradle.kts`. *Fork/planning branches may already document two modules; that is not current upstream truth.*

## Still open (everywhere, unless decided elsewhere)

- **Package** root and **staged renames** — [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) (Sprint 80 blueprint; e.g. `dev.granitelib.*` or org policy).
- **Maven `group` /** publish coordinates.
- **Public** class renames and branding cleanup (inventoried in the rename plan for `:granitelib-fabric`).
- **Physical** rename of `slate-fabric/` — optional **Stage 5** in the rename plan, not the first move.
- **JiJ vs external** — **Proposed and documented** in [granitelib-distribution-adr.md](granitelib-distribution-adr.md) (Milestone D, Option C). External consumption remains a **future** implementation milestone.
- **License / notice** review before shipping a public “GraniteLib” artifact.

## Fork vs upstream (wording to reuse)

- **Fork / full planning** may describe **`:granitelib-fabric` + `:granitelib-platform`**, a two-nested-JAR verify matrix, and more nested JARs — that applies **only** to branches/repos that have **both** subprojects.  
- **Upstream** today: **one** GraniteLib Gradle module, **`:granitelib-fabric`**.

*Sprint 78: reconciled for honest upstream snapshot. Sprint 79: distribution ADR (Milestone D) added. Sprint 80: package / public class rename plan added.*
