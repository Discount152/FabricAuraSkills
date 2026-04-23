# Sprint 21 — manual validation (`ANVIL_DISCOUNT`)

**Build:** `.\gradlew -PfabricOnly=true :fabric:build` (or `:fabric:build`).

## Preconditions

- Grant **`anvil_discount`** (or configured id) and enough **Wisdom**-linked stats if you test multi-character lore; for default validation, a single test player with a non-trivial **effective** trait value is enough.
- Configured **`formula`** in traits YAML (or rely on the built-in fallback curve after a bad formula — expect a log warning).

## Natural checks

1. **Repair / rename / combine** at an anvil: note **level cost** in the anvil screen before take; it should be **reduced** vs the same operation at zero trait (same materials, new creative pair test).
2. **Minimum cost 1** — when the math would round to **0** levels, the UI (and take) should still show/charge **1** (Bukkit behavior).
3. **Disabled world** — repeat in a world listed under disabled/trait-blocked config; **no** discount (vanilla cost).
4. **Trait disabled** in config — no discount.
5. **Sprint 16 take path** — after taking the output, confirm **XP** / rewards still match the **discounted** level cost you saw (and snapshot in `FabricAnvilTakeContext` if you add logging).
6. **Java + Bedrock (optional)** — same server; anvil is server-authoritative; confirm Bedrock does not see a different cost from what the server charged if Geyser shows level numbers.

## Not in this sprint

- **`DoubleDropTrait`**, Bukkit `PrepareAnvilEvent` **plugin** cancellation (default Granite not applicable).
