# FabricAuraSkills 2.3.122 Release Candidate Notes

**Artifact:** `FabricAuraSkills-2.3.122.jar`  
**Status:** Java/Fabric validation pass; **Bedrock/Geyser not yet validated** at runtime.

## Highlights

- Dedicated-server startup blockers addressed (**Sprint 76A** anvil mixin — `VarHandle` / refmap-safe access; see `fabric/SPRINT_76A_STARTUP_BLOCKER_ANVIL_MIXIN.md`).
- Java command/menu smoke matrix **closed enough** to proceed (`fabric/SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`).
- Smart action bar modes validated on Java (`smart` / `always` / `off`).
- MiniMessage / text rendering path aligned with Bukkit-style formatting (`FabricMessageProvider` / `FabricUiProvider` — Sprint 79+).
- Fabric raw `auraskills.granite.*` visibility: **`FabricGraniteText`** audit complete (Sprints 83–90).
- Built-in menu item/lore and `/sources` paged chat use server-resolved literals (Sprints 88–89).
- Storage / backup / preset / prefixed `item` staff command roots validated for **permissioned** Java staff (Sprint 86 context).
- **GraniteLib:** `fabric.mod.json` tracked in repo; `${version}` expanded to **`0.1.0-alpha.2`** in the embedded library (Sprint 91).

## GraniteLib policy (normal server install)

- **Shipped path:** **Jar-in-Jar** only — use **`FabricAuraSkills-2.3.122.jar`** in `mods/`. The remapped JAR embeds **`META-INF/jars/granitelib-fabric-2.3.122-dev.jar`**; Loader loads it from the nested path. **Do not** add a second `granitelib` mod JAR unless you are **deliberately** testing a standalone GraniteLib dev build.
- **Versions:** Main mod **`id`:** `auraskills`, **`version`:** `2.3.122`. Embedded GraniteLib **`id`:** `granitelib`, **`version`:** `0.1.0-alpha.2` (not the literal `${version}` placeholder).

## Install

1. Upload **`FabricAuraSkills-2.3.122.jar`** to the dedicated server **`mods/`** folder.
2. Use **one** GraniteLib policy: **JiJ only** for production (see above).
3. Remove **stale** older `FabricAuraSkills-*.jar` files from `mods/` to avoid duplicate mod loads.
4. Remove **stale external** `granitelib` / duplicate nested-policy jars unless you intentionally run split/dev mode (not the default).
5. **Full restart** after swapping the JAR.
6. Verify **`latest.log`**: Fabric loads **`auraskills`** **`2.3.122`**; GraniteLib nested mod reports **`0.1.0-alpha.2`**.

**Build output path (from this repo):** `fabric/build/libs/FabricAuraSkills-2.3.122.jar`  
**Verification (maintainers):** `.\gradlew -PfabricOnly=true :fabric:test :fabric:build :fabric:verifyFabricReleaseBundle`

## Known limitations

- **Bedrock/Geyser** runtime validation **deferred** (`fabric/SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`).
- **Full Slate / YAML menu parity** with the Bukkit reference is **not** complete (built-in Granite menus; see `fabric/MENU_AND_COMMANDS_PORT.md`).
- **Destructive** backup/preset restore paths: validate only on **disposable** data (matrix **Skip** until then).
- **JobsCommand** and reference **Jobs / economy** hooks: **out of scope** for default Granite.
- Optional third-party Bukkit ecosystems (WorldGuard, PlaceholderAPI, etc.): **out of default scope** (`fabric/PARITY_BACKLOG.md`).

## Release candidate tag

- Optional annotated tag for wider Java testing: **`v2.3.122-fabric-rc1`** (create locally; push only when the operator chooses).  
- Sprint checklist: **`fabric/SPRINT_92_RELEASE_CANDIDATE_PREP.md`**.
