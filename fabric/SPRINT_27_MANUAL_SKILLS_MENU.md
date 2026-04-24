# Sprint 27 — Manual validation (Skills menu GUI slice)

**Scope:** GraniteLib read-only 6×9 + `FabricSkillsMenuGui` (real skill data). **Temporary open path:** `/skills` and `/auraskills skills` (unchanged permissions: `auraskills.command.skills`).

## Java client

- [ ] Run dedicated Fabric server with **auraskills** + **granitelib** mods.
- [ ] ` /skills` opens a **large chest** GUI titled **Skills** (or translated `en_us`).
- [ ] One **book** per **enabled** skill; display name matches skill; lore shows **Level**, **XP**, and **no permission** line when applicable.
- [ ] **Container slots** do not take/move items; **player inventory** still works.
- [ ] **E / Esc** closes the menu; no error in log.
- [ ] Gained XP in another way, re-open: values reflect **current** user state (no stale if you reopen).

## Bedrock (Geyser + Floodgate)

- [ ] Same as Java for open/close and visual sanity (chest UI).
- [ ] If title/lore do not show as expected, note client language pack / Geyser limitation (document in issue).

## Regression

- [ ] **Reload** `/auraskills reload` then `/skills` still works.
- [ ] `StatReward` / code using `getFormat("level_progression", "stat_reward_entry")` still receives a non-empty template (unit: `FabricMenuHelperTest`).

## Sign-off

Tester / date: ____________________
