# Sprint 81A — Action bar legacy user migration + explicit `/abtoggle` forms (manual validation)

| Field | Value |
|--------|--------|
| **Build (in-repo `projectVersion`)** | **2.3.112** (expected JAR: `FabricAuraSkills-2.3.112.jar`); for **stuck bar after level-up in smart** see **2.3.113** and **`SPRINT_81B_MANUAL_SMART_ACTION_BAR_LEVELUP_FIX.md`**. |
| **Sprint 81 (runtime) finding** | **Smart** mode behaves as designed. **Problem:** old **`userdata`** with legacy **`action_bar.idle`** and **no** **`idle_mode`** was being treated like **always**-on until the player cycled **`/abtoggle`**. **81A** migrates that cohort to **smart** (or **off** when `idle: false`) and **persists** **`idle_mode`**. |
| **Sprint 81A code focus** | **No** change to the **smart visibility tracker**; **YAML/SQL load** + **SQL save** + **Fabric/Bukkit** `/abtoggle` **subcommands**. |

## Legacy migration rules (authoritative)

| Storage state | Result mode | Mark `shouldSave`? |
|---------------|------------|----------------------|
| **`idle_mode` present** | Parsed value (smart / always / off) | No (already explicit) |
| **`idle_mode` absent**, **`idle: true`** | **smart** | **Yes** (write `idle_mode` on next save) |
| **`idle_mode` absent**, **`idle: false`** | **off** | **Yes** |
| **Neither** | Global **`action_bar.mode`** from `config.yml` | No |

**Do not** re-migrate after save: the next load sees **`idle_mode`** and the explicit branch applies.

**YAML example — before load / after first save (legacy `idle: true` only):**

```yaml
# Before
action_bar:
  idle: true

# After
action_bar:
  idle: true
  idle_mode: smart
```

**SQL:** Every save now writes a row **`idle_mode=<mode>`** for the action_bar key-value group so **legacy** **`idle`only** rows are not the sole signal after a save cycle.

**Bedrock / Geyser:** This sprint is **not** a Bedrock validation sprint. If no Bedrock test was run, record **N/A** or **not tested**.

## `/abtoggle` / `toggle` — final behavior

| Form | Effect |
|------|--------|
| No args | Cycles **smart → always → off → smart** |
| `status` | Prints **current** mode (read-only) |
| `smart` / `always` / `off` | Sets that mode and saves |
| Unknown word | Error + hint |

**Permission (unchanged):** `auraskills.command.abtoggle`  
**Bukkit:** `/skills toggle …`, `abtoggle` alias; **Fabric:** top-level `/toggle`, `/abtoggle` and mirrors on `/skills` / etc. as already registered.

## Java validation (operator)

**If the idle bar stays on forever after a health/mana level-up while `/abtoggle status` says Smart, that is Sprint 81B** ( **`SPRINT_81B_MANUAL_SMART_ACTION_BAR_LEVELUP_FIX.md`**, **2.3.113+** ), not an 81A migration issue.

1. Deploy **2.3.112**; **full restart** recommended.
2. Use the **same** legacy test player that previously loaded in **always**-style from **`idle: true` only**.
3. Join; run **`/abtoggle status`**. **Expected:** **Current action bar mode: Smart** (or the localized equivalent).
4. Stand idle at full HP/mana: **smart** should **stop** sending Granite idle line after the configured window; trigger **Litematica** (or other) action bar text — it should be **visible** while Granite is idle.
5. **`/abtoggle always`** — idle HP/mana stays visible.
6. **`/abtoggle smart`** — back to **smart** behavior.
7. **`/abtoggle off`** — Granite idle HP/mana not shown.
8. **Restart** and rejoin; mode should **persist** (check `userdata` for **`idle_mode`** or SQL `key_values`).

## Litematica / action bar slot

- Same expectations as **Sprint 81**: in **smart** and **off**, other mods’ action bar text is not monopolized by continuous Granite updates.

## Storage checklist

- [ ] **YAML** — legacy file gains **`idle_mode`** after one save; **`idle` left in place** for compatibility.
- [ ] **SQL** — `idle_mode` row present after save; no regression on load.
- [ ] **Plain `/abtoggle`** still cycles; explicit forms work.

## Pass / fail (fill by operator)

| Step | Result | Notes |
|------|--------|-------|
| `/abtoggle status` after upgrade | | |
| Smart idle + Litematica | | |
| `always` / `smart` / `off` + persistence | | |

## Remaining limitations

- Other locales: **`messages_en.yml`** updated; other language files may still miss new **`toggle.*`** keys until translated (fallback behavior depends on **`MessageProvider`**).
- **Bedrock** through Geyser: **not** validated this sprint unless you run it and fill the table above.
