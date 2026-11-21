# Granite Skills and GraniteLib completion ledger

Starting commit: `0c204cb86689b5a4d4609202c97ea47ba9f041b7` (2.3.179).
Frozen review pair: **GraniteSkills 2.3.244** + **GraniteLib 0.2.0-alpha.21**.
Overall: **INCOMPLETE**. 271 cases PASS on this pair. The open cases are the four user tests below and `AC-MATRIX-FINAL`, which waits on them.

SHA-256 runtime `9686FF0F7F4752087EFF33E25F3987A2EA8C72FDC0867C1BCE744DFD4D147145`  
SHA-256 sources `4EC885D56D655C0C859ECBD8643930AEA0D615B70A38C19DFCDBD9785826E9FE`  
SHA-256 GraniteLib `4DAC9D41B03C2D71126263C0352F3E7245BE498198F6028DAB73548C1AA32CAA`

Worktree `0c204cb8` on `cursor/feature-restoration-26-3-65b4` (uncommitted). Rebuild: `gradlew -PfabricOnly=true :fabric:build :granitelib-fabric:jar -x test`. A clean build from the review ZIP source reproduces all three hashes (`source-rebuild-check.txt`).

Review ZIP: `F:\FabricAuraSkills\fabric\GraniteSkills_Review_2.3.244.zip` SHA-256 `26DFAEDCCD3614C3A989685669EDBE52FBC0B3F42A31BE3925845D6855069BA1` (built by `tools/make_review_bundle.py 2.3.244 0.2.0-alpha.21`; this ledger line was added after the ZIP was built).

### 2.3.243–2.3.244 (review T1–T4, full rerun)

| Area | Result on 2.3.244 |
|------|-------------------|
| T1 text conversion | `FabricTextAdapter` maps literal, translatable with fallback and arguments, keybind, score, every style field (named colors stay named, explicit `false` decorations, shadow, insertion, font) and every click/hover event. Selector, NBT and object content and unusual events go through the 26.3 codec one node at a time. `FabricTextAdapterTest` 11 tests pass. `action-bar`, `chat`, `title`, `levelup-chat`, `review-f2` probes PASS. |
| T2 menu click outcomes | `menu-migration-probe` asserts after each click: Average shows the average title and history `AVERAGE`; the sort click shows the rows of the next sort; Next Page shows `Page 2 of 5`; Back restores page 2. New case `AC-MENU-CLICK-OUTCOMES`. |
| T2 bug found | Level progression Next Page and Back reopened page 1 (context dropped in the GraniteLib bridge). Fixed in 2.3.243. 2.3.243 then lost the return skills-list page; 2.3.244 keeps both pages in history. `menu-level-probe` and `menu-migration-probe` PASS. |
| T3 Repairing | Exact XP cost (`levels 40 → 39`, `AnvilMenu.getCost()` = 1) and full conservation: one sword in, one repaired sword out, ingots 2 → 1 with the spare ingot untouched. |
| T4 provenance | Review ZIP holds base commit, `git diff --binary` patch, untracked file list, buildable source ZIP, Gradle XML/HTML reports for 4 modules, and one evidence folder per probe with the result, server log, stderr and `runtime.txt` (loaded jar SHA-256s). |
| Menu probes | The eight `menu-*` probes checked a log line removed in 2.3.237. They now wrap GraniteLib's registered listener and require that the registry dispatched the open (`libraryDispatched=true`). |
| Matrix | 246 probes, all PASS on 2.3.244 (`matrix-summary.csv`; first pass in `matrix-summary-first-pass.csv`). Two-boot lifecycle probe PASS. Independent GraniteLib consumer PASS on alpha.21. |

Matrix triage (first pass had 10 non-PASS; none was a product defect):

| Probe | Cause | Change |
|-------|-------|--------|
| `death-reset` | Probe needs `reset_xp: true`, ratio 0.5; set by hand in 2.3.197 | Matrix prepare step sets it |
| `lb-cmd-average` | ~130 profiles from other probes pushed `AvgLow` off page 1 | Prepare step empties userdata for this probe |
| `perm-provider` | LuckPerms not installed / not on the compile classpath | Installed for this probe only, removed after |
| `mysql-fail` | Boot must crash, so no in-server result | Verdict from YAML hash before/after plus `Communications link failure` crash cause |
| `review-v2` | Inventory dump, not a verdict | `check-review-v2.py` maps 71 abilities, 8 mana abilities, 44 roots: `unmapped=0` |
| `matrix-final-partial` | Pinned to 2.3.239 | Pinned to 2.3.244 |
| `review-fixes` | Expected alpha.18/19 accepted; floor is alpha.21 by design | Rejects alpha.2 and alpha.20, accepts alpha.21 |
| `preset-runtime` | Expected merge to override; later case `AC-PRESET-MERGE` defines merge as keep existing, add missing | Asserts the `AC-PRESET-MERGE` contract (`preset-merge-probe` also PASS) |
| `stun-event` | 80 same-tick hits on one zombie: only the first counts, so one 20% roll | Fresh zombie per arrow: 13 stuns in 80 hits |
| `xp-fish` | Tropical fish (750 XP) levelled 1 → 2 and the raw delta hid 100 XP | Adds back level-up cost; clears stale items before each cast |

Harness: 26.3 keeps no spawn chunks, so `matrix-harness` force-loads the probe chunk band while no player is online. MySQL probes get SQL config per probe; every other probe runs on file storage from `_config-baseline`.

### 2.3.242 (review P1–P5 + level-up chat)

| Area | Result on 2.3.242 |
|------|-------------------|
| P1 placed-block flush | Ordered writer; flush waits; shutdown rejects later submits. `FabricPlacedBlocksTest` passed. |
| P2 menus | `menu-migration-probe` PASS: containers, clicks, sort, progression, claim, Back. Exceptions fail the case. |
| P3 Repairing | PASS: below-unlock 178 vs enabled 162 (added 16); take consumes ingot and 1 XP level. |
| P4 Retrieval | PASS: inventory +1 and arrow removed. |
| P4 SQL | PASS: disabled rename and enabled rename both executed; version 8.4.13 from the server. |
| P5 library floor | Raised to `>=0.2.0-alpha.21` (`GraniteMenuText` is absent on alpha.20). Boot rejects alpha.20. |
| Rejected hits | PASS: repeat and dead target deal 0; accepted hit still scales (7.87 vs 19.76). |
| Armor ownership | PASS: different same-component stack is returned once; stored stack is not duplicated. |
| Level-up chat | PASS: real farming 1→2 award. Transmitted component has color `aaaa`, bold, strikethrough; text includes Farming, 1, 2, ❤, ➽; no MiniMessage or `§`. Java/Bedrock visuals stay in the client cases. |

### Applicability (historical vs current)

| Label | Versions | Use |
|------|----------|-----|
| **Current pair** | 2.3.244 + alpha.21 | Every disposable-server probe, the lifecycle probe and the consumer rerun on these bytes. |
| **Previous pair (R8/R1)** | 2.3.241 + alpha.21 | Superseded; rows below that cite it are history. |
| **Historical disposable-server** | Mostly 2.3.237 gameplay/provider logs; R6/R7 also on 2.3.240 | Valid for wiring/smoke; **not** relabeled as 2.3.241 execution unless re-run. |
| **Prior freeze ZIP** | 2.3.240 + alpha.21 / 2.3.239 + alpha.20 | Superseded as current pair; retained as artifacts. |

### R8 delta (matrix deps + R6/R7 revalidation)

| Area | Result |
|------|--------|
| `AC-MATRIX-FINAL.depends_on` | Expanded **156 → 272** (all in-scope required cases except matrix + EXCLUDED). |
| `AC-MIGRATION-SQL` | **PASS** on 2.3.240 — `mysql-migration-probe` (real LegacySqlTables + SqlMigrator rerun; MySQL 8.4.13 version from server). |
| `AC-ABILITY-RETRIEVAL` / `REPAIRING` / `SPLASHER` | **PASS** on 2.3.240 — event-path probes (arrow retrieval delay; anvil `createResult`; splash duration scale). Helper probes retained as wiring smoke. |
| `AC-XP-JUMP` | **PASS** on 2.3.240 — `jump-xp-event-probe` advances `FabricJumpingXpHandler` tick path and asserts XP. |
| R2 granitelib floor | **PASS** — reject `alpha.18` (`HARD_DEP … >=0.2.0-alpha.20`); accept `alpha.21` (`Granite Skills enabled`). |
| Armor ownership | **Mitigated** — restore only when stack not still in hand (`docs/solutions/r8-armor-backup-findings.md`). |
| Auto-backup ordering | **Fabric fixed** (meta after success); Bukkit still writes meta before backup (out of Fabric pair scope). |
| `AC-HUD-MYSQL` + `AC-MYSQL-*` (except migration) | **PASS retained** on historical 2.3.239 suite/restart/fail probes + `scripts/mysql-fail-hash.ps1` reference. |
| `AC-PERM-PROVIDER` | **PASS retained** (historical LuckPerms probe). |
| `AC-MATRIX-FINAL` | **NOT STARTED** — `depends_on` covers all 272 required cases; PASS waits external blockers only. Partial freeze file remains historical (2.3.239). |
| R1 menu library routing | **PASS** on 2.3.241 — ordinary opens go through `FabricGraniteLibMenuBridge` → `GraniteLoadedMenuRegistry` → TemplateSupport/`*MenuGui`; `lastOpenedId` set for all 8; `loadDefinition` changes next open (`menu-migration` + `review-f2`). |

Armor / backup notes: `docs/solutions/r8-armor-backup-findings.md`.

Remaining **BLOCKED**: `AC-CLIENT-JAVA`, `AC-CLIENT-BEDROCK`, `AC-LIFECYCLE-GEYSER`, `AC-PACK-PRODUCTION`.

## Audit 2.3.236 → fixes in 2.3.237 (16 Nov 2026)

Disposition of Bronson/@Claude Fable findings (code + dedicated-server boot evidence; combat/menu client still pending):

| ID | Disposition | Evidence |
|----|-------------|----------|
| C1 | **Confirmed defect → fixed** | `hurtServer` has one float; removed `ordinal=2`. Boot with `defaultRequire:1` applies damage mixins. Combat health outcomes still need in-game probe. |
| C2 | **Confirmed defect → fixed** | `injectors.defaultRequire: 1`. Boot fixed FishingBobber 8-arg miss + shredder ModifyVariable signature. Server reaches `Done` + `Granite Skills enabled`. |
| H1 Stun/Piercing | **Confirmed → wired** | Stun on bow damage path; Piercing on bow shoot. |
| H1 Retrieval/Shredder/Repairing/Splasher | **Confirmed → wired** | Arrow `onHitBlock` delayed retrieval; armor `hurtAndBreak` shredder; anvil `createResult` repairing; splash `POTION_DURATION_SCALE`. |
| H1 Revival | **Disproved missing hook** | `AFTER_RESPAWN` already registered; left as-is. |
| H2 | **Confirmed → fixed** | `quickMoveStack` EMPTY; `canTakeItemForPickAll` blocked; swap/throw/drag blocked on top. Menus routed to gameplay GUIs via `FabricMenuRegistry` / `*MenuGui`. |
| H3 | **Confirmed → mitigated** | Equippable + ArmorSlot gates; `setItemSlot` restores stack via `placeItemBackInInventory`. Needs client equip check. |
| H4 | **Confirmed → fixed** | Placed-blocks dirty flag + 5s flush; flush on disable. |
| H5 | **Confirmed → fixed** | JiJ `mysql-connector-j-8.4.0`; `verifyFabricReleaseBundle` requires it. Round-trip **PASS** on MySQL 8.4.13 (2.3.239). |
| H6 | **Confirmed → fixed** | `sql.migrate_legacy_tables` default **false**. |
| H7 | **Confirmed → fixed** | Jump expected uses `0.42f` + epsilon; unit test updated. |
| H8 | **Confirmed → fixed** | `backupAutomatically()` in `finishEnable`; log showed `Automatic backup completed.` |
| H9 | **Confirmed → fixed** | `getDirectEntity()` for damage type. |
| M1/M2/L7 | **Confirmed → fixed** | Tall-plant uses broken state; Life Essence magic-heal flag on instant effects; anti-AFK OR-combine. |
| M4 | **Confirmed → mitigated** | openmenu *other player* requires node or op 2. |
| M12 | **Confirmed → fixed** | README + config.yml AuraSkills attribution. |

Overall status remains **INCOMPLETE** until event-path probes + final matrix + review ZIP against frozen bytes.

Packaged jars (working candidate; review ZIP not frozen yet):

- `fabric/build/libs/GraniteSkills-2.3.237.jar` SHA-256 `67B450306DD5000DEAB291DF2110BCC96584539E0816EC8E9D7D1D1DD38E5491` (sources `17CDB55C670EB609FBCF0338FAD7BD9629865B3BA66CFA59F930CA6FCCEDCCAA`); 15 JiJ including `mysql-connector-j-8.4.0.jar`
- GraniteLib `granitelib-fabric-0.2.0-alpha.20.jar` SHA-256 `7E69C1714D33AEDB27E94EE512E2A974CDAA3318FF11A127E7A6B5D2B4E95A80` (sources `0061910E1D7A401A1057FFD29AF36B9FF8FA853F9F64FDDEFB03A37ECEBF86FB`)
- Prior freeze 2.3.236 SHA-256 `B68A27167452C7DFA58024BF76A21D476891AEF4EA324F6F31E4E189DBEE16A9`

Dedicated-server evidence on these bytes:
- Mixin boot with `defaultRequire:1` → `Done` + `Granite Skills enabled` (+ earlier boot logged `Automatic backup completed.`)
- `damage-event-probe` (`hurtServer` sword hit, fighting 0 vs max): `lostLow=7.87…` `lostHigh=11.88…` **PASS** (`F:\gs-lifecycle-server\damage-event-probe-result.txt`; source under `damage-event-probe-src/`)
- `jump-event-probe` (`jumpFromGround`): `vy=0.41999998688697815` matches expected, **PASS** (`jump-event-probe-result.txt`; source under `jump-event-probe-src/`)
- `menu-conserve-probe` (shift-click / pick-all / throw on top): **PASS** (`menu-conserve-probe-result.txt`; GraniteLib alpha.20)
- `ability-wire-probe`: stun/pierce/retrieval/shredder/repairing/splash/heal helpers **PASS** after wiring (not full game-event MATRIX yet)
- `life-essence-event-probe`: `applyInstantaneousEffect` gainedLow=4.0 gainedHigh=6.92 **PASS** (HealOrHarm mixin)
- `shredder-event-probe`: `hurtAndBreak` dmgLow=1 dmgHigh=3 triples=113 **PASS**
- `stun-event-probe`: `hurtServer`+arrow stunned=true **PASS**
- `pierce-event-probe`: pierce bumps=119 **PASS** (mixin call-site)
- `remaining-ability-probe`: retrieval/repairing/splasher **PASS**

Java, Bedrock, MySQL, Geyser, LuckPerms, and the production pack stay external blockers where the environment cannot run them.

## External blockers

Four cases remain BLOCKED on user testing. `AC-MATRIX-FINAL` is NOT STARTED until they pass. MySQL round-trip, MySQL failure and LuckPerms provider were rerun on **2.3.244**.

- **AC-CLIENT-JAVA:** Join a Minecraft 26.3 Fabric server with an ordinary Java client and exercise menus, titles, and the idle line.
- **AC-CLIENT-BEDROCK:** Join through Geyser 2.12.1-SNAPSHOT b1302 and Floodgate 2.2.7-SNAPSHOT b69 with a Bedrock client.
- **AC-LIFECYCLE-GEYSER:** Same Geyser/Floodgate stack — packaged chunk/quit/menu checks.
- **AC-PACK-PRODUCTION:** Run `GraniteSkills-2.3.244.jar` + `granitelib-fabric-0.2.0-alpha.21.jar` in the production mod pack and confirm startup and skill XP.

Frozen pair: `GraniteSkills-2.3.244.jar` SHA-256 `9686FF0F7F4752087EFF33E25F3987A2EA8C72FDC0867C1BCE744DFD4D147145` with GraniteLib `0.2.0-alpha.21` SHA-256 `4DAC9D41B03C2D71126263C0352F3E7245BE498198F6028DAB73548C1AA32CAA`.

## Restored queue

Unit results that remain valid are separate cases: `AC-MENU-CLICK`, `AC-BACKUP-CODEC`, `AC-PERF-UNIT`, `AC-LIB-CONSUMER-UNIT`, and `AC-PRESET-APPEND-UNIT`. `AC-BACKUP-CODEC` is only the codec test. `AC-PRESET-APPEND-UNIT` is only the append merge. `AC-BACKUP-RUNTIME` and `AC-PRESET-RUNTIME` are the packaged command cases, and both are PASS on 2.3.206. `AC-PERF` is the dedicated-server workload, and it is PASS on the same jar. `AC-LIB-CONSUMER` is PASS against the released GraniteLib jar. `AC-MENU-MIGRATION` is PASS on 2.3.207.

