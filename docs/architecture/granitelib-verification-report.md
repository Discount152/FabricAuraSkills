# GraniteLib verification (upstream `main` after Sprint 77)

**Scope:** [FabricAuraSkills](https://github.com/Discount152/FabricAuraSkills) with **`projectVersion` 2.3.19** and Gradle ID **`:granitelib-fabric`**. This is a **summary**; line numbers in `build.gradle.kts` can drift.

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
- Remapped JAR: **`FabricAuraSkills-2.3.19.jar`** (or current `projectVersion`).
- **Sprint 77 validation:** **`verifyFabricReleaseBundle: OK`**, **14** nested JARs under `META-INF/jars/`.
- Task requires nested **`granitelib-fabric-*.jar`**, plus existing checks (e.g. `api-`, `common-`, Configurate **4.1.2**, polyglot) and the **forbidden** Configurate **4.2** filter — see [`fabric/build.gradle.kts`](../../fabric/build.gradle.kts) `verifyFabricReleaseBundle` block. This is the **current** enforcement that GraniteLib is embedded in the release JAR.

**Fork note:** A **second** `requireJar` for `granitelib-platform-*.jar` only applies in repos with **`:granitelib-platform`** in the build.

## 3. `slate-fabric` source path

Java sources remain under directory **`slate-fabric/src/.../dev/aurelium/slate/...`** (or whatever the tree is today). The **Gradle** id is `granitelib-fabric`, not a package rename. **Planned** package moves are described in [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) (Sprint 80); re-run the same **validation matrix** after any rename implementation.

## See also

- [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md)  
- [granitelib-distribution-adr.md](granitelib-distribution-adr.md)  
- [Sprint 77 handoff](../handoffs/2026-06-28-granitelib-sprint-77-module-rename-implementation.md)  
- [Sprint 79 handoff](../handoffs/2026-06-30-granitelib-sprint-79-distribution-adr.md)  
- [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md)  
- [Sprint 80 handoff](../handoffs/2026-07-01-granitelib-sprint-80-package-public-class-rename-plan.md)

*Sprint 79: linked Milestone D ADR; no build change. Sprint 80: linked rename plan.*
