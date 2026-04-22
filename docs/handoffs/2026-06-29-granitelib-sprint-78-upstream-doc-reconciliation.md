# GraniteLib Sprint 78 — Post-merge doc reconciliation / upstream alignment

**Date:** 2026-06-29  
**Type:** **Documentation only** (no Java, Gradle, YAML, or runtime change)

## Objective

After Sprint 77, replace **stubs and vague pointers** with a **coherent, honest** story for **this** repository (Discount **upstream** `main` shape): **one** GraniteLib Gradle module **`:granitelib-fabric`**, **no** `slate-platform/`, **no** **`:granitelib-platform`**, **`projectVersion` 2.3.19**, **14** nested JARs in `verifyFabricReleaseBundle` validation for the remapped JAR. Clearly label **fork / planning** (two modules, 17 JARs, second `requireJar`, etc.) as **not** current upstream.

## Files reconciled

| File | What changed |
|------|----------------|
| [granitelib-sprint-77-implementation.md](../architecture/granitelib-sprint-77-implementation.md) | Tables + “upstream true” vs “fork not asserted” |
| [granitelib-verification-report.md](../architecture/granitelib-verification-report.md) | Graph, verify, 14 JARs, N/A for platform |
| [granitelib-package-name-adr.md](../architecture/granitelib-package-name-adr.md) | Partial / upstream, accepted vs N/A vs open |
| [granitelib-standalone-extraction-checklist.md](../architecture/granitelib-standalone-extraction-checklist.md) | One module vs two-module fork follow-up |
| [granitelib-v1-embed-contract.md](../architecture/granitelib-v1-embed-contract.md) | v1 = JiJ in mod, no package rename in S77 |
| [granitelib-gradle-artifact-spike-plan.md](../architecture/granitelib-gradle-artifact-spike-plan.md) | Historical spike; upstream already implemented one module |
| [granitelib-gradle-artifact-spike-results.md](../architecture/granitelib-gradle-artifact-spike-results.md) | Fork two-module vs upstream one-module counts |
| [granitelib-renderer-facade.md](../architecture/granitelib-renderer-facade.md) | Design scope; S77 Gradle-only |
| [granitelib-layout-definition-alignment.md](../architecture/granitelib-layout-definition-alignment.md) | Audit scope; one module |

## Upstream truth (short)

- **`:granitelib-fabric`**, `projectDir` `slate-fabric/`, nested **`granitelib-fabric-*.jar`**, `FabricAuraSkills-2.3.19.jar`, 14 nested JARs, no second lib module in repo.

## Fork / planning differences (short)

- Two-module matrices, `granitelib-platform-*.jar` checks, 17 JARs — only where **`slate-platform`** exists in the build.

## Guardrails (preserved)

- No source, build, or YAML edits in Sprint 78.

## Validation (optional, docs sprint)

- Not required. If run: `.\gradlew.bat :granitelib-fabric:test`, `.\gradlew.bat :fabric:test`, `.\gradlew.bat :fabric:build -PfabricOnly=true` — expect **BUILD SUCCESSFUL** and **`verifyFabricReleaseBundle: OK`** for `FabricAuraSkills-2.3.19.jar`.

## Recommended Sprint 79

- **If** a **`slate-platform`** (or similar) subproject is introduced: follow Sprint 77’s pattern for **`:granitelib-platform`** and extend `verifyFabricReleaseBundle` + graph checks; update these docs in the same PR.  
- **Or** product work: **JiJ vs external** (Milestone D), **package** migration planning with ADR buy-in, or merge longer fork-only audit markdown only after trimming for upstream truth.