`AC-BACKUP-RUNTIME`: on the disposable server, `GraniteSkills-2.3.206.jar` saved farming level 12 and 7 XP for the online probe player into `backup-2026-11-15_18-22-54.yml`. A backups path that was a file made `backup create` log `Backup create failed` and left the level at 12. After the live profile was saved at level 3, `backup load <file> cancel` logged `Backup load cancelled` and the following confirm was rejected with the level still 3. A second preview sat for 32 seconds, then confirm was rejected and the level stayed 3. A fresh confirm applied the file, logged `online refreshed`, and the online player's memory and `userdata` file both returned to level 12 and 7 XP. `graniteskills reload` logged `reload finished` and left that restored profile in place. `bad-backup.yml` with `backup_version: 99` logged `Backup load failed` and left level 12. `backup-runtime-probe-result.txt` says PASS. `AC-BACKUP-CODEC` remains the separate codec result.

`AC-PRESET-RUNTIME`: on the same jar, `preset load noprofile.zip` was rejected with `No preset.yml configuration file was found in the zip` and left the live files unchanged. `preset load modes.zip` previewed append, merge, and replace, then `confirm` wrote `backup-2026-11-15_18-27-48.yml` (`backup_version: 2`) before applying. Append kept `kept: live` and added `added: from-preset`. Merge replaced `kept` with `preset` and added the missing key. Replace wrote `kept: replaced` and `only: preset`. The skip entry had no file in the zip, so `preset-probe-skip.yml` stayed `kept: live` and the result listed it under `Skipped files`. `preset-runtime-probe-result.txt` says PASS. `AC-PRESET-APPEND-UNIT` remains the separate append test.

`AC-PERF`: the written bar is 4 players, 250 farming XP awards of 1.0, 20 claims, 100 action-bar opens capped at 100 ticks, at least 40 saves during the awards, and a 5000 ms deadline. On `GraniteSkills-2.3.206.jar`, four connected players finished that workload in 95 ms. Each ended at 250 XP, delivered 20 claims with an empty unclaimed list, and had an action-bar window of 100 ticks. Each had 40 saves during the awards, including a save at the halfway XP, and none of those saves were above 250. The userdata files then stored farming level 1 and 250 XP. `perf-probe-result.txt` says PASS. `AC-PERF-UNIT` remains the 11 ms in-memory run.

`AC-LIB-NAMED-FILE`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.2.jar` loaded `config/granitelib-named-probe/named-menu.yml`. The menu id was `named_probe`, the title was `Named Probe`, it had 3 rows, and the `marker` item was in slot 4. A second file with `rows: 0` was rejected with `'rows' must be between 1 and 6, got 0`. The loader's code source was `mods/granitelib-fabric-0.2.0-alpha.2.jar`. `lib-named-file-probe-result.txt` says PASS.

`AC-LIB-OVERLAY`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.3.jar` applied `user-menu.yml` over `default-menu.yml`. The overlay set the title to `Overlay Title` and the marker name to `Changed`. The menu id stayed `skills`, rows stayed 3, the fill stayed `gray_stained_glass_pane` / `Fill`, and the marker kept material `paper`, slot 4, lore `Default lore`, and action `open`. The untouched `other` item stayed `book` / `Other`. `lib-overlay-probe-result.txt` says PASS.

`AC-LIB-RELOAD`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.4.jar` loaded `reload_probe` with title `First`. Opening it called that listener once. A broken file was rejected and left that listener in place. Reloading the file with title `Second` installed a new listener, called `onReplaced` on the first listener, and the next open called only the new listener. The registry size stayed 1. `lib-reload-probe-result.txt` says PASS.

`AC-LIB-EXPORT`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.5.jar` loaded title `First` and name `Keep`, then reloaded title `Second` and name `Changed`. `export` wrote `exported.yml` from the loaded menu. The file contains `title: Second` and `name: Changed`, and it does not contain `First` or `Keep`. Loading that file back gives id `export_probe`, title `Second`, rows 2, marker material `paper` in slot 4, lore `Default lore`, action `open`, and fill `gray_stained_glass_pane`. `lib-export-probe-result.txt` says PASS.

`AC-LIB-STATUS`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.6.jar` loaded a valid `user.yml`. Status reported that file, fallback false, and no error. After `user.yml` was given `rows: 0`, status reported `default.yml` as the effective file, fallback true, and the error `'rows' must be between 1 and 6, got 0`. The loaded title was `Default`. `lib-status-probe-result.txt` says PASS.

`AC-LIB-DIAGNOSTICS`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.7.jar` rejected `user.yml` with `rows: 0` and loaded the default menu. The diagnostic was `config\granitelib-diagnostics-probe\user.yml :: 'rows' must be between 1 and 6, got 0`. The valid default file produced no diagnostics. The loaded title stayed `Default`. `lib-diagnostics-probe-result.txt` says PASS.

`AC-LIB-BUILDER`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.8.jar` built and loaded `builder_probe`. The loaded menu title was `Built Title`, it had 3 rows, and the `marker` item was `paper` in slots 4 and 13. `lib-builder-probe-result.txt` says PASS.

`AC-LIB-LAYOUT`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.9.jar` placed the menu's `paper` item into slot 4 of a 9-slot container. The count was 1. Every other slot stayed empty. `lib-layout-probe-result.txt` says PASS.

`AC-LIB-FILLER`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.10.jar` placed `paper` in slot 4 and filled the other eight slots of a 9-slot menu with `gray_stained_glass_pane`. Slot 4 stayed `paper`. `lib-filler-probe-result.txt` says PASS.

`AC-LIB-DYNAMIC`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.11.jar` repeated a template row that starts at slot 10. The entries were `wheat` at slot 10, `carrot` at slot 19, and `potato` at slot 28. Those three slots held `paper`, and slot 11 stayed empty. `lib-dynamic-probe-result.txt` says PASS.

`AC-LIB-SHARED-ITEM`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.12.jar` defined `close` once as `barrier`, name `Close`, action `close`. The skills menu referenced it in slot 49 and the stats menu referenced it in slot 40. Both menus kept that material, name, and action. `lib-shared-item-probe-result.txt` says PASS.

`AC-LIB-CONTEXT`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.13.jar` opened menu id `sources` with context `farming` and page 2. The listener received that menu id, that context value, and that page. `lib-context-probe-result.txt` says PASS.

`AC-LIB-PLACEHOLDER`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.14.jar` replaced `{skill}` in `Skill {skill} {missing}` with `Farming`. The name became `Skill Farming {missing}`. The unknown placeholder was reported as `missing`. `lib-placeholder-probe-result.txt` says PASS.

`AC-LIB-SORT`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.15.jar` sorted wheat 30, carrot 5, and potato 10. By name the order was `carrot, potato, wheat`. By value the order was `wheat, potato, carrot`. `lib-sort-probe-result.txt` says PASS.

`AC-LIB-PAGE`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.16.jar` paged wheat, carrot, potato, and beet with two entries per page. Page 0 was `wheat, carrot`. Page 1 was `potato, beet` and did not include wheat or carrot. `lib-page-probe-result.txt` says PASS.

`AC-LIB-PREVIOUS`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.16.jar` opened `sources` for farming, page 2, sort `name`, then opened `stats`. Back returned to `sources` with skill `farming`, page 2, and sort `name`. A second back left that root in place. `lib-previous-probe-result.txt` says PASS.

