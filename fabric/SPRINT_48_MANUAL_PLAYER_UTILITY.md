# Sprint 48 — manual validation: **multiplier**, **toggle**, **abtoggle**, **lang**

**Scope:** **`/multiplier`**, **`/toggle`**, **`/abtoggle`**, **`/lang <code>`** and the same under **`/auraskills`**, **`/sk`**, **`/skill`**. **Java** and **Geyser** (server-side chat). Perms (Bukkit-aligned): **`auraskills.command.multiplier`**, **`auraskills.command.abtoggle`** (covers both **toggle** and **abtoggle**), **`auraskills.command.lang`**.

**Note:** On Fabric, **LuckPerms-style** `auraskills.multiplier.*` aggregation may still read as **0** if **`FabricUser#getPermissionMultiplier`** is not yet wired; **item**-based per-skill multipliers from **`LevelManager`** should still show when they differ from global. **`/lang`** only accepts locales that exist in the loaded **MessageProvider** (after **`/auraskills reload`**, new language files apply).

**Persistence:** After **`/lang`** and after **`/toggle` / `/abtoggle`** (when the toggle actually changes and **`action_bar.enabled`** is true in config), **`user.setShouldSave(true)`** — verify **save** / **restart** as you would for other user data.

## Commands

- **`/multiplier`** — self: global line + per-skill lines when per-skill differs.
- **`/multiplier <onlinePlayer>`** — same view; target must be **online**; unknown name → `auraskills.granite.command.xp.player_not_found`.
- **`/toggle`** and **`/abtoggle`** — same **IDLE** action bar enable (Bukkit **alias** pair); if **`action_bar.enabled`** is **false** in config, **`TOGGLE_NOT_ENABLED`**-style message.
- **`/lang <code>`** — e.g. **`en_us`** → internal **`en-US`**; only **known** message locales; otherwise **`LANG_NOT_FOUND`**.

**Prefix tests:** **`/sk multiplier`**, **`/auraskills lang en_us`**, **`/skill abtoggle`**, etc.

## 1) Java (desktop)

1. As a **player** with perms, run **`/multiplier`** — one **global** line; optional per-skill lines.
2. Run **`/multiplier <otherOnlineName>`** — same shape for the **target**.
3. With **`action_bar.enabled`** true in **server** config, run **`/toggle`** twice — **enabled** / **disabled** messages alternate; run **`/abtoggle`** — same behavior (alias).
4. Run **`/lang en_us`** (or another **loaded** code from tab) — success **`LANG_SET`**-style; run **`/lang not_a_real_locale_zzz`** — **`LANG_NOT_FOUND`**.
5. **Console** **`/multiplier`** (self) — error using **`CommandMessage#MULTIPLIER_PLAYERS_ONLY`**.
6. **Console** **`/toggle`** / **`/abtoggle`** / **`/lang en_us`** — **`auraskills.granite.command.utility.players_only`**.

## 2) Bedrock / Geyser

7. Type the same commands in chat; expect normal **translatable** / literal feedback (no client-only **Java** GUI assumptions).

## 3) Permissions

8. **Deny** `auraskills.command.multiplier` — **`/multiplier`** (and prefix mirrors) blocked.
9. **Deny** `auraskills.command.abtoggle` — **`/toggle`** and **`/abtoggle`** blocked.
10. **Deny** `auraskills.command.lang` — **`/lang`** blocked.

## 4) Reload

11. If you add a **new** `assets/auraskills/lang/*.json` and **`/auraskills reload`**, the new code should appear in **tab** suggestions for **`/lang`**.

## Checklist

- [ ] **Regression:** **`/xp`**, **`/top`**, **openmenu**, and prior sprints’ commands still work.
