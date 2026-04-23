# Fabric AuraSkills — full player guide (simple language)

This guide is for **people playing on a Minecraft Java server** that has **Fabric AuraSkills** installed. It explains what the mod does, what you will see, and what you can type. It matches **this Fabric port** (dedicated server). Some things differ from the original Paper plugin; those are called out at the end.

**Quick command list (plain text):** **`MOD_COMMANDS.txt`** (same folder as this file).

---

## 1. What you need to play

- **Minecraft Java Edition** (same major version the server runs; this mod is built for **1.21.11**).
- A normal **Fabric** or **vanilla** client is usually fine: this build registers **server-side** logic. If the server owner asks you to install a matching client mod, follow their instructions.
- You must be able to **join the multiplayer server** like any other world.

You do **not** need to download AuraSkills yourself unless the server tells you to.

---

## 2. What this mod does (in one paragraph)

**Fabric AuraSkills** adds **skills** (like Farming, Mining, Fighting). You earn **XP** by playing Minecraft (mining, fighting, fishing, brewing, and more—whatever the server configured). When you have enough XP in a skill, that skill **levels up**. Higher levels unlock **passive abilities**, **stat bonuses**, and **mana abilities** (special moves that cost **mana**). Your progress is **saved on the server** so it comes back when you log in again.

---

## 3. The skills (names you will hear)

These are the skill ids the server uses (lowercase in permissions and commands):

| Skill id | Plain meaning |
|----------|-----------------|
| `farming` | Crops, plants, food from farming-style actions |
| `foraging` | Trees, logs, natural plant gathering |
| `mining` | Ores and stone underground |
| `fishing` | Fishing rods and catches |
| `excavation` | Dirt, sand, gravel-type digging |
| `archery` | Bows and crossbows |
| `fighting` | Melee combat against mobs and players |
| `defense` | Taking damage and surviving |
| `agility` | Movement (for example jumping) |
| `endurance` | Walking and sprinting distance |
| `alchemy` | Potions and brewing |
| `enchanting` | Enchanting table usage |
| `sorcery` | Extra magical-style progression (per server config) |
| `healing` | Healing yourself and allies |
| `forging` | Anvils, grindstones, smithing-style work |

**Important:** The **exact XP** you get for each action is decided by the server’s **config files**, not by this guide. If something “should” give XP but does not, that is a **server configuration** question for the owner.

---

## 4. What happens the first time you join

1. The server **creates or loads** your profile in the background.
2. If the server uses **file storage** (usual default), your data lives under the server folder in **`config/auraskills/userdata/<your-uuid>.yml`**. You never need to open this file as a player; it is for backups and admins.
3. If the server uses **MySQL** (optional), data is in a database instead; you still just play normally.
4. There is **no forced tutorial screen** in this Fabric port. You learn by playing and by reading this guide (or the server’s own rules).

---

## 5. How you earn skill XP (player view)

Think in categories. The server awards XP when the game events match the server’s **source** lists (YAML under `config/auraskills/sources/` on the server).

**Typical examples (only if the server left defaults on):**

- **Mining:** break stone and ores → `mining` XP (amount varies by block).
- **Farming:** break or harvest crops with the right tool → `farming` XP.
- **Foraging:** break logs and leaves → `foraging` XP.
- **Fighting:** hurt or kill mobs (and sometimes players) → `fighting` / `archery` / `defense` XP depending on how you fight.
- **Fishing:** reel in fish and junk/treasure → `fishing` XP when the catch matches a rule.
- **Brewing:** work at a brewing stand (stand still while brewing; taking potions out can grant XP for `alchemy` if configured).
- **Enchanting:** spend levels at an enchanting table → `enchanting` XP if configured.
- **Anvil / grindstone:** taking repaired or disenchanted results can grant `forging` XP if configured.
- **Jumping / walking:** repeated natural movement can grant `agility` / `endurance` XP if those sources are enabled (anti-cheat style limits may apply on the server).

**Rules that can stop XP even when you do the action:**

- **Permission denied:** an admin can remove XP for one skill with LuckPerms (see §10).
- **Wrong world:** admins can disable skills in certain dimensions.
- **Creative mode:** the server can turn off XP while you are in Creative if they set that in config.
- **Anti-AFK:** if enabled, farming the same tiny spot over and over may stop counting until you play normally again.

