# Sprint 81 — Smart health/mana action bar (manual validation)

**Build / version:** `FabricAuraSkills-2.3.111.jar` (or current `projectVersion` in `gradle.properties` when built).

**Problem:** Granite’s **idle** health/mana line used the only vanilla **action bar** slot on a fast timer, so it **outcompeted** other mods (e.g. **Litematica** mode text) that also use that line. **Sprint 79** fixed **MiniMessage** rendering, but the **concurrent resend** issue remained.

**Root fix (server-side):** In **`smart`** mode ( **default in embedded `config.yml`** for **`action_bar.mode`**; **legacy** player files with **`idle: true`** and no **`idle_mode`** stay **always**-style for backward compatibility), Granite **only** sends the idle line during **short windows** after relevant events, then **stops** sending. **No** blank packets to “clear” the line.

**Reference screenshot (conceptual):** Before — HP/mana text always above hotbar. After (idle) — other mods’ action bar text can show until the next **smart** window.

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

**Default “product” mode:** **smart** in **embedded** `config.yml`. **Old** player `userdata` with **`idle: true`** only → **always** (legacy). **`idle: false`** → **off**.

---

## `/abtoggle` / `toggle` behavior (Sprint 81)

- **Permission:** `auraskills.command.abtoggle` (unchanged name).
- **Action:** Cycles per-player **idle** mode: **smart → always → off → smart**.
- **Storage:** `userdata/<uuid>.yml` (or SQL key-value): **`action_bar.idle_mode`**, and legacy **`action_bar.idle`** bool.

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
9. **`/abtoggle`** — feedback should mention **smart** / **always** / **off** ( **`messages_*.yml`**, **`CommandMessage.TOGGLE_MODE_*` ).

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
- [ ] **Legacy** profile with **`idle: true`** (no `idle_mode`) still behaves like **continuous** line (**always**).
- [ ] **Unit** tests **`ActionBarIdleModeTest`** pass in CI / local **`fabric:test`**.

## Known limitations

- Only **one** vanilla **action bar** line — two mods can still **clash** **during** a Granite **visible** window; **smart** only removes **idle** **spam** when **hidden**.
- **Client-side** HUD is **out of scope** (Sprint 81).
- **boss bar / scoreboard** HUD: **out of scope**.

## Expected console (developer)

- No **Missing value in config.yml** for new **`action_bar.*` options** after merge.

---

**Definition of done:** See sprint brief; **`.\gradlew -PfabricOnly=true :fabric:test`** and **`:fabric:build`** pass.
