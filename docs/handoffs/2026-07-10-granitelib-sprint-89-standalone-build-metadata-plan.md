# GraniteLib Sprint 89 — Standalone build / metadata **implementation plan**

**Date:** 2026-07-10  
**Type:** **Documentation / build-design** only — **no** new `fabric.mod.json`, **no** Gradle **edits**, **no** Java

## Plan summary

- **Deliverable:** [granitelib-standalone-build-metadata-plan.md](../architecture/granitelib-standalone-build-metadata-plan.md).  
- **Key finding:** **`:granitelib-fabric`** has Loom **`remapJar`** and produces **`granitelib-fabric-*.jar`**, but there is **no** `fabric.mod.json` in **`slate-fabric/src/main/resources/`** **today** — so the output is a **remapped library JAR** for **JiJ** / **namedElements**, **not** a **Loader-recognized standalone mod** until metadata exists.  
- **Recommended metadata path (Sprint 90):** `slate-fabric/src/main/resources/fabric.mod.json`.  
- **Versioning (Sprint 90):** add **`granitelibVersion`** (e.g. `0.1.0-alpha.1`); do **not** conflate with **`projectVersion` `2.3.22`**.

## Metadata file recommendation

- **`slate-fabric/src/main/resources/fabric.mod.json`** — standard Fabric; colocated with **`:granitelib-fabric`**.

## Draft `fabric.mod.json` (in plan doc, not in repo)

- **id** `granitelib`, **name** `GraniteLib`, **depends** aligned with current pins: **Loader `>=0.18.4`**, **`minecraft` `~1.21.11`**, **`java` `>=21`**, **`fabric-api` `*`**; **no** `fabric-permissions-api` in library draft unless code requires. **`entrypoints`:** `{}` — `GraniteFabricBootstrap` is not a `ModInitializer` **today**.

## Versioning recommendation

- **`granitelibVersion`** in **`gradle.properties`**; **:granitelib-fabric** `version` override; **`${version}`** expansion in library **`fabric.mod.json`**.

## Gradle / build questions answered (audit)

- **:granitelib-fabric** uses **fabric-loom 1.13.6**; **`remapJar`** exists.  
- **:fabric** JiJ: **`include(project(":granitelib-fabric", configuration = "namedElements"))`**.  
- **verifyFabricReleaseBundle** requires **`granitelib-fabric-*.jar` nested**.  
- **Sprint 90+:** optional **`loom { mods { register("granitelib") { … } } }`**, **processResources** for **`${version}`**, **mitigate** duplicate mod/class if JiJ + external (see plan §8).

## JiJ transition plan

- **Phase 1–4** in [granitelib-standalone-build-metadata-plan.md](../architecture/granitelib-standalone-build-metadata-plan.md) §8 — **duplicate-mod risk** must be **tested**, not **assumed**.

## Dependency timing

- **Do not** add **`depends: granitelib`** to **`:fabric`** until a **valid** standalone JAR is proven **(plan §9)** — typically **Sprint 10x**, not the first **metadata** PR in isolation.

## Validation matrix (for Sprint 90+)

- Gradle: `:granitelib-fabric:remapJar`, `:fabric:build` **-PfabricOnly=true**, **`verifyFabricReleaseBundle: OK`**.  
- **Runtime A/B/C** defined in plan §10 / §12.

## Behavior preservation

- Sprint 89: **no** code/build changes.

## Guardrail ledger

- As Sprint 89 user list.

## Validation (optional)

- `.\gradlew.bat :granitelib-fabric:test :fabric:test :fabric:build -PfabricOnly=true` — **2.3.22** **unchanged**.

## Sprint 90 recommendation

- **Implement** `fabric.mod.json` + `granitelibVersion` + resource expansion; **smoke** **Runtime B** before adding **`depends: granitelib`** to **`:fabric`**.

**Follows:** [Sprint 88](2026-07-09-granitelib-sprint-88-standalone-dependency-identity-adr.md)

**Done:** this handoff + [granitelib-standalone-build-metadata-plan.md](../architecture/granitelib-standalone-build-metadata-plan.md) + cross-links; [identity ADR](../architecture/granitelib-standalone-dependency-identity-adr.md) updated with **Sprint 89 (done)** + **Sprint 90** pointer.
