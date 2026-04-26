# Sprint 94 — Path A: GraniteLib / Slate framework parity plan

| Field | Value |
|--------|--------|
| **Sprint** | **94** — **Planning / documentation only** (no menu rewrites, no gameplay changes) |
| **Baseline** | Two-JAR policy (**Sprint 93**), RC **`v2.3.123-fabric-rc2`**, built-in Fabric menus = **Java-smoke partials** |
| **Bedrock / Geyser** | **Deferred / not tested** |

## 1. Why this sprint exists

**Product intent:** **GraniteLib** is a **standalone reusable** Fabric library (mod id **`granitelib`**), not only a support class for one gameplay mod. **Granite Skills** (**FabricAuraSkills**, **`auraskills`**) should **consume** GraniteLib for menus and shared UI behavior the way Bukkit AuraSkills **consumes Slate**.

Recent work produced **hard-coded `Fabric*MenuGui`** screens that are **good enough for Java smoke** and share **`GraniteContainerMenus`** (6×9) primitives. That is **not** equivalent to **Slate**: YAML-driven templates, `MenuBuilder`, `LoadedMenu`, full context/action pipelines, and **`MenuFileManager`**-style lifecycle.

This document inventories **Slate-side** expectations (from the **in-repo Bukkit** integration and upstream Slate concepts), **today’s GraniteLib + Fabric** reality, **gaps**, **menu-by-menu** classification, and **phased** implementation for Path A.

**Reference code (in this repo):**

| Area | Location |
|------|----------|
| Bukkit Slate bootstrap + menu registration | `bukkit/.../menus/MenuRegistrar.java` — `ContextManager` (**Skill**, **Stat**, **Source**, **SortType**, **Ability**, **ManaAbility**, **Trait**), `slate.buildMenu(...)` per id |
| Bukkit menu builders (programmatic Slate API) | `bukkit/.../menus/SkillsMenu.java`, `StatsMenu.java`, `LevelProgressionMenu.java`, `SourcesMenu.java`, `AbilitiesMenu.java`, `LeaderboardMenu.java`, `StatInfoMenu.java`, `shared/*` |
| Bukkit openMenu / navigation | `SkillsRootCommand`, `LevelProgressionOpener`, `GlobalItems`, `SlateMenuHelper`, etc. |
| Slate library (types) | Maven **`dev.aurelium:slate`** — `Slate`, `MenuBuilder`, `LoadedMenu`, `context.*`, `builder.*`, `inv.*` (see **`MENU_AND_COMMANDS_PORT.md`** import list) |
| GraniteLib today | `slate-fabric/` → Gradle **`:granitelib-fabric`**: `GraniteContainerMenus`, `GraniteClickable9x6Handler`, `GraniteTopSlotClickHandler` |
| Fabric built-ins | `fabric/.../menu/Fabric*MenuGui.java`, `FabricMenuRegistry`, `FabricMenuLoaders`, `FabricAuraSkillsMenus`, `FabricBuiltInMenuConfig` (**granite** slices in `menus/*.yml` on disk + code defaults) |

**Note:** Upstream **Slate source** is not vendored as `slate/` in this tree; parity work assumes **API shapes** documented in Bukkit usage + Slate package imports. YAML menu **files** may ship in the Bukkit JAR or upstream; Fabric today uses a **narrow** `menus/*.yml` **granite** section, not full Slate YAML.

---

## 2. Slate capability inventory (what “full Slate” implies for AuraSkills)

Capabilities below are derived from **Bukkit `MenuRegistrar` + menu classes** and standard Slate patterns referenced in **`MENU_AND_COMMANDS_PORT.md`**.

### 2.1 Template / layout system

