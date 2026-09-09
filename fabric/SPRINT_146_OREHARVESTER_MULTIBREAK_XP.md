# Sprint 146 — OreHarvester multi-break XP + `/xpdiag` root command

## Problem — no XP for vein-mined blocks (Ore Harvester)

Users mining an ore vein with Serilum's **Ore Harvester** mod (crouch + mine) received XP only for the trigger block, or none at all. Sprint **145** had disabled AuraSkills' multi-break compensation entirely (`mod_compat.multi_break_xp.enabled: false`) because the previous snapshot-scanner implementation interfered with the vein mine.

## Root cause (Ore Harvester)

Confirmed by reading Ore Harvester's source ([github.com/Serilum/Ore-Harvester](https://github.com/Serilum/Ore-Harvester), branch **1.21.11**):

1. Ore Harvester's Fabric entrypoint (`ModFabric.java`) registers **`PlayerBlockBreakEvents.BEFORE`** and its handler **`OreMineEvents.onOreHarvest`** returns **`false`** when a vein harvest triggers.
2. Returning **`false`** **cancels** the vanilla break. Therefore **`PlayerBlockBreakEvents.AFTER`** never fires, and **`FabricBlockBreakHandler`** — which awards all block-break XP from the AFTER event — was never invoked for any block in the vein, not even the block the player actually hit.
3. Ore Harvester then removes every ore itself by calling Collective's **`BlockFunctions.dropBlock`**, which is **`Block.dropResources(state, level, pos, blockEntity, entity, tool)`** followed by a direct **`level.setBlock(pos, AIR, 3)`**. That bypasses every Fabric block event, which is also why the Sprint **142–144** snapshot/world-diffing approach kept reporting **`removed_count=0`**.
4. Additional trap: **`PlayerBlockBreakEvents.BEFORE`** short-circuits on the first listener that returns **`false`**, so if Ore Harvester's listener is registered before AuraSkills', AuraSkills' own BEFORE listener never runs at all. Anything that arms from BEFORE is unreliable by construction.

## Fix — external capture via `Block.dropStacks`

