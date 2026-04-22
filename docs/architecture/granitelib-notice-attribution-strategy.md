# GraniteLib / DiscountAuraSkills — draft NOTICE & attribution strategy

| Field | Value |
|-------|--------|
| **Status** | **Draft** for **human / legal** review — **not** legal advice, **not** final |
| **Date** | 2026-07-05 (Sprint 84) |
| **Prereq** | [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) (Sprint 83) |
| **Related** | [granitelib-distribution-adr.md](granitelib-distribution-adr.md) · [granitelib-branding-metadata-cleanup-plan.md](granitelib-branding-metadata-cleanup-plan.md) (Sprint 85–87) · [Sprint 86 handoff](../handoffs/2026-07-07-granitelib-sprint-86-readme-url-branding-cleanup.md) · [Sprint 87 handoff](../handoffs/2026-07-08-granitelib-sprint-87-fabric-mod-json-branding-cleanup.md) · [Sprint 84 handoff](../handoffs/2026-07-05-granitelib-sprint-84-notice-attribution-strategy.md) |

**Sprint 84 does not create a root `NOTICE` file** unless maintainers explicitly approve a follow-up implementation PR.

---

## 1. Purpose

Decide **where** legally and historically important **attribution** should live so **product** docs and **metadata** can eventually emphasize **GraniteLib** / **DiscountAuraSkills** without **deleting** required **breadcrumbs** from the wrong place.

**Two parallel tracks (must stay distinct):**

| **NOTICE / attribution** | **Product docs & metadata** |
|--------------------------|----------------------------|
| **Legal** / **history** preservation; may name **upstream** when required | **User-facing** identity; should converge on **current** product naming |
| **Stable** file(s) (e.g. future root `NOTICE`, `LICENSE*`, `vendor/.../LICENSE*`) | **README**, Modrinth, `fabric.mod.json` **name/description** — **cleanup candidates** *after* sign-off |
| **Can** mention AuraSkills, Slate, Archy-X, Isaac Montagne, etc. when **obligated** or **factual** | **Should not** send users to **wrong** “home” pages; **if** a link is **only** historical, it may move to **Attribution** / **History** section |

---

## 2. Current license / notice state (snapshot)

| Artifact | Status |
|----------|--------|
| **Root `LICENSE.md`** | **Present** — full **GNU GPL version 3** text (FSF). **Preserve.** |
| **Root `NOTICE` / `NOTICE.txt`** | **Not present** (Sprint 83/84) — **gap** this strategy addresses. |
| **`fabric/.../fabric.mod.json` `license` field** | **`GPL-3.0`** — must stay **consistent** with **shipped** mod license story until changed by **legal** + **build** review. **Preserve** field; **rewording** the **string** is out of scope for Sprint 84. **(Sprint 87:** `name` / `description` / `authors` / `contact` updated; **`license`** unchanged.) |
| **`vendor/Slate/LICENSE.md`** | **Apache License 2.0** (see file; includes **Copyright 2018-2021 Isaac Montagne** boilerplate at end). **Preserve**; this tree is **not** the same license as root GPL — **compatibility** of **distribution** is a **separate** review. |
| **`vendor/Slate/**` `*.java`** | Many per-file **Copyright** lines (e.g. **Isaac Montagne** for SmartInv-like inventory code, **deanveloper** for some utils). **Preserve** in source. |
| **`slate-fabric/.../GraniteFabricBootstrap.java`** | Javadoc references **Slate** upstream (`Archy-X/Slate`) for **porting** context. **Do not remove** in Sprint 84; **see** §7. |

**README / `gradle.properties` / `fabric.mod.json` (non-license fields):** **Product-facing**; **not** a substitute for a **root NOTICE**; see §6.

---

## 3. Candidate root `NOTICE` contents (future file — outline only)

When a **root `NOTICE` or `NOTICE.md`** is **created** (not in Sprint 84), it could contain **sections** along these lines:

1. **Project identity (current)**  
   - Short statement: this repository / product is maintained as **DiscountAuraSkills** / **FabricAuraSkills** / **GraniteLib** (use **exact** names **after** product decision).  
   - **Not** a replacement for `LICENSE.md` — **pointer**: “See `LICENSE.md` for GPL-3.0.”

2. **Primary license**  
   - “The project as a whole is distributed under the terms in **`LICENSE.md`** (GPL-3.0).”

