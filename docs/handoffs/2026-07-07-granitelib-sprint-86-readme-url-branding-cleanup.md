# GraniteLib Sprint 86 — README + clone/home URL + Gradle product fields

**Date:** 2026-07-07  
**Type:** **Narrow** product-facing branding (Track B); **no** `fabric.mod.json`, **no** `NOTICE`, **no** license/vendor/copyright deletions (Track A preserved)

**Prerequisite:** Sprint 85 planning docs **committed** with message `docs: add GraniteLib branding metadata cleanup plan` so Sprint 86 implementation is not mixed with uncommitted planning-only work.

## Files changed

- `README.md` — Rebuilt for this fork: title **FabricAuraSkills**, clone **`Discount152/FabricAuraSkills`**, Releases/Issues nav, one GitHub release badge for **this** repo, **About** + **Upstream Bukkit/Spigot API (reference)** for lineage without presenting Spigot as the install path for this tree; **GraniteLib** + JiJ + `docs/architecture/` pointer; build commands point at `:fabric:build -PfabricOnly=true`.
- `gradle.properties` — `projectVersion` **2.3.20 → 2.3.21**; `releaseTitle` **FabricAuraSkills (MC 1.21.11)**; `repoBaseUrl` **https://github.com/Discount152/FabricAuraSkills**; `supportedMCVersions` unchanged.
- `Changelog.md` — New **2.3.21** section describing README + Gradle metadata change; no gameplay or mod-metadata claims beyond documentation.
- `docs/architecture/granitelib-branding-metadata-cleanup-plan.md` — Stages 1–2 marked implemented; sign-off list partially checked; **§14** → Sprint 87.
- `docs/architecture/granitelib-notice-attribution-strategy.md` — Related: Sprint 86 handoff.
- `docs/architecture/granitelib-license-notice-review-plan.md` — Related + snapshot + audit table (README) row; §2 mod/product bullet.
- `docs/architecture/granitelib-distribution-adr.md` — Related, Sprint 86 Q&A row, footer.
- `docs/architecture/granitelib-v1-embed-contract.md` — See also + footer.
- `docs/architecture/granitelib-verification-report.md` — `projectVersion` / JAR name **2.3.21**; Sprint 86 handoff link.
- `docs/architecture/granitelib-standalone-extraction-checklist.md` — `2.3.21+` snapshot.
- `docs/architecture/granitelib-sprint-77-implementation.md` — `projectVersion` example lines.
- `docs/architecture/granitelib-physical-directory-rename-decision.md` — current version expectation in validation note.

## README cleanup summary

- **Canonical** clone and “home” for **this** project: **Discount152/FabricAuraSkills** (not `Archy-X/AuraSkills` for `git clone`).
- **GraniteLib** named as **embedded** **`:granitelib-fabric`** (JiJ), with pointer to architecture docs; **not** claimed as a separately published stand-alone mod product line.
- **Upstream** AuraSkills / aurelium.dev / wiki / API docs kept in **factual** **lineage** and **separate** “Upstream Bukkit/Spigot API (reference)” section so Fabric users are not given Spigot install/API as the primary path.

## `gradle.properties` changes

- **`repoBaseUrl`:** `https://github.com/Archy-X/AuraSkills` → **`https://github.com/Discount152/FabricAuraSkills`**
- **`releaseTitle`:** `Fabric AuraSkills (MC 1.21.11)` → **`FabricAuraSkills (MC 1.21.11)`** (aligned with output JAR name prefix / product string direction)
- **`projectVersion`:** **2.3.20 → 2.3.21** (with Changelog; reflects release metadata + branding tranche, not a silent bump)

## Legal surfaces preserved

- No edit to **root** **`LICENSE.md`**, **`vendor/**`**, **`fabric.mod.json`** (including **`license`**), **`GraniteFabricBootstrap`** Javadoc, or **copyright** headers.
- No **root** **`NOTICE`** file added.
- README does **not** replace a future **`NOTICE`**; upstream names appear where they describe **lineage** or the **Bukkit** API product.

## Version bump decision

- **2.3.20 → 2.3.21** because **`gradle.properties`** **`projectVersion`** and **release-facing** product fields changed together with a **Changelog** entry (consistent with prior bumps when version-bearing metadata moves).

## Behavior preservation

- **No** Java, Gradle build **logic**, YAML, or **`fabric.mod.json`** changes — **expected** no runtime behavior change; only **docs** and **version** string consumed by build/packaging.

## Guardrail ledger (Sprint 86)

- **Did not** touch: `fabric/src/main/resources/fabric.mod.json` (incl. **mod id**, **`license`**, **name** as in file), vendor trees, `NOTICE`, JiJ wiring, `Phase1YamlMenu`, DTOs, MenuAPI, etc.

## Validation

**Run after `projectVersion` / `gradle.properties` change:**

```text
.\gradlew.bat :granitelib-fabric:test
.\gradlew.bat :fabric:test
.\gradlew.bat :fabric:build -PfabricOnly=true
```

**Expected:** `BUILD SUCCESSFUL`, `verifyFabricReleaseBundle: OK`, remapped JAR name **`FabricAuraSkills-2.3.21.jar`**.

**Observed (2026-07-07, Windows):**

```text
> Task :fabric:verifyFabricReleaseBundle
verifyFabricReleaseBundle: OK (14 nested jars in FabricAuraSkills-2.3.21.jar)
BUILD SUCCESSFUL in 15s
```

(`:granitelib-fabric:test` is **NO-SOURCE** in this tree; `:fabric:test` ran.)

## Sprint 87 recommendation

- **`fabric.mod.json` non-license** pass (**Stage 3**): `name`, `description`, `authors`, optional `sources`/`issues` → align launcher-facing strings with **README** + **`releaseTitle`**, with **`id`** and **`license`** unchanged unless a **separate** migration/legal decision.
- **Or** **NOTICE** **implementation** after **signed-off** draft (Track A) before **wide** public listing changes.
- **Or** continue **GraniteLib** / gameplay port work with branding **frozen** on **mod** metadata.

**Done:** this handoff + **Sprint 86** file list above.
