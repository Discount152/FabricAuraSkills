# GraniteLib / DiscountAuraSkills — license & notice review plan

| Field | Value |
|-------|--------|
| **Status** | **Proposed** (checklist; not legal advice) |
| **Date** | 2026-07-04 (Sprint 83) |
| **Scope** | Preparing for **branding / breadcrumb cleanup** and a possible **public** or **external** GraniteLib story **without** removing legally required text |
| **Related** | [granitelib-distribution-adr.md](granitelib-distribution-adr.md) · [granitelib-standalone-extraction-checklist.md](granitelib-standalone-extraction-checklist.md) · [granitelib-package-name-adr.md](granitelib-package-name-adr.md) · [Sprint 83 handoff](../handoffs/2026-07-04-granitelib-sprint-83-license-notice-review-plan.md) |

---

## 1. Purpose

Answer: **What must be audited (and by whom) before** we aggressively remove **AuraSkills / Slate / Aurelium** *breadcrumbs* from docs and metadata, or **publish** GraniteLib as a standalone artifact?

This file is a **repo-local plan and inventory**. It does **not** replace counsel where licenses conflict or org policy requires a formal notice file.

---

## 2. Current project state (snapshot)

- **Mod / product:** Still positioned as a **Fabric** port of the AuraSkills ecosystem; **`fabric.mod.json`** id **`auraskills`**, name **Fabric AuraSkills**; `gradle.properties` includes **`releaseTitle`**, **`repoBaseUrl`** → `Archy-X/AuraSkills` (see audit).
- **GraniteLib:** **`:granitelib-fabric`**, package **`dev.granitelib.fabric`**, on-disk folder **`slate-fabric/`**; JiJ distribution per Milestone D ADR.
- **Primary license file:** root **`LICENSE.md`** — full **GPL-3.0** text (FSF).
- **Root `NOTICE`:** **N/A** in this tree (no `NOTICE` / `NOTICE.txt` at repo root in Sprint 83 audit) — *evaluate* whether a **NOTICE** aggregating third-party / vendor obligations is required when distributing combined works.

---

## 3. Why license review matters

- **Branding cleanup** (AuraSkills → Discount / GraniteLib in user-visible strings) is **not** the same as **satisfying license and attribution** for **upstream, vendor, and transitive** code.
- **Removing** “Slate” or “AuraSkills” from **legal notices**, **required copyright lines**, or **license files** in **vendor** trees can create **compliance** and **reputation** risk.
- **External** distribution of GraniteLib (per [granitelib-distribution-adr.md](granitelib-distribution-adr.md)) needs **known** `group`/`artifact`, **clear** `NOTICE`, and **accurate** Modrinth/CurseForge/GitHub **origin** — not a silent swap of upstream URLs.

---

## 4. Files / locations to audit (checklist)

| Location | Notes (Sprint 83) |
|----------|-------------------|
| **Root** `LICENSE.md` | **Present** — GPL-3.0 text. **Do not remove or truncate**; any **additional** project copyright line (if added) is a **separate** maintainer decision. |
| **Root** `NOTICE` / `NOTICE.txt` | **N/A** — consider **adding** if policy requires third-party notices in one file. |
| **Root** `README.md` | **Present** — heavily **AuraSkills / Archy-X / aurelium.dev / Spigot**; **product-facing**; cleanup candidate for *wording* only after **legal/product** sign-off. |
| **`Changelog.md`** | Long history; may reference plugin, GPL, upstream; **historical** vs **head** sections. |
| **`gradle.properties`** | **`repoBaseUrl`**, **`releaseTitle`**, `supportedMCVersions` — **metadata / branding**; align with true repo home and product name when ready. |
| **`settings.gradle.kts`**, **root** `build.gradle.kts` | project **names**, comments; link to `slate-fabric/`. |
| **`fabric/build.gradle.kts`**, **`fabric/.../fabric.mod.json`** | **license** field **GPL-3.0**; **authors**; **id** `auraskills`; **entrypoint** class package. |
| **`slate-fabric/`** (GraniteLib) | e.g. **`GraniteFabricBootstrap`** Javadoc links to **Archy-X/Slate** (vendor **reference** — **attribution** to upstream Slate while porting). **Do not strip** without confirming replacement notice. |
| **`fabric/`** sources (excluding resources) | Package **`dev.aurelium.auraskills...`** = **product** identity; rename != license removal. |
| **`docs/`**, **`docs/handoffs/`** | Mix of **current** and **historical** sprint records. |
| **`resources/`** (pack.mcmeta, lang, pack icons) | Not exhaustively listed here — **re-scan** before **public** release. |
| **`vendor/Slate/`** | **Submodule** / vendored Bukkit **Slate**; includes **`vendor/Slate/LICENSE.md`** and per-file **copyright** (e.g. Isaac Montagne, deanveloper). **Heavily** **legal / attribution (A)**. |

