# Sprint 03 — Mob loot handler (EVT-007) — manual validation

**Reference:** Bukkit `bukkit/.../loot/handler/MobLootHandler.java` (`EntityDeathEvent`; killer from `EntityDamageByEntityEvent`: player or `Projectile` whose shooter is a player).

**Fabric:** `FabricMobLootHandler` — `ServerLivingEntityEvents.AFTER_DEATH`; killer = `DamageSource` direct `ServerPlayerEntity`, or `ProjectileEntity` whose owner is `ServerPlayerEntity` (then `EntityDamagers.PROJECTILE`), or `getAttacker()` as player fallback. Skill for loot XP defaults to **Fighting** unless `FabricEntityXpSourceLookup` finds a **DEATH** then **DAMAGE** `EntityXpSource` for the victim path + mapped `DamageCause`. Table: `NamespacedId.fromDefault("mob")` → `loot/mob.yml`. **`mobs:`** contexts use **`FabricMobContextProvider`** (registry paths, e.g. `zombie`).

## Config / data

- Default mob table: `config/auraskills/loot/mob.yml` (packaged like Bukkit).
- `loot_directly_to_inventory` applies the same way as block skill loot (`FabricLootManager#toInventory`).

## Java (Fabric dedicated server)

1. **Melee kill:** Kill a mob with a sword; if `mob.yml` defines a pool with no `mobs:` filter (or matching the mob type) and high `base_chance`, confirm bonus item or command (and optional loot-table XP) when the roll succeeds.
2. **Ranged kill:** Kill with bow/crossbow/trident; confirm the same (killer must be a player-owned projectile; matches Bukkit projectile path).
3. **Mob-specific context:** In `mobs: [zombie]` (or your test entity) only, confirm another entity type does **not** match that entry’s `mobs` context (and vice versa).
4. **Skill resolution:** If you have an `EntityXpSource` with `DEATH` + matching entity + `causes` / `damagers` for that kill, confirm loot **XP** (if any) uses that skill; otherwise expect **Fighting** for the skill parameter to `giveXp` on loot, same as Bukkit’s default.
5. **Creative / disabled world:** With `disable_in_creative` or a blocked/disabled world, confirm **no** mob skill loot.
6. **No exploit duplicate drops:** On one kill, you should not get **two** AuraSkills bonus rolls for the same table from this handler; vanilla mob drops are unchanged (this handler only adds skill loot).

## Bedrock (Geyser / Floodgate)

- Kills are still processed as `ServerPlayerEntity` on the server. Re-run steps 1–2 on a linked Bedrock account; any loot **messages** use the same Fabric `MessageKey` / chat path as Java.

## Automated tests

- None in `:fabric` for world kill flows (same as other Fabric gameplay handlers). Build: `.\gradlew -PfabricOnly=true :fabric:compileJava` or full `:fabric:build`.

## Known differences vs Bukkit

- **LootDropEvent:** Fabric does not fire Bukkit’s cancellable `LootDropEvent` before item spawn.
- **WorldGuard** `CUSTOM_LOOT`: not applied on Fabric.
- **Killer gating:** Bukkit only runs when the last damage is an `EntityDamageByEntityEvent`. Fabric uses the **killing** `DamageSource` from `AFTER_DEATH`; if the final hit is not attributable to a player/owned projectile, no skill loot (aligned intent; edge cases like bleed-only kills may differ from Bukkit if last cause differs).
