# Sprint 135 — Final targeted XP fix (wheat, fishing, vein mining)

**Version:** `FabricAuraSkills-2.3.163.jar` (`projectVersion` **2.3.163**).  
**Scope:** Fix only confirmed gaps; do not rewrite working excavation/carrot/potato/sugar cane paths. **Do not** change `/sources` rendering, ability lore, leaderboard, GraniteLib, or menu layout.

## Wheat (Farming) — root cause

Runtime breaks **`minecraft:wheat`** (`source_lookup_key=break:block:minecraft:wheat`) while some merged or legacy configs list the crop under **seed/item-style** block names (e.g. **`wheat_seeds`**). **`FabricBlockXpSourceLookup`** compared only exact registry **paths**, so **`wheat`** did not match **`wheat_seeds`** and `source_match=false`.

## Wheat — fix

- **`FabricFarmingCropBreakAliases`**: small alias groups (wheat/wheat_seeds, beetroots/beetroot_seeds/beetroot, stems/melon/pumpkin, carrot/potato pairs, cocoa, torchflower, pitcher, sweet berries).
- **`FabricBlockXpSourceLookup.breakSourceBlockMatchesState`**: used in **`findForBreak`**; **Farming** skill sources also accept alias equivalence.
- **`FabricFarmingCropResolver.isMatureOrMatchingFarmBreakState`**: same matcher so maturity probes align.

## Fishing — root causes

1. **`@Redirect`** on **`PlayerEntity#increaseStat`** inside **`FishingBobberEntityMixin`** could **fail to apply** on 1.21.11 if bytecode no longer matches, so **`onFishCaughtStat`** never ran.
2. **`fishing_entry`** diagnostics called **`resolveCaughtStack`**, which **`take()`**s the pending stack, **emptying** it before bonus loot / XP logic.

## Fishing — fixes

- **`PlayerEntityIncreaseStatFishingMixin`**: **`HEAD`** on **`PlayerEntity#increaseStat`** — when **`FabricFishingBobberUseContext`** has the active bobber and stat is fish-caught, call **`FabricFishingXpHandler.onFishCaughtStat`**.
- **`FabricFishingBobberUseContext`**: set on bobber **`use`** **HEAD**, cleared on **`use`** **RETURN** (with **`FabricFishingCatchHolder.clear()`**).
- **`FabricFishingCatchHolder#peek`**: non-destructive read for **`fishing_entry`** logging.
- **`FabricXpDiagnostics.logFishingCompat`**: adds **`source_lookup_key`**; **`shared_xp_gate`** reason when **`shouldBlockXp`** blocks fishing.
- Removed the fragile **`@Redirect`** from **`FishingBobberEntityMixin`**.

## Vein mining — root cause

Fabric **BEFORE** and **AFTER** `PlayerBlockBreakEvents` can land on **different server ticks** for some modded/async paths. **`FabricBlockBreakPreCapture`** and **`FabricModCompatMultiBreakXp`** drafts were keyed **including tick**, so a one-tick skew caused **missed `take` / draft remove** → no vanilla break XP and no multi-break job.

## Vein mining — fix

- **`FabricBlockBreakPreCapture.takeRelaxed`**: try **`afterTick` down to `afterTick - 5`** when removing the pre-break snapshot.
- **`FabricModCompatMultiBreakXp.removeDraftRelaxed`**: same for **`DRAFTS`**; **`JOBS`** key uses **`d.tick`** from the recovered draft.

## Regression checks (in-game)

With **`/auraskills xpdiag on`**:

- **Carrots, potatoes, sugar cane:** still **`source_match=true`** / award (exact path match unchanged for non-alias cases).
- **Excavation:** unchanged handler path.
- **Standing ore:** still **`vanilla_break`** / **`award_called=true`**.
- **Wheat:** mature crop → **`source_match=true`**; immature → no award (state filter unchanged).
- **Fishing:** **`fishing_entry`** + **`fishing_bobber_use`** with **`source_lookup_key`**; unknown loot → **`reason=no_matching_source`**.

## Gradle

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`

## Tests

- **`FabricS135FarmingCropAliasTest`**: alias table only (no full server bootstrap).