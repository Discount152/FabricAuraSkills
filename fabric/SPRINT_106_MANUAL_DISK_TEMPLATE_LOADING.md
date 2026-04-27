# Sprint 106 — Manual validation: disk Granite menu templates

## Purpose

Prove **Path A** operators can override bundled Granite v0 menu YAML from the **Fabric config tree** without breaking menus: valid disk files win; invalid disk files **warn** and **fall back** to bundled resources; bundled failure still uses existing **built-in Java** inventory menus.

## Disk directory

Relative to the server **config** root (Fabric `FabricLoader.getConfigDir()`):

```text
config/auraskills/granite/templates/
```

Files (optional — operator-created):

| Menu id | File name |
|---------|-----------|
| `stat_info` | `stat_info.yml` |
| `sources` | `sources.yml` |
| `level_progression` | `level_progression.yml` |
| `skills` | `skills.yml` |
| `stats` | `stats.yml` |
| `abilities` | `abilities.yml` |
| `leaderboard` | `leaderboard.yml` |
| `unclaimed_items` | `unclaimed_items.yml` |

The **`id:`** field inside each YAML **must** match the menu id (case-insensitive). A mismatch or parse error **does not** crash the server; the mod **falls back** to the bundled template.

## Loading order

1. If **`config/auraskills/granite/templates/<id>.yml`** exists and is valid YAML with **`id`** equal to **`<id>`** → use **disk** template (**one-time** info log with absolute path on first successful load).
2. Else if disk file is missing → use **bundled** classpath template (no error).
3. Else if disk exists but is invalid or wrong **`id`** → **once-per-file** warning, then **bundled** template.
4. If bundled load/validation fails → existing menu code uses **built-in Java** fallback (**warn** as before).

## Auto-copy decision (**Sprint 106**)

**Option A — no auto-copy.** The mod does **not** write bundled YAML into `config/` on startup. Operators who want to start from defaults should copy from the mod JAR (e.g. unzip `assets/auraskills/granite/templates/*.yml`) or from the repository.

## Files changed (reference)

- **`fabric/src/main/java/.../FabricGraniteMenuTemplateLoader.java`** — disk-first + bundled + diagnostics.
- Template support classes: **`Fabric*TemplateSupport`** (eight menus) — call shared loader only for **template source** selection.
- **`fabric/src/test/java/.../FabricGraniteMenuTemplateLoaderTest.java`**

## Diagnostics

| Situation | Log (approx.) | Spam control |
|-----------|----------------|--------------|
| Disk OK | `Loaded Granite menu template '<id>' from <absolute path>` | **Once** per menu id + disk path per process |
| Disk invalid | `Invalid Granite menu template '<id>' at <path>: …; falling back to bundled template` | **Once** per menu id + disk path |
| Bundled missing/invalid | `Bundled Granite menu template '<id>' …; falling back to built-in menu` | When that failure happens |

## Java validation checklist

On a **local dedicated server** or equivalent:

### 1. No disk templates

Ensure **`config/auraskills/granite/templates/`** is absent or has **no** `.yml` overrides.

Open:

- `/skills`
- `/stats`
- `/openmenu stat_info strength`
- `/sources farming`
- `/openmenu level_progression farming`
- `/openmenu abilities farming`
- `/openmenu leaderboard farming`
- `/openmenu unclaimed_items`

**Expected:** Menus open as before (bundled templates). No spurious errors.

### 2. Valid disk override

Create **`config/auraskills/granite/templates/skills.yml`** by copying the bundled file and change **`title`** (keep **`id: skills`** and valid v0 structure).

Open **`/skills`**.

**Expected:** Custom title visible; **one** info line in log about loading from disk.

### 3. Invalid disk override

Use **`id: wrong_id`** or break YAML structure.

Open **`/skills`**.

**Expected:** Warning in log; menu still opens from **bundled** (or built-in if bundled broken); **no** crash.

### 4. Remove disk override

Delete the file; reopen **`/skills`**.

**Expected:** Bundled template again.

## Negative-test checklist

- [ ] Corrupt YAML on disk → warning + fallback, no uncaught exception in console.
- [ ] Wrong **`id`** on disk → warning + bundled.
- [ ] Empty templates directory → same as baseline bundled behavior.

## Known limitations (**Sprint 106**)

- **No file watcher** — disk edits apply after **full restart** or **`/auraskills reload`** (see **`SPRINT_107_MANUAL_TEMPLATE_RELOAD_LIFECYCLE.md`** — Sprint 107 clears template diagnostics debounce on reload).
- **No** full **Slate** / **`MenuFileManager`** parity.
- **No** automatic export of defaults to disk.
- Diagnostics are **process-lifetime** debounced per path, not file-mtime aware.

## Bedrock / Geyser

**Deferred / not tested** for this sprint (unchanged policy).
