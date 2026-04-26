# Sprint 81 — Smart health/mana action bar (manual validation)

| Item | State |
|------|--------|
| **Sprint 81 (code + docs in repo)** | **Accepted** |
| **In-game / operator proof (Sprint 81 core)** | **Pending** for the full checklist below unless superseded by later sprints. |
| **Sprint 81A (legacy `idle` → `idle_mode` migration)** | See **`SPRINT_81A_MANUAL_ACTION_BAR_LEGACY_MIGRATION.md`** — **2.3.112+** migrates old **`idle` without `idle_mode`** to **smart** (or **off**) instead of **always**-style. |
| **Sprint 81B (level-up smart window — partial)** | **`SPRINT_81B_MANUAL_SMART_ACTION_BAR_LEVELUP_FIX.md`**, JAR **2.3.113** — `!wasInWindow` for **stable** diff. |
| **Sprint 81C (smart sticky after trigger — final model)** | **`SPRINT_81C_MANUAL_SMART_ACTION_BAR_STUCK_FIX.md`**, JAR **2.3.114+** — **`SmartIdleObsDisplay`**. **Runtime: Accepted / validated (2026-04-21)** — smart idle + Litematica **Java**; **action bar mode lane** closed. |

**Build / version:** `FabricAuraSkills-2.3.111.jar` for **Sprint 81** core code; for **legacy default + `/abtoggle status|smart|…`**, use **`2.3.112`** ( **`SPRINT_81A_…` manual**). For **smart** idle bar that **stops** after triggers, use **`2.3.114`** (**81C** manual; **81B** on **2.3.113** is a partial step). Current `projectVersion` is in `gradle.properties`. Use the **current GraniteLib** install policy for your server (split vs JiJ — see project docs as needed).

**Problem:** Granite’s **idle** health/mana line used the only vanilla **action bar** slot on a fast timer, so it **outcompeted** other mods (e.g. **Litematica** mode text) that also use that line. **Sprint 79** fixed **MiniMessage** rendering, but the **concurrent resend** issue remained.

**Root fix (server-side):** In **`smart`** mode ( **default in embedded `config.yml`** for **`action_bar.mode`**), Granite **only** sends the idle line during **short windows** after relevant events, then **stops** sending. **No** blank packets to “clear” the line. **Sprint 81A** ( **2.3.112** ) fixes **old** saves that had only legacy **`idle:`** and no **`idle_mode`**: they migrate to **smart** (or **off**) and persist **`idle_mode`** — so players are **not** stuck in de facto **always** until `/abtoggle`.

**Reference screenshot (conceptual):** Before — HP/mana text always above hotbar. After (idle) — other mods’ action bar text can show until the next **smart** window.

---

## Operator validation checklist (dedicated / Java server)

Fill **Pass** / **Fail** / **Skip (reason)** when you run this on a real server. **In-repo / CI** cannot run Minecraft.

### Artifact

Deploy:

```text
FabricAuraSkills-2.3.111.jar
```

Use the **matching** GraniteLib policy your host uses (same world / mod folder layout as for other Granite sprints).

### Startup

Confirm the log shows (wording may vary slightly by loader; capture `latest.log` on failure):

- **auraskills** (or mod id) version **2.3.111** (or the version on the JAR you built)
- **Fabric / AuraSkills** mod enabled
- **Done (...)! For help, type "help"** (or equivalent) — server finished loading

### Smart action bar — Java client

1. **Join** with a **Java** client. Confirm **HP/mana** appears **briefly** after join.
2. Stand **idle** at **full** HP and **full** mana. Confirm HP/mana **stops** being resent after the **configured** visibility window.
3. Trigger **Litematica** (or another mod) **action bar** mode text. With Granite **idle** (smart, full, stable), confirm **Litematica** text **can** appear (Granite is not camping the line).
4. **Take damage.** Confirm HP/mana (or the idle line) appears **briefly** again.
5. **Heal** or **regen.** Confirm the line shows while **displayed** values change, then **hides** again when stable.
6. **Spend** or **change** mana (if you can in your build). Confirm mana line appears briefly, then **hides** after stable.

### `/abtoggle` cycle

Run **`/abtoggle`** (or `/toggle` mirror) and confirm the shown mode advances:

**smart → always → off → smart**

(If messages use translated strings, the **order** and **persistence** after relog are what matter.)

### Console watch (operators)

Check **`latest.log`** for:

- [ ] **Action bar**-related **exceptions** (none expected)
- [ ] **Missing** message / translation keys for **toggle** feedback
- [ ] **Storage** save/load problems for **`idle_mode`** (YAML or SQL)
- [ ] **SQL** schema or migration issues if you use **MySQL** storage
- [ ] **Raw** MiniMessage-style tags (`<red>`, etc.) in player-visible chat/action bar (should **not** appear post–Sprint 79; report if they do with **2.3.111**)

### Bedrock / Geyser / Floodgate

- Do **not** mark **Bedrock validated** unless a **Bedrock** player through **Geyser/Floodgate** actually runs the same **visibility** and **/abtoggle** checks on your server. Until then, status stays **Java-only proof** or **untested**.

### After validation

- If something **fails:** open a **tiny patch** sprint or a focused issue (attach `latest.log` + `userdata` sample if storage-related).
- If **all Pass:** the project is ready to schedule a separate **Bedrock/Geyser player validation** sprint, or to continue the broader **Java smoke matrix** (Sprint 78 / 80 style) on current artifacts.

---

## `config.yml` — `action_bar` keys (new / notable)

