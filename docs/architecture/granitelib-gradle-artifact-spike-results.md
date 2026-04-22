# GraniteLib — Gradle artifact **spike results** (fork reference)

## Upstream `main` (Sprint 77)

- **This repository** merged the **module identity** work as a normal change (not a throwaway spike branch in the sense of the original GraniteLib planning cycle).
- **Implemented:** **`:granitelib-fabric`**, `projectDir` → `slate-fabric/`, `archiveBaseName` `granitelib-fabric`, JiJ + **`verifyFabricReleaseBundle`** for **`granitelib-fabric-*.jar`**, **`projectVersion` 2.3.19**.  
- **14** nested JARs observed in **`FabricAuraSkills-2.3.19.jar`** in validation (not 17 — fork two-module trees can differ).
- **Not implemented here:** **`:granitelib-platform`**, `granitelib-platform-*.jar` checks — no `slate-platform/` in tree.

**Authoritative upstream summary:** [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md) and [Sprint 77 handoff](../handoffs/2026-06-28-granitelib-sprint-77-module-rename-implementation.md).

## Fork / two-module spike (historical)

Branches that included **`slate-fabric` + `slate-platform`** may report:

- `verify` for both **`granitelib-fabric-*.jar`** and **`granitelib-platform-*.jar`**
- **17** (or other) nested JAR counts in the remapped mod

That is **not** claimed for upstream `main` here.

*Sprint 78: aligned wording.*