New read-only capture path built on **`Block.dropStacks`** (Yarn name for Mojang's **`Block.dropResources`**):

- **`BlockDropStacksMultiBreakMixin`** — **`@Mixin(Block.class)`** with an **`@Inject`** at **`@At("HEAD")`** on the **`dropStacks(BlockState, World, BlockPos, BlockEntity, Entity, ItemStack)`** overload. It is **not** cancellable, returns nothing, redirects nothing, and mutates nothing, so Ore Harvester's harvest behaviour is bit-for-bit unchanged. Registered in **`auraskills.mixins.json`**.
- **`FabricExternalBlockBreakXp`** — owns the capture window and the XP award.
- The capture window is armed from the existing **`ServerPlayerInteractionManagerTryBreakPreCaptureMixin`** at **`tryBreakBlock`** **HEAD** (via **`FabricExternalBlockBreakXp.arm`**), which runs **before** any **`PlayerBlockBreakEvents.BEFORE`** listener and therefore cannot be short-circuited by another mod.
- Each captured removal is filtered: it must be attributed to the armed **`ServerPlayerEntity`**, be in the same **`ServerWorld`**, and be within **32** blocks of the origin. Per-harvest block count is capped by **`mod_compat.multi_break_xp.max_blocks`** (default **128**, hard ceiling **1024**).
- XP is applied at **`ServerTickEvents.END_SERVER_TICK`**, i.e. after the normal break pipeline has had its chance to pay the primary block. Positions already paid are skipped via **`FabricXpHarvestDedupe.wasAwardedSince(world, pos, minTick)`**, so normal single-block mining is still paid exactly once. Each captured block runs the same gates as a normal break: block source lookup, **`check_replace`** / placed-block, and **`shouldBlockXp`**.
- Deliberate omissions for external captures, and why: **`FabricBlockLevelerHelper`** is not used because the block is already air at flush time so a live world read would misreport multi-block sources; gathering-luck extra drops are not applied because the harvesting mod already resolved and placed the drops, and re-applying would mean mutating the world.
- AuraSkills' own Treecapitator no-XP path (**`FabricTreecapitator.breakWithoutXp`**) calls **`Block.dropStacks`** directly, so it now brackets that call with **`FabricExternalBlockBreakXp.setInternalBreak(true/false)`** to stay excluded from capture.

## Silk touch

Silk touch is fixed by the same change and needs no special case: **`Block.dropStacks`** is called for silk-touch harvests too, and the capture records the broken **`BlockState`** (e.g. **`deepslate_diamond_ore`**), not the produced item. Silk-touched ore therefore resolves the same Mining source as a normally-mined one, in a vein or on a single block.

## Removed — snapshot scanner

The Sprint **131–144** snapshot scanner **`FabricModCompatMultiBreakXp`** (roughly **550** lines at the tail of **`FabricBlockBreakHandler.java`**) is deleted, along with its now-unused config keys and Option constants **`MOD_COMPAT_MULTI_BREAK_RADIUS`** (`mod_compat.multi_break_xp.radius`) and **`MOD_COMPAT_MULTI_BREAK_WINDOW_TICKS`** (`mod_compat.multi_break_xp.window_ticks`). It could never observe Ore Harvester's removals (they bypass events entirely) and its expanding world scans were the Sprint **145** interference source. **`FabricLevelManager`** now registers **`FabricExternalBlockBreakXp`** instead.

## Config change

Embedded **`config.yml`** (`common/src/main/resources/config.yml`):

Before:

```yaml
multi_break_xp:
  enabled: false
  radius: 4
  window_ticks: 60
```

After:

```yaml
multi_break_xp:
  enabled: true
  max_blocks: 128
```

New Option constant: **`MOD_COMPAT_MULTI_BREAK_XP_MAX_BLOCKS`** (`mod_compat.multi_break_xp.max_blocks`, INT).

Operators upgrading keep their old config file; set **`enabled: true`** and add **`max_blocks: 128`**. Stale **`radius`** / **`window_ticks`** keys are now ignored.

## Problem 2 — `/xpdiag` did not work in game

Two independent causes, both fixed:

1. There was no top-level **`/xpdiag`** command. It only existed as a subcommand of **`/auraskills`**, **`/skills`**, **`/sk`**, and **`/skill`**. **`FabricBrigadierCommands`** now also registers **`FabricXpDiagnosticsCommands.xpDiagRoot()`** as a standalone root, so **`/xpdiag`** works directly. **`/auraskills xpdiag`** etc. still work.
2. The permission gate was **`FabricCommandRequirements.permission(XP_DIAG, false)`**. With the boolean overload of LuckPerms' fabric-permissions-api, servers with no permission provider installed fall back to the literal default **`false`**, so even operators and the server console were denied and the node was hidden from tab-completion. Added **`FabricCommandRequirements.permissionOrOpLevel(node, opLevel)`**, which uses the int overload so it falls back to the vanilla operator level; **`xpdiag`** now requires the **`auraskills.command.xpdiag`** node **OR** operator level **2**.

Also: bare **`/xpdiag`** with no argument now reports **status** instead of printing usage. Usage remains **`/xpdiag on | off | status`**.

## Diagnostics

The **`xpdiag`** lifecycle line for this path is:

`event=multi_break_lifecycle phase=external_capture_flush`

with **`snapshot_count`** / **`removed_count`** / **`matched_count`** / **`xp_awarded_blocks`** / **`dedupe_skipped_count`** / **`placed_block_skipped_count`** / **`fallback_detected`** (overflow beyond **`max_blocks`**) / **`reason`**. It is only emitted when more than one block was captured or when XP was actually awarded, so ordinary single-block mining does not spam the log. The new **`already_awarded_by_break_event`** reason distinguishes normal-mining dedupe from a genuinely blocked award.

## Tests

- **`FabricS146ExternalMultiBreakXpTest`** (6 tests): config defaults **`enabled: true`** + **`max_blocks`** present; capture mixin is read-only (HEAD, no cancellable, no **`CallbackInfoReturnable`**, no **`@Redirect`**, no **`@Overwrite`**, no world mutation); arming comes from **`tryBreakBlock`** and not from the short-circuiting BEFORE event; **`max_blocks`** clamp/fallback behaviour; **`xpdiag`** lifecycle reason classification including **`already_awarded_by_break_event`**; Treecapitator no-XP path excluded from capture.
- Updated: **`FabricS141RuntimeOrderAndStateProofTest`** and **`FabricS142VeinMiningAndAgilityTest`** now call **`FabricExternalBlockBreakXp.reasonFromCounts`** (the helper moved with the class).
- **`FabricS145EmergencyOreHarvesterAndAgilityTest`**: the "multi-break disabled by default" assertion was superseded — it now only asserts the config section still exists; the default assertion lives in the Sprint **146** test.

## Untouched

Farming, fishing, defense/fighting/excavation, **`/sources`** rendering, ability lore, leaderboard, and GraniteLib are untouched. Normal (non-vein) mining XP is unchanged and still paid exactly once.

## Manual validation checklist

1. Enable diagnostics: **`/xpdiag on`** (or bare **`/xpdiag`** to confirm status).
2. With Ore Harvester installed, crouch-mine an ore vein; confirm Mining XP scales with the number of ores broken.
3. Confirm the vein still fully breaks (Ore Harvester behaviour unchanged).
4. Silk-touch a vein; confirm XP awards per ore block (Mining source from ore state, not dropped item).
5. Mine a single ore without crouch; confirm XP is paid **once**, not twice.
6. In server log with **`xpdiag`** on, confirm a **`multi_break_lifecycle`** line with **`phase=external_capture_flush`**, non-zero **`xp_awarded_blocks`** for multi-ore harvests, and no spam on ordinary single-block mining.

## Gradle

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`
