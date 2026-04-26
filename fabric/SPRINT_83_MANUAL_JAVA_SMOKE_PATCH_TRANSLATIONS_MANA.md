# Sprint 83 — manual validation: Granite translatable + `/mana view` (Java dedicated)

| Field | Value |
|--------|--------|
| **Sprint** | **83** — **code + operator re-check** (no new features) |
| **JAR** | **`FabricAuraSkills-2.3.116+`** |
| **Bedrock / Geyser** | **Not tested** this sprint (unchanged) |

## Screenshots / symptoms (Sprint 82 operator run)

- **`/auraskills`**, **`/auraskills help`:** chat showed **raw** keys `auraskills.granite.command.auraskills_usage`, `auraskills.granite.command.help`.
- **`/skills` / `/stats` (and related open menu):** inventory **title** showed `auraskills.granite.menu.skills_title` / `stats_title`; **“opened”** feedback showed `skills_opened` / `stats_opened`.
- **`/mana view`:** Brigadier: **Incorrect argument** — parser did not accept `view` as a child of `mana` in the test setup (root cause: **`MANA_OTHER`** on the **whole** `view` subcommand, so the node was **hidden** for senders who had **`mana` but not** `aura…mana.other`).

## Root cause (translatable)

Minecraft **client** chat / inventory titles resolve `Text.translatable` using **the client’s** language resources. A **vanilla** Java client (or one **without** `assets/auraskills/lang/…` on the **client** side) has **no** `auraskills.granite.*` keys → the **untranslated key string** is shown.

`en_us.json` on the **server** was already correct; the bug was the **translatable wire format**, not a missing file on disk.

## Fix (in-repo, 2.3.116)

- **`FabricGraniteText#key`:** uses **`net.minecraft.util.Language` on the server** (same `en_us.json` loaded in the mod) and builds **`Text.literal(…)`** for player-facing **chat** and **server-built menu titles** on the **fixed paths** in Sprint 83 (root usage/help, **Skills/Stats** built-in **titles** in `FabricMenuRegistry`, “opened” feedback in `FabricBrigadierCommands` + `FabricOpenMenuCommands#sendListMenuFeedback`). Other menus still use `Text.translatable` in deeper GUI slices — future passes can expand **literal** resolution where operators report raw keys.
- **`/mana view` (no `player` argument):** the **`view`** literal is registered with **`.executes(…executeSelf…)`** so **`/mana view`** = **`/mana`** for **in-game** self display (**`auraskills.command.mana`**). **`/mana view <player>`** still requires **`auraskills.command.mana.other`**.

## Keys verified in `en_us.json` (tests)

- `auraskills.granite.command.auraskills_usage`
- `auraskills.granite.command.help`
- `auraskills.granite.menu.skills_title`
- `auraskills.granite.menu.stats_title`
- `auraskills.granite.menu.skills_opened`
- `auraskills.granite.menu.stats_opened`

## Final `/mana` syntax (Fabric, Sprint 83)

| Command | Permission (typical) | Behavior |
|---------|------------------------|----------|
| `/mana` | `auraskills.command.mana` | **Self** current/max (MessageBuilder `MANA_DISPLAY`) |
| `/mana view` | `auraskills.command.mana` | **Same** as `/mana` for a **player**; **console** must use a **player** argument or join |
| `/mana view <onlinePlayer>` | `auraskills.command.mana` + `auraskills.command.mana.other` | **Other** user’s mana (target locale) |
| `/mana add\|set\|remove <player> <amount>` | admin nodes per Sprint 41 | unchanged |

Also available under **`/auraskills mana …`** and **`/skills|/sk|/skill mana …`** (Sprint 46 tree).

## Operator re-validation steps

1. **Deploy** `2.3.116+` on the **disposable** dedicated server; **full restart** after JAR swap.
2. As a **vanilla** Java client (or any client **without** a FabricAuraSkills client mod/resource pack with `auraskills` lang):
   - `/auraskills` and `/auraskills help` → **readable** English, **not** `auraskills.granite.…` strings.
   - `/skills` / `/stats` (or `openmenu` variants) → **“Skills” / “Stats”** (or custom title from your built-in `menus` YAML) on the **window**, **not** a raw key; **“Opened …”** line readable.
3. **Permissions:** with **only** `auraskills.command.mana` (and **not** `…mana.other`), run:
   - `/mana` **Pass**
   - `/mana view` **Pass** (same as `/mana`)
   - `/mana view <otherPlayer>` **denied** or **invisible** as designed without **other** (permission / parse).
4. **Grant** `…mana.other` and confirm **`/mana view <name>`** works with **no** Brigadier “incorrect argument on `view`” when the **player name** is present.

**Console watch:** no new stack traces from menu open or mana commands; **translation** and **text adapter** should stay quiet for these paths.

## Build evidence (in-repo)

- `.\gradlew -PfabricOnly=true :fabric:test` — **PASS**
- `.\gradlew -PfabricOnly=true :fabric:build` — **PASS**

**Bukkit** module: **not** required for this sprint (no `common` API change to shared command layer).

## Remaining limitations

- **Item lore / deep menu rows** may still use `Text.translatable` in some `Fabric*MenuGui` code paths; only the **Sprint 82**-reported **surfaces** were converted in **83**. File follow-ups with screenshots if raw keys reappear in **lore** or sub-menus.
- **Per-player client locale** for **Granite**-prefixed `en_us` is **not** reimplemented here — the server’s loaded language (typically **en_us** for the dedicated server) is the source. YAML **`messages_*.yml`**-driven `MessageBuilder` output is **unchanged** and still per-user locale where the common layer supports it.
- **Bedrock / Geyser:** **not** run-time tested for these fixes.

## Recommended next sprint (process)

- Re-run the **Sprint 82** matrix (or the **Sprint 78** minimum) on **`2.3.116+`** and re-mark any row that was **Fail (raw key)** as **Pass** or open a **targeted** follow-up. Optionally schedule **Geyser** action bar + menu validation when the **Java** matrix is **green**.