`AC-LIB-OPEN-ID`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.17.jar` opened the registered menu `sources` once. Opening `missing` was rejected with `unknown menu id 'missing'` and did not open another menu. `lib-open-id-probe-result.txt` says PASS.

`AC-LIB-CLICK-PERM`: on the dedicated server, packaged `granitelib-fabric-0.2.0-alpha.18.jar` dispatched `close` at slot 49 for actor `Op`. The action ran once and received that actor. The same click for actor `Guest` was denied and did not run. `lib-click-perm-probe-result.txt` says PASS.

`AC-LIB-CONSUMER`: `granitelib-consumer` compiled and tested against `granitelib-fabric-0.2.0-alpha.18.jar` only. Granite Skills was not on the classpath (`graniteSkillsAbsent=true`). It opened menu `fixture` with title `Opened`, ran action `close` for actor `Consumer`, then reloaded the menu to title `Reloaded`. The loader code source was that released jar. `granitelib-consumer/build/independent-consumer-result.txt` says PASS. `AC-LIB-CONSUMER-UNIT` remains the earlier project-classpath result.

`AC-MENU-MIGRATION`: on the dedicated server, `GraniteSkills-2.3.207.jar` opened Skills, Stats, Level Progression, Stat Info, Sources, Abilities, Leaderboard, and Unclaimed Items through GraniteLib open-by-id. The registry held those 8 menus. Each open reported its own id and no error. `menu-migration-probe-result.txt` says PASS. `AC-MENU-CLICK` remains the click-filter test.

`AC-MENU-SKILLS`: on the same server and `GraniteSkills-2.3.207.jar`, opening Skills went through GraniteLib. The log said `GraniteLib menu open id=skills`. History was `skills`. The open container was `GraniteClickable9x6Handler`. `menu-skills-probe-result.txt` says PASS.

`AC-MENU-STATS`: on the same server and `GraniteSkills-2.3.207.jar`, opening Stats went through GraniteLib. The log said `GraniteLib menu open id=stats`. History was `stats`. The open container was `GraniteClickable9x6Handler`. `menu-stats-probe-result.txt` says PASS.

`AC-MENU-LEVEL`: on the same server and `GraniteSkills-2.3.207.jar`, opening Level Progression for farming, page 1, went through GraniteLib. The log said `GraniteLib menu open id=level_progression`. History was `level_progression` with skill `farming` and page 1. The open container was `GraniteClickable9x6Handler`. `menu-level-probe-result.txt` says PASS.

`AC-MENU-STAT-INFO`: on the same server and `GraniteSkills-2.3.207.jar`, opening Stat Info for strength, page 1, went through GraniteLib. The log said `GraniteLib menu open id=stat_info`. History was `stat_info` with stat `strength` and page 1. The open container was `GraniteClickable9x6Handler`. `menu-stat-info-probe-result.txt` says PASS.

`AC-MENU-SOURCES`: on the same server and `GraniteSkills-2.3.207.jar`, opening Sources for farming, page 0, sort `ALPHABETICAL` went through GraniteLib. The log said `GraniteLib menu open id=sources`. History was `sources` with skill `farming`, page 0, and sort `ALPHABETICAL`. The open container was `GraniteClickable9x6Handler`. `menu-sources-probe-result.txt` says PASS.

`AC-MENU-ABILITIES`: on the same server and `GraniteSkills-2.3.207.jar`, opening Abilities for farming went through GraniteLib. The log said `GraniteLib menu open id=abilities`. The open container was `GraniteClickable9x6Handler`. `menu-abilities-probe-result.txt` says PASS.

`AC-MENU-LEADERBOARD`: on the same server and `GraniteSkills-2.3.207.jar`, opening the Power leaderboard on page 1 went through GraniteLib. The log said `GraniteLib menu open id=leaderboard`. History was `leaderboard` with page 1 and sort `POWER`. The open container was `GraniteClickable9x6Handler`. `menu-leaderboard-probe-result.txt` says PASS.

`AC-MENU-UNCLAIMED`: on the same server and `GraniteSkills-2.3.207.jar`, opening Unclaimed Items for one registered reward went through GraniteLib. The log said `GraniteLib menu open id=unclaimed_items`. The open container was `GraniteClickable9x6Handler`. `menu-unclaimed-probe-result.txt` says PASS.

`AC-MENU-INVALID-YAML`: on `GraniteSkills-2.3.208.jar`, a valid `granite/templates/skills.yml` loaded with title `Probe Valid Skills`. Replacing it with `rows: 0` and reloading named that file and the rows error, and the loaded title stayed `Probe Valid Skills`. The probe file was removed afterward. `menu-invalid-yaml-probe-result.txt` says PASS.

`AC-XP-BREAK`: on `GraniteSkills-2.3.208.jar`, a survival player destroyed one natural pumpkin through `destroyBlock`. Farming XP rose by 4, the pumpkin source amount, and a second destroy of that same position added nothing. A second pumpkin awarded 4 again. `xp-break-probe-result.txt` says PASS.

`AC-XP-MULTIBLOCK`: on `GraniteSkills-2.3.209.jar`, a survival player broke one natural dirt block and an external removal cleared the connected dirt without a break event. The origin awarded 0.3 immediately. After the follow-up, excavation XP was 0.9: the origin once, plus the two other natural dirt blocks. The placed dirt in that vein was skipped, and a disconnected dirt block was not awarded. `xp-multiblock-probe-result.txt` says PASS.

`AC-XP-INTERACT`: on `GraniteSkills-2.3.209.jar`, a lectern source whose only trigger was `INTERACT` awarded 6.5 farming XP on right-click. Breaking another lectern awarded 0. The temporary source was removed and sources were reloaded. `xp-interact-probe-result.txt` says PASS.

`AC-XP-COLLECT`: on `GraniteSkills-2.3.209.jar`, a beehive source whose only trigger was `COLLECT` awarded 8 farming XP when a glass bottle collected honey. The hive went from full to empty and the player received one honey bottle. A bottle on an empty hive and a stick on a full hive awarded 0. The temporary source was removed and sources were reloaded. `xp-collect-probe-result.txt` says PASS.

`AC-XP-FISH`: on `GraniteSkills-2.3.210.jar`, reeling a bobber through `retrieve` caught cod and awarded 25 fishing XP, the cod source amount, through the fishing handler. `xp-fish-probe-result.txt` says PASS.

`AC-XP-DAMAGE`: on `GraniteSkills-2.3.210.jar`, a player hit a pig for 2 damage through a fighting source whose only trigger was `DAMAGE`. Fighting XP rose by 8, that source amount times the damage. Defense XP stayed 0. The temporary source was removed and sources were reloaded. `xp-damage-probe-result.txt` says PASS.

`AC-XP-DEFENSE`: on `GraniteSkills-2.3.210.jar`, a pig hit a survival player for 2 damage. Defense XP rose by 2, the `mob_damage` source amount times the damage. Fighting XP stayed 0. `xp-defense-probe-result.txt` says PASS.

`AC-XP-FALL`: on `GraniteSkills-2.3.210.jar`, a survived fall of 2 damage awarded 2.8 agility XP, the `fall_damage` source amount times the damage. A later fall that dealt 4 damage and would kill the player left agility XP at 2.8. `xp-fall-probe-result.txt` says PASS.

`AC-XP-KILL`: on `GraniteSkills-2.3.210.jar`, a player kill of a pig awarded 2 fighting XP, the pig source. A pig killing another pig left that XP at 2. A pig killing the player also left it at 2, so the 50 XP player source was not awarded. `xp-kill-probe-result.txt` says PASS.

`AC-XP-POTION`: on `GraniteSkills-2.3.210.jar`, a splash potion owned by one player killed a pig while another player was the damage cause. The owner received 9 fighting XP, the thrown-potion source. The other player received 0. The temporary source was removed and sources were reloaded. `xp-potion-probe-result.txt` says PASS.

`AC-XP-SPAWNER`: on `GraniteSkills-2.3.210.jar`, fighting `spawner_multiplier` was set to 0.5 for the check. A natural pig kill awarded 2 fighting XP. A spawner pig kill awarded 1. The multiplier was restored to 1 and skills were reloaded. `xp-spawner-probe-result.txt` says PASS.

`AC-XP-BREW-OWNER`: on `GraniteSkills-2.3.211.jar`, one player started a nether-wart brew and another player took the potion out. The starter received 10 alchemy XP, the awkward takeout source. The player who took it out received 0. `xp-brew-owner-probe-result.txt` says PASS.

`AC-XP-ANVIL`: on `GraniteSkills-2.3.211.jar`, taking an anvil result with cost 5 spent 5 levels and then the cost was 0. Enchanting XP rose by 60, the combine-books source amount times that pre-consumption cost. `xp-anvil-probe-result.txt` says PASS.

`AC-XP-CONSUME`: on `GraniteSkills-2.3.211.jar`, drinking a water potion awarded 0 alchemy XP. Finishing a golden apple awarded 30, the golden apple consume source. `xp-consume-probe-result.txt` says PASS.

`AC-XP-SPLASH`: on `GraniteSkills-2.3.212.jar`, a swiftness splash potion owned by one player awarded 40 alchemy XP, the regular splash source. A bystander received 0. `xp-splash-probe-result.txt` says PASS.

`AC-XP-MANA-USE`: on `GraniteSkills-2.3.212.jar`, activating Treecapitator once consumed 20 mana and awarded 60 alchemy XP, the configured 3 XP times that mana cost. A second log break while the ability was already active left alchemy XP at 60. The temporary source was removed and sources were reloaded. `xp-mana-use-probe-result.txt` says PASS.

`AC-XP-DOUBLE`: on `GraniteSkills-2.3.212.jar`, two break callbacks for one mature sweet berry bush awarded 5 farming XP once. An interact on that same bush awarded 5 more, for 10. `xp-double-probe-result.txt` says PASS.

`AC-XP-WRONG-PLAYER`: on `GraniteSkills-2.3.212.jar`, an arrow owned by one player killed a pig while another player was recorded on that arrow damage. The shooter received 2 archery XP, the projectile pig source. The recorded player received 0, and neither player received fighting XP. `xp-wrong-player-probe-result.txt` says PASS.

`AC-XP-AURA-PATHS`: on `GraniteSkills-2.3.213.jar`, a wheat crop that grew on its own went from age 0 to 1 with no aura and to 3 beside a player whose Growth Aura adds 2 stages. Bone meal on wheat went to 2 with no aura and to 4 beside that player. Nether wart grew from 0 to 1 both beside that player and without one. `xp-aura-paths-probe-result.txt` says PASS.

`AC-XP-ANTIAFK`: on `GraniteSkills-2.3.213.jar`, anti-AFK was enabled for the run and the block check's count was 100. One hundred pumpkin breaks at the same facing counted to 99. The next break at that facing counted 100 and added no farming XP. Turning before the next break reset the count and awarded 4.4, the 4 XP pumpkin source times the 1.1 farming multiplier. `anti_afk.enabled` was restored to false. `xp-antiafk-probe-result.txt` says PASS.

`AC-STORE-CONFIRM`: on `GraniteSkills-2.3.213.jar`, `storage removeallmodifiers confirm` with nothing pending left the stored `probe_strength` modifier in place and wrote no backup. The preview also left that modifier and wrote no backup. Confirm after the preview removed the modifier, left farming level 1, and wrote one backup. That backup and the probe user were removed, and the one other stored user the wipe touched was restored. `store-confirm-probe-result.txt` says PASS.

`AC-STORE-STALE`: on `GraniteSkills-2.3.213.jar`, the remove-all-modifiers preview registered `storage:removeallmodifiers` and left `probe_strength` in place. After 33038 ms that pending key was gone. Confirm then left the modifier and farming level 1, wrote no backup, and changed no other stored user. The probe user was removed. `store-stale-probe-result.txt` says PASS.

`AC-STORE-CANCEL`: on `GraniteSkills-2.3.214.jar`, the remove-all-modifiers preview registered `storage:removeallmodifiers`. Cancel cleared that key and left `probe_strength` and farming level 1 in place. Confirm after the cancel still left the modifier, wrote no backup, and changed no other stored user. The probe user was removed. `store-cancel-probe-result.txt` says PASS.

`AC-BACKUP-FAIL`: on `GraniteSkills-2.3.214.jar`, four backup files were already in the backups folder. The next save threw `ConfigurateException` because the folder was not writable. Those four files were unchanged, and no new backup file was written. Write access was restored. `backup-fail-probe-result.txt` says PASS.

`AC-BACKUP-RELOAD`: on `GraniteSkills-2.3.214.jar`, restoring `reload_sees.yml` wrote farming level 15 and 2.5 XP. `graniteskills reload` finished, and the stored user still read as level 15 and 2.5 XP. `backup list` after that reload still showed `reload_sees.yml`. The probe user, that backup, and the pre-restore backup were removed. `backup-reload-probe-result.txt` says PASS.

`AC-BACKUP-ONLINE`: on `GraniteSkills-2.3.214.jar`, an online player was at farming level 1 and 0 XP. Restoring `online_restore.yml` logged `online refreshed`, and that same player then read farming level 12 and 7 XP. The player was not replaced. The probe user and that backup were removed. `backup-online-probe-result.txt` says PASS.

`AC-PRESET-MERGE`: on `GraniteSkills-2.3.215.jar`, `preset load merge_keep.zip confirm` merged a file that already had `kept: live`, `shared: live`, and `only_live: stay`. Those values stayed, and `added: from-preset` was added. The preset's `kept: preset` and `shared: preset` were not written. The probe file, the zip, and the pre-apply backup were removed. `preset-merge-probe-result.txt` says PASS.

`AC-PRESET-REPLACE`: on `GraniteSkills-2.3.215.jar`, `preset load replace_only.zip` left `kept: live` and `only_live: stay` in place and wrote no backup. Confirm then wrote `backup-2026-11-15_20-59-9.yml` with `backup_version: 2` and replaced the file with `kept: replaced` and `only: preset`. The probe file, the zip, and that backup were removed. `preset-replace-probe-result.txt` says PASS.

`AC-PRESET-SKIP`: on `GraniteSkills-2.3.215.jar`, `preset load skip_file.zip confirm` listed `preset-skip-probe.yml` under skipped files because the zip had no copy of that file. The file stayed `kept: live`. The probe file, the zip, and the pre-apply backup were removed. `preset-skip-probe-result.txt` says PASS.

`AC-PRESET-MISSING`: on `GraniteSkills-2.3.215.jar`, `preset load missing_preset.zip` was rejected with `No preset.yml configuration file was found in the zip`. The zip contained `files/preset-missing-probe.yml`, and that file was not written. Confirm wrote no backup. The zip was removed. `preset-missing-probe-result.txt` says PASS.

`AC-YAML-TWO-USERS`: on `GraniteSkills-2.3.215.jar`, two profiles were saved at the same time and then loaded at the same time. One came back as farming level 4, 11.5 XP, and 31 mana. The other came back as farming level 9, 22.5 XP, and 42 mana. Each YAML file contained only that user's id. Both files were removed. `yaml-two-users-probe-result.txt` says PASS.

`AC-YAML-OFFLINE`: on `GraniteSkills-2.3.215.jar`, a YAML profile for a player who was not on the server loaded farming level 6, 3.25 XP, and 18 mana. The loaded profile had no player. The file was removed. `yaml-offline-probe-result.txt` says PASS.

`AC-YAML-RESTART`: on `GraniteSkills-2.3.215.jar`, a profile was saved as farming level 13, 8.5 XP, and 27 mana, and the server process exited. A new process loaded that same file as level 13, 8.5 XP, and 27 mana. The player was not online. The file was removed. `yaml-restart-probe-result.txt` says PASS.

`AC-YAML-MODIFIERS`: on `GraniteSkills-2.3.215.jar`, a profile saved a strength modifier of 3.5 ADD and a luck modifier of 6.25 MULTIPLY. Loading that YAML returned the same names, keys, values, and operations. The file was removed. `yaml-modifiers-probe-result.txt` says PASS.

`AC-YAML-ITEMS`: on `GraniteSkills-2.3.215.jar`, a profile saved unclaimed items `diamond 3` and `probe_stone 7`. Loading that YAML returned the same items in that order. The file was removed. `yaml-items-probe-result.txt` says PASS.

`AC-MIGRATION-ITEM`: on `GraniteSkills-2.3.215.jar`, a player inventory, a chest, and an item entity each held paper stored as `AuraSkills` `auraskills/health`. The first migration rewrote all three to `GraniteSkills` `graniteskills/health`. The second migration left those three items unchanged. `migration-item-probe-result.txt` says PASS.

`AC-CMD-CONSOLE`: on `GraniteSkills-2.3.215.jar`, the server console, which is not a player, ran `storage info`. The reply named the Granite storage diagnostics and `FileStorageProvider`. `cmd-console-probe-result.txt` says PASS.

`AC-CMD-OFFLINE`: on `GraniteSkills-2.3.215.jar`, `profile skills OfflineProbe` read a YAML profile for a player who was not connected. The reply included that player's id and farming level 17. The player was still absent afterward. The profile file and the temporary name-cache entry were removed. `cmd-offline-probe-result.txt` says PASS.

`AC-CMD-VALIDATION`: on `GraniteSkills-2.3.215.jar`, `storage mergeskills notaskill farming` and its confirm were rejected with `Unknown or disabled source skill: notaskill`. A stored farming level of 5 was unchanged, and no backup was written. The probe profile was removed. `cmd-validation-probe-result.txt` says PASS.

`AC-LB-CMD-POWER`: on `GraniteSkills-2.3.215.jar`, `/top` printed the all-skills leaderboard with `PowerHigh` at 500 before `PowerLow` at 400. The probe profiles and temporary name-cache entries were removed. `lb-cmd-power-probe-result.txt` says PASS.

`AC-LB-CMD-AVERAGE`: on `GraniteSkills-2.3.215.jar`, `/top average` printed the skill-average leaderboard with `AvgHigh` at 73 before `AvgLow` at 41. The probe profiles and temporary name-cache entries were removed. `lb-cmd-average-probe-result.txt` says PASS.

`AC-LB-CMD-SKILL`: on `GraniteSkills-2.3.215.jar`, `/skilltop farming` printed the farming leaderboard with `FarmHigh` at 50 before `FarmLow` at 10. `FarmLow` had the higher mining level. The probe profiles and temporary name-cache entries were removed. `lb-cmd-skill-probe-result.txt` says PASS.

`AC-LIFECYCLE-QUIT`: on `GraniteSkills-2.3.215.jar`, a disconnect for a player with no loaded profile returned in 0 ms and the server continued. Shaded MiniMessage, Examinable, and Adventure `Component` loaded, and formatted text was `dev.granite.graniteskills.shaded.kyori.adventure.text.TextComponentImpl`. `lifecycle-quit-probe-result.txt` says PASS.

`AC-SRC-FALLBACK` only records that a missing embedded file sets a flag. `AC-SRC-RECOVERY` is the recovery contract. On `GraniteSkills-2.3.205.jar`, farming.yml omitted torchflower and set wheat XP to 99. Reload kept wheat at 99 and restored torchflower at 9 from the embedded file. `src-recovery-probe-result.txt` says PASS.

The acceptance file now has 164 cases, including the GraniteLib contract rows, the eight menu rows, the missing XP rows, and the storage-mutation rows. `required_ids` lists every case. The stop hook refuses an external-only handoff when a required id is missing, an id is duplicated, a status is unknown, or a PASS lacks the evidence kind it requires.

Statuses: NOT STARTED, IMPLEMENTING, IMPLEMENTED—UNVERIFIED, FAILED, BLOCKED, PASS, EXCLUDED BY EXISTING SCOPE.

## Text and HUD

| Case | Expected | Path | Variants | Status |
| --- | --- | --- | --- | --- |
| AC-TEXT-MINI | `<red>` does not survive as a raw tag | `FabricMessageProvider` via `FabricTextFormatter` | unit | PASS (`FabricTextFormatterTest`) |
| AC-TEXT-CHAT | `FabricUser.sendMessage` sends a native component, not `Component.literal` | `FabricUser` | dedicated server | PASS. `F:\gs-lifecycle-server` with `GraniteSkills-2.3.195.jar`. A gold Adventure component came out as `net.minecraft.network.chat.MutableComponent` with color `gold`, and that same component was the content of `ClientboundSystemChatPacket`. Result file `chat-probe-result.txt`. |
| AC-TEXT-ACTION | Action bar uses the same formatter | `FabricUiProvider.sendActionBar` | dedicated, Java, Bedrock | PASS for the dedicated server. `<gold>Mana` left as native `MutableComponent` text `Mana` with gold, inside an overlay `ClientboundSystemChatPacket`. `F:\gs-lifecycle-server\action-bar-probe-result.txt`. Java and Bedrock clients stay BLOCKED. |
| AC-TEXT-TITLE | Title, subtitle, and timings are packets | `FabricUiProvider.sendTitle` | dedicated, Java, Bedrock | PASS for the dedicated server. `<aqua>Title` and `<gold>Sub` with timings 5, 40, 15 left as `ClientboundSetTitlesAnimationPacket`, `ClientboundSetSubtitleTextPacket` (`Sub`, gold), and `ClientboundSetTitleTextPacket` (`Title`, aqua). `F:\gs-lifecycle-server\title-probe-result.txt`. Java and Bedrock clients stay BLOCKED. |
| AC-TEXT-BROKEN | A bad MiniMessage tag does not crash the tick | `FabricTextFormatter.toComponent` | unit | PASS (formatter catches `ParsingException`) |
| AC-HUD-SMART-EXPIRE | Stable health/mana tuple does not extend the Smart window | `ActionBarManager.refreshSmartWindow` | unit logic, dedicated | PASS for the window logic. The same 20 health and 10/20 mana moved the window from 99 to 98. A health change to 19 moved it to 99. `SmartWindowExpireTest` passed, 1 test, 0 failures. No dedicated client was connected. |
| AC-HUD-JOIN | Join or world change opens 100 Smart ticks | `ActionBarManager` world key | dedicated | PASS for the window logic. Join left 99 ticks, which is 100 minus the same update. After the window had fallen to 90, changing world from `world` to `nether` with the same health and mana raised it to 99. `SmartWindowExpireTest.joinAndWorldChangeOpen100SmartTicks` passed. No dedicated client was connected. |
| AC-HUD-COMBAT | Damage opens 120 Smart ticks | `FabricCombatXpHandler` | dedicated | PASS. Dedicated server `F:\gs-lifecycle-server`, jar `GraniteSkills-2.3.195.jar`. Four damage on a player moved the Smart window from 0 to 120. `combat-probe-result.txt`. |
| AC-HUD-ALWAYS | Always stays visible while idle is allowed | `ActionBarIdleMode.ALWAYS` | YAML, MySQL, dedicated | PASS for the idle line. With the Smart window at 0 and idle allowed, Always still sent `20/20 10/20`. Smart with an expired window sent nothing. Always sent nothing after idle was turned off. `alwaysStaysVisibleWhileIdleIsAllowed` passed. YAML and MySQL storage of the mode stay on their own rows. No dedicated client was connected. |
| AC-HUD-OFF | Off sends no idle line | `User.isActionBarEnabled` | YAML, MySQL | PASS for the idle line. With idle allowed, Off sent nothing. Switching to Always sent `20/20 10/20`, and switching back to Off sent nothing. `offSendsNoIdleLine` passed. YAML and MySQL storage of the mode stay on their own rows. |
| AC-HUD-YAML | `idle_mode` round-trips; legacy boolean still loads | `FileStorageProvider` | unit | PASS for the load rule (`ActionBarIdleModeTest`). The user-file round trip is `AC-HUD-YAML-FILE`. |
| AC-HUD-MYSQL | Same mode round-trips in MySQL key values | `SqlStorageProvider`, `SqlUserLoader` | BLOCKED. External. Start Docker Desktop so `dockerDesktopLinuxEngine` exists, then run `docker run -d --name gs-mysql-test -e MYSQL_ROOT_PASSWORD=granite -e MYSQL_DATABASE=graniteskills -p 3307:3306 mysql:8.4` and round-trip `idle_mode`. Do not use MariaDB. |
| AC-HUD-XP-PRIORITY | Idle update skips while XP or ability line is active | `ActionBarManager` `isGainingXp` / `isPaused` | dedicated | PASS for the idle update. An Always player sent `20/20 10/20` while idle. After `sendXpActionBar`, the next idle update sent nothing. After that XP line was cleared, idle sent again. `sendAbilityActionBar` showed its line, and the next idle update sent nothing until the pause was cleared. `idleSkipsWhileXpOrAbilityLineIsActive` passed. No dedicated client was connected. |

## Traits and death

| Case | Expected | Path | Status |
| --- | --- | --- | --- |
| AC-TRAIT-SATURATION | Saturation above zero adds bonus health | `FabricHealthRegenTraits` | PASS. Dedicated server `F:\gs-lifecycle-server`, jar `GraniteSkills-2.3.195.jar`. Saturation 0 left health at 10. Saturation 4 with a bonus of 2 raised health to 12. Food was 10, so the hunger path did not apply. `saturation-probe-result.txt`. |
| AC-TRAIT-HUNGER | Food at least 14 and no saturation adds bonus health | `FabricHealthRegenTraits` | PASS. Dedicated server `F:\gs-lifecycle-server`, jar `GraniteSkills-2.3.195.jar`. Food 13 and saturation 0 left health at 10. Food 14 and saturation 0, with a hunger-regen bonus of 3, raised health to 13. `hunger-probe-result.txt`. |
| AC-TRAIT-DOUBLE | Chance equal to the trait value duplicates block drops | `FabricDoubleDrop` | PASS. `GraniteSkills-2.3.196.jar` on `F:\gs-lifecycle-server`. A fixed roll of 50 against stone: chance 0 and chance 40 added no cobblestone; chance 60 added 1 cobblestone. Double Drop is now loaded from the traits section. `double-drop-probe-result.txt`. |
| AC-TRAIT-XP | Vanilla experience points scale by the trait. This is not skill XP | `ServerPlayerExperienceBonusMixin` | PASS. `GraniteSkills-2.3.196.jar` on `F:\gs-lifecycle-server`. `giveExperiencePoints(100)` added 100 with no experience bonus and 150 with a bonus of 50. Farming skill XP stayed at 5. `xp-trait-probe-result.txt`. |
| AC-TRAIT-ANVIL | Anvil level cost is reduced and stays at least 1 | `FabricAnvilDiscount` | PASS. `GraniteSkills-2.3.197.jar` on `F:\gs-lifecycle-server`. A cost of 20 stayed 20 with no trait, fell to 7 at trait 40, and stayed at 1 when the unclamped result was 0. The cost slot is a `DataSlot`. `anvil-probe-result.txt`. |
| AC-DEATH-RESET | Configured skill reset or XP ratio runs on player death and not on mob kills | `FabricPlayerDeathReset` | PASS. `GraniteSkills-2.3.197.jar` on `F:\gs-lifecycle-server`. Config was `reset_skills: false`, `reset_xp: true`, `reset_xp_ratio: 0.5`. A zombie death left farming at level 4 and 100 XP. The player's death left level 4 and cut XP to 50. `death-reset-probe-result.txt`. |
| AC-TRAIT-EXISTING | Luck, HP, speed, mana, crit, attack, reduction, gathering luck stay registered | `FabricTraitManager` | PASS at runtime on `GraniteSkills-2.3.198.jar`. Luck +3, max health 24 from HP +4, movement speed +0.1, max mana clamped 100 down to 30, one mana regen step raised 0 to 1.1, crit chance 100 always crits and 0 does not, crit damage adds 0.5, attack adds a 0.4 multiplier, damage reduction adds a negative modifier, and farming luck gained 40 from its ability. `trait-runtime-probe-result.txt`. |

## Placed blocks

| Case | Expected | Path | Status |
| --- | --- | --- | --- |
| AC-PLACE-MARK | A successful block place is remembered and cleared, including a reload of `placed-blocks.txt` | `FabricPlacedBlocks.mark`, `isPlaced`, `clear` | PASS. `FabricPlacedBlocksTest` passed: 1 test, 0 failures. This does not cover a live piston push. |
| AC-PLACE-BREAK | Breaking clears the mark | `FabricBlockBreakHandler` | PASS. `GraniteSkills-2.3.198.jar` on `F:\gs-lifecycle-server`. Stone at 30,120,30 was marked, broken to air, and the record was gone from memory and `placed-blocks.txt`. The marked neighbor at 31,120,30 stayed. `place-break-probe-result.txt`. |
| AC-PLACE-RESTART | `placed-blocks.txt` reloads the marks | `FabricPlacedBlocks` | PASS. A new dedicated-server process of `GraniteSkills-2.3.198.jar` loaded `placed-blocks.txt`. 31,120,30 was placed again without a new mark. 30,120,30, cleared on the previous run, stayed unmarked. `place-restart-probe-result.txt`. |
| AC-PLACE-XP | `check_replace` uses the mark | `FabricBlockLevelerHelper.isPlaced` | PASS. `GraniteSkills-2.3.200.jar` on `F:\gs-lifecycle-server`. `check_block_replace.enabled` is true. A marked pumpkin awarded 0 farming XP. An unmarked pumpkin awarded 4. The mark is read before the break clears it. `place-xp-probe-result.txt`. |
| AC-PLACE-SPEED | Speed Mine uses the mark | `FabricSpeedMine` | PASS. `GraniteSkills-2.3.200.jar` on `F:\gs-lifecycle-server`. A ready Speed Mine did not activate on marked stone. It activated on unmarked stone. `speed-mine-probe-result.txt`. |
| AC-PLACE-TREE | Treecapitator uses the mark | `FabricTreecapitator` | PASS. `GraniteSkills-2.3.201.jar` on `F:\gs-lifecycle-server`. A ready Treecapitator did not activate on a marked oak log, and the log above it stayed. It activated on an unmarked oak log. The break handler now clears the mark after Treecapitator reads it. `tree-probe-result.txt`. |
| AC-PLACE-PISTON | A piston push moves a tracked block's record and clears records for blocks the piston destroys | `PistonPlacedBlockMixin` | PASS for the piston push. Disposable Fabric 26.3 server, Loader 0.20.1, Java 25, jar SHA-256 `73ce6db4e410b0f676ffc99f10be4308767ac01515a5780b32a0668649d08663`. A north-facing piston at 0,200,0 was powered by a redstone block. Result file `F:\gs-piston-server\piston-probe-result.txt`: source record false, destination record true, piston `extended=true`, destination block stone. `PlacedBlockMovesTest` was not used as this pass. |
| AC-PLACE-FALL | A falling block that lands moves its placed record from the start position to the landing position | `FallingBlockPlacedMixin` on `FallingBlockEntity.tick` when the block is placed | PASS. Same disposable server, current jar SHA-256 `9bd2369d8d94c2789dcf0c69afeb73315750b2024d2941ba515b209ab3b18ff3`. Sand marked at 4,210,4 fell onto stone at 4,200,4. Result file `F:\gs-piston-server\falling-probe-result.txt`: source record false, landed record at 4,201,4, block `minecraft:sand`. |

## Loot and requirements

| Case | Expected | Path | Status |
| --- | --- | --- | --- |
| AC-LOOT-REQ | Skill, stat, permission, world, and biome nodes can reject loot | `FabricNodeLootRequirements` | PASS. `GraniteSkills-2.3.202.jar` on `F:\gs-lifecycle-server`. Farming level 3 rejected a level-10 skill node and allowed level 3. Strength 0 rejected 50 and allowed 0. Permission `graniteskills.loot.secret` rejected. World `nether` rejected and `world` allowed. Biome `minecraft:the_end` rejected and `minecraft:plains` allowed. `loot-req-probe-result.txt`. |
| AC-LOOT-EMPTY | No requirement list still allows the roll | `FabricNodeLootRequirements` | PASS. `GraniteSkills-2.3.202.jar` on `F:\gs-lifecycle-server`. A loot table with `requirements: []`, and one with no requirements node, both allowed `checkRequirements` for an offline player. A farming level-99 node on the same path rejected that player. `loot-empty-probe-result.txt`. |
| AC-LOOT-BLOCK | A block award can roll that skill's item loot | `FabricSkillLoot` from `FabricBlockBreakHandler` | PASS on `GraniteSkills-2.3.204.jar`. The 2.3.202 bone x2 result used `max(min, max)` and is not a range proof. `FabricSkillLootAmountTest` saw 1, 2, and 3 from a 1-3 range. On `F:\gs-lifecycle-server`, a 1-3 stick entry rolled 1, a console command printed `granite-loot-command`, and an entity entry spawned a chicken. `inspection-probe-result.txt`. |
| AC-LOOT-MOB | A player mob kill can roll that skill's item loot | `FabricEntityKillHandler` | PASS. `GraniteSkills-2.3.202.jar` on `F:\gs-lifecycle-server`. A player zombie kill awarded 7 fighting XP and rolled `minecraft:emerald` x1 from a fighting loot table. The table was removed after the run. `loot-mob-probe-result.txt`. |
| AC-LOOT-FISH | Fishing keeps its own handler | `FabricFishingLootHandler` | PASS. `GraniteSkills-2.3.202.jar` on `F:\gs-lifecycle-server`. A cod catch went through `FabricFishingXpHandler.onFishCaughtStat`, awarded fishing XP, and put `minecraft:slime_ball` x3 from `loot/fishing.yml` into the inventory. The rare pool chance was 100 for the run and restored to 2. `loot-fish-probe-result.txt`. |
| AC-LOOT-EXCAVATION | Excavation uses the block path when the matched skill is excavation | `FabricSkillLoot` | PASS. `GraniteSkills-2.3.202.jar` on `F:\gs-lifecycle-server`. A dirt break matched excavation, awarded 0.3 excavation XP, and dropped `minecraft:feather` x1 from `loot/excavation.yml` through the block-break handler. The rare pool chance was 100 for the run and restored to 0. `loot-excavation-probe-result.txt`. |

## Sources

| Case | Expected | Path | Status |
| --- | --- | --- | --- |
| AC-SRC-REJECT | One bad source is listed and the rest of the file still loads | `SourceLoader` | PASS. `GraniteSkills-2.3.202.jar` on `F:\gs-lifecycle-server`. `farming.yml` gained `broken_probe` with xp 0. Reload kept 19 sources, including wheat, and recorded the reject as `not version-valid or non-positive xp`. The file was restored and reloaded. `src-reject-probe-result.txt`. |
| AC-SRC-FALLBACK | Missing embedded file sets the fallback flag | `SourceLoader` | PASS. `GraniteSkills-2.3.202.jar` on `F:\gs-lifecycle-server`. Endurance has no embedded `sources/endurance.yml`. Loading its user file set `embeddedFallbackUsed` and loaded `fallback_stone`. Farming, which has an embedded file, kept the flag false. `endurance.yml` was restored. `src-fallback-probe-result.txt`. |
| AC-SRC-DIAG | `/sources diagnose` prints those rejects | existing command reads `rejectedSources` | PASS. `GraniteSkills-2.3.203.jar` on `F:\gs-lifecycle-server`. After `broken_probe` was rejected, the server console ran `/sources diagnose farming`. The log printed `rejected source=broken_probe reason=not version-valid or non-positive xp`. The console can run diagnose without a LuckPerms node. `farming.yml` was restored. `src-diag-probe-result.txt`. |

## Permissions

| Case | Expected | Path | Status |
| --- | --- | --- | --- |
| AC-PERM-ZERO | No provider yields multiplier 0 | `FabricUser.getPermissionMultiplier` | PASS. `GraniteSkills-2.3.203.jar` on `F:\gs-lifecycle-server`. No LuckPerms mod is installed. A resolved player had farming and global multipliers of 0. The unset node `graniteskills.multiplier.100` followed its default: false when the default was false, true when the default was true. `perm-zero-probe-result.txt`. |
| AC-PERM-GLOBAL | `graniteskills.multiplier.<percent>` adds percent/100 for the listed percents | `FabricUser` | PASS. `GraniteSkills-2.3.203.jar` on `F:\gs-lifecycle-server`. Granting 10 and 25 produced 0.35. Granting every listed percent produced 21.03. An unlisted 7 and a farming-specific 50 did not change the global multiplier. `perm-global-probe-result.txt`. |
| AC-PERM-SKILL | `graniteskills.multiplier.<skill>.<percent>` adds only for that skill | `FabricUser` | PASS. `GraniteSkills-2.3.203.jar` on `F:\gs-lifecycle-server`. `graniteskills.multiplier.farming.50` made farming 0.5 and left mining and the global multiplier at 0. Adding `graniteskills.multiplier.mining.25` made mining 0.25 and left farming at 0.5 and the global multiplier at 0. `perm-skill-probe-result.txt`. |
| AC-ITEM-DUPLICATE | A second registration of the same item id keeps the first stack | `FabricItemRegistry.register` | PASS. `GraniteSkills-2.3.203.jar` on `F:\gs-lifecycle-server`. `duplicate_probe` was registered as 3 diamonds, then as 7 emeralds. The stored stack stayed `minecraft:diamond` x3. The id was removed afterward. `item-duplicate-probe-result.txt`. |
| AC-TRANSFER-CONFIRM | A transfer onto existing progress is refused until confirm | `FabricTransferCommands` | PASS. `GraniteSkills-2.3.203.jar` on `F:\gs-lifecycle-server`. The destination stayed at farming level 4 and 8 XP, and the console said to run the command again with confirm. With confirm, the destination became level 9 and 40 XP. The source stayed at level 9 and 40 XP. `transfer-confirm-probe-result.txt`. |
| AC-MENU-HISTORY | Opening two built-in menus lets history return to the previous one | `FabricGraniteSkillsMenus` and `GraniteMenuHistory` | PASS on `GraniteSkills-2.3.204.jar`. The 2.3.203 skills-then-stats result did not carry skill, page, or sort. Back from sources (farming, page 1, DESCENDING) returned level progression for farming at page 2, then skills at page 2. `inspection-probe-result.txt`. |
| AC-LB-MODE | Power and Average leaderboard GUI modes rank differently | `FabricLeaderboardMenuGui` | PASS on `GraniteSkills-2.3.204.jar`. The 2.3.203 ranking result did not exercise the template controls. The bundled template shows Power board, Average board, and Skill board. A disk override showed Disk power. A template without those controls fell back to the built-in Average button. Per-skill open recorded `farming/SKILL`. `inspection-probe-result.txt`. The earlier Power/Average order flip remains in `lb-mode-probe-result.txt`. |
| AC-PERM-PROVIDER | LuckPerms allow, deny, inheritance, non-operator, and Floodgate identity | not run | BLOCKED. External. Install LuckPerms on a disposable server and test allow, deny, inheritance, non-operator, and Floodgate identity. |

- AC-ITEM-DUPLICATE: registering `duplicate_probe` as 3 diamonds and then as 7 emeralds kept `minecraft:diamond` x3. `item-duplicate-probe-result.txt` says PASS. Same `GraniteSkills-2.3.203.jar`.
- AC-ITEM-EXPIRY: a registered item whose `expiration_time` is at or before now is not given. `ItemGrantExpiryTest` and `FabricItemExpiryTest` passed, 4 tests, 0 failures. An expired unclaimed entry is removed and not added to the inventory. A future or missing expiration is still given. Claim duplication remains NOT STARTED.
- AC-ITEM-IGNORE: `ignore_interact` stops the mana right-click handler and the interaction result stays PASS, so the ordinary click still happens. `IgnoreInteractClicksTest` passed, 2 tests, 0 failures. An item without the flag still runs the right-click handler.
- AC-ITEM-CLAIM: a claim records only the amount delivered. A second claim of a full delivery gives nothing, and a partial delivery leaves only the remainder. `UnclaimedClaimTest` passed, 2 tests, 0 failures. Both the single-slot and claim-all paths use `UnclaimedClaim.recordDelivery`.
- AC-XP-CROP: wheat age 7 awards 3 XP and ages 0 and 6 award nothing. Sweet berry age 1 awards nothing; age 2 awards 2.5 and age 3 awards 5 (`age-1`). `CropAgeXpTest` passed, 1 test, 0 failures. The break handler uses `FabricBlockStateMatcher.awardedBreakXp`.
- AC-XP-HARVEST: a sweet berry bush at age 3 snapshots 5 XP. After the bush reaches age 1, the first claim awards 5. The same player, source, and position is rejected, so the second award is 0. A bush that stays at age 3 awards 0, and age 1 is not a harvest. `HarvestXpTest` passed, 1 test, 0 failures. The interact handler uses `harvestAward`.
- AC-XP-JUMP: 100 jumps at the vanilla jump velocity award 7 XP, and the first 99 award nothing. A different velocity, a ladder, a jump while already airborne, a jump that is still on the ground, and a jump-boost velocity that does not match the effect award nothing and do not advance the count. `FabricJumpAwardTest` passed, 1 test, 0 failures. `FabricJumpingXpHandler` uses `FabricJumpAward`.
- AC-XP-PROJECTILE: when the direct hit is a projectile, XP is credited to the projectile owner. A different player recorded on the damage source does not receive it, and the damager is `PROJECTILE` so archery sources match. A hit with no owner credits nobody. `ProjectileKillCreditTest` passed, 1 test, 0 failures. `FabricEntityKillHandler` uses `ProjectileKillCredit`.
- AC-XP-BLEED: a zombie damage source with damager player and cause custom awards 6 XP for 2 damage at 3 XP. A different entity, a death-only source, a projectile damager, and a cause that is not custom award nothing. `BleedXpAwardTest` passed, 1 test, 0 failures. `FabricCombatXpHandler.findEntityDamageSource` uses `BleedXpAward`.
- AC-XP-BREW: in alchemy source order, nether wart awards 10, spider eye awards 15, redstone and glowstone dust award 25, gunpowder awards 35, and dragon breath awards 50. Redstone does not match the awkward ingredient, so that source alone awards 0. A blank ingredient awards 0. `BrewingIngredientXpTest` passed, 1 test, 0 failures. Item filters use `BrewingIngredientXp.matches`.
- AC-XP-ENCHANT: the configured `enchant_level` unit pays 30 XP per added level, so one level awards 30 and four levels award 120. Zero added levels award nothing. The experience-requirement unit pays 30 per level spent and awards nothing when no levels were spent. `EnchantingXpAwardTest` passed, 1 test, 0 failures. `FabricEnchantingXpHandler` uses `EnchantingXpAward`.
- AC-XP-GRIND: taking a grindstone result with two disenchantable levels awards 72 XP, and one level awards 36. Zero levels award nothing. A click that is not the result slot, an empty result, and an unchanged result award nothing. A multiplier that is not a number awards nothing. `GrindstoneXpAwardTest` passed, 1 test, 0 failures. The grindstone handler and click mixin use `GrindstoneXpAward`.
- AC-XP-STAT: the walk source pays `change * 0.01 * 0.2` after at least 1000 cm. 1000 cm awards 2 XP and the next 1500 cm awards 3. An unchanged statistic, a decrease, and a rise below 1000 cm award nothing and do not move the baseline. `StatisticXpAwardTest` passed, 1 test, 0 failures. `FabricStatisticXpHandler` uses `StatisticXpAward`.
- AC-XP-AURA: Growth Aura's award is extra crop age, not a separate skill XP amount. Radius 30 includes a player at exactly 30 blocks and excludes a player at 31. Ability value 112 adds one stage, so age 3 becomes 4 inside the radius and stays 3 outside it. A mature crop stays mature. `GrowthAuraAwardTest` passed, 1 test, 0 failures. `FabricFarmingAbilities.onCropGrown` uses `GrowthAuraAward`.
- AC-TRANSFER-CONFIRM: a transfer onto a destination at farming level 4 and 8 XP was refused, and the console said to run the command again with confirm. With confirm, the destination became level 9 and 40 XP. The source stayed at level 9 and 40 XP. `transfer-confirm-probe-result.txt` says PASS. Same `GraniteSkills-2.3.203.jar`.
- GraniteLib distinguishes pickup, shift-move, swap, clone, throw, drag, and pickup-all. `GraniteMenuContractsTest` PASS.
- AC-MENU-HISTORY: opening the built-in skills menu and then the stats menu left stats current. Back returned skills and left it current. A second back returned nothing. `menu-history-probe-result.txt` says PASS. Same `GraniteSkills-2.3.203.jar`.
- AC-LB-MODE: the Power leaderboard GUI ranked Broad then Spike. The Average leaderboard GUI ranked Spike then Broad. Broad has farming 60 and mining 60. Spike has farming 100. `lb-mode-probe-result.txt` says PASS. Same `GraniteSkills-2.3.203.jar`.
- A pickup click activates a menu slot. Shift-move, swap, clone, throw, drag, and pickup-all do not. `FabricMenuClicksTest` passed, 1 test, 0 failures. All 16 built-in open paths call `FabricMenuClicks.activates`.
- The earlier project-classpath run recorded as `AC-LIB-CONSUMER` is now `AC-LIB-CONSUMER-UNIT`. It opened, clicked, and closed a menu. `IndependentMenuConsumerTest` passed. That run does not pass the released-jar case.
- The codec round-trip that was first recorded as `AC-BACKUP-RUNTIME` is now `AC-BACKUP-CODEC`. It stored farming level 9, 120.25 XP, a health modifier of 4.5, and 33.5 mana. `BackupUserCodecTest` passed, 1 test, 0 failures. That test does not pass the packaged backup command.
- The append fixture first recorded as `AC-PRESET-RUNTIME` is now `AC-PRESET-APPEND-UNIT`. It kept `kept: live` and `shared: live`, and added `added: from-preset`. `PresetApplyTest` passed, 1 test, 0 failures. That test does not pass the command path.
- AC-MIGRATION-LEGACY: the first run left a conflicting player file at farming level 9 and copied only the missing `rewards.yml`. After those files were edited, the second run left level 11, `default_language: fr`, and `source: edited` in place. The old folder stayed at level 2. `IdentityMigratorTest` passed, 3 tests, 0 failures.
- AC-LIFECYCLE-PACKAGED: dedicated server `F:\gs-lifecycle-server` booted `GraniteSkills-2.3.195.jar` (SHA-256 `a2537c46af4506a6ae98fb5e3aa69404f11f882fe9d2cb73f5e1fdd79f07308c`) with GraniteLib `0.2.0-alpha.1`, Fabric Loader 0.20.1, and Minecraft 26.3. The first boot loaded 26 chunks, saved a diamond chest and a dungeon-loot chest, and stopped in 0.486s. The restart loaded 25 chunks, kept the diamond and the loot table, and stopped in 0.285s. Logs: `lifecycle-boot.log` Done in 0.321s and `lifecycle-restart.log` Done in 0.236s. Result files `lifecycle-boot-result.txt` and `lifecycle-restart-result.txt` both say PASS.
- The 11 ms run first recorded as `AC-PERF` is now `AC-PERF-UNIT`. The written bar is 4 players, 250 farming XP awards of 1.0, 20 claims, 100 action-bar opens capped at 100 ticks, at least 40 saves during the awards, and a 5000 ms deadline. `PerformanceWorkloadTest` passed, 1 test, 0 failures. That run does not pass the dedicated-server case.
- AC-TEXT-CHAT: on the dedicated server, `FabricUser.sendMessage` of a gold "Granite" component handed `sendSystemMessage` a `net.minecraft.network.chat.MutableComponent` with color `gold`. A plain `Component.literal("Granite")` has no color. `ClientboundSystemChatPacket` carried that same component. `F:\gs-lifecycle-server\chat-probe-result.txt` says PASS. Jar `GraniteSkills-2.3.195.jar`.
- AC-TEXT-ACTION: on the same dedicated server, `FabricUiProvider.sendActionBar` of `<gold>Mana` called `sendOverlayMessage` with a native `MutableComponent` whose text is `Mana` and whose color is gold. The raw `<gold>` tag did not survive. The overlay `ClientboundSystemChatPacket` carried that component. `action-bar-probe-result.txt` says PASS. Java and Bedrock clients were not connected and stay BLOCKED.
- AC-TEXT-TITLE: `FabricUiProvider.sendTitle` of `<aqua>Title`, `<gold>Sub`, fade 5, stay 40, fade out 15 sent three packets in that order: `ClientboundSetTitlesAnimationPacket` (5, 40, 15), `ClientboundSetSubtitleTextPacket` (`Sub`, gold), and `ClientboundSetTitleTextPacket` (`Title`, aqua). The raw tags did not survive. `title-probe-result.txt` says PASS. Java and Bedrock clients were not connected and stay BLOCKED.
- AC-HUD-SMART-EXPIRE: a stable tuple of 20 health and 10/20 mana counted the Smart window from 99 down to 98. It did not return to 100. Changing health to 19 raised the window to 99, which is the 100-tick health window minus one update. `SmartWindowExpireTest` passed, 1 test, 0 failures.
- AC-HUD-JOIN: the first update opened 100 Smart ticks and left 99 after that update. The window then fell to 90. Changing the world to `nether` while health stayed 20 and mana stayed 10/20 opened 100 ticks again and left 99. `joinAndWorldChangeOpen100SmartTicks` passed.
- AC-HUD-ALWAYS: with the Smart window forced to 0 and idle allowed, Always still sent `20/20 10/20`. The same expired window in Smart sent nothing. Always sent nothing after idle was turned off. `alwaysStaysVisibleWhileIdleIsAllowed` passed.
- AC-HUD-OFF: with idle allowed, Off sent no idle line. Always then sent `20/20 10/20`, and Off sent nothing again. `offSendsNoIdleLine` passed.
- AC-HUD-XP-PRIORITY: while Always was sending `20/20 10/20`, an XP line made the next idle update send nothing. Clearing that line let idle send again. An ability line for `Bleed` also made the next idle update send nothing until the pause was cleared. `idleSkipsWhileXpOrAbilityLineIsActive` passed. `SmartWindowExpireTest` passed, 5 tests, 0 failures.
- AC-TRAIT-SATURATION: on the dedicated server, a player at 10 health with food 10 and saturation 0 stayed at 10. Raising saturation to 4, with a saturation-regen bonus of 2, raised health to 12. `F:\gs-lifecycle-server\saturation-probe-result.txt` says PASS. Jar `GraniteSkills-2.3.195.jar`.
- AC-TRAIT-HUNGER: on the same server, food 13 and saturation 0 left health at 10. Food 14 and saturation 0, with a hunger-regen bonus of 3, raised health to 13. `hunger-probe-result.txt` says PASS.
- AC-TRAIT-DOUBLE: on `GraniteSkills-2.3.196.jar`, a roll of 50 against stone added no cobblestone at chance 0 or 40, and added 1 cobblestone at chance 60. Double Drop was not loaded before this build because no stat linked it. `double-drop-probe-result.txt` says PASS. SHA-256 `7ad6d98c43ad81e17ad2c1c83ed3c32608479f64e9f23acab8fd9cf0e268290c`.
- AC-TRAIT-XP: on the same jar, `giveExperiencePoints(100)` added 100 vanilla points with no experience bonus and 150 points with a bonus of 50. Farming skill XP stayed at 5. `xp-trait-probe-result.txt` says PASS.
- AC-TRAIT-ANVIL: on `GraniteSkills-2.3.197.jar`, an anvil cost of 20 stayed 20 with no discount, fell to 7 at trait 40, and stayed at 1 when rounding 20 times the 1% remainder would have produced 0. `anvil-probe-result.txt` says PASS. SHA-256 `10a5c4f319bad86c9902f91a75bb2fb18ebd15a82e263216300471bd9f0e35d3`.
- AC-DEATH-RESET: on the same jar, with `reset_xp` and ratio 0.5, a zombie death left farming at level 4 and 100 XP. The player's death left level 4 and cut XP to 50. `death-reset-probe-result.txt` says PASS.
- AC-TRAIT-EXISTING: on `GraniteSkills-2.3.198.jar`, luck +3, HP raised max health from 20 to 24, speed +0.1, max mana clamped 100 to 30, mana regen raised 0 to 1.1, crit chance 100 crits and 0 does not, crit damage adds 0.5, attack adds 0.4, damage reduction is negative, and farming luck gained 40. `trait-runtime-probe-result.txt` says PASS. SHA-256 `553e2db902665e959ae0f4ff81c619e71f6b36de78d872d2239e04a581e9e964`.
- AC-PLACE-BREAK: a marked stone at 30,120,30 was broken to air. Its placed record was removed from memory and `placed-blocks.txt`. The marked neighbor at 31,120,30 stayed. `place-break-probe-result.txt` says PASS.
- AC-PLACE-RESTART: the dedicated server was started again. 31,120,30 was placed from `placed-blocks.txt` without a new mark. 30,120,30 stayed unmarked. `place-restart-probe-result.txt` says PASS.
- AC-PLACE-XP: with block-replace checking on, a marked pumpkin awarded 0 farming XP and an unmarked pumpkin awarded 4. `place-xp-probe-result.txt` says PASS. Jar `GraniteSkills-2.3.200.jar`, SHA-256 `a1764bd686928653df9ad14943e0f42e17b3a71ddd9c123a454612c36f3665d3`.
- AC-PLACE-SPEED: a ready Speed Mine stayed inactive on marked stone and activated on unmarked stone. `speed-mine-probe-result.txt` says PASS.
- AC-PLACE-TREE: a ready Treecapitator stayed inactive on a marked oak log, and the log above it stayed. It activated on an unmarked oak log. `tree-probe-result.txt` says PASS. Jar `GraniteSkills-2.3.201.jar`, SHA-256 `0aa7cbbe90bfd67e4a55cbdb94783737fd8f080c5c1a3077b786fd0d7e520122`.
- AC-LOOT-REQ: skill, stat, permission, world, and biome nodes each rejected a loot check, and the matching allowed values passed. The player was in `minecraft:plains`. `loot-req-probe-result.txt` says PASS. Jar `GraniteSkills-2.3.202.jar`, SHA-256 `15071e21f8642c9e54768febc0747c8b8d0f28b48a05175a7342eb431986cf23`.
- AC-LOOT-EMPTY: an empty requirements list and a missing requirements node both allowed the loot-table check for an offline player. A farming level-99 node rejected that player. `loot-empty-probe-result.txt` says PASS. Same `GraniteSkills-2.3.202.jar`.
- AC-LOOT-BLOCK: a dirt break awarded 0.3 excavation XP and rolled `minecraft:bone` x2 from the excavation rare pool. The pool chance was 100 for that run and the file was restored to 0. `loot-block-probe-result.txt` says PASS. Same `GraniteSkills-2.3.202.jar`.
- AC-LOOT-MOB: a player zombie kill awarded 7 fighting XP and rolled `minecraft:emerald` x1. The fighting loot file was removed afterward. `loot-mob-probe-result.txt` says PASS. Same `GraniteSkills-2.3.202.jar`.
- AC-LOOT-FISH: a cod catch used `FabricFishingXpHandler`, which calls `FabricFishingLootHandler`. Fishing XP was awarded and the inventory received `minecraft:slime_ball` x3 from `loot/fishing.yml`. The rare pool chance was restored to 2. `loot-fish-probe-result.txt` says PASS. Same `GraniteSkills-2.3.202.jar`.
- AC-LOOT-EXCAVATION: a dirt break matched excavation, awarded 0.3 excavation XP, and dropped `minecraft:feather` x1 from `loot/excavation.yml` through the block-break handler. The rare pool chance was restored to 0. `loot-excavation-probe-result.txt` says PASS. Same `GraniteSkills-2.3.202.jar`.
- AC-SRC-REJECT: a farming file with `broken_probe` at xp 0 still loaded 19 sources, including wheat. The reject was `not version-valid or non-positive xp`, and `broken_probe` was not registered. The file was restored. `src-reject-probe-result.txt` says PASS. Same `GraniteSkills-2.3.202.jar`.
- AC-SRC-FALLBACK: endurance has no embedded source file. Loading its user file set the fallback flag and loaded one source. Farming, which has an embedded file, kept the flag false. `src-fallback-probe-result.txt` says PASS. Same `GraniteSkills-2.3.202.jar`.
- AC-SRC-DIAG: the server console ran `/sources diagnose farming` after `broken_probe` was rejected. The log printed `rejected source=broken_probe reason=not version-valid or non-positive xp`. `src-diag-probe-result.txt` says PASS. Jar `GraniteSkills-2.3.203.jar`, SHA-256 `b53fc3d4ae4f0ca7dfaeead4c6c4b878074d30bdf4ec6e028080c8973dae588f`.
- AC-PERM-ZERO: with no permission provider, a resolved player's farming and global multipliers were both 0. An unset multiplier node followed the caller's default. `perm-zero-probe-result.txt` says PASS. Same `GraniteSkills-2.3.203.jar`.
- AC-PERM-GLOBAL: `graniteskills.multiplier.10` and `.25` added 0.35. Every listed percent added 21.03. An unlisted 7 and `graniteskills.multiplier.farming.50` did not change the global total. `perm-global-probe-result.txt` says PASS. Same `GraniteSkills-2.3.203.jar`.
- AC-PERM-SKILL: `graniteskills.multiplier.farming.50` raised only farming, to 0.5. `graniteskills.multiplier.mining.25` raised only mining, to 0.25. The global multiplier stayed 0. `perm-skill-probe-result.txt` says PASS. Same `GraniteSkills-2.3.203.jar`.
- AC-HUD-YAML-FILE: `FileStorageProvider` saved and loaded the same user file for `off`, `always`, and `smart`. The file stored `idle_mode: 'off'` because YAML quotes that word, then `idle_mode: always` and `idle_mode: smart`. Each load returned the mode that was saved. `YamlIdleModeFileTest` passed, 1 test, 0 failures. This is separate from `ActionBarIdleModeTest`.
- AC-HUD-COMBAT: on the dedicated server, 4 damage through `ServerLivingEntityEvents.AFTER_DAMAGE` moved a player's Smart window from 0 to 120. `F:\gs-lifecycle-server\combat-probe-result.txt` says PASS. Jar `GraniteSkills-2.3.195.jar`.
- The same player, source, and block position cannot take a second XP award inside 50ms. `FabricXpAwardGuardTest` covers that. The rest of the source-by-source matrix is still NOT STARTED.
- A missing user no longer throws from `FabricXpHandlerUtil.shouldBlockXp`.

The 15 November 2026 review of 2.3.215 / alpha.18 stays a source finding, not product acceptance. The fixes below were proven on `GraniteSkills-2.3.217.jar` with GraniteLib `0.2.0-alpha.19` by `review-fixes-probe-result.txt`. That file says PASS. The full command and ability inventory, LuckPerms inheritance, and a raw-log bundle for every older case are still open (`AC-REVIEW-V2`, `AC-REVIEW-F7`, `AC-REVIEW-V3`).

`AC-REVIEW-F2`: on `GraniteSkills-2.3.218.jar`, the skills menu opened from the loaded definition. The first screen title was `§8§lYour Skills` and slot 0 was `§6Leaderboard`. The loaded definition was then changed to the title `Edited Skills`, and the leaderboard item was renamed `Edited Leaderboard` and moved to slot 2. The next open used that title, left slot 0 empty, and showed `Edited Leaderboard` in slot 2. `review-f2-probe-result.txt` says PASS. Clicks use the action named on the definition item.

`AC-REVIEW-V1`: on 2.3.218 the break path awarded one pumpkin break once when the callback ran twice, and then dropped a second pumpkin placed and broken at that same position (`afterRepeat=4`, `afterDistinct=4`). 2.3.219 keeps the repeated callback at 4 XP and awards the new block as well (`afterDistinct=8`). `review-v1-probe-result.txt` says PASS.

`AC-REVIEW-V2`: the running 2.3.219 server listed 249 PASSive abilities, 8 mana abilities, and the Granite command roots. Each one now has its own case. That added 9 NOT STARTED cases, including the 15 skill shortcuts and the `graniteskills`/`skills`/`skill`/`sk` aliases. The previous case list was not the scope. `review-v2-probe-result.txt` says PASS with `unmapped=0`. Those new cases are not done.

`AC-REVIEW-V3`: the raw probe results, server logs, disposable config, source revision, and both release jars are files in `F:\FabricAuraSkills\fabric\build\libs\granite-2.3.219-alpha.19-bundle`. That directory holds 112 probe results, the current `latest.log` and the rotated logs, `config.yml`, `server.properties`, and the jars. `MANIFEST.txt` records Granite Skills `d5e023d39957b118dd664b53deffc919d857fe3ed49ad723eb204ba2801fd0cf`, GraniteLib `d171e548f9e219198d3ddb0c77d0c4d13fd2738636e53cb9abf1a91f66fecf34`, commit `0c204cb86689b5a4d4609202c97ea47ba9f041b7` with a dirty working tree, and the 2.3.219 boot's Minecraft 26.3, Fabric Loader 0.20.1, Fabric API 0.166.2+26.3, and Java 25. `review-v1` and `review-v2` were produced on this pair. Older results are in the bundle as their original bytes and were not rerun. The manifest says PASS.

`AC-SCOPE-CMD-SKILL-SHORTCUT`: on `GraniteSkills-2.3.220.jar`, each of the 15 skill roots opened `level_progression` for that skill. The titles were Farming, Foraging, Mining, Fishing, Excavation, Archery, Defense, Fighting, Endurance, Agility, Alchemy, Enchanting, Sorcery, Healing, and Forging. `skill-shortcut-probe-result.txt` says PASS with `opened=15`. Endurance, sorcery, healing, and forging were enabled only for that run and `skills.yml` was restored afterward.

`AC-SCOPE-CMD-ALIASES`: on `GraniteSkills-2.3.221.jar`, `graniteskills`, `skills`, `skill`, and `sk` each registered the same 35 commands: abtoggle, antiafk, armor, backup, claimitems, help, item, lang, mana, manaability, modifier, multiplier, openmenu, preset, profile, rank, reload, resethealth, save, skilllevel, skillrank, skills, skilltop, sources, stats, storage, templates, toggle, top, trait, transfer, updateleaderboards, version, xp, and xpdiag. `cmd-aliases-probe-result.txt` says PASS with `same=true` and `complete=true`.

`AC-SCOPE-CMD-ABTOGGLE`: on `GraniteSkills-2.3.222.jar`, `/abtoggle` cycled Smart to Always, Off, and back to Smart. Status reported `Action bar idle mode: Smart`. `always`, `off`, and `smart` set those modes. `nope` was rejected and left the mode on Smart. `abtoggle-probe-result.txt` says PASS.

`AC-SCOPE-CMD-ARMOR`: on `GraniteSkills-2.3.222.jar`, with a diamond chestplate in the main hand, `/armor modifier add strength 2.5`, `/armor trait add luck 4`, `/armor requirement add farming 5`, and `/armor multiplier add global 1.5` wrote those values onto the held armor. The reader returned `strength=2.5`, `luck=4.0`, `farming=5`, and `global=1.5`. Chat confirmed each add. `armor-cmd-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-BACKUP`: on `GraniteSkills-2.3.222.jar`, `/backup create` wrote `backup-2026-11-15_23-37-46.yml` and `/backup save` wrote `backup-2026-11-15_23-37-47.yml`. `/backup list` named both files. `/backup info` reported the create|save|list|info and load|restore permission lines. After farming was lowered to 2, `/backup load … confirm` restored level 18. After farming was lowered to 3, `/backup restore … confirm` restored level 18 again. `backup-scope-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-CLAIMITEMS`: on `GraniteSkills-2.3.222.jar`, empty `/claimitems` reported no items to claim. With three unclaimed `probe_stone`, `/claimitems` opened `unclaimed_items` on `GraniteClickable9x6Handler`. `/claimitems all` cleared the unclaimed list, put 3 stone in inventory, and reported that all unclaimed items were merged. `claimitems-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-LANG`: on `GraniteSkills-2.3.222.jar`, `/lang de` set the player locale to `de` and reported `Sprache geändert auf de`. `/lang notalang` left the locale on `de` and reported `Sprache nicht gefunden!`. `/lang en` set the locale back to `en` and reported `Language set to en`. `lang-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-MANA`: on `GraniteSkills-2.3.222.jar`, `/mana set ManaCmd 10` set mana to 10, `/mana add ManaCmd 5` raised it to 15, and `/mana remove ManaCmd 3` left 12. Plain `/mana` and `/mana view` reported `12/20`. `/mana view ManaCmd` reported mana of ManaCmd as `12/20`. `mana-cmd-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-MODIFIER`: on `GraniteSkills-2.3.222.jar`, `/modifier add` stored `perm_mod` at strength 4.5, `/modifier addtemp` stored `temp_mod` at 2.25 for 30s, and `/modifier list` named both. `/modifier remove perm_mod` dropped only that name. After another permanent add, `/modifier removeall` cleared every remaining stat modifier. `modifier-cmd-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-MULTIPLIER`: on `GraniteSkills-2.3.222.jar`, plain `/multiplier` and `/multiplier MulCmd` both reported `Global XP multiplier for MulCmd: 1x / +0%`. `/multiplier MissingPlayer` reported no online player. `multiplier-cmd-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-OPENMENU`: on `GraniteSkills-2.3.222.jar`, `/openmenu` opened all eight ids on `GraniteClickable9x6Handler`: skills, stats, level_progression (farming), stat_info (strength), sources (farming), unclaimed_items, leaderboard, and abilities. Each open reported the matching menu feedback line. `openmenu-cmd-probe-result.txt` says PASS with `opened=8`. No jar bump.

`AC-SCOPE-CMD-PRESET`: on `GraniteSkills-2.3.222.jar`, `/preset list` named `modes.zip` and `noprofile.zip`. `/preset info modes.zip` showed the read-only entry list for append/merge/replace/skip probe files. `/preset load modes.zip` required confirm, and `/preset load modes.zip confirm` replaced `preset-probe-replace.yml` with `kept: replaced` / `only: preset`, then reloaded from disk. `preset-scope-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-PROFILE`: on `GraniteSkills-2.3.222.jar`, `/profile skills ProfileCmd` reported Farming level 14 and XP 3.5 with the player UUID. `/profile stats ProfileCmd` reported Strength total 9 (base 7, modified 2), mana 8.3, and persistent modifier `scope_str`. `profile-cmd-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-RANK`: on `GraniteSkills-2.3.222.jar`, `/rank` for a top Farming player reported `Your Skill Rankings`, Power `#1 of 39`, and Farming `#1 of 12`, matching the leaderboard manager ranks. `rank-cmd-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-RESETHEALTH`: on `GraniteSkills-2.3.222.jar`, a player `/resethealth` was refused with `Only the dedicated server console (or RCON) may run this command` and left the offline `graniteskills:health` modifier in place. Console `/resethealth` examined 1 offline data file, stripped that GraniteSkills attribute modifier, and left the non-Granite modifier. `resethealth-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-SKILLLEVEL`: on `GraniteSkills-2.3.222.jar`, `/skilllevel set SkillLv farming 10` then `add … 5` then `remove … 3` left Farming at 12. `/skilllevel view SkillLv farming` reported `SkillLv — Farming: level 12`. `/skilllevel setall SkillLv 8` set Farming and Mining to 8. `/skilllevel reset SkillLv farming` restored Farming to start level 1 while Mining stayed 8, and `/skilllevel reset SkillLv` reset both to 1. `skilllevel-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-SKILLRANK`: on `GraniteSkills-2.3.222.jar`, `/skillrank farming` for a top Farming player reported `Farming: #1 of 12`, matching the leaderboard manager skill rank. `skillrank-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-SKILLTOP`: on `GraniteSkills-2.3.222.jar`, `/skilltop mining` printed the Mining Leaderboard with `MineHigh` at 9999 before `MineLow` at 9998 (`MineLow` had the higher farming level). Farming order remains covered by `AC-LB-CMD-SKILL`. Probe profiles were removed. `skilltop-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-SOURCES`: on `GraniteSkills-2.3.222.jar`, `/sources farming` opened `GraniteClickable9x6Handler` with lastOpenedId `sources`. Console `/sources diagnose farming` wrote `Source diagnostics written to server log for farming` and `Summary: loaded=19, versionValid=19, visible=19`, and logged `[/sources diagnose] skill=graniteskills/farming`. `sources-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-STATS`: on `GraniteSkills-2.3.222.jar`, `/stats` opened `GraniteClickable9x6Handler` with lastOpenedId and history place `stats`, and reported `Opened stats menu (E to close).` Console `/stats` was refused with `Players only.` `stats-cmd-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-STORAGE`: on `GraniteSkills-2.3.222.jar`, console `/storage info` named YAML `FileStorageProvider`. `/storage save` reported saved profiles. `/storage reload` explained it does not hot-swap the backend. `/storage mergeskills farming mining` previewed then confirmed (1 user merged; mining raised to farming 20) with a pre-backup. `/storage removeallmodifiers` previewed and `cancel` left `scope_storage_mod` in place. Probe userdata and the merge backup were restored/removed. Confirm/stale/cancel and bad-skill validation remain covered by `AC-STORE-*` / `AC-CMD-*`. `storage-scope-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-TOGGLE`: on `GraniteSkills-2.3.222.jar`, `/toggle` cycled Smart to Always, Off, and back to Smart. Status reported `Action bar idle mode: Smart`. `always`, `off`, and `smart` set those modes. `nope` was rejected and left the mode on Smart. `toggle-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-TOP`: on `GraniteSkills-2.3.222.jar`, `/top` printed `All Skills Leaderboard` with ranks 1–10, `/top average` printed `Skill Average Leaderboard` with ranks 1–10, and `/top 2` / `/top average 2` printed the Page 2 headers with ranks from 11. Ordered power/average ranking remains covered by `AC-LB-CMD-POWER` and `AC-LB-CMD-AVERAGE`. `top-scope-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-TRAIT`: on `GraniteSkills-2.3.222.jar`, `/trait add TraitCmd luck perm_trait 4.5` and `/trait addtemp … temp_trait 2.25 30s` stored both modifiers. `/trait list` named both. `/trait remove … perm_trait` left only `temp_trait`. `/trait removeall` cleared the remaining modifiers. Temporary-only coverage remains in `AC-REVIEW-F6`. `trait-cmd-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-TRANSFER`: on `GraniteSkills-2.3.222.jar`, same-UUID `/transfer` was refused with `Source and target UUIDs must be different.` Transfer onto an empty destination copied Farming 11 / 22.5 XP without confirm, left the source unchanged, and noted the old identity still has a separate profile. Destination-has-progress confirm remains covered by `AC-TRANSFER-CONFIRM`. `transfer-scope-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-UPDATELEADERBOARDS`: on `GraniteSkills-2.3.222.jar`, `/updateleaderboards` reported `Leaderboard updated!` and `/updateleaderboards 3` reported that plus `Average update time over 3 samples: 5 ms`. `updateleaderboards-probe-result.txt` says PASS. No jar bump.

