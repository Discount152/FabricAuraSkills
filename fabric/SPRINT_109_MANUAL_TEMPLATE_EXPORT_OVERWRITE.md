# Sprint 109 — Manual validation: guarded Granite template export overwrite

## Scope

**Sprint 108** added **`/auraskills templates export`** — copies **missing** bundled YAML into **`config/auraskills/granite/templates/`** and **never** overwrites existing files.

**Sprint 109** adds a **guarded** path to **replace** disk files with **bundled defaults**: **preview** (no writes), then **confirm** from **dedicated server console or RCON only**.

**Not in scope:** file watcher, hot reload, full **`MenuFileManager`**, Bedrock/Geyser validation.

## Commands

| Step | Syntax |
|------|--------|
| Safe export (unchanged) | **`/auraskills templates export`** |
| Overwrite **preview** | **`/auraskills templates export overwrite`** |
| Overwrite **execute** | **`/auraskills templates export overwrite confirm`** |

Mirrors: **`/skills`**, **`/sk`**, **`/skill`** — same subcommand path under each root.

## Permission

- **`auraskills.command.templates`**
- Default **deny** for players.
- **Console** and **RCON** pass **`requires`** without an explicit grant (**`permissionOrNonPlayerSource`**), like other admin utilities.

## Console vs in-game policy

| Action | In-game (staff with permission) | Console / RCON |
|--------|--------------------------------|----------------|
| **`templates export`** | Allowed | Allowed |
| **`templates export overwrite`** (preview) | Allowed — shows **would create / would overwrite / would fail**; **does not** arm confirmation | Allowed — same preview + **arms** a **30s** pending key for **`confirm`** |
| **`templates export overwrite confirm`** | **Rejected** — `auraskills.granite.command.templates.overwrite.confirm_console_only` | Allowed after matching **console** preview |

**Rationale:** confirm **replaces operator YAML** on disk; restricting **confirm** to **console/RCON** matches **`/storage mergeskills … confirm`** style safety.

## Paths and files

- **Directory:** `<server run directory>/config/auraskills/granite/templates/` (same root as **`FabricAuraSkills#getPluginFolder()`**).
- **Template ids:** `stat_info`, `sources`, `level_progression`, `skills`, `stats`, `abilities`, `leaderboard`, `unclaimed_items` (each as **`<id>.yml`**).

## Preview behavior

- **No** directories or files created or modified.
- Output shape:
  - **`Granite template overwrite preview:`**
  - **`Would create:`** *N*
  - **`Would overwrite:`** *N*
  - **`Would fail:`** *N*
  - **`Path:`** *(absolute)*
- If **`Would fail` > 0**, lists forecast failures (e.g. missing bundled resource).
- **Console/RCON:** instructs to run **`… overwrite confirm`** within **30s**.
- **In-game:** **`auraskills.granite.command.templates.overwrite.preview_arm_from_console`** (run preview from console to arm, then confirm).
- Trailing reminder: templates read on **each menu open**; **`/auraskills reload`** resets **template diagnostic debounce** only; **already-open** GUIs unchanged until reopened.

## Confirm behavior

- Only **console/RCON**.
- Requires a **`templates export overwrite`** preview from **console/RCON** within **30s** with the same pending key (**`FabricCommandConfirmation.TEMPLATES_EXPORT_OVERWRITE_PENDING`**). Otherwise **`confirmation.no_pending`** / **`confirmation.mismatch`**.
- **Mutates disk:** existing files are **overwritten** with bundled content; missing files are **created**.
- Summary: **`Exported Granite menu templates with overwrite: created X, overwritten Y, failed Z.`**
- **No** automatic **`reloadFromDisk()`**.

## Overwrite result counts

- **created:** bundled copy when **no** file existed at destination.
- **overwritten:** bundled copy **replaced** an existing file.
- **failed:** bundled resource missing or I/O error — listed per id when non-zero.

## Java validation checklist (local dedicated server)

1. Ensure templates exist: **`/auraskills templates export`** (or use an existing folder).
2. Edit **`config/auraskills/granite/templates/skills.yml`** — set **`title: "Custom Operator Edit"`** (or another obvious marker).
3. **In-game:** **`/auraskills templates export overwrite`**
   - Expect preview counts; **`skills.yml`** still contains **Custom Operator Edit**.
4. **Console:** **`/auraskills templates export overwrite`**
   - Expect preview + “confirm within 30s”.
5. Within **30s**, **console:** **`/auraskills templates export overwrite confirm`**
   - Expect **`overwritten` ≥ 1**; **`skills.yml`** content matches **bundled** default (marker gone).
6. Re-open **`/skills`** — bundled title (or default template title) appears after **reopen**.
7. **`/auraskills templates export`** again — **`created 0, skipped 8, failed 0`** (no overwrite from safe export).
8. (Optional) **In-game** **`… overwrite confirm`** — expect **confirm_console_only** error and **no** mutation.

## Tests run (dev)

```
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

**Result:** **PASS** (local). Artifact **`FabricAuraSkills-2.3.136.jar`** (see **`gradle.properties`** **`projectVersion`**).

## Known limitations

- **No** single-command “force overwrite” without **preview** + **confirm**.
- **Console-only** confirm — operators cannot apply overwrite from an OP account in-world.
- **One** shared console pending slot (same model as other **`FabricCommandConfirmation`** flows).
- **No** file watcher; **no** claim of full Slate / **`MenuFileManager`** parity.

## Bedrock / Geyser

**Deferred** — not run-time tested for this sprint.
