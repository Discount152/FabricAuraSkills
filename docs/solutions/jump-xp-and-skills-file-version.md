# Jump XP and missing skills.yml sections (2.3.247)

## Agility jump XP never arrived after 100+ real jumps

Problem: `FabricJumpingXpHandler` counted only the vanilla `minecraft:jump` statistic.
`ServerPlayer.jumpFromGround` is what increments it, and a probe that calls that method gets XP.
A 26.3 client jump is applied as a position packet. The server often never calls
`jumpFromGround`, so the statistic stays 0 and the interval never completes. Fall XP is a
separate damage source, which is why falling worked.

Fix: also count leaving the ground while moving up (about 0.15–1.25 blocks). Flight, water,
and riding are ignored. If the statistic later records that same jump, the pending credit
swallows it so it is not awarded twice.

Verify: `FabricJumpAwardTest`. In game, jump until the action bar shows agility XP (7 per 100).

## Endurance, Sorcery, and Healing absent from an existing skills.yml

`updateUserFile` only merges new keys when the embedded `file_version` is higher than the
server file. Both were version 1, so a skills.yml created before those sections existed never
gained them. A missing section is treated as disabled.

Fix: bundled `skills.yml` is `file_version: 2`. The next boot merges the missing skill sections
and keeps values the server already set.