3. **Vendored **Slate** (Bukkit) — `vendor/Slate/`**  
   - State that a **vendored** copy of **Slate** (or components) is included under **`vendor/Slate/`**.  
   - **Point to** **`vendor/Slate/LICENSE.md`** (Apache-2.0) **verbatim path**.  
   - Optionally summarize: **copyright** to **Isaac Montagne** and other **third-party** lines **as** in that file — **or** “see per-file copyright in `vendor/Slate/` and `vendor/Slate/LICENSE.md`” (human/legal chooses level of **detail**).

4. **Upstream lineage / inspiration (factual)**  
   - One paragraph: **AuraSkills** / **Aurelium** / **Archy-X** **lineage** if the **mod** remains a **port** or **derivative** of that work — **wording** must be **accurate** and **not** claim ownership of **third-party** marks.

5. **GraniteLib (embedded library)**  
   - State that **GraniteLib** code under **`slate-fabric/`** (and Java package `dev.granitelib.*`) is part of the same distribution, JiJ’d per build; **porting** references to **Slate** in **Javadoc** exist for **transparency** until **absorbed** into this notice.

6. **No trademark grant** (optional standard disclaimer)  
   - That third-party **names** are **property** of their owners, etc. (template language — **lawyer** to approve).

7. **List of “preserved” license files** (paths)  
   - e.g. `LICENSE.md`, `vendor/Slate/LICENSE.md`, any **other** `vendor/**/LICENSE*` added later.

**Format:** many projects use **plain** `NOTICE` (no extension) or **`NOTICE.md`**. **Compatibility** with **packaging** (Modrinth zip, **Maven**) should be **checked** when implementing.

---

## 4. Vendor / third-party attribution inventory (summary)

*See [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) §5 and §13 for the Sprint 83 pass.*

| Component | Path / signal | License / notice |
|-----------|----------------|------------------|
| **Slate (vendored)** | `vendor/Slate/**` | **`vendor/Slate/LICENSE.md` — Apache-2.0**; per-file **Copyright** in `inv/`, `util/`, etc. |
| **Root project** | `LICENSE.md` | **GPL-3.0** full text |
| **GraniteLib bootstrap** | `slate-fabric/.../GraniteFabricBootstrap.java` | Javadoc **link** to **Archy-X/Slate** (reference, not a license file) |
| **Future vendors** | TBD | Re-run inventory when new **`vendor/*`** is added; **append** to future `NOTICE` strategy |

**Apache vs GPL:** Shipping **Apache-2.0**-licensed **vendor** code **inside** a **GPL-3.0** project is a **well-known** combination scenario — **Sprint 84** only **records** that **two** different license **files** exist; **compatibility** for **your** **distribution** must be **confirmed** in a **legal** or **compliance** review before **public** “standalone” claims.

---

## 5. Project-origin / fork-lineage notes

