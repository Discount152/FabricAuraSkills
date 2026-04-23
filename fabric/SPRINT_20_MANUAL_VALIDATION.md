# Sprint 20 — manual validation (`HUNGER_REGEN` / `SATURATION_REGEN`)

**Build:** `.\gradlew -PfabricOnly=true :fabric:build` (or `:fabric:build`).

## Preconditions

- `Natural Regeneration` gamerule **on** (default).
- Difficulty not Peaceful (for some hunger-tick behavior).
- Grant non-zero `hunger_regen` / `saturation_regen` **bonus trait** on a test account (e.g. via rewards/stats) with `use_custom_delay: false` first.

## Natural regen (default `use_custom_delay: false`)

1. **Saturated full hunger** (food 20, extra saturation) — stand still with missing HP. **Expect** faster/bigger heal per natural tick than a character with 0 bonus (compare side-by-side or log effective heal delta).
2. **High food, no sat** (e.g. food 18–20, no saturation) — same with missing HP. **Expect** hunger-branch bonus on the 1 HP tick path vs 0 bonus.
3. **Disabled world** — same as (1) in a `disabled_worlds` entry. **Expect** no trait addition (vanilla only).
4. **Spectator / creative** — `canFoodHeal` should prevent natural food regen; **no** double-heal from our code when vanilla does not call `heal`.
5. **Java vs Bedrock (optional):** same server; confirm HP and hunger sync — logic is server-side.

## Custom delay (`use_custom_delay: true` on a trait, plus `base` / `delay`)

1. **Turn off** natural heal branch for that trait: vanilla `heal` in `HungerManager#update` should be skipped for that path; **custom** timer should add health + drain food as Bukkit.
2. Match YAML **delay** units to Bukkit (integer × **50 ms** per task period).
3. **Note:** Bukkit `CustomRegenEvent` does not exist on Fabric; **no** in-repo test for `api-bukkit` ability listeners until those abilities exist on Fabric.

## Not in this sprint

- `DoubleDropTrait`, `AnvilDiscountTrait`.
