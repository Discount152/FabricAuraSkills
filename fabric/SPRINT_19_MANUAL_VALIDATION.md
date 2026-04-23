# Sprint 19 — manual validation (`EXPERIENCE_BONUS` trait, Fabric)

**Build:** `.\gradlew -PfabricOnly=true :fabric:build` (or full `:fabric:build`).

## Preconditions

- Skill config loads; trait **`experience_bonus`** (or the configured id for `Traits.EXPERIENCE_BONUS`) is enabled and granted on a test player (e.g. via stat/reward with non-zero value).
- Note **disabled world** list in `config.yml` if you test world gating.

## Cases

1. **Positive vanilla XP (orbs)**  
   - Grant XP orbs or break a block that drops XP.  
   - **Expect:** received points ≈ `floor(base * (1 + trait%/100))` (same as Bukkit `PlayerExpChangeEvent` int cast), compared to a character with 0% trait in the same situation.

2. **Negative delta (e.g. enchanting)**  
   - Spend levels at an **enchanting table** (or other path that uses negative `addExperience` int).  
   - **Expect:** the **magnitude** of the penalty scales with the same `(1 + bonus)` factor (Bukkit multiplies the event amount, including negatives).

3. **Disabled world**  
   - With the character standing in a **disabled** world (per `FabricTraitWorlds` / `world` naming rules), gain XP orbs.  
   - **Expect:** **no** trait scaling — same as plain vanilla for that world.

4. **Java vs Bedrock (optional)**  
   - Repeat (1) on a **Geyser/Floodgate** account.  
   - **Expect:** same server-side XP total as Java for the same action (no duplicate scaling).

5. **Skill XP**  
   - Confirm **Skill** XP (AuraSkills `LevelManager` / skill progress) is still independent — this sprint only affects **vanilla** experience **points** applied through `addExperience` on the server.

## Not tested here

- `HealthRegenTraits`, `DoubleDropTrait`, `AnvilDiscountTrait` (not part of this sprint’s implementation).