---

## 5. Upstream / vendor reference inventory (high level)

*Non-exhaustive; re-run `rg` before any cleanup milestone.*

| Source | Where seen | License / notice |
|--------|------------|------------------|
| **FSF / GPL-3.0** text | `LICENSE.md` | Keep verbatim in repo. |
| **Archy-X / AuraSkills** (upstream) | `README`, badges, `gradle.properties` `repoBaseUrl`, handoffs, docs | **Distinguish** “fork points to X” (may stay true) vs “we are X” (must not misrepresent). |
| **Aurelium.dev / wiki / Discord** | `README` | **Product** links; update only with **approved** new home. |
| **Bukkit / Spigot / Paper** | Docs, `README` | Often **ecosystem** context; not necessarily removable without rewriting docs. |
| **Archy-X Slate** | `GraniteFabricBootstrap` Javadoc, `vendor/Slate` | **Vendor** code; `vendor/Slate/LICENSE.md` + file headers. |
| **Isaac Montagne / SmartInvs** (example) | Many files under `vendor/Slate/.../inv/` | **Copyright** lines in source — **preserve** per those files’ licenses. |

---

## 6. Product identity vs legal attribution (must stay separate)

| **Product / branding cleanup** (allowed only with process) | **Legal attribution** (do not remove casually) |
|----------------------------------------------------------------|-------------------------------------------------|
| User-visible **name**, mod listing text, “About” copy | **License** files, **SPDX** where used, **copyright** in **vendor** files |
| `fabric.mod.json` **name** / **description** (when product approves) | **Third-party** LICENSE blocks in **vendor** subdirs |
| README **badges** and **clone URLs** when repo is the source of truth | **Links to upstream** if license requires **attribution** or **credit** (verify per license) |
| Package renames (already **GraniteLib** in `dev.granitelib.fabric`) | **“Porting reference”** Javadoc to **Slate** until port is no longer derived / notice moved to **NOTICE** |

**Rule of thumb:** If it appears in a **`LICENSE`**, **`NOTICE`**, or a **per-file copyright** in **vendor** code, treat as **(A)** until a **human** confirms otherwise.

---

## 7. What may be removable **later** (only after review)

- **Outdated** marketing copy in **README** that **misstates** this repo’s relationship to **Discount** / **fork** (requires **factual** review, not only grep).
- **Redundant** duplicate upstream URLs **if** replaced by a **single** accurate “Upstream / attribution” section.
- **Internal** handoff phrasing that refers to old sprint codenames **if** handoffs are **archived** (optional; low priority).

**Never** “clean” by **deleting** `LICENSE.md` or `vendor/.../LICENSE.md` without replacement.

---

## 8. What must be **preserved until confirmed**

- All **root and vendor** license files and **in-file** copyright headers.
- **Accurate** statement of **derivative** or **port** relationship if the codebase **still** incorporates **upstream** or **vendor** work under obligations.
- **`fabric.mod.json` `license`** field consistency with **shipped** artifacts (e.g. **GPL-3.0** if that remains the mod license).
- **Modrinth/CurseForge/GitHub** metadata: **no** false **ownership**; **if** the listing must credit **original** author, keep it until policy says otherwise.

---

## 9. External publish prerequisites (GraniteLib standalone)

Before **publishing** GraniteLib as its own **Maven** or **Modrinth** artifact (per [granitelib-distribution-adr.md](granitelib-distribution-adr.md)):

