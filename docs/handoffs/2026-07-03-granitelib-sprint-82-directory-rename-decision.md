# GraniteLib Sprint 82 — Physical directory rename (`slate-fabric/` vs `granitelib-fabric/`)

**Date:** 2026-07-03  
**Type:** **Documentation / decision** (no `git mv`, no Gradle/Java/YAML change in Sprint 82)

## Decision outcome

- **Recorded in:** [granitelib-physical-directory-rename-decision.md](../architecture/granitelib-physical-directory-rename-decision.md) — **Status: Proposed**
- **Recommended default: Option C — Defer** physical rename until a public-breadcrumb / pre-release pass or more GraniteLib code, **unless** concrete path pain appears.

**Rationale (short):** Module id **`:granitelib-fabric`**, package **`dev.granitelib.fabric`**, and nested JAR **`granitelib-fabric-*.jar`** already define identity; folder rename is mostly **cosmetic churn** (settings + docs + git noise).

## Search / audit results (summary)

| Kind | Findings |
|------|----------|
| **Build** | `settings.gradle.kts`: `projectDir = file("slate-fabric")` (2×). `slate-fabric/build.gradle.kts` at old path. `fabric/build.gradle.kts`: `:granitelib-fabric` + JiJ + verify. |
| **CI** | No `slate-fabric` in `.github/` in this scan. |
| **Docs** | Many current path references; handoffs 77–81 historical. |
| **Changelog** | Module/JAR language; not a path list. |

Full tables: [granitelib-physical-directory-rename-decision.md](../architecture/granitelib-physical-directory-rename-decision.md#audit-where-slate-fabric--granitelib-fabric-appear-sprint-82).

## Recommended timing

- **Revisit** when: **(a)** preparing **public** repo branding / **external** artifact story, **(b)** a **broad** doc breadcrumb cleanup, or **(c)** the **`slate-fabric/`** tree grows enough that new contributors keep tripping on the name.
- **Until then:** keep `projectDir = file("slate-fabric")` and document the folder as **historical** on disk.

## Implementation plan if accepted later

- See decision doc: **`git mv` → `settings.gradle.kts` → docs `rg` → full validation matrix** in one PR.

## Behavior preservation (Sprint 82)

- **No** runtime, build, or path changes from this sprint alone.

## Guardrail ledger

- No directory rename, no Gradle, no Java, no metadata, no JiJ removal, no license stripping.

## Validation

- **Docs-only sprint;** Gradle optional. If run: expect **BUILD SUCCESSFUL**, **`verifyFabricReleaseBundle: OK`**, **FabricAuraSkills-2.3.20.jar** (unchanged by Sprint 82).

## Recommended Sprint 83

- **Execute** folder rename **if** maintainers **reject** deferral and want alignment **now** — use the decision doc’s implementation plan.  
- **Otherwise:** **license/notice**, **external** distribution prep, or **feature** work in `dev.granitelib.…` before spending churn on the folder name.

**Done:** this handoff + [granitelib-physical-directory-rename-decision.md](../architecture/granitelib-physical-directory-rename-decision.md).