`AC-SCOPE-CMD-XPDIAG`: on `GraniteSkills-2.3.222.jar`, `/xpdiag status` started OFF, `/xpdiag on` then status reported ON, `/xpdiag off` then bare `/xpdiag` reported OFF. `xpdiag-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-ALCHEMIST`: implemented and proven on `GraniteSkills-2.3.223.jar` (SHA-256 `9FF240D623284BC8F97FD1BAEC86CF27DFF58D133EB6C25167B04C72D246673A`). Below unlock, Alchemist stamps no duration bonus. At ability level 1 a Strength potion gained +108 ticks (3%) with lore; drinking applied 3708 ticks. At ability level 2 the bonus scaled to +252 ticks (7%). `alchemist-probe-result.txt` says PASS.

`AC-ABILITY-ANTI-HUNGER`: proven on `GraniteSkills-2.3.225.jar` (SHA-256 `06C62878AB307A3103C5D00B327C01583010210DE82E74F7DB54E14635EF4300`). Endurance was restored in `skills.yml` so Anti Hunger loads. Below unlock, hunger exhaustion still applied. At 5% chance boundary rolls hit/miss correctly; at 100% chance `causeFoodExhaustion` was cancelled; at ability level 2 (10%) a 0.06 roll that missed level 1 hit. `anti-hunger-probe-result.txt` says PASS.

