# Sprint 01 — Player death reset (EVT-014) — manual validation

**Config keys** (in `config/auraskills/config.yml` under `on_death:`; see `Option` in common):

- `reset_skills` — full skill reset per skill (Bukkit: `user.resetSkill`)
- `reset_xp` — reset or scale **skill XP** per skill (Bukkit: `setSkillXp`; ignored when `reset_skills` is true for that iteration)
- `reset_xp_ratio` — if `0 < ratio < 1`, remaining XP = current × ratio; otherwise XP set to 0

**Prerequisite:** one death per scenario (e.g. `/kill` or mobs), server running Granite Skills with runtime ready.

## Java client (Fabric dedicated server)

1. Set `reset_skills: true`, `reset_xp: false`. Gain non-default level in a skill, die, respawn. Confirm that skill reverts to configured start level and XP/permissions match expectations (same as Paper reference).
2. Set `reset_skills: false`, `reset_xp: true`, `reset_xp_ratio: 0`. Gain XP in a skill, die. Confirm skill XP in that skill is 0 (level may still reflect pre-death if not recomputed from XP on next gain — Bukkit-same).
3. Set `reset_xp_ratio: 0.5`, gain skill XP, die. Confirm about half the **skill XP** remains for each skill in the loop (per Bukkit).
4. Toggle both booleans to match your production policy; reload if needed. Confirm no crash and modifiers still apply (armor/items re-evaluated after reset).

## Bedrock via Geyser + Floodgate

1. Use the same three scenarios as above with a **Bedrock-linked** account (Floodgate). Behavior should match Java because the server only sees a `ServerPlayerEntity`; there is no separate Bedrock code path in this handler.
2. If anything differs, note **permission/world** differences first (`blocked_worlds` / `disabled_worlds`), not this listener.

**Automated tests:** The `fabric` module has no JUnit harness for in-game death events in this tree; coverage is manual plus parity with the Bukkit reference class.