---

## 6. Leveling up: what you see and hear

When a skill levels up, the server tries to celebrate it:

- A **sound** (often like the vanilla level-up sound) if the server left sounds on in `config.yml`.
- A **message** in chat and/or above the hotbar (**action bar**) depending on `config.yml` and `messages` files.
- On **this Fabric port**, “title” style popups from the original plugin may appear as a **plain chat line** instead of a big screen title. That is a known difference; the level-up still happened.

**Roman numerals** for levels in text are optional; the server turns them on or off in config.

---

## 7. Stats, traits, and passive abilities

- **Stats** are numbers like health, damage, and movement bonuses. Many come from skill levels and passives automatically. You do not “spend points” in this port during normal play unless the server added something else on top.
- **Passive abilities** unlock as you raise skills. They use names like **Farmer**, **Lucky Miner**, **Lumberjack**, and so on. Their **exact numbers** are in the server’s `skills.yml` / `abilities.yml` and language files. Read in-game messages or ask staff for a short list for *this* server.
- **Mana** is a separate bar the server tracks if mana is enabled. Mana refills over time (speed depends on config and traits).

---

## 8. Mana abilities (special moves)

Mana abilities cost **mana** and usually have a **cooldown**. They are **tied to a parent skill** (for example Terraform belongs to Excavation). You must be **high enough level** in that skill and meet any **readiness** rule (often “right-click with the right tool to ready, then do the action”).

**On this Fabric port, these providers are wired:**

| Mana ability | Parent skill (typical) | How it works in simple terms |
|--------------|------------------------|--------------------------------|
| **Replenish** | Farming | **Right-click** with a **hoe** to “ready” the ability, then **break fully grown crops** to replant them automatically while the ability is active. |
| **Speed Mine** | Mining | While you hold a **pickaxe** and break certain mineable blocks, the server can try to **trigger Speed Mine** on eligible blocks (tags in config). |
| **Terraform** | Excavation | **Right-click** with a **shovel** to ready, then **break one eligible ground block**; matching **neighboring blocks in a line** can break too, up to server limits. |
| **Treecapitator** | Foraging | Hold an **axe**. **Right-click** (use) to **raise / ready** the ability (you should see a short message). While it is readied, **break one log** in a tree; matching **connected logs** can break in a chain up to server limits. Sneaking may be required if the server turned that on. |
| **Charged Shot** | Archery | Optional **toggle** with **left-click / swing** while holding a **bow**; your **next bow shot** can deal extra damage for mana. |
| **Absorption** | Defense | Works with **shields**; helps the **shield readiness / block** style defense loop the server configured. |
| **Lightning Blade** | Fighting | Combat proc ability; triggers under the rules in `mana_abilities.yml` / messages when you fight with eligible melee hits. |
| **Sharp Hook** | Fishing | Wired through fishing use; improves or changes fishing outcomes per server numbers. |

If you try to use a mana ability and you **do not have enough mana**, you will see a short message like **“Not enough mana.”**

**Readying and cooldowns:** each ability has its own rules. If nothing happens, you may be on cooldown, not high enough level, using the wrong tool, or the block/mob is not tagged for that ability on this server.

---

## 9. Items, armor, and “you cannot use this yet”

The server can put **skill requirements** on gear (for example “Mining 20 required”). If you try to **wear armor** or **use a tool or weapon** you do not qualify for, the server may **block** the action and show a **red message** explaining the requirement (wording comes from the server’s message files).

**Bows and crossbows** can also be checked before you shoot.

Harvest blocks like **sweet berry bushes**, **cave vines**, and **bee nests** may have extra checks similar to the original plugin.

If you think a message is wrong, tell staff the **exact item** and **skill levels** from `/auraskills skills`.

---

## 10. Commands you can type (players)

All commands start with **`/auraskills`**. Type them in the **chat box** while you are on the server.

### `/auraskills skills`

- **Who can use it:** any player the permission system allows (default **allowed** if the server did not change LuckPerms).
- **What it does:** prints one line listing **each skill** and your **current level** (for example `mining L12`).
- **If it says “Players only”:** you ran it from the **console** or a **command block**. It only works for **real player accounts**.

