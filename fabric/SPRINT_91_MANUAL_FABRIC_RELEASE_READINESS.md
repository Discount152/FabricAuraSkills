# Sprint 91 — Manual: Fabric release readiness and working-tree hygiene

| Field | Value |
|--------|--------|
| **Sprint** | **91** — Release artifact clarity, doc consistency, operator install, dirty-tree hygiene |
| **Build / artifact** | **`FabricAuraSkills-2.3.122.jar`** (`projectVersion` **2.3.122**) |
| **Bedrock / Geyser** | **Not tested** — **`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`** remains deferred |

## Operator validation (prior sprint)

- **Sprint 90** global Granite text audit — **Java operator checklist: Pass** (no raw `auraskills.granite.*` on exercised command/menu paths). **Bedrock** still unavailable.

## Release artifacts (what to ship / test)

| Output | Location | Notes |
|--------|-----------|--------|
| Remapped mod JAR | **`fabric/build/libs/FabricAuraSkills-<version>.jar`** | `<version>` = **`gradle.properties` → `projectVersion`** |
| Sources JAR (optional) | **`fabric/build/libs/FabricAuraSkills-<version>-sources.jar`** | Debugging / compliance |
| Timestamped history | **`fabric-mod-jar-history/`** | Populated when **`archiveFabricModJar`** / clean hooks run — optional local rollback trail |
| GraniteLib | **Separate mod JAR** (`granitelib-fabric-*.jar`) — **Sprint 93** restored two-JAR policy; JiJ-only was a **transitional** artifact shape | **Never** mix external + JiJ GraniteLib; see **`SPRINT_93_TWO_JAR_RELEASE_AND_PARITY_RESET.md`** |

**Build (maintainer / CI):**

```text
.\gradlew -PfabricOnly=true :fabric:test :fabric:build
```

## Version metadata (consistency check)

- **`gradle.properties`:** **`projectVersion`**, **`granitelibVersion`**, **`supportedMCVersions`**, **`releaseTitle`**
- **`Changelog.md`:** top section matches shipped **`projectVersion`**
- **Embedded GraniteLib `fabric.mod.json`:** **`${version}`** is expanded at **`slate-fabric`** **`processResources`** time from **`granitelibVersion`** (Sprint 91) — verify Loader no longer prints a literal **`${version}`** for **`granitelib`**

## Code landed in Sprint 91 (was previously local-only)

- **`AnvilScreenHandlerAnvilDiscountMixin`:** **Sprint 76A** **`VarHandle`** approach — avoids **`@Shadow`** **`player`** on **`AnvilScreenHandler`** when refmaps are missing (dedicated-server crash). See **`fabric/SPRINT_76A_STARTUP_BLOCKER_ANVIL_MIXIN.md`**.
- **`slate-fabric/build.gradle.kts`:** resource expansion for **`granitelib`** **`fabric.mod.json`** version.

## Working-tree hygiene

### Ignored by repo (Sprint 91)

- **`fabric/logs/`** — local server logs
- **`run-strict/`** — optional local transition / smoke harness trees

### Optional untracked paths (triage before a release tag)

The following may appear as **untracked** on developer machines; they are **not** required for a Fabric release build. **Commit** if they are intentional project docs; **delete** or **exclude** if they are personal scratch:

- **`docs/architecture/granitelib-*.md`** (and similar)
- **`docs/handoffs/*.md`**
- **`fabric/MOD_COMMANDS.txt`**, **`fabric/SPRINT_SUMMARY_SPEC.md`**

**Do not commit** world data, secrets, or full log dumps.

## Operator install (short)

See **root `README.md` → Server install (operators)**. Minimum: compatible **Minecraft** + **Fabric Loader** + **Fabric API** + **`FabricAuraSkills-<version>.jar`** in **`mods/`**.

## Recommended next sprint

- **Sprint 87** when a Bedrock tester exists, **or**
- Release tagging / GitHub **Releases** checklist (outside this repo if you use external automation).
