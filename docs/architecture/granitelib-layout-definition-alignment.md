# GraniteLib — layout vs `FabricMenuDefinition` alignment (audit, **upstream**)

**Status:** Historical / design audit. **Sprint 77** did **not** change `Phase1YamlMenu`, loaders, or layout math — only **Gradle** module id **`:granitelib-fabric`** and JiJ/verify for **`granitelib-fabric-*.jar`**. See [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md).

## Upstream `main` scope

- Relevant code for definitions/planning lives in **`slate-fabric/`** (Gradle: **`:granitelib-fabric`**). **`fabric/`** still owns Phase1 consumption and gameplay.
- **No** `slate-platform` split on this monorepo **today**; any doc that assumed a separate **`dev.aurelium.slate.platform`** build module describes **fork / future** structure, not current upstream.

## Using fork audit documents

Sprints 15+ style audits may be longer on forks. This file’s role on **this** repo is a **short anchor**: align planning expectations with the **one-module** build actually in `settings.gradle.kts`.

*Sprint 78: placeholder expanded for upstream–fork split.*
