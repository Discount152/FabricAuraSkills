# Sprint 87 — Bedrock / Geyser validation pass (runtime)

| Field | Value |
|--------|--------|
| **Sprint** | **87** — **runtime validation only** (no default feature work) |
| **Goal** | First **dedicated** pass: Bedrock players via **Geyser + Floodgate** on the **same** Fabric server running **Granite Skills** + **GraniteLib** behave **safely** and **acceptably** vs Java-validated command / menu / action-bar behavior. |
| **Out of scope** | New commands, new menus, full Slate parity, renames, balance/XP/storage changes **unless** a **confirmed** Bedrock/Geyser bug requires a **small** safe fix. **JobsCommand** and third-party plugin ecosystems remain out of scope (see **`PARITY_BACKLOG.md`**). |

## Hard scope (reminder)

This project is **only**: Granite Skills, GraniteLib, Fabric, Java players, Bedrock through Geyser/Floodgate, and Bukkit reference parity where practical. **No** Bukkit/Paper at runtime. **No** WorldGuard, Vault, PAPI, etc.

## Baseline (Java)

| Area | Status |
|------|--------|
| Java startup | Pass |
| Core roots / menus | Pass |
| XP / skilllevel / mana | Pass |
| Smart action bar (81C) | Pass |
| Storage / backup / preset / item roots (staff) | Pass |
| Destructive backup/preset | Intentional Skip |
| Deeper menu lore raw keys | Watch-list |
| Bedrock/Geyser (pre–87) | Not tested |

Java evidence: **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** (closeout **Sprint 86**). Relevant code/docs sprints: **79** (text), **81C** (smart action bar), **83–85** (commands).

## Required artifact

**Minimum:** **`FabricAuraSkills-2.3.118.jar`** (or any build **≥ 2.3.118** that matches your Java smoke baseline).

Record the **exact** JAR name in **Environment** below.

---

## Environment (fill when you run)

| Item | Your value |
|------|------------|
| **Test date** | |
| **FabricAuraSkills JAR** | e.g. `FabricAuraSkills-2.3.118.jar` |
| **GraniteLib** | Version / JiJ / external — describe |
| **Minecraft** | |
| **Fabric Loader / API** | |
| **Geyser** | Version + platform (Standalone / mod) |
| **Floodgate** | Version |
| **Bedrock client** | Device + MC version |
| **Bedrock test account** | (linked via Floodgate) |
| **Java control account** | (optional, for side-by-side comparison) |
| **Permission plugin** | e.g. LuckPerms — note Floodgate/Bedrock user mapping |

---

## Prerequisites

1. **Fabric** dedicated server with **FabricAuraSkills** + **GraniteLib** as for Java smoke.
2. **Geyser** + **Floodgate** correctly configured so a **Bedrock** player can join and obtain a **Java-linked** identity (UUID prefix / Floodgate rules per your setup).
3. Bedrock tester has at least **default player** AuraSkills permissions needed for the rows you exercise (mirror Java **non-staff** smoke unless intentionally testing staff).
4. Server **restart** or clean session after JAR changes (recommended).

---

## Pass / fail rules

| Observation | Classification |
|-------------|----------------|
| Feature works; text readable; no crash | **Pass** |
| Works with minor UX difference vs Java but usable | **Pass (limitation noted)** |
| Raw `auraskills.granite.*` or broken formatting on Bedrock | **Fail** — document surface |
| Disconnect, spam packets, inventory desync, unreadable action bar | **Fail** |
| Not tested this pass | **Skip** — say why |

**Small code fix** only if you **confirm** a Bedrock-specific bug and the fix is **narrow** (no feature creep). Otherwise **document** and file follow-up.

---

## Checklist — connectivity & safety

| # | Check | Result | Notes |
|---|--------|--------|--------|
| B1 | Bedrock player joins server without disconnect loop | | |
| B2 | No repeated Granite Skills errors in `latest.log` on join | | |
| B3 | Basic movement, break/place (if applicable) stable | | |

---

## Checklist — core progression (Bedrock)

| # | Check | Result | Notes |
|---|--------|--------|--------|
| P1 | XP / skill progression still advances from normal gameplay you test | | |
| P2 | Skills/stats values plausible (no obvious desync vs Java for same account class) | | Optional: compare with Java alt |

---

## Checklists — menus (high-risk on Bedrock)

| # | Check | Result | Notes |
|---|--------|--------|--------|
| M1 | `/skills` or equivalent opens **Skills** menu; can close | | |
| M2 | `/stats` or equivalent opens **Stats** menu; can close | | |
| M3 | At least one **detail** path: e.g. `openmenu level_progression <skill>` or click-through from Skills | | |
| M4 | **No** item steal / dup signals; clicks don’t hard-crash client | | |
| M5 | Titles / primary labels **readable** (not raw translation keys in **chat** or obvious HUD breakage) | | Deeper slot lore = watch-list |

---

## Checklist — commands (Bedrock chat)

Exercise a **representative** subset (typing as Bedrock player):

| # | Command / action | Result | Notes |
|---|-------------------|--------|--------|
| C1 | `/skills` (default open menu) | | |
| C2 | `/mana` or `/mana view` (self) | | |
| C3 | `/abtoggle status` | | |
| C4 | `/abtoggle smart` then `/abtoggle always` then `/abtoggle off` (restore **smart** after) | | |
| C5 | One **read-only** Granite command you use on Java (e.g. `/top`, `/multiplier`) | | |

**Staff-only** commands (`/storage`, `/backup`, `/item` admin, etc.): **Skip** unless you intentionally grant Bedrock-linked account staff perms.

---

## Checklist — action bar (81C parity target)

| # | Check | Result | Notes |
|---|--------|--------|--------|
| A1 | **Smart:** health/mana idle line appears in **expected windows** (e.g. after relevant triggers) | | |
| A2 | **Smart:** while **idle**, no sticky spam replacing other Bedrock UI (e.g. other mods’ hints) | | Subjective — note device |
| A3 | **Always:** persistent idle H/M when enabled | | |
| A4 | **Off:** Granite H/M suppressed on action bar when off | | |
| A5 | Text **readable** on Bedrock action bar (no garbled legacy codes) | | |

---

## After the pass

1. Summarize **Pass / Pass (limitation) / Fail / Skip** per section.
2. Attach **screenshots** or **log snippets** for any **Fail** or **limitation**.
3. Update **`PARITY_BACKLOG.md`** *Bedrock / Geyser validation follow-up* with **date + outcome**.
4. If Java code changed for a confirmed bug: note commit hash + minimal rationale; otherwise **docs-only** update is enough.

---

## Related

- **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** — Java baseline
- **`SPRINT_81C_MANUAL_SMART_ACTION_BAR_STUCK_FIX.md`** — smart idle reference
- **`PARITY_BACKLOG.md`** — product Bedrock requirements
- **`MENU_AND_COMMANDS_PORT.md`** — cross-platform acceptance
- **`PAPER_PARITY_PROGRAM.md`** (repo root) — § client / Bedrock where applicable

**Bedrock/Geyser not run in-repo:** this manual is for **operators** only.