- **`gradle.properties` `repoBaseUrl`** may still point to **`https://github.com/Archy-X/AuraSkills`** — **factual** if the **fork** **tracks** that **lineage**; **if** the **canonical** repo is **elsewhere**, **NOTICE** + **README** should **agree** after **decision** (out of scope for Sprint 84 **implementation**).  
- **Handoffs** in `docs/handoffs/` may describe **Sprint** history with **Slate/Granite** naming — **(B) historical** in [license review plan](granitelib-license-notice-review-plan.md#11-classification-buckets-expected-findings).  
- **Changelog** retains **historical** product names; **not** a **license** document.

---

## 6. What should remain in **source** comments (until reviewed)

- **`vendor/Slate/**`:** **All** **Copyright (c)** and **@license**-style lines **in place**.  
- **`GraniteFabricBootstrap`:** Javadoc to **Slate** **upstream** and **`MENU_AND_COMMANDS_PORT.md`** **until** a **signed-off** line in **`NOTICE` / README** “**Attribution**” **replaces** the **need** for **that** level of **inline** **link** (product choice).

---

## 7. What can **move** from product-facing docs into a future `NOTICE` (after review)

- **Repeated** long **URLs** to **aurelium.dev** / **Spigot** that exist **only** for **“where it came from”** could be **condensed** into one **“Upstream**”** section in **README** **plus** a **pointer** to **`NOTICE`**.  
- **Duplicate** “this is a port of …” **paragraphs** across **README** and **wiki** could be **centralized** in **`NOTICE` + one** **README** subsection — **not** **deleted**, **moved** / **deduplicated** after **sign-off**.

---

## 8. What **cannot** be changed without human/legal review

- **Deletion** or **emptying** of **`LICENSE.md`**, **`vendor/Slate/LICENSE.md`**, or **bulk** removal of **vendor** **copyright** **headers**.  
- **Changing** **`fabric.mod.json` `license`** **value** without **aligned** **legal** **and** **release** process.  
- **Claiming** **exclusive** **ownership** of **Slate** / **SmartInvs**-derived code **without** **retaining** **attribution** required by **Apache-2.0** / other **licenses**.  
- **Replacing** **upstream** **links** in **README** with **misleading** **destinations** (fraud / confusion).

**Do-not-remove-yet (explicit — repeat of Sprint 83):**  
- **`LICENSE.md`** (root)  
- **`vendor/Slate/LICENSE.md`** and **`vendor/**` copyright headers** in **source**  
- **`fabric.mod.json` `license`:** `GPL-3.0` (preserve **field**; content **policy** TBD)  
- **`GraniteFabricBootstrap` Javadoc** **upstream** **Slate** **reference**  
- **README** **upstream** **references** (may be **moved**/**rewritten** **only** after **review** — **not** **deleted** **without** **replacement** **attribution** **elsewhere**)  
- **`gradle.properties` `repoBaseUrl`**, **`releaseTitle`** (same **rule** as README)

---

## 9. Draft `NOTICE` outline (copy-paste starter — **not** applied to disk)

```text
NOTICE — DiscountAuraSkills / FabricAuraSkills (draft — NOT INSTALLED)
--------------------------------------------------------------------
This file is a DRAFT for maintainer and legal review. It does not exist
at the repository root until explicitly added in a follow-up change.

1. This project is provided under the GNU General Public License v3.
   See the file LICENSE.md in this repository for the full license text.

2. Vendored third-party code:
   - Slate-related sources under vendor/Slate/ are subject to the terms
     in vendor/Slate/LICENSE.md (Apache License, Version 2.0) and to
     per-file copyright notices in that tree.

3. Upstream / lineage: [factual statement TBD by maintainers about
   relationship to Archy-X/AuraSkills and other upstreams.]

4. GraniteLib: The embedded library built from slate-fabric/ (Gradle
   :granitelib-fabric) uses the dev.granitelib.* Java package; porting
   context may refer to the Slate project in Javadoc. See [granitelib-*]
   docs under docs/architecture/.

5. Trademarks: [optional disclaimer TBD.]
```

**Sprint 84** leaves this as **documentation only** — **no** file created at repo root.

---

## 10. Review checklist (before any root `NOTICE` is added)

- [ ] **Legal** or **designated** **maintainer** has **read** this strategy + **`vendor/Slate/LICENSE.md`** + root **`LICENSE.md`**.  
- [ ] **Apache-2.0** **+** **GPL-3.0** **distribution** story is **acknowledged** (or **counsel** consulted).  
- [ ] **Product** **names** in **`NOTICE` draft** **match** **README** / **Modrinth** / **GitHub** **“about”** **after** **branding** **pass**.  
- [ ] **Per-file** **copyrights** in **`vendor/Slate`**: **not** **removed**; **NOTICE** **either** **summarizes** **or** **points** to **files**.  
- [ ] **CI** / **release** **zip** **includes** **`NOTICE`** (if **added**).  
- [ ] **Changelog** **entry** for **“**Add NOTICE**”** when **shipped**.

---

## 11. Non-goals (Sprint 84)

- **No** new **`NOTICE`** at **repository** **root** **unless** **explicitly** **approved** **after** this **draft**.  
- **No** **deletion** of **licenses** / **attribution** / **links**; **no** **vendor** file **edits**; **no** **Java** / **Gradle** / **YAML** / **`fabric.mod.json`** / **metadata** **edits** in the **Sprint 84** **doc** **deliverable**.

---

## 12. Sprint 85 (what landed)

Sprint 85 did **not** add a root `NOTICE` (same guardrail as Sprint 84: **wait** for maintainer / legal sign-off on a **draft** before installation).

**What to use next for product-facing cleanup (docs/planning only in Sprint 85):**

- [granitelib-branding-metadata-cleanup-plan.md](granitelib-branding-metadata-cleanup-plan.md) — which README / `gradle.properties` / `fabric.mod.json` (non-license) / docs / Changelog surfaces can move to Discount / GraniteLib **wording** **later**, in **what order**, without treating **(A)** legal surfaces as “branding.”
- [Sprint 85 handoff](../handoffs/2026-07-06-granitelib-sprint-85-branding-metadata-cleanup-plan.md) — short summary, staging, and Sprint 86 ideas.

**When a root `NOTICE` is still the right follow-up** (separate PR from branding cleanup): use §9–10 in this file and [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) §10 before any file is added at the repository root.

*Sprint 84: strategy doc + cross-links only.*
