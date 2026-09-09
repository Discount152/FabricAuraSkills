# Sprint 143 - Emergency: statistic resolver NPE crash (Agility)

## Crash root cause

Dedicated server crashed with NPE in vanilla code: `Identifier.toString()` on null when registering or looking up a custom statistic. The Fabric resolver could reach `Stats.CUSTOM.getOrCreateStat` with a null or invalid `Identifier` derived from YAML `statistic` strings.

## Null-safety fix

- `FabricStatisticSourceResolver`: `resolve(null/blank)` returns null without calling `trim()` on null; `customStat` and slash branches validate identifiers and paths before any `Stats.*.getOrCreateStat`; `resolveDetailed` returns reasons (`null_stat_identifier`, `blank_stat_identifier`, `invalid_stat_identifier`, `exception:Type`).
- `FabricStatisticXpHandler`: per-player and per-source try/catch so no exception escapes the tick; unresolved sources are skipped only for that entry.
- `FabricXpDiagnostics.logStatisticResolution`: `[XPDIAG] event=statistic_resolution` when `xpdiag` is enabled (unresolved + caught exceptions).

## Startup diagnostics

Each Agility `StatisticXpSource` logs one line at handler start:

`[AuraSkills] Agility statistic source config: source=<id> raw_stat=... normalized=... resolved=... reason=...`

## Tests

`FabricS143EmergencyStatisticResolverTest` covers null/blank and early-exit paths without bootstrapping Minecraft registries in plain JUnit. Full `walk_one_cm` resolution is validated on a running dedicated server.

## Scope

Touched: resolver, statistic XP handler, XP diagnostics, tests, docs. Not touched: farming, fishing, `/sources`, menus, leaderboard, ability lore, GraniteLib.

## Manual validation checklist

- Server starts with agility statistic sources; no crash on bad `statistic` string.
- Startup lines show `raw_stat` / `reason` for each agility statistic source.
- Valid sources (e.g. fall damage) still award XP; one bad source does not stop others.
- With `xpdiag on`, unresolved sources log `event=statistic_resolution` with `resolved=false`.

## Follow-up

If `walk_per_meter` style ids show `resolved=false`, align YAML with vanilla custom stat ids (`walk_one_cm`, `sprint_one_cm`, `swim_one_cm`, jump stats).