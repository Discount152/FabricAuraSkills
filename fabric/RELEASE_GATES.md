# Granite Skills / GraniteLib — release gates (binary checklist)

This file gives a **hard** answer to “ready to run?” vs “100% complete?” — **not** a single vague “deployable.”

**Canonical “100%” for this product** = **two** standalone, fully functioning **Fabric** JARs — **GraniteLib** and **Granite Skills** (gameplay) — that together meet **absolute parallel** to the in-repo **Bukkit reference** on a **dedicated Java Fabric** server, with **Java + Bedrock (Geyser/Floodgate)** validation where user-visible behavior differs. Details: [PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md) *Two-JAR completion (canonical)*.

**Do not conflate:**

| Gate | Meaning |
|------|--------|
| **A — Runnable build** | Artifact builds, server loads, core systems function; dependencies correct. |
| **B — Limited production** | Acceptable for **controlled** live use (friends / small server) with **documented** gaps; data safety and worst blockers understood. |
| **C — Release-grade / “absolute completion”** | **Both** JARs (**GraniteLib** + **Granite Skills**); reference capability closed or explicitly accepted; **Java + Bedrock (Geyser/Floodgate)** acceptance; **final Granite** public UX/naming; production hardening — per [PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md) (incl. two-JAR bar). |

**As of the doc set that introduced this file:** the project is intended to sit **between A and B** for many servers (real Fabric mod, meaningful play), **not** at **C** until the open workstreams close. **Re-verify** each row against your branch and `PARITY_BACKLOG.md` before claiming a gate.

---

## Gate A — Runnable build (must all be true)

| # | Criterion | Pass = |
|---|------------|--------|
| A1 | **GraniteLib** and **Granite Skills** each build: `:granitelib-fabric:build` **and** `:fabric:build` (or your CI) succeed; **two** JARs produced (library + gameplay) | Yes / No |
| A2 | Required runtime: **Java Fabric** server, documented MC version | Yes / No |
| A3 | **Fabric API** + declared `depends` (gameplay mod depends on **GraniteLib** as in `fabric.mod.json`) — install order documented | Yes / No |
| A4 | **Both** mods load **together**; `config/auraskills/` appears; no crash on start | Yes / No |
| A5 | Core loop works: join, at least one XP source, save/reload path smoke-tested | Yes / No |

---

## Gate B — Limited production / controlled rollout

**Requires Gate A.** All of the following should be true *or* explicitly documented as accepted risk.

| # | Criterion | Pass = |
|---|------------|--------|
| B1 | **Known gaps** listed (see [PARITY_BACKLOG.md](PARITY_BACKLOG.md)); operators know what is missing vs **Bukkit `reference`** | Yes / No |
| B2 | **Data:** storage, migrate, backup story acceptable for *your* audience | Yes / No |
| B3 | **Permissions:** `fabric-permissions-api` / LP (and **Floodgate**-linked nodes if you use Bedrock) understood | Yes / No |
| B4 | **Exploit posture:** anti-AFK and source coverage acceptable for *your* server size | Yes / No |
| B5 | **Command surface:** operators know current commands ([MOD_COMMANDS.txt](MOD_COMMANDS.txt)) vs full **reference** tree | Yes / No |

---

## Gate C — Release-grade (“absolute completion” for this product definition)

**Requires Gate B for production honesty; Gate C is the stricter bar.**

### C1 — Runtime / identity

| # | Criterion | Pass = |
|---|------------|--------|
| C1.1 | Server runtime remains **Fabric only**; **Bukkit** is not a deploy target | Yes / No |
| C1.2 | Shipped identity is **Granite Skills** (gameplay) + **GraniteLib** (library) as **two** named products; **AuraSkills**-facing public UX removed where the rebrand requires it | Yes / No |
| C1.3 | **GraniteLib** JAR is **complete for its role** (Slate/scheduler/Text bridges, or whatever the dependency contract requires — no “stub” library in a release claim) | Yes / No |
| C1.4 | **Granite Skills** JAR is **complete for its role** (depends on **GraniteLib**; all gameplay features claimed “done” run against a **non-dev** published library build, not a local-only path) | Yes / No |

### C2 — Reference capability coverage

| # | Criterion | Pass = |
|---|------------|--------|
| C2.1 | **Command** capability vs **Bukkit `CommandRegistrar`** closed or **explicitly** waived with doc | Yes / No |
| C2.2 | **Menu / Slate** path per [MENU_AND_COMMANDS_PORT.md](MENU_AND_COMMANDS_PORT.md) closed or **explicitly** waived | Yes / No |
| C2.3 | **Reference** gaps in [PARITY_BACKLOG.md](PARITY_BACKLOG.md) either **done** or **accepted** with migration/ops notes | Yes / No |
| C2.4 | [PARITY_AUDIT_LEDGER_PHASE1.md](PARITY_AUDIT_LEDGER_PHASE1.md) (and later passes) reconciled: no critical **Missing** without decision | Yes / No |

### C3 — Cross-play (Java + Bedrock via Geyser + Floodgate)

| # | Criterion | Pass = |
|---|------------|--------|
| C3.1 | **Java** path validated for user-visible features you ship | Yes / No |
| C3.2 | **Bedrock** path validated where UX differs, **or** limitation **documented and accepted** per row (ledger `Bedrock` columns) | Yes / No |
| C3.3 | **Menus, commands, action bar, title, chat** not “done” on Java-only evidence alone | Yes / No |

### C4 — Production hardening

| # | Criterion | Pass = |
|---|------------|--------|
| C4.1 | Save/stop/ crash behavior trusted for your SLA | Yes / No |
| C4.2 | Mixins and MC upgrades have a **retest** plan (e.g. fishing) | Yes / No |
| C4.3 | Exploit resistance acceptable for **public** server bar | Yes / No |

### C5 — Final Granite public UX

| # | Criterion | Pass = |
|---|------------|--------|
| C5.1 | Final public **command roots/aliases** and product copy are **not** all **TBD** (rebrand/UX sprint **done** where required) | Yes / No |
| C5.2 | No **temporary** command/menu scaffolding sold as final **Granite** design without a decision record | Yes / No |

---

## How to use this

1. **Gate A** before any server install claim.  
2. **Gate B** before inviting non-devs to a long-running world you care about.  
3. **Gate C** before marketing **1.0** / “complete” **both** JARs (**GraniteLib** + **Granite Skills**) or promising full **reference + cross-play** parity.

**Source docs:** [PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md), [FABRIC_PORT_HANDOFF.md](../FABRIC_PORT_HANDOFF.md), [PARITY_BACKLOG.md](PARITY_BACKLOG.md), [MENU_AND_COMMANDS_PORT.md](MENU_AND_COMMANDS_PORT.md).
