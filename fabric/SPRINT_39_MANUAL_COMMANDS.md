# Sprint 39 — manual validation (command audit + Brigadier scaffold + `/mana`)

**Scope:** Confirm **Sprint 39** wiring: shared **`fabric/.../command/*`** registration, **permission** defaults, and the **`/mana`** self-display (Bukkit `ManaCommand` default). **Clients:** **Java** and **Bedrock (Geyser + Floodgate)** on the same Fabric server.

**Audit artifact:** in-repo table **`fabric/COMMAND_PARITY_AUDIT.md`**.

## Prereq

- Mod loads; no startup errors. Optional: **LuckPerms** (or other permission provider) to test denied nodes.
- A player with a loaded **User** profile (join once before `/mana` if testing empty profile edge cases).

## Java

1. **Root / usage:** Run **`/auraskills`** with no args — should show a short **usage** line (translation **`auraskills.granite.command.auraskills_usage`**), including **/mana**.
2. **Help:** **`/auraskills help`** — help line with **/mana** ( **`auraskills.granite.command.help`**).
3. **Menu commands (unchanged):** **`/skills`**, **`/auraskills skills`**, **`/stats`**, **`/auraskills stats`** — menus open; action-bar / chat feedback as before.
4. **`/mana`:** As a **player**, with permission **`auraskills.command.mana`** (default allow), you should see **prefixed** mana current/max from messages (`CommandMessage.MANA_DISPLAY`).
5. **Console / non-player:** Run **`/mana` from the server console** as execute — should get **“Players only”**-style error (no crash).
6. **Permissions:** Deny **`auraskills.command.mana`** for a test group; command should be **unavailable** (Brigadier `requires` — no feedback line may appear; depends on the permission mod).
7. **Staff:** **`/auraskills reload`** / **`save`** with and without staff nodes — same as pre–Sprint-39.

## Bedrock / Geyser

- Repeat **2–4** and **6**; chat-based feedback should still display (server-side `Text`).

## Checklist

| Check | Java | Bedrock |
|-------|------|---------|
| /auraskills, /auraskills help | [ ] | [ ] |
| /mana (player, has profile) | [ ] | [ ] |
| /mana (console) error | [ ] | N/A |
| skills/stats still open | [ ] | [ ] |
