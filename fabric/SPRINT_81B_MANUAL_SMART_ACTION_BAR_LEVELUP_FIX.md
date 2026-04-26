# Sprint 81B — Smart action bar: level-up no longer leaves HP/mana visible forever (manual validation)

| Field | Value |
|--------|--------|
| **Tested artifact (in-repo)** | **`projectVersion` 2.3.113** → `FabricAuraSkills-2.3.113.jar` (build locally or from CI) |
| **Depends on** | **Sprint 81** (smart bar), **Sprint 81A** (legacy migration + `/abtoggle` subcommands) |

## Bug summary (fixed in code)

**Symptom:** After a **health** or **mana** level-up (max HP or max mana changes), the idle health/mana line stayed on **until** the player set **`/abtoggle off`**.

**Root cause (Case B, not A or C):** **`ActionBarManager#tickSmartIdle`** — the branch that compares current HP/mana to **`stable*`** (values from when the last visibility window **ended**) extended `visibleUntil` on **every** tick while the window was open, because `stable*` is **intentionally stale** until the window **closes**. So the “smart” window **never** expired. **`/abtoggle status`** would still show **Smart** (mode was not flipped to **Always**).

**Fix (81B):** Only extend the window from the **stable**-vs-current check when the tick **starts** with **`!wasInWindow`**. Ongoing bar updates while already visible are handled by the **`inWin*`** / intra-window path.

**Not found:** no evidence of a **direct** health/mana action bar `send` bypassing **`ActionBarManager`** for the idle line in this path; no mutation of `ActionBarIdleMode` from level-up in code review (Case A / C not chosen).

## First diagnostic (operators)

If something similar happens again during validation:

1. Run **`/abtoggle status`**.

| If status says | Suggests |
|----------------|----------|
| **Always** | Case A: something is changing stored mode. |
| **Smart** (while bar is stuck) | Case B: visibility / timer (81B was this). |
| **Off** (but bar text still from Granite) | Case C: sender bypass. |

2. Reproduce with a **2.3.112** vs **2.3.113** JAR to confirm the fix for **stable**-extension.

## Expected behavior after fix

### Smart (default for migrated legacy users and new `config` default)

- After health/mana level-up, the bar **appears** for a **bounded** time (per `action_bar` tick options).
- When values **stabilize** and `visibleUntil` elapses, Granite **stops** sending the idle line (no blank-packet “clear”).
- **`/abtoggle status`** still shows **Smart** through the test.
- **Litematica** (or other) action bar text can show while Granite is idle, after the window ends.

### Always

- Idle health/mana continues to be sent on the **always** (idle) schedule as in Sprint 81.

### Off

- Granite does **not** show idle health/mana on the action bar for this mod.

## Java validation steps (operator)

1. **Full restart**; deploy **2.3.113** JAR.
2. **Java** client, join, run:

   ```text
   /abtoggle smart
   /abtoggle status
   ```

   **Expected:** `Current action bar mode: Smart` (or localized).

3. Trigger a **health** or **mana** level-up (reward / skill that changes max health or max mana as you use the product).
4. Observe: bar shows while values are changing, then—after a wait **longer** than your configured **visibility** duration—the idle line should **go away** without touching **`/abtoggle`**.

5. Run again:

   ```text
   /abtoggle status
   ```

   **Expected:** still **Smart**.

6. **Litematica** (or similar): with Granite **idle** and **full** (smart window off), your tool’s action bar string should be **visible**.

7. Confirm:

   ```text
   /abtoggle always
   ```

   → HP/mana idle line **keeps** updating when idle-appropriate.

8. ```text
   /abtoggle off
   ```

   → Granite idle HP/mana **does not** show.

9. **Console:** no new recurring errors on idle tick; capture `latest.log` on failure.

## SQL / YAML

- **81A** migration and **`idle_mode`** persistence are **unchanged**; this sprint only changes **in-memory** smart visibility timing in **`ActionBarManager`**.

## Bedrock / Geyser

- **Not** run-time tested this sprint unless you fill a dedicated row. Track as **N/A** or **NRP** until cross-play evidence exists.

## Pass / fail checklist

| Step | Result | Notes |
|------|--------|-------|
| Smart + level-up, bar hides after window | | |
| `/abtoggle status` = Smart after | | |
| Litematica while idle (smart) | | |
| Always / off unchanged | | |

## Operator validation result (in-repo)

| Field | Value |
|--------|--------|
| **Filled by** | _(agent cannot run the game; leave for operator)_ |
| **Result** | **Pending** |

## Remaining limitations

- Other locales may still lack full **`toggle.*`** string parity until translated (same as 81A).
- Two mods can still **contend** for the one vanilla action **during** Granite’s visible **window**; 81B only ensures the window **ends** after a max/HP or mana level-up as intended.
