# GraniteLib — module ID implementation (Sprint 77, **upstream**)

| Field | Value |
|-------|--------|
| **Handoff** | [2026-06-28 handoff](../handoffs/2026-06-28-granitelib-sprint-77-module-rename-implementation.md) |
| **Status** | Landed: Gradle project **`:granitelib-fabric`**, `projectDir` = `slate-fabric/`, `projectVersion` **2.3.19** |

## What is true on **this** repo (upstream `main` after merge)

- **Gradle** includes **`:granitelib-fabric`** with **`project(":granitelib-fabric").projectDir = file("slate-fabric")`**. The directory is still named **`slate-fabric/`** (no physical rename).
- **`:fabric` depends** on `project(":granitelib-fabric", configuration = "namedElements")` and **JiJ-embeds** the library; nested JAR name prefix is **`granitelib-fabric-*.jar`**; **`slate-fabric/build.gradle.kts`** sets `archiveBaseName` to `granitelib-fabric`.
- **`verifyFabricReleaseBundle`** requires a nested **`granitelib-fabric-*.jar`**. (Observed: **14** nested JARs in **`FabricAuraSkills-2.3.19.jar`** in Sprint 77 validation; count can shift if dependencies change.)
- **No** `slate-platform/` and **no** **`:granitelib-platform`** in this monorepo **today** — that is not an omission in the handoff; upstream never had the second subproject.
- **No** Java package, class, directory, or YAML renames in Sprint 77 — only Gradle/JiJ/verification and version bump.

## Fork / planning branches (not asserted as upstream)

Some forks or long-lived feature branches add **`slate-platform/`** and a second module **`:granitelib-platform`**. A **two-module** JiJ/verify matrix and **17** nested JARs can apply there only. **Do not** copy fork-only lines into upstream docs as if `main` has two modules.

**Follow-up on upstream (when / if `slate-platform` exists):** add `include("granitelib-platform")` + `projectDir`, wire `fabric` + JiJ + `requireJar` for `granitelib-platform-*.jar`, re-run a full graph.

## Related (this doc tree)

- [granitelib-verification-report.md](granitelib-verification-report.md) — Gradle graph + verify snapshot  
- [granitelib-package-name-adr.md](granitelib-package-name-adr.md) — ADR status on upstream  
- [granitelib-standalone-extraction-checklist.md](granitelib-standalone-extraction-checklist.md) — single-module vs two-module  
- [granitelib-v1-embed-contract.md](granitelib-v1-embed-contract.md) — v1 “embedded in mod” story  

**Doc pass:** [Sprint 78 handoff](../handoffs/2026-06-29-granitelib-sprint-78-upstream-doc-reconciliation.md) — stubs reconciled to this file’s upstream/fork split.
