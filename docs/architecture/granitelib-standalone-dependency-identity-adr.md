# ADR: GraniteLib standalone dependency identity

| Field | Value |
|-------|--------|
| **Status** | **Proposed** (identity / roadmap decision — not implementation) |
| **Date** | 2026-07-09 (Sprint 88) |
| **Sprint** | 88 — planning / ADR only; **no** Gradle, **no** new `fabric.mod.json`, **no** Java |
| **Supersedes in spirit** | “**External later** as vague future” (see [granitelib-distribution-adr.md](granitelib-distribution-adr.md) Milestone D): standalone GraniteLib is now a **committed near-term product goal** with a **defined transition** |
| **Related** | [granitelib-distribution-adr.md](granitelib-distribution-adr.md) · [granitelib-v1-embed-contract.md](granitelib-v1-embed-contract.md) · [granitelib-standalone-extraction-checklist.md](granitelib-standalone-extraction-checklist.md) · [granitelib-standalone-build-metadata-plan.md](granitelib-standalone-build-metadata-plan.md) (Sprint 89) · [granitelib-package-name-adr.md](granitelib-package-name-adr.md) · [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) · [granitelib-verification-report.md](granitelib-verification-report.md) · [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) · [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) · [granitelib-branding-metadata-cleanup-plan.md](granitelib-branding-metadata-cleanup-plan.md) · [Sprint 87 handoff](../handoffs/2026-07-08-granitelib-sprint-87-fabric-mod-json-branding-cleanup.md) · [Sprint 88 handoff](../handoffs/2026-07-09-granitelib-sprint-88-standalone-dependency-identity-adr.md) · [Sprint 89 handoff](../handoffs/2026-07-10-granitelib-sprint-89-standalone-build-metadata-plan.md) |

**This document does not add build files, mod metadata, or published artifacts.** Implementation belongs in a follow-on sprint (recommended: **Sprint 89** — build/metadata implementation **plan** or first implementation PR per product).

---

## Current IDs (snapshot after Sprint 87)

| Item | Value |
|------|--------|
| **Gameplay mod ID** (`fabric.mod.json` for `:fabric`) | `auraskills` |
| **Gameplay mod display name** (after Sprint 87) | `FabricAuraSkills` |
| **Gameplay `projectVersion` / mod version** (monorepo) | `2.3.22` (see `gradle.properties`) |
| **GraniteLib Gradle module** | `:granitelib-fabric` |
| **GraniteLib on-disk directory** | `slate-fabric/` (physical rename still optional; see [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md)) |
| **GraniteLib Java package (bootstrap / v1 tree)** | `dev.granitelib.fabric` |
| **GraniteLib bootstrap class** | `GraniteFabricBootstrap` |
| **Nested JAR in release** (today) | `granitelib-fabric-*.jar` under `META-INF/jars/`; **`verifyFabricReleaseBundle`** enforces presence |
| **Proposed Fabric mod `id` for standalone GraniteLib** (this ADR) | **`granitelib`** (see § Mod ID) |

---

## Context

