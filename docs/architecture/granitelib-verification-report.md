# GraniteLib verification (Sprint 77, `main`)

**Gradle (after [Sprint 77 handoff](../handoffs/2026-06-28-granitelib-sprint-77-module-rename-implementation.md)):**

- **`:fabric` →** `project :granitelib-fabric` on `compileClasspath` (Loom `namedElements`).
- **`:granitelib-fabric` →** no `project :fabric` on `compileClasspath`.
- **`verifyFabricReleaseBundle`:** requires **`granitelib-fabric-*.jar`** among nested JARs; **14** nested JARs observed for `FabricAuraSkills-2.3.19.jar` in validation.

**`:granitelib-platform`:** not in this monorepo yet.

See [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md).