| Capability | Bukkit / Slate reference | Notes |
|------------|-------------------------|--------|
| Named menus | `buildMenu("skills")`, `"stats"`, … | Stable ids |
| **Rows / title / fill / slots** | `MenuBuilder`: `fillItem`, `item`, `template`, position providers | Layout abstraction |
| **Static items** | `menu.item("close", …)` | Declarative ids |
| **Dynamic / templated items** | `menu.template("skill", Skill.class, …)` | Per-context repetition |
| **Item templates & aliases** | Template builders + shared `SkillItem` / `GlobalItems` | DRY lore/click |
| **Fallback / default templates** | Code defaults + YAML overlay | Partially in Fabric **granite** slice only |

### 2.2 YAML load / reload

| Capability | Reference | Fabric today |
|------------|-----------|--------------|
| Menu definitions from YAML | Bukkit/Slate + `MenuFileManager` (reference) | Fabric: **`FabricBuiltInMenuConfig`** loads **granite** subsection only for **skills/stats/level_progression/stat_info** — **not** full Slate menu YAML |
| Reload from disk | Bukkit reload pipeline | Fabric: tied to **`reloadFromDisk`** for config slice; **no** full template engine reload |

### 2.3 Item rendering

| Capability | Reference | GraniteLib / Fabric |
|------------|-----------|---------------------|
| Material / item mapping | Bukkit `ItemStack` + parsers | Fabric `ItemStack` in GUIs |
| Display name / lore | Slate + MiniMessage / placeholders | **FabricGraniteText** + `FabricMessageProvider` for granite keys |
| Enchant / glint / CMD / count | Bukkit menus | Partial in built-ins |
| Skull / player head | e.g. `SkillsMenu` `SkullMeta` | Varies by menu |
| Placeholder expansion | `PlaceholderHelper`, `behavior.replacer`, contexts | Code-driven in `Fabric*MenuGui`; **no** generic Slate replacer |

### 2.4 Context system

| Context | Registered on Bukkit (`MenuRegistrar`) |
|---------|----------------------------------------|
| Skill | ✓ |
| Stat | ✓ |
| Source (XP source) | ✓ |
| SortType | ✓ |
| Ability | ✓ |
| ManaAbility | ✓ |
| Trait | ✓ |

Fabric uses **`FabricMenuContext`** (skill, stat, list page, sort, etc.) **inside gameplay code**, not a **GraniteLib-global** `ContextManager` API.

### 2.5 Navigation / global items

| Behavior | Reference (`GlobalItems`, Slate `openMenu`) |
|----------|---------------------------------------------|
| Back / next / prev page | ✓ |
| Close | ✓ |
| Sort / refresh | Sources, leaderboard patterns |
| `previous_menu` property | ✓ |

Fabric: **per-GUI** handlers (`FabricListMenuPaging`, back buttons) — **not** a shared GraniteLib navigation layer.

### 2.6 Click / action system

| Action | Reference |
|--------|-----------|
| Open menu | `slate.openMenu(player, id, props, page)` |
| Run command / claim / page change | Slate click triggers |
| Read-only / anti-theft | Slate + inventory rules |

Fabric: **`GraniteClickable9x6Handler`** — top grid clicks delegated; **must remain** safe (no item stealing) as parity hardens.

### 2.7 Reload / lifecycle / validation

| Topic | Reference |
|-------|-----------|
| Load validation | Slate / plugin load |
| Bad config fallback | Plugin logs + defaults |
| Server-only safety | Dedicated server |

Fabric: partial (built-in YAML slice errors fall back to code defaults).

### 2.8 API surface (library)

| Topic | Target for GraniteLib |
|-------|------------------------|
| Open menu by id | Bukkit: `Slate.openMenu` |
| Context API | `ContextManager` |
| Item provider / template API | `MenuBuilder` |
| Click handler API | Slate triggers |
| **Third-party mods** consume GraniteLib | **Open** — needs docs + stable API |

---

## 3. Current GraniteLib capability inventory