- **:granitelib-fabric** is a real Gradle module and ships **inside** the FabricAuraSkills remapped JAR via **Jar-in-Jar (JiJ)**. End users **download one mod JAR** today; GraniteLib is **not** a separate installable Fabric mod in release metadata.
- [granitelib-distribution-adr.md](granitelib-distribution-adr.md) (Milestone D) previously framed **“JiJ now, external later”** as the default, with **no firm deadline** for external.
- **Product goal (Sprint 88+):** GraniteLib **must** become a **separately downloadable** dependency **sooner rather than later** — public sharing by the owner, with **Modrinth / CurseForge** plausible **after** the library is a clean standalone artifact. This **raises priority** and **names** the standalone mod identity so implementation sprints are not ad hoc.
- **Sprint 83 / 84** (license, NOTICE strategy) and **vendor/Slate** attribution remain **prerequisites** before **marketing** a broad public “GraniteLib” library; this ADR does **not** waive that (see [Legal / attribution prerequisites](#legal--attribution-prerequisites)).

---

## Decision drivers

- **Ecosystem clarity:** Mod loaders and hosts key off **`id`**, **SemVer ranges**, and **`depends`**. A **stable, short** mod ID for the library avoids collisions and support confusion.
- **Separation of concerns:** **Gameplay** product (AuraSkills port) vs **library** (menu / layout / planning) should be **distinguishable** in metadata and docs.
- **Transition safety:** **JiJ** and **`verifyFabricReleaseBundle`** must **remain** during migration so existing releases and CI do not break mid-transition.
- **API honesty:** The library’s public surface is still **growing**; **initial** standalone versioning should **not** fake **1.0** stability.
- **Gameplay mod stability:** The **`auraskills`** mod ID is **long-lived**; changing it is **high blast radius** and **out of scope** for the GraniteLib standalone track (see [FabricAuraSkills mod ID](#8-fabricauraskills-gameplay-mod-id-unchanged-for-this-track)).

---

## Options considered (standalone mod `id`)

| Option | `id` | Pros | Cons / notes |
|--------|------|------|----------------|
| **A (recommended default)** | **`granitelib`** | Short; matches product name; not tied to AuraSkills/Slate; easy to type in `depends` | Must ensure no namespace collision on hosts (unlikely for a new mod). |
| **B** | `granite_lib` | Underscore style | Unusual for Fabric; longer; no strong benefit over **`granitelib`**. |
| **C** | `granitelib_fabric` | Screams “Fabric flavor” | Redundant if the artifact is **only** Fabric in this repo; may imply Neo/other splits later. |
| **D** | `discount_granitelib` | Tied to maintainer | Couples mod ID to org handle; rebrand/host moves become harder. |

**Recommendation (unless a concrete blocker is found, e.g. name collision on a target host):**

- **GraniteLib standalone Fabric mod `id`:** **`granitelib`**

---

## Display name, description, authors, contact

### Display name

| Option | Verdict |
|--------|---------|
| `GraniteLib` | **Recommended** — public display name. |
| `GraniteLib Fabric` | Optional subtitle context; not required in `name` if the description states Fabric. |
| `GraniteLib Menu Framework` | Use in **description** or marketing if scope is **clearly** menu/layout; may over-narrow as API grows. |

**Recommendation:** **`name` (display) = `GraniteLib`**. Use **description** and artifact naming for **Fabric**-specific context.

### Description (proposed for **future** standalone `fabric.mod.json` — not created in Sprint 88)

- **Primary (user-facing, concise):**  
  *A server-side menu definition and layout planning library for Fabric. Used by FabricAuraSkills; intended for other mods that embed GraniteLib-powered menus.*

- **More technical (alternative / longer):**  
  *A neutral Fabric menu definition and layout planning layer for mod menus — not a full client GUI framework.*

**Do not claim:** third-party adoption “everywhere,” or a **complete** GUI solution if the scope remains **server-side** menu/plan work.

### Authors and contact (future metadata)

- **`authors`:** **`Discount152`** (primary maintainer for standalone packaging). If policy requires, **add** or **retain** upstream attribution in **description** or **NOTICE** per [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) — not silently removed.
- **`contact` (homepage / sources / issues):** **Initially** the **existing** game/mod repository **if** GraniteLib is built from **[Discount152/FabricAuraSkills](https://github.com/Discount152/FabricAuraSkills)** and no separate public repo exists yet: same base URL, issues path. **If** a **future** split repo **`https://github.com/Discount152/GraniteLib`** (or another **real** URL) exists, **contact** and publish docs should **move** there in an implementation milestone — **do not invent** URLs in build files until they exist.

### Versioning direction (initial standalone)

| Option | Verdict |
|--------|---------|
| **A — Match FabricAuraSkills `2.3.x`** | **Not recommended** for first external lib line — conflates **gameplay** product version with **library** API stability. |
| **B — `0.1.0` or `0.1.0-alpha`** | **Recommended** for first public standalone line — **honest** pre-1.0 API; can diverge from `2.3.22` mod version. |
| **C — `1.0.0` day one** | **Not recommended** unless API freeze + review explicitly approve — risks **overstating** stability. |

**Recommendation:** **First published standalone GraniteLib** use **`0.1.0` or `0.1.0-alpha` SemVer** (or equivalent), **independent** of FabricAuraSkills **`projectVersion`**, with its **own** changelog line when implementation exists.

*Sprint 88 does not bump or split versions in Gradle — decision only.*

---

## Relationship to JiJ and the gameplay mod

### JiJ during transition

- **JiJ remains** (nested `granitelib-fabric-*.jar` + verification) **through** the transition while **standalone** metadata and **publish** paths are added. **Do not remove** JiJ in Sprint 88; **do not** remove `verifyFabricReleaseBundle` checks without a **replacement** plan.
- **End state (target):** players/server owners can **install GraniteLib as a separate mod**; FabricAuraSkills **declares** a **runtime** dependency on **`granitelib`**, not (only) a bundled copy.

### Phased transition (recommended)

| Phase | What happens |
|-------|----------------|
| **1** | **Keep JiJ**; add **designated** build output path + **standalone** `fabric.mod.json` (for `:granitelib-fabric` only) in an **implementation** sprint — *not* Sprint 88. |
| **2** | **Publish** standalone GraniteLib (owner-hosted and/or **Maven**-style) for download/testing; version **`0.1.x`**. |
| **3** | **FabricAuraSkills** `fabric.mod.json` gains **`"granitelib": ">=0.1.0"`** (or agreed range) under **`depends`**; implementation sprint wires **Loader**-visible dependency. **Still** may **JiJ** for one release for safety — product decides **overlap** length. |
| **4** | **Decide** whether to **drop JiJ** from the gameplay JAR, keep JiJ for **dev** builds only, or **ship one release** with both to flush migration issues. |

---

## 8. FabricAuraSkills gameplay mod ID (unchanged for this track)

- **Current `id`:** `auraskills` — **do not change** as part of the GraniteLib standalone identity work.
- **Rationale:** changing the mod id affects **configs**, **world data references**, **dependency** graphs, **logs**, and **user** muscle memory; it deserves a **dedicated migration ADR** and tooling if ever attempted.
- **Strawman future ids** (not decided, not implemented): `discountauraskills`, `graniteskills`, `fabricauraskills` — **documented only** for brainstorming.

---

## 9. Future `depends` direction (not edited in Sprint 88)

When standalone **`granitelib`** exists and is versioned:

```json
"depends": {
  "granitelib": ">=0.1.0"
}
```

Exact **minimum** and **inclusive** range are **TBD** at wire-up. **Sprint 88** does not edit **`fabric/src/main/resources/fabric.mod.json`**.

---

## 10. Build & repo shape (implementation — next sprints)

| Approach | Notes |
|----------|--------|
| **A** | Add a **dedicated** `fabric.mod.json` (and resources) under **`:granitelib-fabric`** for **standalone** remapped JAR output. |
| **B** | Add/adjust Gradle tasks: **remapped standalone** JAR for the library, distinct from the JiJ intermediate. |
| **C** | **Future** **split** to a second repository — **optional**; not required to **name** the mod id now. |
| **D (recommended default for next implementation)** | **Same monorepo**, **multi-module** as today: **publish** or **CI-artifact** GraniteLib **separately** from FabricAuraSkills while **optionally** keeping JiJ during overlap. |

**Sprint 88 recommendation:** **D + A** — same repo, **new** mod metadata in **`:granitelib-fabric`**, **parallel** with JiJ until migration completes.

---

## 11. Publishing path

- **First:** **Owner-hosted** releases (GitHub **Releases** or equivalent) and/or **Maven**-style **coordinates** as decided in implementation — **no** claim in Sprint 88 that **Modrinth**/**CurseForge** pages **exist** yet.
- **Later:** **Modrinth** / **CurseForge** when metadata, license, and support story are **ready**.

---

## Legal / attribution prerequisites

Before **widely** marketing or hosting standalone GraniteLib as a **public** “product”:

- Complete processes in [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) and [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md).
- **Preserve** `LICENSE*`, `vendor/Slate/**`, and **copyrights**; **root `NOTICE`** only after **signed-off** draft.
- **Product** docs and Modrinth text must **not** **erase** required **Slate**/**Apache** / **GPL** **attribution** (see Track A in [granitelib-branding-metadata-cleanup-plan.md](granitelib-branding-metadata-cleanup-plan.md)).

*Sprint 88 does **not** add **NOTICE** or change license files.*

---

## Recommendation (summary)

| Item | Value |
|------|--------|
| **GraniteLib standalone mod `id`** | **`granitelib`** (unless a **documented** blocker) |
| **Display name** | **`GraniteLib`** |
| **Initial standalone version line** | **`0.1.0` or `0.1.0-alpha`** (independent of gameplay `2.3.x` when first published) |
| **Description direction** | Server-side **menu / layout** library; used by **FabricAuraSkills**; avoid overclaiming |
| **Distribution** | **JiJ during transition**; **external** **required** mod dependency is the **end goal** |
| **FabricAuraSkills** | **Keep** mod **`id` `auraskills`** for this track; **later** add **`depends: granitelib`**. |

---

## Consequences

- **Loader graph:** two mods in `mods/` when migration completes — must **document** install order and **version** pins in README/server packs.
- **CI:** separate **verify** (or **extended** `verifyFabricReleaseBundle`) for **“standalone** GraniteLib + **dependant** FabricAuraSkills” — implementation detail for Sprint 89+.
- **SemVer:** **two** version lines (gameplay `2.3.x` vs library `0.1.x`) until/unless product **aligns** them explicitly.

---

## Non-goals (Sprint 88)

- **No** new **`fabric.mod.json`** for `:granitelib-fabric` in the repo in Sprint 88.
- **No** **Gradle** script changes, **no** **Java** / **YAML** / **runtime** changes.
- **No** **JiJ removal**; **no** **publishing**; **no** mod **`id`** change for `auraskills`.
- **No** **NOTICE** file creation; **no** **vendor** edits.

---

## Open questions

- **Exact** SemVer for **`0.1.0` vs `0.1.0-alpha.1`** on first **tag**.
- **Whether** to **align** or **diverge** `granitelib-fabric-*.jar` **file name** vs **mod `id`** string in Loader (today artifact uses **Gradle** naming).
- **NeoForge** or **other** loaders — **out of scope**; Fabric-only identity here.
- **When** to **end** **JiJ** overlap — product + telemetry on **user** confusion.

---

## Sprint 89 (build / metadata plan — **done**)

- Deliverable: [granitelib-standalone-build-metadata-plan.md](granitelib-standalone-build-metadata-plan.md) — *where* `fabric.mod.json` lives, *how* **`:granitelib-fabric`** becomes a **Loader-visible** mod (vs **today’s** **library** **JiJ** JAR), **version** split, **JiJ** / **`depends`**, **validation** matrix, **risks** (duplicate mod/class). [Handoff](../handoffs/2026-07-10-granitelib-sprint-89-standalone-build-metadata-plan.md).

## Recommended Sprint 90

- **Implement** per that plan: **`granitelibVersion`**, **`slate-fabric/.../fabric.mod.json`**, **resource** expansion, **optional** Loom **`mods { }`**, **remap** output as **distributable**; **no** `depends: granitelib` on **:fabric** until **valid** standalone JAR + **smoke** **(Runtime B/C)** as needed.

---

*Sprint 88: ADR + cross-links + handoff only. Sprint 89: build plan + cross-links + handoff only.*
