# Sprint 28 — Manual validation (Skills + Stats menus, shared router)

**Mods:** `auraskills` + `granitelib` on dedicated Fabric. **Default permissions** (from reference): `auraskills.command.skills` and `auraskills.command.stats` (see Bukkit `plugin.yml` — adjust for your test server).

## Java

- [ ] `/skills` opens **Skills** large chest; books match **enabled** skills with level/XP; container top read-only.
- [ ] `/auraskills skills` same as above.
- [ ] `/stats` opens **Stats** large chest; one book per **enabled** stat; item name uses color/symbol/stat/level; lore has description (truncated) and trait sub-lines when a **Fabric** trait impl exists.
- [ ] `/auraskills stats` same as `/stats`.
- [ ] **Help/usage** strings mention `stats` and do not claim full command parity.
- [ ] **Reload** then reopen both menus; no crash.

## Bedrock (Geyser + Floodgate)

- [ ] `/skills` and `/stats` open chest UIs; text readable enough for sanity check.
- [ ] If inventory/title issues appear, file under Geyser + document.

## Sign-off

Tester / date: ____________________
