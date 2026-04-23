# Capability & reference-baseline program (Fabric)

> **Filename:** `PAPER_PARITY_PROGRAM.md` (retained for links). **Runtime target:** **Fabric only** — not Paper, not Bukkit, not Spigot. The **`bukkit` module in this repository** is the **reference implementation** and **baseline capability inventory** (what to implement on Fabric), not a second platform the product runs on. **Shipped product identity:** [Granite Skills / GraniteLib](#granite-skills--granitelib--product-identity-read-second). **Supported players:** the mod must work for **Java clients** and for **Bedrock clients joining through Geyser + Floodgate** (see **Client & player access** below — cross-platform validation is part of “done”).

## Runtime vs reference (read first)

| | |
|--|--|
| **Production server** | **Dedicated Java Fabric** (see [FABRIC_PORT_HANDOFF.md](../FABRIC_PORT_HANDOFF.md) §0.1). All gameplay integration work targets **Fabric-native** APIs, mixins, and mods. |
| **Client / player access (product)** | **Java edition players** (direct) **and** **Bedrock players** connecting via **Geyser** + **Floodgate** on the same Fabric server. “Equivalent capability” must be validated for **both** paths where their UX differs; where Bedrock cannot support a feature, **document the limitation** and ship an acceptable alternative or accept the gap explicitly. |
| **Reference / audit source** | The **existing `bukkit` module** in this monorepo: current **feature list**, registration order, and behavior to match or replace with **equivalent** Fabric behavior. *Not* a runtime dependency. *Not* the final public command/menu branding (that is **Granite**, later). |
| **“Parity” in these documents** | **Fabric-native implementation** with **equivalent capability coverage** and config-driven **behavioral** alignment to that reference, where Minecraft allows — **not** “ship Paper as the platform” and **not** freezing AuraSkills naming as the long-term contract. |

---

## Granite Skills / GraniteLib — product identity (read second)

**Shipped product (target):** the gameplay mod ships as **Granite Skills**; the shared library as **GraniteLib**. The final public release should **remove AuraSkills-only branding and player/operator-facing identity** (names in chat, default command roots/aliases, menu titles where they encode legacy product name, etc.). **When** those strings and command namespaces are finalized is a **separate rebrand / UX sprint** — not gating the technical parity program.

**This document and the [parity ledger](PARITY_AUDIT_LEDGER_PHASE1.md) track *functional* coverage** against the **`bukkit` reference module** in this repo as the **baseline capability inventory** (what exists to port or match on **Fabric**). They do **not** assert that old command names, aliases (`skills`/`sk`/`skill`, `/auraskills`, …), or permission string spellings are the **final Granite Skills public surface**.

**Engineering target for the Fabric mod** (in-tree: **FabricAuraSkills** / current Gradle coordinates): a **Fabric-native** implementation with **equivalent** progression (`api` + `common`), same YAML-driven behavior, and the same **classes of** player/operator **capabilities** as the reference, **where the Fabric platform allows** — compared against the `bukkit` module, not by shipping Bukkit at runtime.

**Related:** [FABRIC_PORT_HANDOFF.md](../FABRIC_PORT_HANDOFF.md) (deep wiring, file index, Phase A–G workstreams), [PARITY_BACKLOG.md](PARITY_BACKLOG.md) (checklist of gaps), [MENU_AND_COMMANDS_PORT.md](MENU_AND_COMMANDS_PORT.md) (Slate + full command tree), **[PARITY_AUDIT_LEDGER_PHASE1.md](PARITY_AUDIT_LEDGER_PHASE1.md)** (repo-anchored audit rows + CSV for the parity workbook).

### Two-JAR completion (canonical)

**Program completion** is **not** a single blended percentage. It is **two** standalone, production-runnable **Fabric** artifacts that operators install together:

| JAR (product) | Role in repo (Gradle) | Must be |
|---------------|------------------------|--------|
| **GraniteLib** | Library mod — `:granitelib-fabric` (sources under `slate-fabric/`) | **Full-functioning** on its own: valid `fabric.mod.json`, version metadata, API surface the gameplay mod requires; build and release as its **own** artifact. |
| **Granite Skills** (gameplay) | Gameplay mod — `:fabric` (in-tree **FabricAuraSkills** / current coordinates) | **Full-functioning** on its own: depends on **GraniteLib** as declared; no missing library behavior at runtime. |

**Acceptance (both JARs together):** behavior is **in absolute parallel** to the **in-repo Bukkit / Paper reference module** (same program’s capability baseline: [§1](#1-what-equivalent-capability-means-reference-baseline-fabric-runtime), [PARITY_AUDIT_LEDGER_PHASE1.md](PARITY_AUDIT_LEDGER_PHASE1.md)) on a **dedicated Java Fabric** server, with **Java** and **Bedrock via Geyser + Floodgate** paths validated for user-facing flows (see [RELEASE_GATES.md](RELEASE_GATES.md) Gate **C**). *Reference = capability inventory, not a runtime target.*

A **single** mod JAR with a broken or stubbed **GraniteLib** does **not** count as done. A **GraniteLib** that builds but is not the **active** path for the shipped menu/Slate/contract the gameplay mod needs does **not** count as done. See [RELEASE_GATES.md](RELEASE_GATES.md) for the binary checklists (Gates A–C).

---

## 1. What “equivalent capability” means (reference baseline, Fabric runtime)

| Layer | Meaning |
|--------|--------|
| **Gameplay** | For the same `config/auraskills` files and skill/stat YAML, **outcomes** match the **reference** behavior: XP awards, level-ups, rewards, traits, abilities, mana abilities, loot rules, requirements, anti-AFK, etc. (validated against the `bukkit` module’s behavior, not by running Bukkit in prod). |
| **Operator & player *capability* (inventory)** | **Coverage** and **flow classes** match the **reference `bukkit` implementation** (commands, menus, admin actions) — the audit list ([PARITY_AUDIT_LEDGER_PHASE1.md](PARITY_AUDIT_LEDGER_PHASE1.md) `CMD-*`, `MENU-*`). **Final** public command roots, aliases, and permission spellings for **Granite Skills** are **TBD**; the reference tree is a **scope checklist**, not the final namespace. **Acceptance:** usable for **Java** and, where applicable, for **Bedrock via Geyser/Floodgate**; track **Bedrock** status in the ledger optional columns. |
| **Final public UX (Granite)** | Shipped **Granite Skills** copy and command names are **TBD** until the branding/rename sprint. Implementation work should complete **behavior**; **do not** treat legacy AuraSkills strings as immovable. |
| **Storage & data** | Same `common` abstractions (YAML default, MySQL when enabled); shared migrations and layout. |
| **Integrations (reference used plugins)** | The reference build uses **Bukkit-side plugins** (WorldGuard, Vault, jobs, …). The **Fabric** product implements **the same config knobs and behavior** via **Fabric mods and bridges** — not a Bukkit plugin classpath at runtime. |
| **API** | `api-bukkit` is reference-only for this port. A **Fabric-native** API for mod authors may be added; re-exposing every Bukkit event is a **separate** deliverable (see [FABRIC_PORT_HANDOFF.md](../FABRIC_PORT_HANDOFF.md) §0.2). |

---

## 2. Gaps in the reference vs Fabric (tracked work)

Some behaviors in the **reference** build depend on **Bukkit-only or third-party plugin APIs**. The Fabric product matches **behavior** where possible; it does **not** copy those classpaths. Implementation is **Fabric-native**.

| Reference ecosystem (not shipped on Fabric) | Fabric approach |
|---------------------------------------------|-----------------|
| **PlaceholderAPI** | Fabric placeholder stack (e.g. server-side placeholder mods) or a small embedded resolver for documented `%` keys. |
| **ProtocolLib** | Only if the **capability** requires raw packets; use Fabric networking APIs or document exclusion. |
| **WorldGuard, Vault, …** (Bukkit plugins) | **Named Fabric mod** bridges or `common` + Fabric backends — not Bukkit JARs at runtime. |

These items stay on the **same** parity backlog, with explicit “bridge mod” or “native implementation” decisions.

---

## 3. Reference module in this repo (anchors for work)

Use the **`bukkit` module** as the **feature and behavior inventory** to implement on Fabric — not as a second runtime:
- **Bootstrap:** `bukkit/.../AuraSkills.java` — `registerLevelers`, `registerEvents`, `registerAndLoadMenus`, hooks, `MenuRegistrar`, `CommandRegistrar`, `LootManager`, traits/abilities/mana registration.
- **Commands:** `bukkit/.../commands/**`, especially `CommandRegistrar` and the ACF-registered tree (root aliases `skills` / `sk` / `skill` per `CommandRegistrar` / `plugin.yml`).
- **Menus:** `bukkit/.../menus/**`, `MenuRegistrar`, Slate usage, YAML under Bukkit resources.
- **Listeners / loot / regions:** e.g. `DamageListener`, `BlockLootHandler`, `FishingLootHandler`, `MobLootHandler`, `RegionListener`, `RegionBlockListener`, `ItemListener`, `ArmorListener`, `JobsListener`, … (same file as in `registerEvents()`).

Any **Fabric** class that is still a **stub** or **no-op** relative to the **corresponding** reference class is **known debt** until this program closes (always within a **Fabric-native** design, not “embed Bukkit”).

---

## 4. Major workstreams (map to handoff Phases A–G)

The phased roadmap in [FABRIC_PORT_HANDOFF.md](../FABRIC_PORT_HANDOFF.md) **§11** is the technical breakdown. **Phase D (integrations)** and **Phase E (UI / commands)** are **in scope** for **equivalent coverage** (Fabric-native), not “only if a deployment asks.” Sequencing is by **risk and dependency** (e.g. stable levelers and persistence first).

| Stream | Deliverable (on Fabric) | Primary references |
|--------|------------------------|------------------|
| **A** | Levelers, fishing/block/entity/damage, statistics (reference: `*Leveler`) | Reference `*Leveler`, `Fabric*Handler` / mixins |
| **B** | Traits: register and wire **equivalent** handlers | `FabricTraitManager`, `BukkitTraitManager` (reference) |
| **C** | Items, modifiers, armor, loot extras | Reference `ItemListener`, `Armor*`, `BukkitLootManager` + handlers |
| **D** | Regions, economy, jobs, third-party mobs — **Fabric** bridges | `HookManager` (reference), `RegionListener`, `JobsListener` |
| **E** | **Slate on Fabric** + full command **capability** (Brigadier/ACF) | [MENU_AND_COMMANDS_PORT.md](MENU_AND_COMMANDS_PORT.md) |
| **F** | Public API for consumers (as needed) | `api`; `api-bukkit` is reference-only |
| **G** | CI, tests, build hygiene | Handoff §11 Phase G |

---

## 5. How we know we’re done

- **Two JARs ship:** [GraniteLib + Granite Skills](#two-jar-completion-canonical) both build, install, and run as **standalone** artifacts (see [RELEASE_GATES.md](RELEASE_GATES.md) Gate **C**). No “half a product.”
- **Config-driven regression:** Same default configs run on the **Fabric** mod → **comparable** XP curves, level-ups, and reward lines to the **reference** behavior (side-by-side check against the `bukkit` build or spec where useful; **Fabric** is the runtime under test).
- **Command/menu *capability* matrix:** Every **reference** `CommandRegistrar` / `MenuRegistrar` item has a **Fabric-native** answer or a **documented** unsupported substitute. **Final Granite Skills** public names and copy: **separate** acceptance pass.
- **Java + Bedrock (Geyser / Floodgate):** “Done” is **not** only “works for Java clients.” Features that surface as **chat, action bar, title, boss bar, commands, or inventory UIs** should be **verified** (or **explicitly** scoped as Java-only) on **Bedrock via Geyser + Floodgate** when behavior may diverge. **Equivalent capability** = correct for the **supported player population** (see §1), not a Java-only green check.
- **Integrations:** Each **reference** integration is either a **Fabric** mod bridge or **explicitly** unsupported, with a clear alternative in [PARITY_BACKLOG.md](PARITY_BACKLOG.md).
- **Tests:** Expand `common` + `fabric` tests; server smoke for fragile mixins (e.g. fishing) after MC bumps; add **Geyser/Floodgate** to manual acceptance where UI or commands change.

---

## 6. Governance (same as handoff — still applies)

Re-read [FABRIC_PORT_HANDOFF.md](../FABRIC_PORT_HANDOFF.md) **§0.6–0.7**: if stubs multiply without a boundary strategy, or `common` fills with Bukkit-only hacks, **pause** and redesign. A **capability** goal does not mean *infinite* shims — it means **deliberate** Fabric-native ports and, where needed, **refactors** in `common` for **Fabric + any future** consumers, not “pretend the server is Bukkit.”

---

## 7. Document ownership

- **Product target (this file):** **Two** **Fabric** JARs (**GraniteLib** + **Granite Skills**) with **equivalent capability** to the **reference `bukkit` module** for **Java and Bedrock (Geyser/Floodgate)** player paths where supported; **Granite** branding and final public command surface **out of scope** except where noted above.
- **Pass 1 audit spine:** [PARITY_AUDIT_LEDGER_PHASE1.md](PARITY_AUDIT_LEDGER_PHASE1.md) — **repo-anchored**; workbook = **dashboard**; **repo wins** on conflict.
- **Gap list:** [PARITY_BACKLOG.md](PARITY_BACKLOG.md) — shrink bullets as Fabric catches the reference.
- **Handoff / build facts:** [FABRIC_PORT_HANDOFF.md](../FABRIC_PORT_HANDOFF.md) — file paths, bootstrap order, Gradle.
- **“Deployable” vs “100% complete”:** do **not** use one word for both — use **[RELEASE_GATES.md](RELEASE_GATES.md)** (Gates A / B / C, binary checklists).

When handoff **§0.2** and this file disagree on *intent*, **prefer this file**; for *paths and toolchain*, **prefer the handoff**.
