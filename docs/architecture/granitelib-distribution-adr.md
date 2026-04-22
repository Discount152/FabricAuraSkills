# ADR: GraniteLib distribution model

| Field | Value |
|-------|--------|
| **Status** | **Proposed** (awaiting product buy-in) |
| **Date** | 2026-06-30 |
| **Scope** | [FabricAuraSkills](https://github.com/Discount152/FabricAuraSkills) on upstream `main` after Sprint 77/78 (one module: **`:granitelib-fabric`**) |
| **Related** | [granitelib-v1-embed-contract.md](granitelib-v1-embed-contract.md) · [granitelib-package-name-adr.md](granitelib-package-name-adr.md) · [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) · [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md) · [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) · [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) · [granitelib-branding-metadata-cleanup-plan.md](granitelib-branding-metadata-cleanup-plan.md) (Sprint 85–87) · [Sprint 86 handoff](../handoffs/2026-07-07-granitelib-sprint-86-readme-url-branding-cleanup.md) · [Sprint 87 handoff](../handoffs/2026-07-08-granitelib-sprint-87-fabric-mod-json-branding-cleanup.md) · [granitelib-verification-report.md](granitelib-verification-report.md) · [Sprint 79 handoff](../handoffs/2026-06-30-granitelib-sprint-79-distribution-adr.md) · [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md) |

---

## Context

**Milestone D** is the product choice: how GraniteLib reaches consumers of DiscountAuraSkills (and, in the future, other mods).

**Current actual distribution model (upstream, Sprint 77+):**

- GraniteLib ships **only** as a **Jar-in-Jar (JiJ) nested JAR** inside the remapped **FabricAuraSkills** mod JAR: **`granitelib-fabric-*.jar`** under `META-INF/jars/`.
- **`:fabric` compileClasspath** includes **`project :granitelib-fabric`**; **`:granitelib-fabric`** has **no** reverse compile dependency on **`:fabric`**.
- There is **no** separate installable “GraniteLib” mod, **no** Maven publication of GraniteLib as a first-class product line on this repo to date, and **no** `fabric.mod.json` entry advertising GraniteLib as a standalone dependency to end users.
- **Java package names** for **`:granitelib-fabric`** are now **`dev.granitelib.fabric`** (Sprint 81); other product code may still use historical roots. **Gradle** module id is **`granitelib-fabric`**; physical directory is **`slate-fabric/`**.

**What `verifyFabricReleaseBundle` enforces (today):**

- The release verification task in **`fabric/build.gradle.kts`** (among other checks) **requires** a nested JAR whose name matches **`granitelib-fabric-*.jar`**, together with policy for `api-`, `common-`, Configurate 4.1.2, polyglot, and forbidden 4.2 — see the task block for the authoritative list. This encodes the **“GraniteLib must be embedded in the remapped JAR”** build contract for the current pipeline.

**This ADR does not change the build** — it records the intended **strategy** and **evolution** so that package rename, publishing, and docs stay aligned.

---

## Decision drivers

- **User experience:** minimize mistaken installs; single JAR to drop in `mods/` is simpler than a two-mod set.
- **Reusability:** other mods may eventually want Granitelib without bundling; duplicate embedded copies complicate the ecosystem.
- **Product maturity:** package root, Maven group, public API stability, and license/notice review are not finished — shipping an external “product” may be premature.
- **Support surface:** every external dependency is another version to explain on Modrinth/CurseForge and in issue templates.
- **Build reality:** the pipeline already **works** with JiJ + verification; external mode needs deliberate Gradle/metadata work later.

---

## Options considered

### Option A — Keep JiJ embedded (status quo in code)

| | |
|--|--|
| **Idea** | GraniteLib remains **only** bundled inside FabricAuraSkills; no separate mod to install. |
| **Pros** | Easiest for players and server owners; fewer wrong installs; **current build already works**; **`verifyFabricReleaseBundle`** enforces the nested JAR; no external dependency metadata required yet. |
| **Cons** | GraniteLib is not a **reusable installed mod**; **duplicate embedded copies** are possible if multiple mods each JiJ the same code; public “GraniteLib library” story stays tied to DiscountAuraSkills releases. |

**Verdict:** Accurate as **current implementation**; alone it does not describe **intentional evolution** toward standalone.

### Option B — External required dependency

| | |
|--|--|
| **Idea** | GraniteLib becomes a **separate required** mod (and/or published Maven coordinates consumed at build time as a `modImplementation`-style edge). |
| **Pros** | Clean standalone product identity; reusable by other mods; **separate versioning** possible; avoids duplicate full copies in every dependent mod. |
| **Cons** | Users and servers must **install another artifact**; **version skew** between DiscountAuraSkills and GraniteLib; Modrinth/CurseForge/dependency fields must be **precise**; **support and CI** get harder; build/publish pipeline **more complex**. |

**Verdict:** Desirable long-term for a real “library” product line — **after** API/package/licensing decisions, not as an immediate default on upstream as of Sprint 79.

### Option C — Hybrid: JiJ now, external later *(recommended default)*

| | |
|--|--|
| **Idea** | **Keep JiJ** for upstream releases; **do not remove** or weaken JiJ verification in a planning-only sprint. Plan **package rename**, **API surface**, and **publishing** so that a future sprint can add **external** distribution without a surprise pivot. |
| **Pros** | **Safest short-term**; preserves one-JAR install story; allows **stabilization** of names and API before a standalone release; does not block a later external mode. |
| **Cons** | **Temporary** “embedded now, might split later” messaging; a **real migration** will still be needed; avoid **overpromising** a standalone mod until prerequisites are met. |

**Verdict:** **Recommended** for upstream **unless** product explicitly chooses to fund external immediately.

### Option D — Support both JiJ and external

| | |
|--|--|
| **Idea** | Some consumers JiJ, others depend on an external JAR. |
| **Pros** | Flexible for different mod ecosystems. |
| **Cons** | **Classpath / version conflict** risk (two copies of the same code); **harder** support matrix; Fabric Loader and mod dependency graphs favor **one** clear story per release channel. **Unclear** value before GraniteLib is a well-versioned, published product. |

**Verdict:** **Defer**; revisit only with a strong compatibility story and testing.

---

## Recommendation

**Adopt Option C — JiJ now, external later** as the **default product direction** for GraniteLib on this repo, pending explicit approval.

**Reasoning (summary):**

- Upstream **already embeds** `granitelib-fabric` successfully; **`verifyFabricReleaseBundle`** is the gate.
- **Maven group, broader public API / SemVer, and license review** remain open per [granitelib-package-name-adr.md](granitelib-package-name-adr.md) — the **bootstrap** package rename (**Sprint 81**) does **not** by itself make an external library product ready. An **external “library product”** before publishing prereqs close would still be **premature**.
- **granitelib-fabric** should **not** be treated as a **fully independent consumer-facing product artifact** (with its own support tier) **until** those prerequisites close.
- **JiJ should remain the enforced distribution** for released DiscountAuraSkills builds **until** a deliberate “external phase” rewrites Gradle, metadata, and docs in a follow-on epic.

**Not a choice to “remove JiJ” in documentation** — the repo continues to require nested **`granitelib-fabric-*.jar`** for current releases. Moving to external is a **future** build and release change, not a Sprint 79 doc flip.

**Sprint 80 note:** **Package / class identity** and **publishing** are coupled — before external is implemented, follow [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) and close **package + license** items in [granitelib-package-name-adr.md](granitelib-package-name-adr.md).

---

## Consequences

| Area | If we stay with JiJ short-term (Option C) |
|------|------------------------------------------|
| **Players** | Keep **single-mod** install for FabricAuraSkills; no second mandatory download. |
| **Ecosystem** | Other mods that copy/embed similar code may still create **duplicates** — document as known risk; mitigation is **future** shared artifact + semver. |
| **Team** | **Install docs** for end users stay “install FabricAuraSkills”; no new Modrinth **required** dependency for GraniteLib yet. |
| **Build** | **`verifyFabricReleaseBundle` stays** the JiJ check; no Gradle change in this ADR. |
| **Comms** | Honest: GraniteLib is **versioned with the mod** today; a **future** split will be a **milestone** with migration notes. |

---

## Migration plan (when “external later” is executed — not now)

*Documentation-only preview; implementation is a later sprint.*

1. **Prerequisites:** package root and/or public API policy agreed; **Maven coordinates**; **license/notice** review; **SemVer** rules for a standalone line.
2. **Build:** add publish for GraniteLib; switch consumer edge from project + JiJ to `modImplementation` (or Loom equivalent) of published artifact; **or** offer two channels with strong testing — only if product selects Option D later.
3. **Metadata:** `fabric.mod.json` (and CurseForge/Modrinth) **depends** or **suggests** the GraniteLib mod ID as required.
4. **Docs:** install instructions (“install GraniteLib + FabricAuraSkills”); version compatibility table; troubleshooting for wrong versions.
5. **Verification:** replace or extend **`verifyFabricReleaseBundle`** so release artifacts match the new policy.
6. **Retire** redundant JiJ only when external path is default and supported — not before.

---

## Non-goals (Sprint 79 and this ADR)

- **No** Gradle, Java, YAML, or metadata edits as part of adopting this document.
- **No** removal of JiJ or of **`granitelib-fabric-*.jar`** checks.
- **No** publishing, Modrinth upload, or new mod IDs.
- **No** package or class renames in the sprint that only added the distribution ADR (see [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) for a later implementation milestone).

---

## Open questions

| Question | Notes |
|----------|--------|
| **What metadata files need edits for external?** | Typically `fabric.build.json` / `mods.toml` or `fabric.mod.json` `depends`, Modrinth/CurseForge “dependencies”, and CI release job inputs — **TBD** at implementation time. |
| **What install docs?** | Mod page body, server pack README, and optional “troubleshooting” for version pins — only when external ships. |
| **Versioning for external GraniteLib?** | Independent **SemVer** (or mod-version aligned) is a product decision; today GraniteLib is **coupled** to `projectVersion` in the JAR name inside the remapped mod. |
| **Risks if multiple mods embed?** | Duplicate classes / divergent patches; worst case mod load order or duplicate behavior. Mitigation: **one** published lib + dependency range. |
| **Risks if users must install separately?** | Wrong version, “missing dependency” reports, and support load — trade for reusability. |
| **Is `granitelib-fabric` a product yet?** | **Not** as a stand-alone shippable library with its own support story — **it is** the Gradle/artifact name for the embedded lib **inside** FabricAuraSkills. Re-evaluate when publish + API freeze land. |
| **Must JiJ stay until package rename and license review?** | **This ADR recommends yes** for **released** upstream artifacts until a deliberate switch — avoid half-external, half-embedded confusion. |
| **What should Sprint 80 do?** | **Done (docs):** [granitelib-package-public-class-rename-plan.md](granitelib-package-public-class-rename-plan.md) · [Sprint 80 handoff](../handoffs/2026-07-01-granitelib-sprint-80-package-public-class-rename-plan.md). |
| **Sprint 81 follow-up** | **Done (code + docs):** first **`:granitelib-fabric`** package/class rename — [Sprint 81 handoff](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md). **Further** port renames TBD. |
| **Sprint 82** | **Done (docs):** [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md) — **Proposed: defer** on-disk `slate-fabric/` → `granitelib-fabric/`; [Sprint 82 handoff](../handoffs/2026-07-03-granitelib-sprint-82-directory-rename-decision.md). |
| **Sprint 83** | **Done (docs):** [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) — license/notice **audit plan** (branding vs attribution); [Sprint 83 handoff](../handoffs/2026-07-04-granitelib-sprint-83-license-notice-review-plan.md). |
| **Sprint 84** | **Done (docs):** [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) — **draft** root `NOTICE` **outline** (no file at repo root); [Sprint 84 handoff](../handoffs/2026-07-05-granitelib-sprint-84-notice-attribution-strategy.md). |
| **Sprint 85** | **Done (docs):** [granitelib-branding-metadata-cleanup-plan.md](granitelib-branding-metadata-cleanup-plan.md) — product-facing metadata cleanup **plan** (README / Gradle / `fabric.mod.json` **non-license** / docs order); no metadata edits in the sprint; [Sprint 85 handoff](../handoffs/2026-07-06-granitelib-sprint-85-branding-metadata-cleanup-plan.md). |
| **Sprint 86** | **Done (product docs + `gradle.properties`):** README + clone/home URL + **`releaseTitle`/`repoBaseUrl`** + **`projectVersion` 2.3.21**; **`fabric.mod.json` not edited**; [Sprint 86 handoff](../handoffs/2026-07-07-granitelib-sprint-86-readme-url-branding-cleanup.md). |
| **Sprint 87** | **Done (`fabric.mod.json` non-`id`/`license` + version):** `name`, `description`, `authors`, **`contact`**; **`id`**, **`license`**, **`depends`**, **entrypoints**, **mixins**, **`environment`** unchanged; **`projectVersion` 2.3.22**; [Sprint 87 handoff](../handoffs/2026-07-08-granitelib-sprint-87-fabric-mod-json-branding-cleanup.md). |

### Embedded answers (index)

- **Current distribution model:** JiJ nested JAR, enforced by `verifyFabricReleaseBundle`.
- **For external later:** Gradle dependency edges, `fabric.mod.json` / platform metadata, publish pipeline, user-facing install docs, verification task updates, and SemVer for the lib line.

---

*Sprint 79: initial Milestone D ADR. Does not change build output. Sprint 85: linked branding / metadata cleanup plan (docs only). Sprint 86: README + Gradle + 2.3.21. Sprint 87: `fabric.mod.json` + 2.3.22.*
