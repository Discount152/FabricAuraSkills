# Sprint 108 — Manual validation: export bundled Granite menu templates

## Purpose

Operators can copy **bundled** v0 Granite menu YAML from the JAR into **`config/auraskills/granite/templates/`** without manual unzipping. **Existing** files are **never** overwritten (Sprint 108 default).

## Command syntax

| Form | Notes |
|------|--------|
| **`/auraskills templates export`** | Primary |
| **`/skills templates export`** | Alias mirror (**Sprint 65** shared tree) |
| **`/sk templates export`** | Alias mirror |
| **`/skill templates export`** | Alias mirror |

## Permission

- **`auraskills.command.templates`**
- Default **deny** for players (grant via LuckPerms / fabric-permissions-api).
- **Console** and **RCON** may run the command **without** an explicit grant (**`permissionOrNonPlayerSource`**), like other admin utilities.

## Exported path

Absolute path is under the server **`config`** directory (Fabric `FabricLoader.getConfigDir()`):

```text
<config>/auraskills/granite/templates/
```

The command prints the resolved **absolute** path in its summary.

## Template files (eight)

| File | Menu id |
|------|---------|
| `stat_info.yml` | `stat_info` |
| `sources.yml` | `sources` |
| `level_progression.yml` | `level_progression` |
| `skills.yml` | `skills` |
| `stats.yml` | `stats` |
| `abilities.yml` | `abilities` |
| `leaderboard.yml` | `leaderboard` |
| `unclaimed_items.yml` | `unclaimed_items` |

## No-overwrite behavior

- If **`skills.yml`** (etc.) **already exists** on disk → **skipped** (not touched).
- Second run on a full folder → **`created 0, skipped 8, failed 0`** (when all resources load).

## Example output

```text
Exported Granite menu templates: created 8, skipped 0, failed 0.
Path: C:\...\config\auraskills\granite\templates
Templates are read on each menu open; run /auraskills reload to reset template diagnostic debounce after edits.
```

If some files already exist:

```text
Exported Granite menu templates: created 2, skipped 6, failed 0.
Path: ...
Existing files were not overwritten.
```

## Reload / edit guidance

- **Templates are read from disk on every menu open** (**Sprint 106**). Editing YAML does **not** require **`/auraskills reload`** for **content** to update.
- **`/auraskills reload`** (**Sprint 107**) clears **diagnostic debounce** (success / invalid-disk one-time logs). Use it after fixes if you want **fresh** console messages.

The command does **not** trigger **`reloadFromDisk()`** automatically.

## Tests / build (dev)

```text
.\gradlew -PfabricOnly=true :fabric:test :fabric:build
```

**Expected:** **PASS**. Artifact **`FabricAuraSkills-2.3.135.jar`**.

## Java validation checklist

1. Remove or rename **`config/auraskills/granite/templates/`** (or ensure empty of the eight `.yml` files).
2. As **console** or a player with **`auraskills.command.templates`**, run **`/auraskills templates export`**.
3. Confirm all eight `.yml` files exist.
4. Run **`/auraskills templates export`** again → **`skipped 8`** (or **`created 0, skipped 8`**).
5. Edit **`skills.yml`** `title`, open **`/skills`** → custom title on **next** open.
6. (Optional) **`/auraskills reload`**, reopen → content unchanged; diagnostics may log again (**Sprint 107**).

## Known limitations

- **Overwrite** bundled defaults onto disk: **`SPRINT_109_MANUAL_TEMPLATE_EXPORT_OVERWRITE.md`** (**preview** + **console/RCON-only** **confirm**).
- **No** overwrite / force in **Sprint 108** alone — default **`templates export`** never replaces existing files.
- **No** file watcher; **no** Slate **`MenuFileManager`** parity claim.

## Bedrock / Geyser

**Deferred / not tested.**
