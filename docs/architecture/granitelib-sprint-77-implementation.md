# GraniteLib — module ID implementation (Sprint 77, **upstream**)

| Field | Value |
|-------|--------|
| **Handoff** | [2026-06-28 handoff](../handoffs/2026-06-28-granitelib-sprint-77-module-rename-implementation.md) |
| **Status** | Landed: Gradle project **`:granitelib-fabric`**, `projectDir` = `slate-fabric/`, `projectVersion` **2.3.21**+ (bump for packaging / identity work as released) |

## What is true on **this** repo (upstream `main` after merge)

- **Gradle** includes **`:granitelib-fabric`** with **`project(":granitelib-fabric").projectDir = file("slate-fabric")`**. The directory is still named **`slate-fabric/`**; **physical** rename to `granitelib-fabric/` is **optional and deferred** by default — [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md) (Sprint 82).
- **`:fabric` depends** on `project(":granitelib-fabric", configuration = "namedElements")` and **JiJ-embeds** the library; nested JAR name prefix is **`granitelib-fabric-*.jar`**; **`slate-fabric/build.gradle.kts`** sets `archiveBaseName` to `granitelib-fabric`.
- **`verifyFabricReleaseBundle`** requires a nested **`granitelib-fabric-*.jar`**. (Sprint 77: **14** nested JARs in **`FabricAuraSkills-2.3.19.jar`**; use current `projectVersion` for filename — e.g. **2.3.21**+ after Sprint 86.)
- **No** `slate-platform/` and **no** **`:granitelib-platform`** in this monorepo **today** — that is not an omission in the handoff; upstream never had the second subproject.
- **No** Java package, class, directory, or YAML renames in Sprint 77 — only Gradle/JiJ/verification and version bump. **Sprint 81** renamed the stub package/class in `:granitelib-fabric` to **`dev.granitelib.fabric`** / **`GraniteFabricBootstrap`** (see [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md)).

## Fork / planning branches (not asserted as upstream)

Some forks or long-lived feature branches add **`slate-platform/`** and a second module **`:granitelib-platform`**. A **two-module** JiJ/verify matrix and **17** nested JARs can apply there only. **Do not** copy fork-only lines into upstream docs as if `main` has two modules.

**Follow-up on upstream (when / if `slate-platform` exists):** add `include("granitelib-platform")` + `projectDir`, wire `fabric` + JiJ + `requireJar` for `granitelib-platform-*.jar`, re-run a full graph.

## Related (this doc tree)

- [granitelib-verification-report.md](granitelib-verification-report.md) — Gradle graph + verify snapshot  
- [granitelib-package-name-adr.md](granitelib-package-name-adr.md) — ADR status on upstream  
- [granitelib-distribution-adr.md](granitelib-distribution-adr.md) — Milestone D: JiJ vs external (**Proposed:** JiJ now, external later)  
- [granitelib-standalone-extraction-checklist.md](granitelib-standalone-extraction-checklist.md) — single-module vs two-module  
- [granitelib-v1-embed-contract.md](granitelib-v1-embed-contract.md) — v1 “embedded in mod” story  
- [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) — future package / public class staging  
- [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md) — on-disk `slate-fabric/` vs `granitelib-fabric/` (Sprint 82)  
- [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) — license / notice (Sprint 83)  

**Doc pass:** [Sprint 78 handoff](../handoffs/2026-06-29-granitelib-sprint-78-upstream-doc-reconciliation.md) — stubs reconciled to this file’s upstream/fork split. [Sprint 79 handoff](../handoffs/2026-06-30-granitelib-sprint-79-distribution-adr.md) — distribution ADR. [Sprint 80 handoff](../handoffs/2026-07-01-granitelib-sprint-80-package-public-class-rename-plan.md) — rename plan. [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md) — first package/class rename in `:granitelib-fabric`. [Sprint 82 handoff](../handoffs/2026-07-03-granitelib-sprint-82-directory-rename-decision.md) — physical directory decision. [Sprint 83 handoff](../handoffs/2026-07-04-granitelib-sprint-83-license-notice-review-plan.md) — license/notice plan.