`AC-ABILITY-ANVIL-MASTER`: proven on `GraniteSkills-2.3.225.jar`. Below unlock, cost 41 was refused. At ability level 1 (max cost 43) costs 41 and 43 were allowed and 44 refused. At ability level 2 (max 46) cost 44 was allowed. `anvil-master-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-ARCHER`: proven on `GraniteSkills-2.3.225.jar`. Below unlock, Archery XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `archer-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-AXE-MASTER`: proven on `GraniteSkills-2.3.225.jar`. Below unlock, axe attack damage stayed 10.0 with no ADD_COMBINED modifier. At ability level 1 (+4%) axe damage became 10.4 while sword damage stayed 10.0. At ability level 2 (+7%) axe damage became 10.7. `axe-master-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-BIGGER-SCOOP`: proven on `GraniteSkills-2.3.225.jar`. Below unlock, excavation luck stayed 0 and dig luck added no dirt. At ability level 1 excavation luck became 10. At ability level 2 it scaled to 20. At luck 100 / 200 the dig path guaranteed 1 then 2 extra dirt. `bigger-scoop-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-BLEED`: proven on `GraniteSkills-2.3.225.jar`. Below unlock, bleed failed checks, chance was 0, and a sword hit left no bleed ticks. At ability level 1 a 0.02 roll hit the 3% chance and a 0.04 roll missed; applied bleed started at 3 ticks and dealt 0.5 DoT. At ability level 2 chance scaled to 6% (0.04 hit) and DoT scaled to 1.0. `bleed-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-BOUNTIFUL-HARVEST`: proven on `GraniteSkills-2.3.225.jar`. Below unlock, farming luck stayed 0 and crop luck added no wheat. At ability level 1 farming luck became 10. At ability level 2 it scaled to 20. At luck 100 / 200 the wheat path guaranteed 1 then 2 extra wheat. `bountiful-harvest-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-BOW-MASTER`: proven on `GraniteSkills-2.3.225.jar`. Below unlock, bow attack damage stayed 10.0 with no ADD_COMBINED modifier. At ability level 1 (+3%) bow damage became 10.3 while sword damage stayed 10.0. At ability level 2 (+5%) bow damage became 10.5. `bow-master-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-BREWER`: proven on `GraniteSkills-2.3.225.jar`. Below unlock, Alchemy XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `brewer-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-DEFENDER`: proven on `GraniteSkills-2.3.225.jar`. Below unlock, Defense XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `defender-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-DISENCHANTER`: proven on `GraniteSkills-2.3.226.jar` (SHA-256 `F906A69B635138E40E623FF85C6D01E833AD16FA0A436ACBB48FBBEE9E62D08A`). Forging was restored in `skills.yml` and Fabric grindstone bonus XP was implemented. Below unlock, Mending I iron sword bonus XP stayed 0. At ability level 1 (value 5) bonus XP was 1. At ability level 2 (value 8) bonus XP was 2. `disenchanter-probe-result.txt` says PASS.