1. **License compatibility** of **combined** distribution (mod + embedded JiJ + dependencies).
2. **NOTICE** (or equivalent) listing **obligations** for embedded and **vendored** code paths actually **shipped**.
3. **Locked** **package** and **Maven coordinates** in [granitelib-package-name-adr.md](granitelib-package-name-adr.md).
4. **Public** docs and **listings** that **do not** misrepresent **origin** or **trademark** use.
5. **Attribution** for any **Slate**/**AuraSkills**-derived code that remains in the **binary**, per the **actual** licenses (may require **retained** notices in JAR or **metadata**).

---

## 10. Recommended review process

1. **Static audit:** `rg` / IDE search with terms in §12; export or paste into a **private** review doc.
2. **Classify** each hit into **A–E** (§11).
3. **Legal / maintainer** pass on **(A)** and any **(C)** that is **user-visible**.
4. **Product** pass on **(C)** and **(D)** renames.
5. **Changelog** entry when **intentional** notice changes ship (separate from **rename** sprints).
6. **Re-run** when **`vendor/Slate`** is updated or **new** third-party code is **vendored**.

---

## 11. Classification buckets (expected findings)

| Bucket | Meaning | Default action |
|--------|---------|----------------|
| **A. Legal / license / attribution** | `LICENSE*`, `NOTICE`, `vendor/**/LICENSE*`, per-file **copyright** | **Preserve**; change only with **review** |
| **B. Historical internal docs / handoffs** | Sprint `docs/handoffs/*` | May **archive** or leave; not user-facing |
| **C. Product-facing docs / metadata** | `README`, `fabric.mod.json` strings, `gradle.properties` marketing fields | **Cleanup candidate** after sign-off |
| **D. Code identity** | `dev.aurelium.auraskill*` packages, `dev.granitelib.*` | **Rename** / **rebrand** per roadmap; not **license** deletion |
| **E. Vendor dependency names** | e.g. module coordinates in Gradle | **Preserve** as **names of real deps**; not “branding” to strip |

---

## 12. Search terms (for re-use)

`AuraSkills`, `Aurelium`, `Slate`, `slate` (case-sensitive where needed), `Archy-X`, `AureliumSkills`, `Modrinth`, `CurseForge`, `Spigot`, `Bukkit`, `Paper`, `plugin`, `LICENSE`, `NOTICE`, `copyright`, `GPL`, `aurelium.dev`

**Suggested commands (repo root):**  
`rg -i "auraskills|aurelium|slate|archy-x" --glob '!vendor/**' docs fabric slate-fabric gradle.properties *.md`  
`rg "Copyright|LICENSE|GPL" vendor/Slate` (vendor-heavy)

---

## 13. Preliminary findings (Sprint 83, non-exhaustive)

- **Root `LICENSE.md`:** full **GPL-3.0** text — **(A)**.
- **`README.md`:** **AuraSkills** / **Archy-X** / **aurelium.dev** / **Spigot** badges — **(C)** + **upstream** pointer; do not “clean” in a way that **implies** the fork is the **only** line of provenance without **factual** update.
- **`gradle.properties`:** `repoBaseUrl` → **Archy-X/AuraSkills**; `releaseTitle` **Fabric AuraSkills** — **(C)**.
- **`fabric.mod.json`:** **GPL-3.0**, **authors** “Archyx (original), Fabric port”, id **auraskills** — **(C)** for branding; **license** field is **(A)** compliance surface.
- **`slate-fabric/.../GraniteFabricBootstrap.java`:** Javadoc to **Archy-X/Slate** — **(A)**-leaning **attribution** for porting reference; keep until **NOTICE** or doc strategy absorbs it.
- **`vendor/Slate`:** many **Copyright** lines + `LICENSE.md` — **(A)**; **do not** bulk-delete.
- **No** root **`NOTICE`:** flag for **(future)** **aggregated** third-party notice if shipping a **combined** artifact publicly.

---

## 14. Risk register

| Risk | Mitigation |
|------|------------|
| Confuse **rebrand** with **license** removal | This plan + **bucket** (A) vs (C) |
| **Modrinth** / **CF** false **author** or **source** | Factual **metadata** pass before publish |
| **GPL** combination with **non-GPL** deps | Dependency / lawyer review for **standalone** GraniteLib JAR |
| Stale **README** after fork | Single **“Relationship to upstream”** section |

---

## 15. Non-goals (Sprint 83)

- **No** deletion of **license** files, **attribution** lines, or **required** **links** in this sprint’s deliverable.
- **No** Java, Gradle, YAML, or **`fabric.mod.json`** changes **as part of Sprint 83** (plan-only unless maintainers ask otherwise).

---

## 16. Recommended Sprint 84

- **Optional:** create a **root `NOTICE` draft** (human-reviewed) that lists **Slate**/**SmartInvs**/etc. with paths to **`vendor/.../LICENSE`**, *or*  
- **Product:** approved **README** / **fabric.mod.json** / **`gradle.properties`** string updates that **factualize** the **Discount** fork **without** dropping **(A)** content — as a **separate** “branding + legal review” implementation sprint, **not** mixed with refactors.  
- **Or** continue **GraniteLib** **feature** port work; re-run this checklist before **any** public **GraniteLib** **Maven** publish.

*Sprint 83: plan + inventory only.*
