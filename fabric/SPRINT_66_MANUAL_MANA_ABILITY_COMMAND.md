# Sprint 66 — manual validation: `/manaability resetcooldown`

**Build:** **≥ 2.3.94** (`gradle.properties` → `projectVersion`).

**Reference:** Bukkit **`ManaAbilityCommand`** (`@Subcommand("manaability")`) has **one** method: **`resetcooldown`** with permission **`auraskills.command.manaability.resetcooldown`** (`plugin.yml` default **op** / **true** in the parent group; Fabric uses default **deny** for the node in **`FabricCommandRequirements`**).

## Command roots

| Root | Path |
|------|------|
| **`/auraskills`** | **`/auraskills manaability resetcooldown …`** |
| **`/skills`**, **`/sk`**, **`/skill`** | **`/skills manaability resetcooldown …`** (and same for **`/sk`**, **`/skill`**) |

**No** dedicated top-level **`/manaability`** (matches Bukkit: only under **`skills|sk|skill`**; Fabric also chains under **`/auraskills`** like other skills-root features).

## Syntax

```text
… manaability resetcooldown <player> <ability> [fill_mana]
```

- **`<player>`** — online player name (tab: online players).
- **`<ability>`** — enabled mana ability id (tab: registry **key**, e.g. values from **`ManaAbilities`**; **`FabricCommandManaAbilityParser`** accepts **`namespace:key`** when registered).
- **`[fill_mana]`** — optional Brigadier boolean: **`true`** or **`false`** (default when omitted = **false**).

## Permission

- **`auraskills.command.manaability.resetcooldown`** — default **deny** in Fabric’s permission check style; grant for staff.

## Behavior (reference-aligned)

- **Other-only in-game:** If the **sender** is a **player**, the **target** must **not** be the same player (Bukkit **`@Flags("other")`**). If you target yourself, you get **`auraskills.granite.command.manaability.other_target_only`**.
- **Console / RCON** — may run for **any** **online** target (no “other” rule).
- **Unknown / disabled** ability — **`commands.unknown_mana_ability`** (via **`CommandMessage.UNKNOWN_MANA_ABILITY`**) with formatting.
- **No profile** on target — **`CommandMessage.NO_PROFILE`**.
- If **`ability.isEnabled()`** and data has **cooldown &gt; 0** and **not activated** — set **cooldown** to **0**.  
- If **`fill_mana`** is **true** — set **mana** to **max** **mana** for that **User**.
- **`user.setShouldSave(true)`** after mutation.
- **No** **`ManaAbilityRefreshEvent`** on Fabric (Bukkit / **api-bukkit** only) — see **`FabricTimerCountdown`** javadoc.

## Relationship to `/abtoggle` and `/toggle`

- **`/abtoggle`** and **`/toggle`** (**Sprint 48**) only toggle the **IDLE** **action bar** line (`ActionBarType.IDLE`) for **self**.
- They do **not** change mana **ability** **cooldowns** or read **`ManaAbilityData`**.
- This sprint is an **admin** **reset** for **another** player’s (or **console**-specified) **cooldown** state — different surface.

## Java dedicated server — checks

1. **Grant** `auraskills.command.manaability.resetcooldown` to a test **op** account; ensure normal players **lack** it.
2. **Two** online players **A** and **B** with AuraSkills **profiles** and a **known** **enabled** **mana** **ability** id.
3. As **A** (in-game) with perms, run: **`/sk manaability resetcooldown B <ability> false`** — expect **success** (or no-op on cooldown/activated per reference) and message **`commands.manaability.resetcooldown.cooldown_reset`** (from **`messages_*.yml`**).
4. As **A**, run with **A** as target — expect **other_target_only** error.
5. **Console:** **`manaability resetcooldown A <ability> true`** under **`/auraskills …`** or literal path — may target **A**; verify **fill_mana** optional **true** top-offs **mana** when you can observe it in-game.
6. Invalid **ability** name — expect **unknown_mana_ability** feedback.

## Bedrock / Geyser

- Chat line length: keep commands short. **Not** proof-tested on a **Bedrock** client in this document unless you add it.

## Pass / fail

- [ ] **Permission** denied without the node.
- [ ] **Self**-target in-game **rejected**.
- [ ] **Unknown** **ability** rejected with clear text.
- [ ] **Success** line uses **prefix** + **`messages`** **`cooldown_reset`** keys.
- [ ] **Optional** `fill_mana` **true** / **false** works.
- [ ] **No** regression in **`/abtoggle`**.

## Risks / caveats

- **Online** **players** only (no offline UUID admin).
- **Bukkit** **event**-dependent **behaviors** (other plugins listening for **`ManaAbilityRefreshEvent`**) do **not** run on **Fabric**.

See **`fabric/COMMAND_PARITY_AUDIT.md`**.