`AC-ABILITY-ENCHANTED-STRENGTH`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, a Sharpness I iron sword left no `AbilityModifier-EnchantedStrength`. At ability level 1 (0.5 per enchant type) the strength modifier was 0.5. At ability level 2 (1.0 per enchant type) it scaled to 1.0. `enchanted-strength-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-ENCHANTER`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, Enchanting XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `enchanter-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-EPIC-CATCH`: proven on `GraniteSkills-2.3.226.jar`. Epic fishing pool base chance stayed 0.005. Below unlock, Epic Catch added 0. At ability level 1 (+0.2%) chance became 0.007. At ability level 2 (+0.4%) it scaled to 0.009. `epic-catch-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-EXCAVATOR`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, Excavation XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `excavator-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-FARMER`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, Farming XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `farmer-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-FIGHTER`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, Fighting XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `fighter-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-FIRST-STRIKE`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, successive sword hits had no first-strike bonus. At ability level 1 (+15%) the first sword hit added 0.15 ADD_COMBINED and the cooldown blocked the second. At ability level 2 (+20%) the first-hit bonus scaled to 0.20. `first-strike-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-FISHER`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, Fishing XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `fisher-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-FLEETING`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, damage that would drop health under 20% left no fleeting speed modifier. At unlock, damage leaving health at/above 20% also left none. At ability level 1 a sub-20% hit applied +5 movement speed. At ability level 2 it scaled to +9. `fleeting-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-FORAGER`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, Foraging XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `forager-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-FORGER`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, Forging XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `forger-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-GENETICIST`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, eating an apple added no saturation. At unlock, cooked beef added none. At ability level 1 (value 1.0) an apple added 0.1 saturation. At ability level 2 (value 3.0) it scaled to 0.3. `geneticist-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-GOLDEN-HEAL`: proven on `GraniteSkills-2.3.226.jar`. Below unlock with regeneration, heal amount stayed 10. Without regeneration at unlock it stayed 10. At ability level 1 (+5%) regeneration heal became 10.5. At ability level 2 (+11%) it scaled to 11.1. `golden-heal-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-GOLDEN-HEART`: proven on `GraniteSkills-2.3.226.jar`. Below unlock with absorption, damage multiplier stayed 1.0. Without absorption at unlock it stayed 1.0. At ability level 1 (value 5) the multiplier was 0.95. At ability level 2 (value 8) it scaled to 0.92. `golden-heart-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-GRAPPLER`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, a hooked pig kept delta movement 0. At unlock, an unsafe far pull that would exceed component 4 also stayed 0. At ability level 1 (value 50, scale 0.006) a 10-block pull set delta to (-0.06, 0, 0). At ability level 2 (value 150, scale 0.01) it scaled to (-0.1, 0, 0). `grappler-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-GROWTH-AURA`: proven on `GraniteSkills-2.3.226.jar`. Below unlock, a wheat crop at age 3 stayed 3 on a hit roll. At unlock outside radius 30 it stayed 3. At ability level 1 (value 12) a 0.0 roll advanced age to 4. A 0.15 roll missed level 1 and hit level 2 (value 24), advancing age to 4. `growth-aura-probe-result.txt` says PASS. No jar bump. Crop XP paths remain covered by `AC-XP-AURA` / `AC-XP-AURA-PATHS`.

`AC-ABILITY-HARDENED-ARMOR`: proven on `GraniteSkills-2.3.227.jar` (SHA-256 `1126C16DB6BE21262D213EB9747C27E8AB11321DDEEFCA8AA10B0BCD8B1D5115`). Fabric ports Bukkit Hardened Armor: chance to cancel armor durability loss. Below unlock, a 0.0 roll did not cancel. A pickaxe never cancelled. At ability level 1 (3%) a 0.0 roll cancelled and 0.05 did not. A 0.04 roll missed level 1 and hit level 2 (6%). The mixin left chestplate damage at 0 on a hit roll and applied 1 on a miss. `hardened-armor-probe-result.txt` says PASS.

`AC-ABILITY-HEALER`: proven on `GraniteSkills-2.3.228.jar` (SHA-256 `D903A21F366A580DEE5456DFA844E89771435376D304EB71F8389B051640C9D5`). Healing was restored in `skills.yml` so Healer loads as the Healing XP multiplier. Below unlock, multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `healer-probe-result.txt` says PASS.

