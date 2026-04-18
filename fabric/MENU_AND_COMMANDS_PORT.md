# Paper-style menus + commands on Fabric — **Path A: full Slate port** (locked plan)

This document is the **engineering plan** for shipping **menus and the full command tree together** (one release goal), by **porting Slate** to Fabric instead of writing a second menu engine.

Work still lands in **multiple commits/PRs**; “one big push” means **one coordinated program**, not a single patch.

---

## Locked decisions

| Topic | Choice |
|--------|--------|
| Menus | **Path A — full Slate port** (same engine concepts as Bukkit Slate, implemented for Fabric / `net.minecraft`). |
| Commands | **Same tree and permissions as Paper**, implemented on Fabric using **ACF Brigadier** (`co.aikar:acf-brigadier`) where practical, registered from **`CommandRegistrationCallback`**. |
| Delivery | **Menus + commands** ship in the **same release wave** (no “commands only” release first). |

---

## Where Slate source actually lives

Slate is **not only a Maven jar** — it is an open-source project:

- **https://github.com/Archy-X/Slate**  
- Bukkit AuraSkills depends on **`dev.aurelium:slate:1.2.2`** (see `api-bukkit/build.gradle.kts`).

**Porting Slate properly** means editing **that codebase** (or a fork of it), adding a **Fabric backend** (screen handlers, `ItemStack`, `Text`, scheduler, click events), and publishing something like **`dev.aurelium:slate-fabric`** — *or* vendoring the ported sources into this monorepo. Keeping the port **next to upstream Slate** reduces drift when Slate gets bugfixes.

---

## What AuraSkills uses from Slate today (scope hint)

AuraSkills Bukkit touches many Slate packages. A Fabric port must cover at least these **namespaces** (from `import dev.aurelium.slate…` usage in this repo):

- **Core:** `dev.aurelium.slate.Slate`, `…menu.LoadedMenu`, `…menu.ActiveMenu`, `…builder.BuiltMenu`, `…builder.MenuBuilder`, `…builder.TemplateBuilder`, `…builder.ItemBuilder`
- **Inventory / clicks:** `…inv.InventoryManager`, `…inv.SmartInventory`, `…inv.ClickableItem`, `…inv.content.*`, `…action.trigger.ClickTrigger`
- **Layout:** `…position.*`, `…inv.content.SlotPos`
- **Contexts / templates:** `…context.*`, `…info.*`, `…item.MenuItem`, `…item.TemplateItem`, `…item.provider.*`, `…function.*`
- **Text / options:** `…text.TextFormatter`, `…option.SlateOptions*`, `…scheduler.Scheduler`
- **Util:** `…util.SkullCreator`, `…util.VersionUtil`

That is **the real port surface** — not just “open a chest and set items.”

---

## Commands (Paper parity)

- **Root alias:** `skills|sk|skill` (same replacement as `%skills_alias%` in `CommandRegistrar`).
- **Mirror** `bukkit/.../CommandRegistrar.java` + every registered command class (`SkillsRootCommand`, `StatsCommand`, `SkillCommand`, `XpCommand`, …).
- **Permissions:** keep the same strings as `plugin.yml` / LuckPerms (`auraskills.command.*`, `auraskills.skill.*`, …).
- **Implementation:** **`acf-brigadier`** + Fabric registration bridge; enable ACF unstable APIs as required for Brigadier (same pattern as Paper’s `enableUnstableAPI("help")` / brigadier flags).

---

## FabricAuraSkills integration (after Slate-Fabric exists)

1. **Dependency:** `:fabric` depends on **`slate-fabric`** (or the published Maven coordinate).
2. **Resources:** ship **`menus/*.yml`** inside the Fabric jar (copy from `bukkit/src/main/resources/menus/` via Gradle `processResources`, or move menus to **`common`** once both platforms read the same files).
3. **Bootstrap:** on server start, construct **Fabric Slate** with the same options pattern as `MenuOptions` / `MenuRegistrar` on Bukkit (`AuraSkills.java` + `MenuRegistrar`).
4. **Replace stubs:**  
   - Replace **`FabricMenuHelper`** with a real implementation that delegates to Slate (same role as **`SlateMenuHelper`** on Bukkit).  
   - Replace **`FabricAuraSkillsCommands`** piecemeal: root **`/skills`** tree from ACF; keep **`/auraskills`** only if you still want a dev alias, or delegate to the same handlers.
5. **API:** port **`ApiMenuManager`** / Bukkit menu entry points that other code calls.

---

## Suggested execution order (inside the “one big push”)

1. **Submodule or fork** Slate at a pinned commit; create **`slate-fabric`** module in that repo (or in this monorepo under `slate-fabric/`).
2. **Layer map:** Bukkit type → Fabric type (`Player` → `ServerPlayerEntity`, `Inventory` → `ScreenHandler` + syncId, `ItemStack` → MC registry stack, `Component` / MiniMessage → Adventure `Text` / Fabric `Text`).
3. **Minimal vertical slice:** load **one** YAML menu (`skills`) end-to-end (open, click, close) on a dev server.
4. **Fill Slate subsystems:** scheduler, inventory manager, click routing, templates, placeholders, paging.
5. **Port AuraSkills-specific menu code** from `bukkit/.../menus/**` (context providers, `MenuRegistrar`, `UnclaimedItemsMenu`, etc.) to **`fabric/.../menus/**`**, calling the same Slate APIs now backed by Fabric.
6. **Wire ACF Brigadier** and register the full command tree; migrate `/auraskills` subcommands or alias them.
7. **Docs:** update **`fabric/PLAYER_GUIDE.md`**, **`fabric/PARITY_BACKLOG.md`**, **`fabric/FABRIC_QUICKSTART.md`**.

---

## What we are *not* doing

- **Path B** (custom YAML renderer without Slate) is **out of scope** for this initiative.
- There is still **no** separate CurseForge “Slate for Fabric” mod to install; the port is **your code** (Slate + AuraSkills Fabric).

---

## Slate submodule (in this repo)

Upstream Bukkit Slate is vendored as a **git submodule**:

```text
vendor/Slate   →   https://github.com/Archy-X/Slate.git
```

After clone, init submodules:

```text
git submodule update --init --recursive
```

The **`slate-fabric`** Gradle module is **this repo’s** Fabric port skeleton; it is **not** inside the submodule. Port work reads **`vendor/Slate`** and implements APIs under **`slate-fabric/src/main/java/...`**.

---

*When the first `slate-fabric` artifact builds and opens `skills.yml` on a Fabric dev server, update this file with the exact coordinates and any API renames.*
