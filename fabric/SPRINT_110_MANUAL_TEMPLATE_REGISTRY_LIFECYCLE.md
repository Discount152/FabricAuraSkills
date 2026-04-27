# Sprint 110 — Manual validation: Granite menu template registry / lifecycle

## Purpose

**Sprints 95–105** delivered bundled YAML templates and **disk-first** paths (**Sprint 106**). Operators could edit disk YAML; **Sprint 107** tied **`FabricGraniteMenuTemplateLoader`** diagnostic **debounce** to **`/auraskills reload`**. Until **Sprint 110**, each menu open still hit the low-level loader (re-read from disk).

**Sprint 110** introduces a **managed registry** — **`FabricGraniteMenuTemplateRegistry`** — that loads all known templates at **startup** and on **`reloadFromDisk()`**. Menus consume **cached** parsed templates from the registry. This is a **bounded MenuFileManager-shaped** slice, **not** full Slate parity.

## Current operator workflow (honest)

1. Edit **`config/auraskills/granite/templates/<id>.yml`** (or use **`templates export`** / overwrite from **Sprint 108–109**).
2. Run **`/auraskills reload`** (or apply a **preset**, which calls **`reloadFromDisk()`**).
3. Open menus — they use the **registry** snapshot from that reload.

**Without reload**, disk edits do **not** affect opened menus (unlike pre–Sprint-110 per-open reads).

## Classes

| Piece | Role |
|--------|------|
| **`FabricGraniteMenuTemplateBindings`** | Maps each **`menuId`** → classpath anchor + bundled path (aligned with **`FabricGraniteBundledMenuTemplateIds#ALL`**). |
| **`FabricGraniteMenuTemplateLoader`** | **`loadManagedCore`** — parse-only, structured **`GraniteMenuTemplateState`**; legacy **`load`** still debounces logs for tests. |
| **`FabricGraniteMenuTemplateRegistry`** | **`reloadAll`** — fills registry, logs summary + per-failure warns. |
| **`FabricGraniteTemplateCommands`** | **`templates export`**, **`templates status`** — see **`SPRINT_111_MANUAL_TEMPLATE_STATUS_DIAGNOSTICS.md`** for multiline diagnostics (**Sprint 111**). |

## Template states (`GraniteMenuTemplateState`)

| State | Meaning |
|--------|---------|
| **DISK_LOADED** | Valid disk YAML ( **`id`** matches **`<menu_id>`** ). |
| **BUNDLED_LOADED** | No usable disk file; bundled classpath YAML loaded. |
| **DISK_INVALID_BUNDLED_LOADED** | Disk present but invalid; bundled loaded. |
| **BUNDLED_MISSING_OR_INVALID** | No **`GraniteMenuTemplate`** — menu uses **built-in Java** fallback. |

There is **no** separate **UNAVAILABLE** enum; “unknown **`menuId`**” → **`getLoadedTemplate`** returns **empty** (key not in registry).

## Startup / reload

- **`finishEnable`**: **`menuTemplateRegistry.reloadAll(this)`** before **`runtimeReady`**.  
- **`reloadFromDisk`**: **`runReloadStep("granite menu templates (registry)", …)`** near end of pipeline.  
- Log: **`Granite menu templates loaded: disk X, bundled Y, failed Z.`** (`**failed**` = **`BUNDLED_MISSING_OR_INVALID`**).

## Menu consumption

- **`Fabric*TemplateSupport`** classes call **`plugin.menuTemplateRegistry().getLoadedTemplate("<id>")`**.  
- **Render** failures (grid validation, etc.) still fall back to built-in Java menus as before.

## Diagnostics / status

- **Console**: reload summary + warns for invalid disk / unavailable template.  
- **Game**: **`/auraskills templates status`** ( **`auraskills.command.templates`** ; mirrors under **`/skills`**, **`/sk`**, **`/skill`** ).

## Tests run (dev)

```
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

## Java validation checklist

1. **No disk templates** — start server; open **`/skills`**, **`/stats`**, **`/sources farming`**, **`/openmenu level_progression farming`**, **`/openmenu abilities farming`**, **`/openmenu leaderboard farming`**, **`/openmenu unclaimed_items`** — expect **bundled** behavior, no crash.  
2. **`/auraskills templates status`** — expect **`bundled`** lines for ids without disk files.  
3. Edit **`skills.yml`** `title: "Registry Test Skills"`; **`/auraskills reload`**; **`/skills`** — title matches.  
4. Break **`skills.yml`** (`id: wrong_id`, invalid rows); **`/auraskills reload`**; **`/skills`** — **bundled** or built-in opens; **status** shows **`disk invalid -> bundled`**; no crash.  
5. Fix **`skills.yml`**; **`/auraskills reload`** — disk path works again.  

## Known limitations

- **No** file watcher; **no** hot reload without **`/auraskills reload`**.  
- **Not** full **`MenuFileManager`** / Slate.  
- **Sprint 108–109** export/overwrite remain **operator convenience** only.

## Bedrock / Geyser

**Deferred** — not run-time tested for this sprint.
