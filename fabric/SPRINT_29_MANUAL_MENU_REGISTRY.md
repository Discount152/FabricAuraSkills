# Sprint 29 — Manual validation (menu registry + Skills/Stats)

**Prereq:** `auraskills` + `granitelib` on a Fabric server. **Log:** on server start, one line lists built-in menu ids. **Sprint 29:** two (`skills`, `stats`). **Sprint 35+:** four — also `level_progression` and `stat_info` (order may match registry insertion order).

## Skills + Stats (unchanged player UX)

- [ ] `/skills` and `/auraskills skills` open **Skills** GUI; title is the translated **Skills** string.
- [ ] `/stats` and `/auraskills stats` open **Stats** GUI; title is the translated **Stats** string.
- [ ] Java: open/close both menus; no errors in console from menu code.
- [ ] Bedrock (Geyser/Floodgate): same two commands; chest UIs open.

## Registry behavior (light)

- [ ] (Optional / dev) `getMenuRegistry().menuIds()` includes **`skills`** and **`stats`**; **Sprint 35+** also **`level_progression`** and **`stat_info`**.

## Sign-off

Tester / date: ____________________
