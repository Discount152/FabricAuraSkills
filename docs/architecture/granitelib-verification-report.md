# GraniteLib verification (upstream `main` after Sprint 77)

**Scope:** [FabricAuraSkills](https://github.com/Discount152/FabricAuraSkills) with **`projectVersion` 2.3.22** and Gradle ID **`:granitelib-fabric`**. This is a **summary**; line numbers in `build.gradle.kts` can drift.

## 1. Gradle dependencies (Sprint 77+)

**Commands (Windows):**

```text
.\gradlew.bat :fabric:dependencies --configuration compileClasspath
.\gradlew.bat :granitelib-fabric:dependencies --configuration compileClasspath
```

**Expected / observed (Sprint 77):**

| From | To | Notes |
|------|-----|--------|
| `:fabric` | `project :api`, `project :common`, **`project :granitelib-fabric`**| `granitelib-fabric` via Loom **`namedElements`**. |
| `:granitelib-fabric` | _not_ `project :fabric` | **No** reverse compile edge from the library to the mod. |

**Not applicable on upstream:** `.\gradlew.bat :granitelib-platform:dependencies` — **no** such module in this monorepo until `slate-platform` exists and `settings`/`fabric` are wired (fork-only until then).

## 2. JiJ / `verifyFabricReleaseBundle`

- **Distribution policy (Milestone D):** [granitelib-distribution-adr.md](granitelib-distribution-adr.md) — **Proposed:** keep **JiJ** for upstream releases; external dependency is a **future** migration after naming/publish readiness.
- Remapped JAR: **`FabricAuraSkills-2.3.22.jar`** (or current `projectVersion`).
- **Sprint 77+ validation (counts vary):** **`verifyFabricReleaseBundle: OK`**, **14** nested JARs under `META-INF/jars/` was observed for **2.3.19**; re-check after dependency changes.
- Task requires nested **`granitelib-fabric-*.jar`**, plus existing checks (e.g. `api-`, `common-`, Configurate **4.1.2**, polyglot) and the **forbidden** Configurate **4.2** filter — see [`fabric/build.gradle.kts`](../../fabric/build.gradle.kts) `verifyFabricReleaseBundle` block. This is the **current** enforcement that GraniteLib is embedded in the release JAR.

**Fork note:** A **second** `requireJar` for `granitelib-platform-*.jar` only applies in repos with **`:granitelib-platform`** in the build.

## 3. `slate-fabric` source path

The **on-disk** directory is still **`slate-fabric/src/...`** (see [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md) for rename-vs-defer). The **Java** package for the bootstrap is **`dev.granitelib.fabric`** (Sprint 81). The **Gradle** id remains **`granitelib-fabric`**. [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) covers further staging; re-run the **validation matrix** after future moves.

## See also

- [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md)  
- [granitelib-distribution-adr.md](granitelib-distribution-adr.md)  
- [Sprint 77 handoff](../handoffs/2026-06-28-granitelib-sprint-77-module-rename-implementation.md)  
- [Sprint 79 handoff](../handoffs/2026-06-30-granitelib-sprint-79-distribution-adr.md)  
- [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md)  
- [Sprint 80 handoff](../handoffs/2026-07-01-granitelib-sprint-80-package-public-class-rename-plan.md)  
- [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md)  
- [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md)  
- [Sprint 82 handoff](../handoffs/2026-07-03-granitelib-sprint-82-directory-rename-decision.md)  
- [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md)  
- [Sprint 83 handoff](../handoffs/2026-07-04-granitelib-sprint-83-license-notice-review-plan.md)  
- [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md)  
- [Sprint 84 handoff](../handoffs/2026-07-05-granitelib-sprint-84-notice-attribution-strategy.md)  
- [granitelib-branding-metadata-cleanup-plan.md](granitelib-branding-metadata-cleanup-plan.md) (Sprint 85)  
- [Sprint 85 handoff](../handoffs/2026-07-06-granitelib-sprint-85-branding-metadata-cleanup-plan.md)  
- [Sprint 86 handoff](../handoffs/2026-07-07-granitelib-sprint-86-readme-url-branding-cleanup.md)  
- [Sprint 87 handoff](../handoffs/2026-07-08-granitelib-sprint-87-fabric-mod-json-branding-cleanup.md)  
- [granitelib-standalone-dependency-identity-adr.md](granitelib-standalone-dependency-identity-adr.md) (Sprint 88)  
- [Sprint 88 handoff](../handoffs/2026-07-09-granitelib-sprint-88-standalone-dependency-identity-adr.md)

*Sprint 79: linked Milestone D ADR; no build change. Sprint 80: linked rename plan. Sprint 81: package + class rename in `slate-fabric`. Sprint 82: directory rename decision (docs). Sprint 83: license/notice plan. Sprint 84: draft NOTICE strategy. Sprint 85: branding & metadata cleanup plan (docs only). Sprint 86: README + `gradle.properties` + `2.3.21`. Sprint 87: `fabric.mod.json` + `2.3.22`. Sprint 88: standalone dependency **identity** ADR (no build change).*