### `/auraskills version`

- **Who can use it:** usually everyone (default **allowed**).
- **What it does:** shows the **mod name** and the **folder path** where AuraSkills stores data on the server (admins care about this more than players).

### `/auraskills reload` and `/auraskills save`

- **Who can use it:** almost always **staff only**. Default permission is **deny** until an admin grants it in LuckPerms.
- **What they do:** reload config files from disk, or save all **currently online** players to disk.
- **If you are not staff:** do not worry about these.

**There is no `/skills` menu command tree** on this Fabric port like the Paper plugin. Players get **`/auraskills skills`** as the simple text summary.

---

## 11. Permissions (why you “stopped earning XP”)

Servers often use **LuckPerms Fabric**. These nodes matter:

| Permission pattern | Default if not set | Meaning |
|--------------------|-------------------|---------|
| `auraskills.skill.<skillname>` | **Allow** | Example: `auraskills.skill.mining`. If an admin sets this to **deny**, you earn **no XP** for that skill. |
| `auraskills.command.version` | **Allow** | `/auraskills version` |
| `auraskills.command.skills` | **Allow** | `/auraskills skills` |
| `auraskills.command.reload` | **Deny** until granted | `/auraskills reload` |
| `auraskills.command.save` | **Deny** until granted | `/auraskills save` |

Skill names in nodes are **lowercase** (`farming`, `mining`, …).

**Jobs:** if the server turns **jobs** on, extra nodes like `auraskills.jobs.block.<id>` can exist. Follow the server’s job guide; defaults vary.

---

## 12. Where your data lives (so you are not surprised)

- **Server folder → `config/auraskills/`** holds all AuraSkills **settings and messages**.
- **Player progress (file mode):** `config/auraskills/userdata/<uuid>.yml` **per player**.
- **Backups:** the server may zip or copy `config/auraskills/` on a schedule. That is admin work, not something you trigger in-game.

If the server **resets a world** but keeps `config/auraskills/userdata`, you **keep your skills**. If an admin deletes your `uuid.yml` file, you **lose progress** for file storage mode.

---

## 13. Fair play and exploits

- Do not use **bots**, **macros**, or **AFK pools** to farm XP unless the server rules say you can. Anti-AFK checks may flag repetitive patterns.
- **Spawner mobs** may give reduced XP if the server configured a multiplier.
- **Placed blocks:** some “player placed” blocks may be excluded from XP to stop bridge-farm exploits; that depends on server options.

When in doubt, follow the server’s **rules page**, not this guide.

---

## 14. Known differences on Fabric (this port)

Read this so you do not think the server is “broken”:

- **No big inventory menu** from Slate for `/skills` like Paper; use **`/auraskills skills`** and chat messages.
- **Boss bar XP display** is **not** implemented; XP feedback is mainly **action bar / chat** depending on config.
- **Level-up “title”** may appear as **chat text**, not a cinematic title.
- **Optional Paper/Bukkit-only** hooks from the reference build (economy, external region plugins, protocol/placeholder shims) are **not** part of this Fabric product; core skills + config come from **`bukkit` as reference**, not from third-party plugin stacks.
- **Item modifiers / custom NBT items** from the full Paper item pipeline are **not** ported; do not expect every item lore line from a Paper server to behave identically.

For a shorter admin checklist, see **`fabric/FABRIC_QUICKSTART.md`**. For deep config keys, the official wiki is still helpful for **YAML shape**, but always trust the files actually on **your** server:  
https://wiki.aurelium.dev/auraskills

---

## 15. Quick “if this happens, do this” table

| Situation | What to do |
|-----------|------------|
| You never seem to gain XP | Ask staff if your world is disabled, you are in Creative, or a skill permission is denied. |
| A mana ability never triggers | Check tool, skill level, mana bar, cooldown; read server’s `mana_abilities.yml` notes if staff share them. |
| You cannot equip an item | Read the red message; grind the skill it names. |
| You changed servers | Skills do not travel unless the new server **imports** your data. |
| Staff changed configs | They must run **`/auraskills reload`** (or restart) before new numbers apply. |

---

*End of player guide. For developers building the mod, see `DEV_TESTING.md` and `fabric/PARITY_BACKLOG.md`.*