| Key | Default (embedded) | Role |
|-----|----------------------|------|
| `mode` | `smart` | Server default for **new** YAML **userdata** with no `action_bar` section. Per-player: **`/abtoggle`**. **Values:** `smart`, `always`, `off` |
| `show_on_join_ticks` | `100` (5s) | Window after **user load** / **world change** extension |
| `show_after_health_change_ticks` | `100` | Extend when **displayed** HP/max HP (ints) change |
| `show_after_mana_change_ticks` | `100` | Extend when **displayed** mana/max mana (ints) change |
| `show_after_combat_ticks` | `120` (6s) | Extend on **player damage** (Fabric/Bukkit listeners) |
| `update_while_visible_ticks` | `10` | Minimum spacing between **sends** while a window is active |
| `hide_when_full_and_idle` | `true` | When the window is over and HP/mana are **full** (and stable), do not reopen until stats change again |

**Existing** keys (unchanged in meaning): `enabled`, `idle`, `update_period` ( **scheduler** tick for the **idle** task base loop — still used; **always** mode sends each tick of this period), etc.

**Default “product” mode:** **smart** in **embedded** `config.yml`. **Pre–81A** on-disk `userdata` could show **always**-style for **`idle: true` only**; **81A** migrates that to **smart** and writes **`idle_mode`**. **`idle: false`** without **`idle_mode`** → **off** (unchanged intent).

---

## `/abtoggle` / `toggle` behavior (Sprint 81, extended Sprint 81A)

- **Permission:** `auraskills.command.abtoggle` (unchanged name).
- **Action:** Cycles per-player **idle** mode: **smart → always → off → smart** with **no** argument. **Sprint 81A** adds: **`status`**, **`smart`**, **`always`**, **`off`** (see **`SPRINT_81A_MANUAL_ACTION_BAR_LEGACY_MIGRATION.md`**).
- **Storage:** `userdata/<uuid>.yml` (or SQL key-value): **`action_bar.idle_mode`**, and legacy **`action_bar.idle`** bool ( **`idle` retained** for compatibility).

---

## Java validation steps

1. **Build / deploy** `FabricAuraSkills-2.3.111.jar` (or current version). **Full restart** recommended.
2. **Join** as a **Java** player. Confirm **brief** health/mana on the **action bar** ( **smart** window from **user load** ).
3. Stand **idle** at **full** HP and **full** mana. After ~5s + a margin, **Granite** should **stop** updating that line.
4. Trigger **another** mod’s action bar (e.g. **Litematica** mode). With Granite **idle** in **smart** and you **stable/full**, the **other** text should be **readable** without Granite constantly overriding it.
5. **Take damage** — health/mana line should reappear for the **configured** duration, **throttled** by **`update_while_visible_ticks`** while visible.
6. **Regen to full** — line should follow **integer**-rounded values, then the window should **end**; idle should **not** run forever on tiny **float** differences.
7. **Spend mana** (or use an ability that changes displayed mana) — mana should appear, then **hide** after a stable period.
8. **Change dimension** / teleport to another world — a **new** **show_on_join**-style window should run ( **Fabric** mixin + **Bukkit** `PlayerChangedWorld` hook ).
9. **`/abtoggle`** — feedback should mention **smart** / **always** / **off** ( **`messages_*.yml`**, **`CommandMessage.TOGGLE_MODE_SET_*`**, **`TOGGLE_MODE_STATUS`** where applicable).

---

## Litematica / action bar conflict (repro)

1. Install **Litematica** (Java client) as usual. Enable a mode that shows **action bar** text.
2. With **Granite in smart**, **idle and full** — **Litematica** text should **not** be perpetually **overwritten** by HP/mana.
3. Take damage — Granite can **temporarily** take priority; after the window, **Litematica** can show again when idle.

---

## Console / log expectations

- **No** new **error** on startup from **unknown `Option` keys** — embedded **`config.yml`** must include all **`Option` enums** for new keys.
- **Normal** `Loaded N config options` on reload.

## Bedrock / Geyser

- **Not** run-time tested this sprint. **Expected:** Geyser still uses the same **vanilla** action bar; **fewer** Granite packets in **smart** should **help** with spam; **not** a claimed parity until tested.

## Pass / fail checklist

- [ ] **Smart** does **not** send idle HP/mana every tick when the player is **idle** and **full** (after window ends).
- [ ] **Join** / **world change** / **damage** / **stat change (ints)** re-open a **visible** window.
- [ ] **No** blank / clear action bar packets from Granite when **hidden** (unavoidable: cannot prove absence in console — **in-client** / packet capture is optional; **Minecraft** shows other text when idle).
- [ ] **`/abtoggle`** cycles and **persists** across **relog** (check `userdata` or behavior).
- [ ] **Legacy** profile: on **2.3.111** only, **`idle: true`** without **`idle_mode`** could match **always**-style; on **2.3.112+** it should load as **smart** unless **`idle_mode`** says otherwise — see **81A** manual.
- [ ] **Unit** tests **`ActionBarIdleModeTest`** pass in CI / local **`fabric:test`**.

## Known limitations

- Only **one** vanilla **action bar** line — two mods can still **clash** **during** a Granite **visible** window; **smart** only removes **idle** **spam** when **hidden**.
- **Client-side** HUD is **out of scope** (Sprint 81).
- **boss bar / scoreboard** HUD: **out of scope**.

## Expected console (developer)

- No **Missing value in config.yml** for new **`action_bar.*` options** after merge.

---

**Definition of done:** See sprint brief; **`.\gradlew -PfabricOnly=true :fabric:test`** and **`:fabric:build`** pass.
