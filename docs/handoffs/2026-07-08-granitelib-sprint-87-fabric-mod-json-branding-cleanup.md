# GraniteLib Sprint 87 — `fabric.mod.json` non-license metadata (Track B)

**Date:** 2026-07-08  
**Type:** Narrow product metadata — **no** `id`, **`license`**, **dependencies**, **entrypoint**, **mixin**, or **environment** changes

## File path

- [`fabric/src/main/resources/fabric.mod.json`](../../fabric/src/main/resources/fabric.mod.json) — **only** this Fabric mod JSON for Sprint 87 implementation.

## Version bump

- **`gradle.properties` `projectVersion`:** **2.3.21 → 2.3.22**
- **`Changelog.md`:** new **2.3.22** section

## Fields changed

| Field | Change |
|--------|--------|
| **`name`** | `Fabric AuraSkills` → **`FabricAuraSkills`** (align with `releaseTitle` / README) |
| **`description`** | Fork-accurate **Fabric** dedicated-server copy; **GraniteLib** **(:granitelib-fabric)**; keeps pointer to `fabric/FABRIC_QUICKSTART.md` |
| **`authors`** | `Archyx (original AuraSkills)`, `Discount152 (Fabric port)` — **upstream** credit **retained**, **maintainer** named |
| **`contact`** | **Added:** `homepage`, `sources` → `https://github.com/Discount152/FabricAuraSkills`; `issues` → `.../issues` |

## Fields explicitly not changed

- **`id`** — `auraskills`
- **`version`** — `${version}` (Gradle)
- **`license`** — `GPL-3.0`
- **`environment`**
- **`entrypoints`**
- **`mixins`**
- **`depends`** (and no `suggests` / `breaks` added)

## Legal surfaces preserved

- **`license`** value unchanged.  
- **Authors** still reference **Archyx** (original product).  
- No **`LICENSE*`**, **`vendor/**`**, **root `NOTICE`**, or **Java** Javadoc edits.

## Behavior preservation

- **No** source moves, **no** YAML, **no** gameplay logic. Packaged mod **metadata** and **version** only.

## Guardrail ledger

- Per Sprint 86/87 **guardrails** list: no JiJ removal, no Gradle logic change, no Phase1YamlMenu, etc.

## Validation

**Commands:**

```text
.\gradlew.bat :granitelib-fabric:test
.\gradlew.bat :fabric:test
.\gradlew.bat :fabric:build -PfabricOnly=true
```

**Observed (2026-07-08, Windows):**

```text
> Task :fabric:verifyFabricReleaseBundle
verifyFabricReleaseBundle: OK (14 nested jars in FabricAuraSkills-2.3.22.jar)
BUILD SUCCESSFUL in 14s
```

(`:granitelib-fabric:test` is **NO-SOURCE**; `:fabric:test` ran.)

**Follow-up (Sprint 88):** [Standalone dependency identity ADR](2026-07-09-granitelib-sprint-88-standalone-dependency-identity-adr.md) — **proposed** mod **`id` `granitelib`**, **JiJ**-through-**transition**, **Sprint 89** **implementation** next.

**Done:** this handoff + [granitelib-branding-metadata-cleanup-plan.md](../architecture/granitelib-branding-metadata-cleanup-plan.md) updates (Stage 3 complete).