`AC-ABILITY-IMMUNITY`: proven on `GraniteSkills-2.3.228.jar`. Below unlock, a 0.0 roll did not cancel damage. At ability level 1 (value 0.5) a 0.0 roll cancelled and 0.01 did not. A 0.006 roll missed level 1 and hit level 2 (value 0.9). `immunity-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-JUMPER`: proven on `GraniteSkills-2.3.228.jar`. Below unlock, Agility XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `jumper-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-LIFE-ESSENCE`: proven on `GraniteSkills-2.3.228.jar`. Below unlock with instant health, heal amount stayed 10. Without instant health at unlock it stayed 10. At ability level 1 (+10%) magic heal became 11.0. At ability level 2 (+17%) it scaled to 11.7. `life-essence-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-LIFE-STEAL`: proven on `GraniteSkills-2.3.228.jar`. Below unlock, a hostile kill left health at 10. A pig kill at unlock also left health at 10. At ability level 1 (2.5% of 20 max health) health became 10.5. At ability level 2 (3.5%) it scaled to 10.7. `life-steal-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-LIGHT-FALL`: proven on `GraniteSkills-2.3.228.jar`. Below unlock, fall damage of 10 stayed 10. Generic non-fall damage at unlock also stayed 10. At ability level 1 (2% reduction) fall damage became 9.8. At ability level 2 (3.5%) it scaled to 9.65. `light-fall-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-LINGERING`: proven on `GraniteSkills-2.3.229.jar` (SHA-256 `5DD74BDBA3BFED699190FF61DC3E26D01E3BD4595D00C92B0E91A21B5E0078BD`). Fabric ports Bukkit Lingering: slower area-cloud `radiusPerTick` / `radiusOnUse`. Listed under alchemy in `skills.yml`. Below unlock, vanilla `-0.005` / `-0.5` stayed unchanged. At ability level 1 (value 5 / secondary 3) radii became `-0.00475` / `-0.485`. At ability level 2 (value 9 / secondary 5) they scaled to `-0.00455` / `-0.475`. `lingering-probe-result.txt` says PASS.

`AC-ABILITY-LUCKY-CATCH`: proven on `GraniteSkills-2.3.230.jar` (SHA-256 `5F85525293F0637E69BA6AB6817691ADDC63FB584BD278098E7AF27EB17A9403`). Fabric ports Bukkit Lucky Catch: fishing-luck trait grant plus extra catch stack amount. Below unlock, no `fishing_luck_ability` modifier and a cod catch stayed at 1. At ability level 1 the ability grant was 5. At ability level 2 it scaled to 10. With luck pinned near 100 / 200, applyLuckyCatch added 1 then 2 cod. `lucky-catch-probe-result.txt` says PASS. No jar bump beyond 2.3.230 for that case.

`AC-ABILITY-LUCKY-MINER`: proven on `GraniteSkills-2.3.230.jar`. Below unlock, no `mining_luck_ability` modifier and a stone break added 0 extra cobble. At ability level 1 the ability grant was 5. At ability level 2 it scaled to 10. With luck pinned near 100 / 200, `FabricInteractGatheringLuck.tryApply` dropped 1 then 2 cobble. `lucky-miner-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-LUCKY-SPADES`: proven on `GraniteSkills-2.3.231.jar` (SHA-256 `140D27E8CA8C65047839C872F1A0198DAEF7E95964B8E136A960BAE4FAAB4051`). Fabric ports Bukkit Lucky Spades into `FabricSkillLoot` for the excavation epic pool. Below unlock, epic chance stayed 0. At ability level 1 (+0.05%) chance became 0.0005. At ability level 2 (+0.08%) it scaled to 0.0008. `lucky-spades-probe-result.txt` says PASS.

`AC-ABILITY-LUCKY-TABLE`: proven on `GraniteSkills-2.3.232.jar` (SHA-256 `EC4599E0ED50E6640FF392F24661815E563DF67B31886A7074B2C2A622542CD2`). Fabric Lucky Table can bump a newly added enchant by +1. Below unlock, a 0.0 roll left Sharpness I. At unlock a maxed Sharpness V stayed V. At ability level 1 (5%) a 0.0 roll bumped Sharpness I to II and a 0.06 roll missed. At ability level 2 (6%) a 0.055 roll missed level 1 and hit level 2. `lucky-table-probe-result.txt` says PASS.

`AC-ABILITY-LUMBERJACK`: proven on `GraniteSkills-2.3.232.jar`. Below unlock, no `foraging_luck_ability` and oak log added 0 extra. At ability level 1 the ability grant was 10. At ability level 2 it scaled to 20. With luck pinned at 100 / 200, `tryApply` dropped 1 then 2 oak logs. `lumberjack-probe-result.txt` says PASS. No jar bump.

`AC-ABILITY-MEAL-STEAL`: proven on `GraniteSkills-2.3.235.jar`. Below unlock, a 0.0 roll left both players at food 10. At ability level 1 (1%) a 0.0 roll moved food 10→11 / 10→9 and a 0.02 roll missed. At ability level 2 (3%) a 0.02 roll missed level 1 and hit level 2. `meal-steal-probe-result.txt` says PASS.

`AC-ABILITY-METAL-DETECTOR`: proven on `GraniteSkills-2.3.235.jar`. Excavation rare pool base chance 0. Below unlock stayed 0. At ability level 1 (+0.3%) chance became 0.003. At ability level 2 (+0.6%) it scaled to 0.006. `metal-detector-probe-result.txt` says PASS. No new loot wiring beyond 2.3.231.

`AC-ABILITY-MINER`: proven on `GraniteSkills-2.3.235.jar`. Below unlock, Mining XP multiplier stayed 1.0 (100 XP from 100). At ability level 1 (+10%) addXp awarded 110. At ability level 2 (+20%) it awarded 120. `miner-probe-result.txt` says PASS. No jar bump beyond meal-steal.

`AC-MANA-SPEED-MINE`: proven on `GraniteSkills-2.3.235.jar`. Below unlock, mana ability level stayed 0 and `failsBreakActivationChecks` blocked. At level 1, `tryActivate` applied Haste, spent 20 mana for value 10.0, and `stop` cleared the effect with cooldown 4000 ticks. Zero mana refused activation. Level 2 scaled value to 15.0 and cost to 32. `mana-speed-mine-probe-result.txt` says PASS.

`AC-MANA-TERRAFORM`: proven on `GraniteSkills-2.3.235.jar`. Below unlock, `failsChecks` blocked. At level 1, activation spent 20 mana for value 5.0; `stop` set cooldown. Level 2 scaled to value 9.0 / cost 32. `mana-terraform-probe-result.txt` says PASS.

`AC-MANA-TREECAPITATOR`: proven on `GraniteSkills-2.3.235.jar`. Below unlock, `failsChecks` blocked. At level 1, activation spent 20 mana for value 5.0; `stop` set cooldown. Level 2 scaled to value 10.0 / cost 32. `mana-treecapitator-probe-result.txt` says PASS.

`AC-MANA-LIGHTNING-BLADE`: proven on `GraniteSkills-2.3.235.jar`. Below unlock, mana ability level stayed 0. At level 1, activation spent 20 mana, applied the `lightning_blade_mana_ability` attack-speed modifier for value 5.0, and `stop` removed it with cooldown. Level 2 scaled to value 10.0 / cost 32. `mana-lightning-blade-probe-result.txt` says PASS.

`AC-MANA-ABSORPTION`: proven on `GraniteSkills-2.3.235.jar`. Below unlock, mana ability level stayed 0. At level 1, `checkActivation` spent 10 mana for value 2.0 and set the absorption active flag; `stop` cleared it with cooldown. Level 2 scaled to value 5.0 / cost 20. `mana-absorption-probe-result.txt` says PASS.

`AC-MANA-REPLENISH`: proven on `GraniteSkills-2.3.235.jar`. Below unlock, mana ability level stayed 0. At level 1, `checkActivation` spent 20 mana for value 5.0; `stop` set cooldown. Level 2 scaled to value 10.0 / cost 32. `mana-replenish-probe-result.txt` says PASS.

`AC-MANA-CHARGED-SHOT`: proven on `GraniteSkills-2.3.235.jar`. Below unlock, `failsChecks` blocked. At level 1 with toggle enabled, a full-pull shot spent 10 mana, registered projectile multiplier 1.05 for value 0.5, and finished activated immediately. Toggle-off cancelled the shot path with no mana spend. Level 2 scaled to value 0.6 / cost 15 / multiplier 1.09. `mana-charged-shot-probe-result.txt` says PASS.

`AC-MANA-SHARP-HOOK`: proven on `GraniteSkills-2.3.235.jar`. Below unlock, activation did not damage or spend mana. At level 1, `activateSharpHook` dealt 0.5 damage, spent 5 mana, and set cooldown 40 ticks. Zero mana refused. Level 2 scaled to value 1.0 / cost 7. `mana-sharp-hook-probe-result.txt` says PASS.


`AC-REVIEW-F7`: resolved on `GraniteSkills-2.3.236.jar` as the implementation/permission-multiplier contract. Arbitrary percents (`7`, `-10`, `12.5`), per-skill forms, denial-by-omission from the LuckPerms-shaped map, and inheritance-as-resolved-grants were proven through `FabricUser.getPermissionMultiplier` with the probe grant seam. No-provider multiplier stays 0. Real LuckPerms allow/deny/inheritance/Floodgate remains `AC-PERM-PROVIDER`. `review-f7-probe-result.txt` says PASS. `PermissionMultipliersTest` and `FabricLuckPermsGrantsTest` passed.

`AC-PERM-ZERO` / `AC-PERM-GLOBAL` / `AC-PERM-SKILL`: revalidated on `GraniteSkills-2.3.236.jar` against the resolved-grant path (not fabric-permissions `PermissionCheckEvent`). Global listed sum 21.03; arbitrary `7` adds when present in the grant list; farming/mining skill nodes stay skill-scoped. Probe results say PASS.

Mana/ability event-path integration on `2.3.236`: `PlayerBlockBreakEvents.BEFORE` activates Speed Mine and Terraform; `ServerLivingEntityEvents.ALLOW_DAMAGE` activates Absorption and absorbs a follow-up hit; `FabricEnduranceAbilities.onPlayerMelee` reaches Meal Steal with unlock gate and miss roll. Forced-roll probes remain valid probability-boundary evidence. Result files: `mana-speed-mine-event-probe-result.txt`, `mana-terraform-event-probe-result.txt`, `mana-absorption-event-probe-result.txt`, `meal-steal-event-probe-result.txt`.

`AC-MATRIX-FINAL` (partial): disposable server booted `GraniteSkills-2.3.236.jar` (SHA-256 `B68A27167452C7DFA58024BF76A21D476891AEF4EA324F6F31E4E189DBEE16A9`) with `granitelib-fabric-0.2.0-alpha.19`. Skills and mana managers loaded; LuckPerms absent. MATRIX PASS is withheld while external BLOCKED deps remain. `matrix-final-partial-probe-result.txt` says PASS for the partial freeze only.

Environment recheck: Docker CLI present but `dockerDesktopLinuxEngine` pipe missing (Desktop engine not running). No LuckPerms/Geyser/Floodgate jars found for this disposable server. Java/Bedrock clients and production pack not available in-session.

`AC-REVIEW-F1`: the running mod dependency is `>=0.2.0-alpha.18`. `0.2.0-alpha.2` does not match. `0.2.0-alpha.18` and `0.2.0-alpha.19` do. `AC-REVIEW-F3`: the generic layout placed paper named `Named Paper`, lore `Line One` and `Line Two`, count 3, custom model data 7, and a filler named `Filler`. `AC-REVIEW-F4`: respawn replaced the cached player, a nether change replaced it again, farming stayed at level 9, and the new player's health modifier was 8. `AC-REVIEW-F5`: with `loot.directly_to_inventory` on, 3 diamonds entered an empty survival inventory and no diamond entity spawned. A full survival inventory dropped the next 2 diamonds. The option off dropped 2 emeralds and left the inventory without them. A full creative inventory also dropped 2 diamonds instead of deleting them. Fishing was not changed. The option was restored to false. `AC-REVIEW-F6`: console `modifier addtemp` stored `probe_temp` at 3.5 for 10 seconds, rejected the same name, stacked `probe_temp(2)`, rejected `nope` and `notastat`, and `trait addtemp` stored `probe_luck` at 6.25 with pause-offline. `short_temp` was gone after the expiry check. Save and load kept the temporary stat and the paused trait. `AC-REVIEW-F7`: `graniteskills.multiplier.7` contributes 0.07, and 7, -10, and farming 15 together contribute 0.12 while mining 40 does not. LuckPerms is not installed, so the online player had no granted nodes and the multiplier was 0. Deny and inheritance remain on `AC-PERM-PROVIDER`.

## Next actions

Status stays **INCOMPLETE**. Executable in-session work is exhausted. Remaining non-PASS rows are external blockers plus `AC-MATRIX-FINAL` (partial freeze only on `2.3.236`).

1. **AC-HUD-MYSQL / AC-MYSQL-* / AC-MIGRATION-SQL:** Start Docker Desktop so `dockerDesktopLinuxEngine` exists, then `docker run -d --name gs-mysql-test -e MYSQL_ROOT_PASSWORD=granite -e MYSQL_DATABASE=graniteskills -p 3307:3306 mysql:8.4`. Do not use MariaDB.
2. **AC-PERM-PROVIDER:** Install LuckPerms Fabric on the disposable server; prove real allow, deny, inheritance, non-operator, and Floodgate identity. Implementation coverage is already PASS on `AC-REVIEW-F7` / `AC-PERM-*`.
3. **AC-CLIENT-JAVA:** Join a Minecraft 26.3 Fabric server with an ordinary Java client and exercise menus, titles, and the idle line.
4. **AC-CLIENT-BEDROCK / AC-LIFECYCLE-GEYSER:** Geyser 2.12.1-SNAPSHOT b1302 + Floodgate 2.2.7-SNAPSHOT b69 with a Bedrock client.
5. **AC-PACK-PRODUCTION:** Run `GraniteSkills-2.3.236.jar` + `granitelib-fabric-0.2.0-alpha.19.jar` in the production mod pack.
6. **AC-MATRIX-FINAL:** After the above are PASS, rerun the full matrix on the frozen `2.3.236` bytes (or a newer freeze if bytes change).

## Earlier notes

These notes are historical narrative. The executable cases are the NOT STARTED rows in `acceptance-state.json`, not this list.

