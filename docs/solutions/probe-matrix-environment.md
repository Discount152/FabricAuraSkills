# Disposable-server probe matrix: environment gotchas (2.3.244)

`F:\gs-lifecycle-server\scripts\run-matrix.ps1` reruns every `*-probe-src` against the jars in `mods\`.
A probe FAIL on the matrix is not a product defect until these are ruled out.

## No player online means no loaded chunks

26.3 keeps no spawn chunks. With no player, item entities dropped at probe positions are invisible to
`getEntitiesOfClass`. `matrix-harness` force-loads the probe band. Side effect: those chunks now save
and reload item entities between runs, so a probe that finds "the nearest item" must clear old items first
(`xp-fish`).

## Same-tick hits on one entity count once

`hurtServer` called repeatedly in one tick lands only the first hit, even after clearing
`invulnerableTime` (private in 26.3). A chance-based on-hit ability then gets one roll. Use a fresh target
per hit (`stun-event`: 80 zombies, ~16% stun vs 20% configured).

## A level-up hides part of an XP award

XP spent on a level-up leaves the skill XP. Measure an award as
`delta + sum(getXpRequired(skill, l))` for each level gained. Raising the start level instead changes the
award (level rewards multiplied salmon 60 → 96 at level 30).

## Shared userdata

About 130 profiles from other probes stay in `config\graniteskills\userdata`. Leaderboard probes that
rank their own fixtures need an empty userdata (`lb-cmd-average` prepare step).

## Probes that need a non-default setup

Per-probe `prepare`/`cleanup` in `run-matrix.ps1`: SQL on 3307 for MySQL probes, dead port 3999 for
`mysql-fail`, LuckPerms jar only for `perm-provider` (other probes assert it is absent), death-reset config.
`mysql-fail` must crash at boot; its verdict comes from the YAML hash script and the crash cause.

## Stale pins

When a probe asserts a version, dependency floor or behavior that a later case changed, update the probe
to the current contract and cite the case that changed it (`review-fixes` floor alpha.21,
`preset-runtime` merge per `AC-PRESET-MERGE`). Do not relax the check.
