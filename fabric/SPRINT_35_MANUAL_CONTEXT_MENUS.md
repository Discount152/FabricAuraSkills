# Sprint 35 — manual validation (context-aware built-in menus)

**Scope:** `level_progression` and `stat_info` are registered in **`FabricMenuRegistry`** and require **`FabricMenuContext#ofSkill`** / **`#ofStat`**. **Runtime:** Fabric; **clients:** Java and Bedrock (Geyser + Floodgate) if available.

## Prereq

- Granite Skills + GraniteLib. On server start, the log should show **four** built-in menu ids, including `level_progression` and `stat_info` (order may vary).

## Java

1. **Skills → Level Progression:** Open **Skills**, click a permitted skill. Confirm **Level Progression** opens (title includes skill name), **Back** returns to **Skills**.
2. **Stats → Stat Info:** With **`menus.stat_info.enabled: true`**, open **Stats**, click a stat row. **Stat Info** opens; **Back** returns to **Stats**.
3. **Reload:** Run **`/auraskills reload`**. Repeat (1) and (2) — no regression.

## Bedrock / Geyser

Repeat the same three flows. Confirm chest UI + clicks + back navigation. Note any Geyser quirks in the parity backlog if new.

## Registry / log checks (optional)

- Calling **`open("level_progression", …, FabricMenuContext.none())`** from a mod/dev harness should log a **warn** and not open a GUI (not exposed to normal players via shipped commands in this sprint).

## Checklist

| Check | Java | Bedrock |
|-------|------|---------|
| Skills → Level Progression → Back | [ ] | [ ] |
| Stats → Stat Info → Back | [ ] | [ ] |
| After `/auraskills reload` | [ ] | [ ] (optional) |
