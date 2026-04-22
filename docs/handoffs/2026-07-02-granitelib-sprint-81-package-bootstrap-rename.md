# GraniteLib Sprint 81 — `dev.granitelib.fabric` + `GraniteFabricBootstrap`

**Date:** 2026-07-02  
**Type:** **Implementation** (Java + `projectVersion` + [Changelog.md](../../Changelog.md)) — no Gradle module rename, no `slate-fabric/` directory rename, no YAML/metadata, no publish

## Package root chosen

- **`dev.granitelib.fabric`** (replaces `dev.aurelium.slate.fabric` for the stub in `:granitelib-fabric`).

## Class rename completed

- **`SlateFabricBootstrap` → `GraniteFabricBootstrap`**
- **File:** `slate-fabric/src/main/java/dev/granitelib/fabric/GraniteFabricBootstrap.java`

## Files changed (summary)

| Area | Change |
|------|--------|
| `slate-fabric/.../SlateFabricBootstrap.java` | **Removed** (old path) |
| `slate-fabric/.../granitelib/fabric/GraniteFabricBootstrap.java` | **Added** |
| `gradle.properties` | `projectVersion` **2.3.19 → 2.3.20** |
| [Changelog.md](../../Changelog.md) | **2.3.20** entry |
| `docs/architecture/*` (listed in Sprint 81 prompt) | Aligned to post-rename + **2.3.20** where applicable |
| [granitelib-package-public-class-rename-plan.md](../architecture/granitelib-package-public-class-rename-plan.md) | Sprint 81 “landed” state + Sprint **82+** follow-up |

## Stale reference search (post-change)

- **`SlateFabricBootstrap`**, **`dev.aurelium.slate.fabric`**: expect **no** Java hits; **Changelog 2.3.20** and architecture docs may mention old names as **history**.
- Re-run: `rg "SlateFabricBootstrap|dev\.aurelium\.slate\.fabric"` from repo root.

## Version bump

- **2.3.19 → 2.3.20** in `gradle.properties`  
- **Remapped JAR** name: **`FabricAuraSkills-2.3.20.jar`**

## Behavior preservation

- Same **Gradle** module **`:granitelib-fabric`**, same **`slate-fabric/`** tree, same **JiJ** and **`verifyFabricReleaseBundle`** contract for `granitelib-fabric-*.jar`.  
- **`status()`** string updated only to say `granitelib-fabric` (cosmetic, no menu/gameplay/ID/YAML change).

## Guardrail ledger (preserved)

- No `slate-fabric/` **directory** rename, no new `:granitelib-platform`, no remove-JiJ, no `fabric.mod.json`, no publish, no Phase1/menu logic edits, no license stripping (vendor/Archy-X references preserved in Javadoc where applicable).

## Validation (local)

Run the matrix from [granitelib-package-public-class-rename-plan.md](../architecture/granitelib-package-public-class-rename-plan.md#9-validation-matrix-run-after-any-rename-implementation) — see commit message / CI for outcomes.

| Command | Result |
|---------|--------|
| `.\gradlew.bat :granitelib-fabric:test` | **NO-SOURCE**; task/graph succeeds (part of full build) |
| `.\gradlew.bat :fabric:test` | **BUILD SUCCESSFUL** |
| `.\gradlew.bat :fabric:build -PfabricOnly=true` | **BUILD SUCCESSFUL**; **`verifyFabricReleaseBundle: OK (14 nested jars in FabricAuraSkills-2.3.20.jar)`** |
| `.\gradlew.bat :fabric:dependencies --configuration compileClasspath` | Includes **`project :granitelib-fabric`** (with `:api`, `:common`) |
| `.\gradlew.bat :granitelib-fabric:dependencies --configuration compileClasspath` | **No** `project :fabric`; MC/Fabric API only |

## Recommended Sprint 82

- **Port work** that adds real `slate-fabric` surface: **re-audit** the [rename plan](../architecture/granitelib-package-public-class-rename-plan.md) and add subpackages under `dev.granitelib.…` with the same staging rules.  
- **Or** optional **Stage 5** (rename folder `slate-fabric` → `granitelib-fabric`) only if the team wants path alignment, with a single mechanical commit — see [Sprint 82 decision](../handoffs/2026-07-03-granitelib-sprint-82-directory-rename-decision.md) (default: **defer**).  
- **Or** **Stage 6** / distribution: Maven group, license, external when [granitelib-distribution-adr.md](../architecture/granitelib-distribution-adr.md) gates are met.