| Capability | Present today? | Where |
|------------|------------------|--------|
| 6×9 generic container open | **Yes** | `GraniteContainerMenus.openReadOnly9x6` / `openClickable9x6` |
| Top-inventory click hook | **Yes** | `GraniteTopSlotClickHandler`, `GraniteClickable9x6Handler` |
| Menu IDs / `LoadedMenu` equivalent | **No** | — |
| YAML template loader | **No** | — |
| Context manager | **No** | — |
| Placeholder pipeline | **No** | — |
| Navigation abstraction | **No** | — |
| Public “openMenu(id, props)” for consumers | **No** | Gameplay uses **`FabricAuraSkillsMenus`** only |

**Conclusion:** GraniteLib is a **low-level UI primitive**, not yet a **Slate successor**.

---

## 4. Current Fabric built-in menu comparison (menu-by-menu)

Classification: **Built-in partial** = works on Java smoke; **Slate/YAML parity** = **not complete** for all rows in §2.

| Menu id | Bukkit Slate menu | Fabric implementation | Status | Migrate to GraniteLib templates later? | Notes |
|---------|-------------------|------------------------|--------|----------------------------------------|--------|
| **skills** | ✓ `SkillsMenu` | `FabricSkillsMenuGui` + registry | Built-in partial; Java tested; Bedrock N/A | **Yes** (high value) | Custom skills / position providers in Bukkit; Fabric simplifies |
| **stats** | ✓ `StatsMenu` | `FabricStatsMenuGui` | Built-in partial | **Yes** | Opens stat_info |
| **level_progression** | ✓ `LevelProgressionMenu` | `FabricLevelProgressionMenuGui` | Built-in partial | **Yes** | Skill context required |
| **stat_info** | ✓ `StatInfoMenu` | `FabricStatInfoMenuGui` | Built-in partial | **Yes** | Stat context |
| **sources** | ✓ `SourcesMenu` | `FabricSourcesMenuGui` via **`FabricAuraSkillsMenus.openSources`** (**not** in `FabricMenuLoaders`) | Built-in partial | **Yes** (good Phase D candidate) | Sort/page context |
| **unclaimed_items** | ✓ (Bukkit menu) | `FabricUnclaimedItemsMenuGui` | Built-in partial | **Maybe** — complex claims | Code-heavy |
| **leaderboard** | ✓ `LeaderboardMenu` | `FabricLeaderboardMenuGui` | Built-in partial | **Yes** (Phase F) | Refresh/page |
| **abilities** | ✓ `AbilitiesMenu` | `FabricAbilitiesMenuGui` | Built-in partial | **Yes** | Mana abilities |
| **confirm flows** | ACF / Brigadier / Bukkit confirm | Fabric: **`FabricCommandConfirmation`** + literals (**not** Slate confirm GUI) | **Different model** | **Phase F** — align or document N/A | Inventory confirm UIs **not** ported |

**Missing Slate/YAML behavior (horizontal):** full template DSL, `MenuFileManager`, arbitrary menu YAML, API menu extensions (`ApiMenuManager.applyBuilders`), full context registry in **GraniteLib**, Bedrock validation.

---

## 5. Gap table (summary)

| Area | Gap severity |
|------|----------------|
| Template / YAML menu model in GraniteLib | **Critical** |
| Context manager + typed templates | **Critical** |
| Placeholder / item rendering pipeline in library | **High** |
| Navigation + global items as library primitives | **High** |
| Click action dispatcher (open menu, page, sort, command) | **High** |
| Reload / validation / diagnostics | **Medium** |
| External mod API + docs | **Medium** |
| Bedrock/Geyser menu UX | **Unknown** (deferred) |

---

## 6. Proposed implementation phases (Path A)

Phases are **sequential guidelines**; exact ordering can adjust after Spike 0 (Sprint 95).

