# GraniteLib Sprint 88 — Standalone dependency identity ADR

**Date:** 2026-07-09  
**Type:** **Documentation / decision** only — **no** Java, **no** Gradle, **no** `fabric.mod.json`, **no** publishing, **no** JiJ removal

## ADR summary

- **Deliverable:** [granitelib-standalone-dependency-identity-adr.md](../architecture/granitelib-standalone-dependency-identity-adr.md) — **Proposed** (2026-07-09).  
- **Hard product goal (locked):** GraniteLib becomes a **separately downloadable** Fabric mod dependency **soon** (owner-shared; Modrinth/CurseForge **later**).  
- **Roadmap shift:** [granitelib-distribution-adr.md](../architecture/granitelib-distribution-adr.md) Milestone D **“external later”** is now **in flight** — see **Sprint 88 supplement** in that file. **JiJ remains** through **transition**; **target** = **runtime** **`depends` on `granitelib`**.  
- **Non-goals:** No second `fabric.mod.json` in repo in Sprint 88; no version bumps; no NOTICE.

## Current IDs (post–Sprint 87)

| Item | Value |
|------|--------|
| **Gameplay mod `id`** | `auraskills` |
| **Gameplay display name** | `FabricAuraSkills` |
| **Gameplay `projectVersion`** | `2.3.22` |
| **GraniteLib Gradle module** | `:granitelib-fabric` |
| **GraniteLib Java package (v1)** | `dev.granitelib.fabric` |
| **Proposed standalone mod `id`** | **`granitelib`** (Fabric Loader) |

## Recommended GraniteLib identity (from ADR)

| Field | Recommendation |
|--------|----------------|
| **Mod `id`** | **`granitelib`** (unless a documented collision/blocker) |
| **Display `name`** | **`GraniteLib`** |
| **Initial standalone version line** | **`0.1.0` or `0.1.0-alpha`** (independent of gameplay `2.3.x` at first publish) |
| **Description direction** | Server-side **menu / layout / planning** library; used by FabricAuraSkills; avoid overclaiming third-party adoption |

## Dependency strategy

- **End state:** FabricAuraSkills declares **`"granitelib": ">=0.1.0"`** (range TBD at wire-up) in **`depends`**.  
- **During transition:** **keep JiJ** + verification; **overlap** allowed until **Phase 4** in ADR decides **drop** vs **dev-only** JiJ.

## JiJ transition stance

- **Phases 1–4** in ADR: **metadata/build path** → **publish standalone** → **`depends`** on gameplay mod → **decide** JiJ endgame.  
- **Sprint 88** does **not** remove **nested** `granitelib-fabric-*.jar` or **change** `verifyFabricReleaseBundle` behavior.

## FabricAuraSkills mod `id` stance

- **Keep** **`auraskills`** for this track. **No** `id` migration in the GraniteLib standalone work. **Future** ids (`discountauraskills`, etc.) = **separate** ADR if ever.

## Legal prerequisites (unchanged from Sprint 83/84)

- [granitelib-license-notice-review-plan.md](../architecture/granitelib-license-notice-review-plan.md) + [granitelib-notice-attribution-strategy.md](../architecture/granitelib-notice-attribution-strategy.md) **before** **broad** public “GraniteLib product” **claims**.  
- **No** Sprint 88 **NOTICE** file; **no** **vendor** edits.

## Behavior preservation

- **No** code or build changes — **expected** no runtime delta.

## Guardrail ledger

- As user Sprint 88 list: **no** Java, Gradle, `fabric.mod.json`, `id`/license/depends/entrypoint edits, **no** publish, **no** JiJ removal, **no** root NOTICE, **no** vendor, etc.

## Validation

- **Docs-only sprint.** Optional: `.\gradlew.bat :granitelib-fabric:test :fabric:test :fabric:build -PfabricOnly=true` — expect **unchanged** **2.3.22** **artifact** and **verifyFabricReleaseBundle: OK** (no file edits in Sprint 88).

## Sprint 89 recommendation

- **Sprint 89 — GraniteLib standalone build / metadata implementation plan** (or first implementation PR): where **`fabric.mod.json`** for **`:granitelib-fabric`** lives; how **remapped standalone JAR** is produced; **when** **`depends: granitelib`** lands on **`:fabric`**; **double-JAR** / Loader **validation** matrix.

**Follows:** [Sprint 87](2026-07-08-granitelib-sprint-87-fabric-mod-json-branding-cleanup.md).

**Done:** this handoff + [granitelib-standalone-dependency-identity-adr.md](../architecture/granitelib-standalone-dependency-identity-adr.md) + [granitelib-distribution-adr.md](../architecture/granitelib-distribution-adr.md) supplement + cross-links.
