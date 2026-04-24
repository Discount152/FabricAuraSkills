# Sprint 29 — Manual validation (menu registry + Skills/Stats)

**Prereq:** `auraskills` + `granitelib` on a Fabric server. **Log:** on server start, one line: `Registered 2 built-in Fabric menu id(s): skills, stats` (order may match registry insertion order).

## Skills + Stats (unchanged player UX)

- [ ] `/skills` and `/auraskills skills` open **Skills** GUI; title is the translated **Skills** string.
- [ ] `/stats` and `/auraskills stats` open **Stats** GUI; title is the translated **Stats** string.
- [ ] Java: open/close both menus; no errors in console from menu code.
- [ ] Bedrock (Geyser/Floodgate): same two commands; chest UIs open.

## Registry behavior (light)

- [ ] (Optional / dev) From a one-off or debug, call `FabricAuraSkills.getInstance().getMenuRegistry().menuIds()` and confirm **`skills`** and **`stats`** are present (Sprint 29+).

## Sign-off

Tester / date: ____________________
