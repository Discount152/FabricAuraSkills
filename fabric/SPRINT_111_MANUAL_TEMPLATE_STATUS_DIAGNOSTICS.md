# Sprint 111 — Granite menu template registry diagnostics (`templates status`)

## Purpose

**Sprint 110** introduced **`FabricGraniteMenuTemplateRegistry`** so YAML templates load on startup and on **`/auraskills reload`**, not on every menu open. **Sprint 111** adds **operator-visible diagnostics**: what loaded, from where, and why disk or bundled failed—without changing load order, YAML schema, or menu rendering.

**Bedrock / Geyser:** unchanged from prior sprints — **not** validated here.

---

## Commands

All require **`auraskills.command.templates`** (default **deny** for players; **console/RCON** allowed via **`permissionOrNonPlayerSource`**).

```text
/auraskills templates status
/skills templates status
/sk templates status
/skill templates status
```

---

## State definitions (`GraniteMenuTemplateState`)

| State | Meaning |
|-------|---------|
| **`DISK_LOADED`** | Valid disk YAML under **`config/auraskills/granite/templates/<id>.yml`** with matching **`id`**. |
| **`BUNDLED_LOADED`** | No usable disk file; bundled **`assets/auraskills/granite/templates/<id>.yml`** loaded. |
| **`DISK_INVALID_BUNDLED_LOADED`** | Disk file present but invalid or wrong **`id`**; bundled template used. **Not** counted as **failed**. |
| **`BUNDLED_MISSING_OR_INVALID`** | No **`GraniteMenuTemplate`** available; menu falls back to **built-in Java**. Counted as **failed**. |

---

## Reload / status summary counts

Log line after reload (unchanged intent):

```text
Granite menu templates loaded: disk X, bundled Y, failed Z.
```

**Definitions (must match `/templates status` header):**

- **`disk` (`X`)** = entries in **`DISK_LOADED`**.
- **`bundled` (`Y`)** = **`BUNDLED_LOADED`** + **`DISK_INVALID_BUNDLED_LOADED`** (bundled YAML is the effective template).
- **`failed` (`Z`)** = **`BUNDLED_MISSING_OR_INVALID`**.

Chat header:

```text
Granite menu templates: disk X, bundled Y, failed Z
```

---

## Example status output (shape)

```text
Granite menu templates: disk 0, bundled 8, failed 0

- skills: BUNDLED_LOADED
  source: assets/auraskills/granite/templates/skills.yml

- skills: DISK_LOADED
  source: config/auraskills/granite/templates/skills.yml

- sources: DISK_INVALID_BUNDLED_LOADED
  disk: config/auraskills/granite/templates/sources.yml
  reason: wrong id: expected sources, got wrong_id
  fallback: assets/auraskills/granite/templates/sources.yml

- leaderboard: BUNDLED_MISSING_OR_INVALID
  reason: bundled resource missing or invalid
  fallback: built-in Java menu
```

**Invalid disk reasons (concise):**

- Wrong **`id`:** `wrong id: expected <menu_id>, got <actual>`
- Validation: first YAML validation error, plus **`(and N more)`** if several
- IO: `read failed: …` (single line; no stack traces in chat)

Full combined detail remains in **server log** warnings where applicable.

---

## Registry entry fields (`RegistryEntry`)

| Field | Use |
|-------|-----|
| **`menuId`** | Bound menu id (e.g. **`skills`**). |
| **`state`** | **`GraniteMenuTemplateState`**. |
| **`loaded`** | **`Optional<LoadedTemplate>`** — empty when **`BUNDLED_MISSING_OR_INVALID`**. |
| **`primarySourceDisplay`** | Effective YAML label (**`config/auraskills/…`** or **`assets/…`**) for successful loads. |
| **`diskSourceDisplay`** | Disk path label when a disk file was present at load; else **`null`**. |
| **`bundledSourceDisplay`** | Bundled classpath path from **`FabricGraniteMenuTemplateBindings`**. |
| **`reason`** | Short diagnostic for **`DISK_INVALID_BUNDLED_LOADED`** and **`BUNDLED_MISSING_OR_INVALID`**. |
| **`fallbackDisplay`** | Bundled asset path when recovering from bad disk; **`built-in Java menu`** when no template. |

---

## Operator lifecycle (unchanged)

```text
Edit YAML → /auraskills reload → open menu
```

---

## Validation checklist (Java dedicated server)

### 1. All bundled

- Remove or rename all files under **`config/auraskills/granite/templates/`** (or use a fresh data dir).
- Run **`/auraskills reload`**, then **`/auraskills templates status`**.

**Expect:** most rows **`BUNDLED_LOADED`**; **`disk 0`**, **`bundled 8`**, **`failed 0`**; each **`source:`** line shows **`assets/auraskills/granite/templates/...`**.

### 2. Valid disk

- Copy or author valid **`skills.yml`** (matching **`id: skills`**) under **`config/auraskills/granite/templates/`**.
- **`/auraskills reload`**, **`/auraskills templates status`**.

**Expect:** **`skills:`** **`DISK_LOADED`**; **`source:`** **`config/auraskills/granite/templates/skills.yml`**; summary reflects **`disk 1`** (if only **`skills`** is on disk).

### 3. Invalid disk, bundled fallback

- Break **`skills.yml`** (wrong **`id`**, invalid rows/slots, or non-mapping root).
- **`/auraskills reload`**, **`/auraskills templates status`**.

**Expect:** **`DISK_INVALID_BUNDLED_LOADED`**; **`disk:`**, **`reason:`**, **`fallback:`** lines; **`failed`** still **`0`** if bundled loads.

### 4. Menu still opens

- **`/skills`** (or relevant opener).

**Expect:** menu opens from bundled or built-in fallback; **no** crash.

---

## Known limitations

- **No** file watcher — disk edits apply only after **`/auraskills reload`** (or restart).
- **No** hot reload of templates outside **`reloadFromDisk`**.
- **No** full **Slate / `MenuFileManager`** parity.
- **Verbose** subcommand (**`templates status verbose`**) — **not** implemented; base **`status`** is the supported surface.

---

## Bedrock / Geyser

**Deferred / not tested** for this sprint. Chat output may differ in density or wrapping on Bedrock clients; core behavior is unchanged from **Sprint 110**.

---

## Related

- **`SPRINT_110_MANUAL_TEMPLATE_REGISTRY_LIFECYCLE.md`** — registry lifecycle and **`loadManagedCore`**.