- Every remaining XP source behavior in the completion plan unit U13 except crop age (`AC-XP-CROP` PASS), right-click harvest (`AC-XP-HARVEST` PASS), jump (`AC-XP-JUMP` PASS), projectile credit (`AC-XP-PROJECTILE` PASS), bleed (`AC-XP-BLEED` PASS), brewing (`AC-XP-BREW` PASS), enchanting (`AC-XP-ENCHANT` PASS), grindstone (`AC-XP-GRIND` PASS), statistics (`AC-XP-STAT` PASS), and Growth Aura (`AC-XP-AURA` PASS). Wrong player does not yet have one PASS row.
- Item registry and modifier manager were not replaced. Duplicate ids are PASS (`AC-ITEM-DUPLICATE`). Expiry of a registered item grant is PASS (`AC-ITEM-EXPIRY`). `ignore_interact` is PASS (`AC-ITEM-IGNORE`). Unclaimed claim duplication is PASS (`AC-ITEM-CLAIM`).
- Profile transfer uses `UserState`, the same payload as the Bukkit `onTransfer` reference: skill levels, skill XP, stat modifiers, trait modifiers, and mana. That is not a missing-field defect. Conflict protection is the confirm gate in `AC-TRANSFER-CONFIRM`: a destination that already has progress is refused until `confirm` is supplied.
- Leaderboard GUI mode switching is PASS (`AC-LB-MODE`). Power ranked Broad then Spike. Average ranked Spike then Broad.
- Named menu load from a validated file is PASS (`AC-LIB-NAMED-FILE`). A user overlay that replaces specified menu values and keeps the rest is PASS (`AC-LIB-OVERLAY`). Reload that replaces the loaded menu and drops the previous listener is PASS (`AC-LIB-RELOAD`). Export of the current menu definition is PASS (`AC-LIB-EXPORT`). Status that reports the effective file, fallback, and error is PASS (`AC-LIB-STATUS`). Diagnostics that name the invalid file and the reason are PASS (`AC-LIB-DIAGNOSTICS`). A builder that loads a menu with title, rows, and slots is PASS (`AC-LIB-BUILDER`). Layout that places an item in the requested slot is PASS (`AC-LIB-LAYOUT`). Filler that fills empty slots and leaves a real item in place is PASS (`AC-LIB-FILLER`). A dynamic template row that repeats for the supplied entries is PASS (`AC-LIB-DYNAMIC`). A shared item referenced by more than one menu is PASS (`AC-LIB-SHARED-ITEM`). Open-by-id that receives a typed context and page is PASS (`AC-LIB-CONTEXT`). A placeholder that is replaced in a name, with an unknown placeholder reported, is PASS (`AC-LIB-PLACEHOLDER`). The same entries sorted in two orders is PASS (`AC-LIB-SORT`). Paging that shows the next slice without repeating the previous slice is PASS (`AC-LIB-PAGE`). Back that returns to the previous menu with its context is PASS (`AC-LIB-PREVIOUS`). Open by id that opens a registered menu and rejects an unknown id is PASS (`AC-LIB-OPEN-ID`). A click dispatched with the actor, and not run for a denied actor, is PASS (`AC-LIB-CLICK-PERM`). The independent consumer against the released GraniteLib jar is PASS (`AC-LIB-CONSUMER`). The eight built-in menus open through GraniteLib (`AC-MENU-MIGRATION`). The Skills menu open is PASS (`AC-MENU-SKILLS`). The Stats menu open is PASS (`AC-MENU-STATS`). The Level Progression open is PASS (`AC-MENU-LEVEL`). The Stat Info open is PASS (`AC-MENU-STAT-INFO`). The Sources open is PASS (`AC-MENU-SOURCES`). The Abilities open is PASS (`AC-MENU-ABILITIES`). The Leaderboard open is PASS (`AC-MENU-LEADERBOARD`). The Unclaimed Items open is PASS (`AC-MENU-UNCLAIMED`). Invalid menu YAML that names the file and keeps the last valid menu is PASS (`AC-MENU-INVALID-YAML`). A normal block break that awards its source XP once is PASS (`AC-XP-BREAK`). An external multi-block break that awards each eligible block and not the origin twice is PASS (`AC-XP-MULTIBLOCK`). An interact source that awards on interact and not on break is PASS (`AC-XP-INTERACT`). A collect source that awards when the item is collected is PASS (`AC-XP-COLLECT`). A fishing catch that awards fishing XP through the fishing handler is PASS (`AC-XP-FISH`). Dealing damage that awards the fighting source and not a defense source is PASS (`AC-XP-DAMAGE`). Taking damage that awards the defense source and not a fighting source is PASS (`AC-XP-DEFENSE`). A qualifying fall that awards agility XP, and a non-qualifying fall that does not, is PASS (`AC-XP-FALL`). A player kill that awards the entity source, and a mob kill that does not award the player source, is PASS (`AC-XP-KILL`). A thrown potion that credits its owner is PASS (`AC-XP-POTION`). A spawner mob that uses the configured multiplier, and a natural mob that does not, is PASS (`AC-XP-SPAWNER`). Brewing takeout that credits the player who started the brew is PASS (`AC-XP-BREW-OWNER`). Anvil XP awarded from the pre-consumption cost is PASS (`AC-XP-ANVIL`). Consuming a configured item that awards its source is PASS (`AC-XP-CONSUME`). A splash potion that awards the owner and not a bystander is PASS (`AC-XP-SPLASH`). Using a mana ability that awards its configured XP once is PASS (`AC-XP-MANA-USE`). Two callbacks for one event that do not award twice, and a different event at the same block that does, is PASS (`AC-XP-DOUBLE`). XP that is not credited to a different player recorded on the same event is PASS (`AC-XP-WRONG-PLAYER`). Growth Aura that runs on natural crop growth and bone meal, and not on nether wart, is PASS (`AC-XP-AURA-PATHS`). A repeated invalid action blocked by anti-AFK, and a valid action that is not, is PASS (`AC-XP-ANTIAFK`). A storage mutation that does not run until its confirmation is supplied is PASS (`AC-STORE-CONFIRM`). A stale confirmation that does not run the mutation is PASS (`AC-STORE-STALE`). Cancel that leaves the stored data unchanged is PASS (`AC-STORE-CANCEL`). A failed backup that does not replace the previous backup is PASS (`AC-BACKUP-FAIL`). Reload that sees the restored file is PASS (`AC-BACKUP-RELOAD`). An online player who sees restored progress without a reconnect is PASS (`AC-BACKUP-ONLINE`). Preset merge that keeps existing values and adds missing keys through the command is PASS (`AC-PRESET-MERGE`). Preset replace that writes the preset values only after confirm and a backup is PASS (`AC-PRESET-REPLACE`). Preset skip that leaves the target file unchanged is PASS (`AC-PRESET-SKIP`). A missing `preset.yml` that is rejected and does not write a file is PASS (`AC-PRESET-MISSING`). Two users who can save and load different YAML profiles at the same time is PASS (`AC-YAML-TWO-USERS`). An offline YAML profile that loads without the player present is PASS (`AC-YAML-OFFLINE`). A YAML profile that survives a server restart is PASS (`AC-YAML-RESTART`). Stat and trait modifiers that survive a YAML save and load are PASS (`AC-YAML-MODIFIERS`). Unclaimed items that survive a YAML save and load are PASS (`AC-YAML-ITEMS`). Item migration that updates an inventory, a container, and an entity once is PASS (`AC-MIGRATION-ITEM`). The covered admin command that runs from the server console is PASS (`AC-CMD-CONSOLE`). An offline target that is handled without loading a connected player is PASS (`AC-CMD-OFFLINE`). A bad argument that is rejected and does not change stored data is PASS (`AC-CMD-VALIDATION`). The power leaderboard command that prints the power order is PASS (`AC-LB-CMD-POWER`). The average leaderboard command that prints the average order is PASS (`AC-LB-CMD-AVERAGE`). The per-skill leaderboard command that prints that skill's order is PASS (`AC-LB-CMD-SKILL`). A quit with a missing user that does not hang, with the shaded text classes loaded, is PASS (`AC-LIFECYCLE-QUIT`). A GraniteLib older than 0.2.0-alpha.18 is rejected by the gameplay dependency, is PASS (`AC-REVIEW-F1`). A generic menu item that keeps its name, lore, count, and custom model data is PASS (`AC-REVIEW-F3`). Respawn and a dimension change that keep the current player, skill level, and health capacity is PASS (`AC-REVIEW-F4`). Inventory loot, including a full inventory and creative overflow, is PASS (`AC-REVIEW-F5`). Temporary stat and trait modifiers that expire, stack, and reload are PASS (`AC-REVIEW-F6`). A loaded menu definition whose edit changes the next opened title and item is PASS (`AC-REVIEW-F2`). A repeated break callback that awards once, and a new block at that position that still awards, is PASS (`AC-REVIEW-V1`). The live passive abilities, mana abilities, and Granite command roots each have a case, which added 9 NOT STARTED cases, is PASS (`AC-REVIEW-V2`). The raw probe results, logs, config, source revision, and both release jars are files in the 2.3.219 bundle, is PASS (`AC-REVIEW-V3`). Each of the 15 skill roots opening that skill's level progression is PASS (`AC-SCOPE-CMD-SKILL-SHORTCUT`). The four command aliases exposing the same commands is PASS (`AC-SCOPE-CMD-ALIASES`). `/abtoggle` cycling and setting the idle mode is PASS (`AC-SCOPE-CMD-ABTOGGLE`). `/armor` modifier, trait, requirement, and multiplier on held armor is PASS (`AC-SCOPE-CMD-ARMOR`). `/backup` create, save, list, info, load, and restore is PASS (`AC-SCOPE-CMD-BACKUP`). `/claimitems` opening the unclaimed menu and `/claimitems all` merging stacks is PASS (`AC-SCOPE-CMD-CLAIMITEMS`). `/lang` setting a loaded locale, rejecting an unknown code, and switching back is PASS (`AC-SCOPE-CMD-LANG`). `/mana` set, add, remove, and view is PASS (`AC-SCOPE-CMD-MANA`). `/modifier` add, addtemp, list, remove, and removeall is PASS (`AC-SCOPE-CMD-MODIFIER`). `/multiplier` self and named-player view is PASS (`AC-SCOPE-CMD-MULTIPLIER`). `/openmenu` for abilities, leaderboard, level_progression, skills, sources, stat_info, stats, and unclaimed_items is PASS (`AC-SCOPE-CMD-OPENMENU`). `/preset` list, info, and load confirm is PASS (`AC-SCOPE-CMD-PRESET`). `/profile` skills and stats is PASS (`AC-SCOPE-CMD-PROFILE`). `/rank` power and per-skill ranking lines is PASS (`AC-SCOPE-CMD-RANK`). Console-only `/resethealth` that strips offline `graniteskills:` attribute modifiers is PASS (`AC-SCOPE-CMD-RESETHEALTH`). `/skilllevel` add, remove, reset, set, setall, and view is PASS (`AC-SCOPE-CMD-SKILLLEVEL`). `/skillrank` for a skill ranking line is PASS (`AC-SCOPE-CMD-SKILLRANK`). `/skilltop mining` ordered leaderboard lines is PASS (`AC-SCOPE-CMD-SKILLTOP`). `/sources` menu open and `/sources diagnose` is PASS (`AC-SCOPE-CMD-SOURCES`). `/stats` opening the stats menu is PASS (`AC-SCOPE-CMD-STATS`). `/storage` info, save, reload, mergeskills, and removeallmodifiers is PASS (`AC-SCOPE-CMD-STORAGE`). `/toggle` cycling and setting the idle mode is PASS (`AC-SCOPE-CMD-TOGGLE`). `/top` and `/top average` including page 2 is PASS (`AC-SCOPE-CMD-TOP`). `/trait` add, addtemp, list, remove, and removeall is PASS (`AC-SCOPE-CMD-TRAIT`). `/transfer` onto an empty destination is PASS (`AC-SCOPE-CMD-TRANSFER`). `/updateleaderboards` including samples is PASS (`AC-SCOPE-CMD-UPDATELEADERBOARDS`). `/xpdiag` off/on/status is PASS (`AC-SCOPE-CMD-XPDIAG`). Alchemist potion duration bonus with scaling and unlock gate is PASS (`AC-ABILITY-ALCHEMIST`). Anti Hunger chance, exhaustion cancel, scaling, and unlock gate is PASS (`AC-ABILITY-ANTI-HUNGER`). Anvil Master max repair-cost allowance with scaling and unlock gate is PASS (`AC-ABILITY-ANVIL-MASTER`). Archer Archery XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-ARCHER`). Axe Master axe attack-damage bonus with scaling and unlock gate is PASS (`AC-ABILITY-AXE-MASTER`). Bigger Scoop excavation luck with scaling, unlock gate, and guaranteed extra dig drops is PASS (`AC-ABILITY-BIGGER-SCOOP`). Bleed proc chance and DoT damage with scaling and unlock gate is PASS (`AC-ABILITY-BLEED`). Bountiful Harvest farming luck with scaling, unlock gate, and guaranteed extra wheat is PASS (`AC-ABILITY-BOUNTIFUL-HARVEST`). Bow Master bow attack-damage bonus with scaling and unlock gate is PASS (`AC-ABILITY-BOW-MASTER`). Brewer Alchemy XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-BREWER`). Defender Defense XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-DEFENDER`). Disenchanter grindstone bonus XP with scaling and unlock gate is PASS (`AC-ABILITY-DISENCHANTER`). Enchanted Strength strength-per-enchant modifier with scaling and unlock gate is PASS (`AC-ABILITY-ENCHANTED-STRENGTH`). Enchanter Enchanting XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-ENCHANTER`). Epic Catch added epic fishing loot chance with scaling and unlock gate is PASS (`AC-ABILITY-EPIC-CATCH`). Excavator Excavation XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-EXCAVATOR`). Farmer Farming XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-FARMER`). Fighter Fighting XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-FIGHTER`). First Strike first sword-hit damage bonus with scaling, unlock gate, and cooldown is PASS (`AC-ABILITY-FIRST-STRIKE`). Fisher Fishing XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-FISHER`). Fleeting low-health movement-speed boost with scaling, unlock gate, and health threshold gate is PASS (`AC-ABILITY-FLEETING`). Forager Foraging XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-FORAGER`). Forger Forging XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-FORGER`). Geneticist plant-food saturation bonus with scaling, unlock gate, and non-plant failure gate is PASS (`AC-ABILITY-GENETICIST`). Golden Heal regeneration heal multiplier with scaling, unlock gate, and no-regen failure gate is PASS (`AC-ABILITY-GOLDEN-HEAL`). Golden Heart absorption damage reduction with scaling, unlock gate, and no-absorption failure gate is PASS (`AC-ABILITY-GOLDEN-HEART`). Grappler hooked-entity pull velocity with scaling, unlock gate, and unsafe-velocity failure gate is PASS (`AC-ABILITY-GRAPPLER`). Growth Aura extra crop stages with scaling, unlock gate, and out-of-range failure gate is PASS (`AC-ABILITY-GROWTH-AURA`). Hardened Armor armor durability cancel chance with scaling, unlock gate, and non-armor failure gate is PASS (`AC-ABILITY-HARDENED-ARMOR`). Healer Healing XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-HEALER`). Immunity entity-damage cancel chance with scaling and unlock gate is PASS (`AC-ABILITY-IMMUNITY`). Jumper Agility XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-JUMPER`). Life Essence magic heal multiplier with scaling, unlock gate, and no-magic failure gate is PASS (`AC-ABILITY-LIFE-ESSENCE`). Life Steal hostile-kill heal with scaling, unlock gate, and non-hostile failure gate is PASS (`AC-ABILITY-LIFE-STEAL`). Light Fall fall-damage reduction with scaling, unlock gate, and non-fall failure gate is PASS (`AC-ABILITY-LIGHT-FALL`). Lingering area-cloud radius-decay reduction with scaling and unlock gate is PASS (`AC-ABILITY-LINGERING`). Lucky Catch fishing-luck grant with extra catch amount, scaling, and unlock gate is PASS (`AC-ABILITY-LUCKY-CATCH`). Lucky Miner mining-luck grant with extra block drops, scaling, and unlock gate is PASS (`AC-ABILITY-LUCKY-MINER`). Lucky Spades epic excavation loot chance with scaling and unlock gate is PASS (`AC-ABILITY-LUCKY-SPADES`). Lucky Table enchant-level bump chance with scaling, unlock gate, and max-level failure gate is PASS (`AC-ABILITY-LUCKY-TABLE`). Lumberjack foraging-luck grant with extra log drops, scaling, and unlock gate is PASS (`AC-ABILITY-LUMBERJACK`). Meal Steal food-transfer chance with scaling and unlock gate is PASS (`AC-ABILITY-MEAL-STEAL`). Metal Detector rare excavation loot chance with scaling and unlock gate is PASS (`AC-ABILITY-METAL-DETECTOR`). Miner Mining XP multiplier with scaling and unlock gate is PASS (`AC-ABILITY-MINER`). Mob Master mob-attack damage reduction with scaling, unlock gate, and player-attacker failure gate is PASS (`AC-ABILITY-MOB-MASTER`). Pick Master pickaxe attack-damage bonus with scaling, unlock gate, and non-pickaxe failure gate is PASS (`AC-ABILITY-PICK-MASTER`). No Debuff harmful splash/lingering block chance with scaling and unlock gate is PASS (AC-ABILITY-NO-DEBUFF). Spade Master shovel attack-damage bonus with scaling, unlock gate, and non-shovel failure gate is PASS (AC-ABILITY-SPADE-MASTER). Scythe Master hoe attack-damage bonus with scaling, unlock gate, and non-hoe failure gate is PASS (AC-ABILITY-SCYTHE-MASTER). Runner Endurance XP multiplier with scaling and unlock gate is PASS (AC-ABILITY-RUNNER). Recovery low-health heal multiplier with scaling, unlock gate, and food/health failure gates is PASS (AC-ABILITY-RECOVERY). Sword Master sword attack-damage bonus with scaling, unlock gate, and non-sword failure gate is PASS (AC-ABILITY-SWORD-MASTER). Shielding sneak damage reduction with scaling, unlock gate, and standing failure gate is PASS (AC-ABILITY-SHIELDING). Sorcerer Sorcery XP multiplier with scaling and unlock gate is PASS (AC-ABILITY-SORCERER).
- Backup restore through the packaged command is PASS (`AC-BACKUP-RUNTIME`). Preset append, merge, replace, and skip through `preset load` confirm are PASS (`AC-PRESET-RUNTIME`). Storage-mutation runtime cases are NOT STARTED.
- Legacy folder migration is PASS (`AC-MIGRATION-LEGACY`). A conflicting player file stayed at level 9, then the second run left the edited level 11, `default_language: fr`, and `source: edited` in place. `IdentityMigratorTest` passed, 3 tests, 0 failures. Item and SQL migrations are still separate cases.
- MySQL runtime is BLOCKED (`AC-HUD-MYSQL`). Start Docker Desktop so `dockerDesktopLinuxEngine` exists, then run `docker run -d --name gs-mysql-test -e MYSQL_ROOT_PASSWORD=granite -e MYSQL_DATABASE=graniteskills -p 3307:3306 mysql:8.4` and round-trip `idle_mode`. MariaDB was not used as a substitute.
- Java client (`AC-CLIENT-JAVA`), Bedrock client (`AC-CLIENT-BEDROCK`), LuckPerms (`AC-PERM-PROVIDER`), and the production mod pack (`AC-PACK-PRODUCTION`) stay BLOCKED. See External handoff.
- Packaged dedicated-server lifecycle is PASS (`AC-LIFECYCLE-PACKAGED`) on `GraniteSkills-2.3.195.jar`. Geyser, `/skill` navigation, and the production pack are not part of that run.
- Java client and Bedrock client rows are BLOCKED.
- Production mod pack is BLOCKED.
- Concurrency and performance are PASS (`AC-PERF`). Four players finished the written bar in 11 ms. The run did not use a connected client.

## Exclusions

Jobs, economy, WorldGuard, Vault, PlaceholderAPI, MythicMobs, and ProtocolLib stay EXCLUDED BY EXISTING SCOPE. Placed-block tracking, permissions, and loot requirements are not in that exclusion.
