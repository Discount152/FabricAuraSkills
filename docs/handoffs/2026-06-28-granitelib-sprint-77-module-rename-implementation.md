# GraniteLib Sprint 77 — Module rename merge PR implementation

**Date:** 2026-06-28  
**Branch:** `feature/granitelib-module-ids` (from `origin/main`)  
**Base:** [Discount152/FabricAuraSkills](https://github.com/Discount152/FabricAuraSkills) `main` @ `2.3.18`

## Scope (this repo / upstream `main`)

Upstream **`main` does not** include a `slate-platform/` subproject (only `slate-fabric/`). This PR therefore implements **`:granitelib-fabric`** only:

- `include("granitelib-fabric")` + `project(":granitelib-fabric").projectDir = file("slate-fabric")`
- **`:granitelib-platform`** is **out of scope** until `slate-platform` exists in `settings.gradle.kts` and `fabric` depends on it (follow the same `projectDir` + JiJ + `verifyFabricReleaseBundle` pattern from planning docs on forks that have both modules).

## Files changed

| File | Change |
|------|--------|
| [settings.gradle.kts](../../settings.gradle.kts) | `include("granitelib-fabric")` + `projectDir` → `slate-fabric/`; Javadoc list updated. |
| [fabric/build.gradle.kts](../../fabric/build.gradle.kts) | `project(":granitelib-fabric", configuration = "namedElements")`; JiJ `include` for `granitelib-fabric`; `verifyFabricReleaseBundle` requires `granitelib-fabric-*.jar`. |
| [slate-fabric/build.gradle.kts](../../slate-fabric/build.gradle.kts) | `archiveBaseName` → `granitelib-fabric`. |
| [gradle.properties](../../gradle.properties) | `projectVersion` **2.3.18 → 2.3.19**. |

## Module IDs implemented

- **`:granitelib-fabric`** (Gradle) → directory **`slate-fabric/`** (unchanged on disk)
- **`:slate-fabric`** removed from `include` (replaced by above)

## ProjectDir mapping

```kotlin
include("granitelib-fabric")
project(":granitelib-fabric").projectDir = file("slate-fabric")
```

(in both `fabricOnly` and full `include` blocks)

## Archive / JiJ

- **Library JAR** filename prefix: **`granitelib-fabric-*.jar`**
- **JiJ:** `granitelib-fabric` is now **embedded** in the remapped mod (previously the lib was a compile dependency but **not** in `include(implementation(...))` on upstream `main`)

## Dependency graph

- **`:fabric` `compileClasspath`:** `project :granitelib-fabric` (Loom `namedElements`)
- **`:granitelib-fabric` `compileClasspath`:** no `project :fabric`

## projectVersion

- **2.3.18 → 2.3.19** (patch bump: Gradle module ID + shipping nested JAR name change + JiJ inclusion of GraniteLib JAR)

## Validation matrix (Windows)

| Command | Result |
|---------|--------|
| `.\gradlew.bat :granitelib-fabric:test` | **NO-SOURCE** (no tests in module) — task succeeds |
| `.\gradlew.bat :fabric:test` | **BUILD SUCCESSFUL** |
| `.\gradlew.bat :fabric:build -PfabricOnly=true` | **BUILD SUCCESSFUL** |
| `verifyFabricReleaseBundle` | **OK (14 nested jars in FabricAuraSkills-2.3.19.jar)** |
| `.\gradlew.bat :fabric:dependencies --configuration compileClasspath` | includes `project :granitelib-fabric` |
| `.\gradlew.bat :granitelib-fabric:dependencies --configuration compileClasspath` | no `project :fabric` |

**`:granitelib-platform:build` / `:granitelib-platform:dependencies`:** N/A (module not in this tree).

## Behavior preservation

- **No** Java, package, class, or YAML changes.
- **No** `Phase1YamlMenu` or ID changes.
- **Intent:** same runtime behavior; different Gradle project identity and remapped JAR now embeds the library JAR under a GraniteLib-prefixed name.

## Guardrails (preserved)

- No package or directory renames, no publish, no license stripping, no menuapi.

## Rollback

- Revert the PR or `git revert` the merge commit; VCS-only.

## Sprint 78 recommendation

- **Merge** this PR to `main` after review.
- If the fork adds **`slate-platform`**, a **follow-up** PR: `include("granitelib-platform")` + `projectDir`, `fabric` lines + JiJ + `requireJar` for `granitelib-platform-*.jar`, re-run the full two-module matrix.
- **Milestone D** (JiJ vs external) when the team is ready.
- Reconcile **local planning docs** (from other branches) with **upstream** file layout in a doc-only pass if needed.

**Done (Sprint 78):** [2026-06-29-granitelib-sprint-78-upstream-doc-reconciliation.md](2026-06-29-granitelib-sprint-78-upstream-doc-reconciliation.md).
