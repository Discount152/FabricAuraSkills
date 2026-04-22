# ADR: GraniteLib package, module, and identity naming

| Field | Value |
|-------|--------|
| **Status** | **Partial / upstream** (not a full product-wide ADR on `main` yet) |
| **This repo (upstream `main`, post–Sprint 77)** | See table below |
| **Detail** | [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md) · [Sprint 77 handoff](../handoffs/2026-06-28-granitelib-sprint-77-module-rename-implementation.md) |

## Accepted on **this** repository (Sprint 77)

- **Gradle module ID** **`:granitelib-fabric`** with **`projectDir` → `slate-fabric/`** (directory name unchanged).
- **Artifact / nested JAR** name prefix for that library: **`granitelib-fabric-*.jar`** in the remapped mod JiJ.
- **No** `slate-fabric` Gradle `include` — replaced by the above.

## N/A on upstream **for now**

- **`:granitelib-platform`**, until a **`slate-platform/`** subproject exists and is included in `settings.gradle.kts`. *Fork/planning branches may already document two modules; that is not current upstream truth.*

## Still open (everywhere, unless decided elsewhere)

- **Package** root (e.g. `dev.granitelib.*` vs staying under `dev.aurelium.slate.*`).
- **Maven `group` /** publish coordinates.
- **Public** class renames and branding cleanup.
- **Physical** rename of `slate-fabric/` to match a future folder convention (optional, separate from Gradle `projectDir`).
- **JiJ vs external** consumer dependency (Milestone D–style product decision).
- **License / notice** review before shipping a public “GraniteLib” artifact.

## Fork vs upstream (wording to reuse)

- **Fork / full planning** may describe **`:granitelib-fabric` + `:granitelib-platform`**, a two-nested-JAR verify matrix, and more nested JARs — that applies **only** to branches/repos that have **both** subprojects.  
- **Upstream** today: **one** GraniteLib Gradle module, **`:granitelib-fabric`**.

*Sprint 78: reconciled for honest upstream snapshot.*