| Phase | Name | Objective |
|-------|------|-----------|
| **A** | **GraniteLib core template model** | Introduce **menu id**, **template**, **slot/fill** model **in GraniteLib** (no AuraSkills gameplay types yet — use generics or example mod tests). |
| **B** | **Item rendering + placeholder pipeline** | Server-safe text (**literal / Granite i18n policy**), lore lines, materials; hook placeholder resolution. |
| **C** | **Context + click actions** | Context registry (Skill-like keys **as strings or generic** first); dispatch **open_menu**, **page**, **sort**, **close**; keep **safe** click contracts. |
| **D** | **Migrate one low-risk menu** | **sources** or **stat_info** as first **YAML-driven** consumer; keep **built-in fallback** if YAML fails. |
| **E** | **High-traffic menus** | **skills**, **stats**, **level_progression**. |
| **F** | **Advanced / edge** | **leaderboard**, **abilities**, **unclaimed_items**, **confirm** story (document parity vs Brigadier-only). |
| **G** | **Reload / validation** | Hot reload, schema validation, operator-visible errors. |
| **H** | **External mod API** | Stable entrypoints, version policy, consumer docs. |

---

## 7. Risks

| Risk | Mitigation |
|------|------------|
| Scope explosion (full Slate clone) | Phase **A–C** as **narrow MVP**; defer exotic Slate features |
| Bedrock inventory differences | Track separately; don’t block Java Phase D–E |
| Duplicate menu systems during migration | **Feature flag** or menu id routing: template vs built-in fallback |
| Performance (YAML + refresh) | Cache `LoadedMenu`-like structures; profile on dedicated server |

---

## 8. Acceptance criteria (for “Slate parity complete” — future)

**Not met today.** When implementing Path A, “complete” should include at least:

1. GraniteLib loads **menu definitions** from YAML (or equivalent) **without** hard-coding layout in gameplay-only classes.
2. Granite Skills menus **can** be driven by GraniteLib templates **with** fallback to current built-ins on parse failure.
3. Java player behavior **matches or improves** current built-in UX; **no raw `auraskills.granite.*`** on vanilla clients (**existing `FabricGraniteText` policy**).
4. Clicks are **safe** (no item theft; server-authoritative).
5. Config/menu reload path is defined and tested.
6. **Bedrock/Geyser** remains **separately** tracked — not claimed as part of “Slate parity complete” until Sprint 87-style validation.

---

## 9. Recommendation for Sprint 95

**Sprint 95 — Path A Spike 0 (design + scaffolding only, still no full menu rewrite):**

1. **Spike:** Choose **one** GraniteLib package layout for **template + menu id** (namespaces, module boundaries vs `FabricAuraSkills`).
2. **Prototype:** Minimal **read-only** menu from **one** YAML file in a **test** or **dev** entrypoint (no production menu swap yet).
3. **ADR / short doc:** Lock **YAML schema v0** and **migration** strategy for **`menus/*.yml`** (granite slice vs full Slate file).
4. **Estimate** Phase D menu (**sources** vs **stat_info**) with team.

**Avoid in Sprint 95:** Rewriting **skills**/**stats** GUIs in full; Bedrock certification; public package rename of entire repo.

---

## 10. References

- **`fabric/PARITY_COMPLETION_ROADMAP.md`** — honest **partial vs complete** framing  
- **`fabric/MENU_AND_COMMANDS_PORT.md`** — Path A locked decision + Slate import surface  
- **`fabric/SPRINT_93_TWO_JAR_RELEASE_AND_PARITY_RESET.md`** — two-JAR architecture  
- **Bukkit:** `MenuRegistrar.java`, `SkillsMenu.java`, `GlobalItems.java`, `LevelProgressionOpener.java`  
- **GraniteLib:** `GraniteContainerMenus.java`  
- **Fabric:** `FabricMenuLoaders.java`, `FabricMenuRegistry.java`, `FabricAuraSkillsMenus.java`, `FabricBuiltInMenuConfig.java`

---

**Sprint 94 definition of done:** This file exists; parity docs updated; **no** gameplay/menu code changes; Bedrock still deferred.
