# Sprint 54 — manual validation: `/armor requirement` and `/armor multiplier`

**Runtime:** Fabric dedicated server; **Java** and **Bedrock (Geyser/Floodgate)**.

## Permissions (Bukkit `plugin.yml` names)

- `auraskills.command.armor.modifier` — **`/armor modifier`** / **`/armor trait`**
- `auraskills.command.armor.requirement` — **`/armor requirement …`**
- `auraskills.command.armor.multiplier` — **`/armor multiplier …`**

**Sprint 54** splits the **`/armor` root** so **requirement** / **multiplier** do **not** require **`armor.modifier`**. Grant only what each role needs.

## Targeting (Bukkit parity)

- **Main hand** only (same as **`/armor modifier`**, **`/item …`**): hold a **helmet, chestplate, leggings, boots**, or any item you are treating as “armor” for NBT — the reference uses **`@Flags("itemheld")`** on **main hand**.

## Java — requirement

1. Hold a **helmet** (or other piece). **`/armor requirement add farming 5`** — success; PDC includes **`auraskills:armor_requirements`** on that stack.
2. **`/armor requirement list`**, **`remove`**, **`removeall`** — same behavior as **`/item requirement`** but **`ModifierType.ARMOR`**.
3. Without **`armor.requirement`**, deny; with it but without **`armor.modifier`**, **`/armor modifier`** should still deny if not granted.

## Java — multiplier

1. **`/armor multiplier add global 15`**, **`/armor multiplier add mining 8`**, **`/armor multiplier list`**, **`/armor multiplier remove …`**, **`removeall`** — same shape as **`/item multiplier`**, **`ModifierType.ARMOR`**.
2. Invalid target (neither skill nor `global`) — same plain-English error as item path.

## Bedrock / Geyser

- Server-side **Text** feedback; no Java-only client assumptions.

## Persistence

- NBT on the **ItemStack**; drop/pickup preserves with normal stack rules.

## Risks

- **Main-hand only** — not all armor slots at once (matches Bukkit **`ArmorCommand`** / Fabric **Sprint 51** armor command design).
- Invalid multiplier target message is still a **fixed English** string (same as Sprint 53 item path).
