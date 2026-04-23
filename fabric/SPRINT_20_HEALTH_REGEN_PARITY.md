# Sprint 20 — Bukkit `HealthRegenTraits` vs Fabric (narrow diff)

**Reference:** `bukkit/.../trait/HealthRegenTraits.java`  
**Fabric:** `trait/FabricHealthRegenTraits`, `FabricHealthRegen`, `mixin/HungerManagerHealthRegenMixin`  
**Baseline:** [SPRINT_19_TRAIT_PARITY.md](SPRINT_19_TRAIT_PARITY.md) (Bukkit 14 / Fabric 11+).

## Traits in the group

| Trait | Bukkit | Fabric (Sprint 20) |
|-------|--------|-------------------|
| `SATURATION_REGEN` | `EntityRegainHealthEvent` (SATIATED) if `saturation > 0` + `use_custom_delay` / `CustomRegen` | Mixin: first `heal` in `HungerManager#update` → same **bonus = base + getBonusTraitLevel**; optional custom task if `use_custom_delay` |
| `HUNGER_REGEN` | else if `food >= 14` in event + custom | Mixin: second `heal` in `update` → same bonus math; optional custom task |

## Gating

| Check | Bukkit | Fabric |
|------|--------|--------|
| World | `isInDisabledWorld` (location) | `FabricTraitWorlds.isInDisabledWorld` |
| Trait enabled / bonus | `getBonusTraitLevel` in event / custom | Same |
| `use_custom_delay` (natural) | Cancel event, timer applies | Redirect skips `heal` (no vanilla tick heal); timer applies `setHealth` + `food--` as Bukkit |
| `CustomRegenEvent` (api-bukkit) | Fired; abilities may cancel/change | **Not fired** — in-repo Bukkit ability listeners (Endurance/Agility `recoveryCustom` / `fleetingEndCustom`) are **not** ported; document until Fabric exists |

## Vanilla vs Bukkit *SATIATED* shape (1.21.11)

- **Minecraft 1.21.11** `HungerManager#update` has exactly **two** `ServerPlayerEntity#heal` calls: saturation branch (variable amount) and a **1.0f** branch when `food >= 18` (and rules).
- Bukkit’s handler branches on `saturation > 0` vs `food >= 14` **independently** of which bytecode path ran. The Fabric **mixin** attaches traits to the **first** and **second** `heal` in source order, mapping them to **SATURATION_REGEN** and **HUNGER_REGEN** to mirror Bukkit’s if/else. Edge cases where Spigot would still fire a `SATIATED` event but 1.21.11 does not call `heal` are **rare**; treat as *Needs Regression Proof* if reported.

## Status

- **HealthRegenTraits group (natural + custom options):** **Equivalent** in math and gates vs reference, except **no** `CustomRegenEvent` and possible edge divergence above.  
- **Full Bukkit `BukkitTraitManager` set:** still **Partial** until `DoubleDropTrait` / `AnvilDiscountTrait` (and any follow-ups).
