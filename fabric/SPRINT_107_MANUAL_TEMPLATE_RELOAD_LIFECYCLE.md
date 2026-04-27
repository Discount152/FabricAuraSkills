# Sprint 107 — Manual validation: Granite menu template reload lifecycle

## Purpose

**Sprint 106** added disk-first YAML under **`config/auraskills/granite/templates/`** and debounced **info** / **invalid-disk** logs so menus stay quiet after the first open. **Sprint 107** ties that debounce to the **existing** AuraSkills **`reloadFromDisk()`** path so operators who edit disk templates can run a **manual reload** and see **fresh** template diagnostics — **without** a JVM restart and **without** a file watcher.

**Sprint 110** adds **`FabricGraniteMenuTemplateRegistry`**: after **110**, Granite menus consume **cached** templates from the registry, and **`reloadFromDisk()`** reloads that cache (disk edits no longer apply on every menu open without **`/auraskills reload`**). See **`SPRINT_110_MANUAL_TEMPLATE_REGISTRY_LIFECYCLE.md`**.

## Canonical reload command / path

| Entry | Detail |
|-------|--------|
| **Command** | **`/auraskills reload`** |
| **Permission** | **`auraskills.command.reload`** (default **op** / configured via fabric-permissions-api) |
| **Code** | **`FabricBrigadierCommands#reload`** → **`FabricAuraSkills#reloadFromDisk()`** |
| **Also** | **Preset load** after apply (**`FabricPresetCommands`**) calls **`reloadFromDisk()`** — template debounce clears there too. |

**Not** covered: **`/skills reload`** (if present elsewhere) as a distinct tree — use **`/auraskills reload`** as the canonical full config reload.

## Template directory (unchanged)

```text
config/auraskills/granite/templates/
```

Files: **`stat_info.yml`**, **`sources.yml`**, **`level_progression.yml`**, **`skills.yml`**, **`stats.yml`**, **`abilities.yml`**, **`leaderboard.yml`**, **`unclaimed_items.yml`**.

## Reload behavior

When **`reloadFromDisk()`** runs (after **`runtimeReady`**):

1. **Existing steps** run as today: messages, config options, items, skills/stats, requirements, worlds, loot, XP tables, rewards, boss bar options, **`FabricBuiltInMenuConfig`**, trait reload, async leaderboard update.
2. **`FabricGraniteMenuTemplateLoader.clearDiagnosticsState()`** runs — clears in-memory **debounce sets only** (no cached template bytes; each menu open already **`Files.readString`** / classpath read).
3. **One** info log: `Granite menu template loader state cleared; disk templates will be re-read on next menu open.`
4. **`AuraSkills: reload finished.`**

**Open menus** do not live-update. Only the **next** **`/skills`** (etc.) open uses freshly read YAML and may log **Loaded Granite menu template …** or **Invalid … falling back to bundled** again if appropriate.

### What is **not** reloaded by this sprint

- **No** new file watcher or background polling.
- **No** change to bundled classpath assets (still from JAR).
- **No** `MenuFileManager` / full Slate parity.

## Diagnostics reset behavior

| Before reload | After reload + next open |
|---------------|---------------------------|
| Invalid disk file caused a **warn** (once) | If file still invalid → **warn** can appear **again** |
| Valid disk caused **info** (once) | If file changed → **info** can appear **again** for the same path |
| Operator fixed disk YAML after a warn | **`clearDiagnosticsState`** allows success **info** / bundled fallback to reflect new state without restarting the server |

## Build / test (dev)

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

**Expected:** **PASS**; artifact **`FabricAuraSkills-2.3.134.jar`**.

## Java validation checklist

Use **`FabricAuraSkills-2.3.134.jar`** (+ matching **GraniteLib** JAR).

### 1. Valid override

Create **`config/auraskills/granite/templates/skills.yml`** (valid v0 **`skills`** template) with:

```yaml
title: "Reload Test A"
```

(id **`skills`**, **`rows: 6`**, items, etc. — copy from bundled default if needed.)

Open **`/skills`**.

**Expected:** Title shows **Reload Test A** (or equivalent after placeholder expansion).

### 2. Edit disk YAML + reload

Change **`title`** to **`Reload Test B`**. Run **`/auraskills reload`**. Open **`/skills`**.

**Expected:** **Reload Test B**; console shows **Granite menu template loader state cleared** once for that reload.

### 3. Break disk YAML

Set **`id: wrong_id`** or invalid structure. Run **`/auraskills reload`**. Open **`/skills`**.

**Expected:** **Warn** about invalid disk / fallback; menu from **bundled** or **built-in**; **no** crash.

### 4. Fix disk YAML

Restore valid **`skills`** template. Run **`/auraskills reload`**. Open **`/skills`**.

**Expected:** Disk template works again; diagnostics are **not** stuck suppressing warns or success logs inappropriately.

## Known limitations

- **No file watcher** — edits apply on **reload** or **restart**, not instantly.
- **Open GUI** — no live refresh; close and reopen (or navigate) after reload.
- **Per-path debounce** is only in-memory — cleared by **`reloadFromDisk()`**, not by file mtime alone.

## Bedrock / Geyser

**Deferred / not tested** — unchanged.
