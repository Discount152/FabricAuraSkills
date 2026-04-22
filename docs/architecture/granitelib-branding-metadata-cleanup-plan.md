# GraniteLib / DiscountAuraSkills — branding & metadata cleanup plan

| Field | Value |
|-------|--------|
| **Status** | **Stages 1–3** **implemented** (Sprint 86: README + Gradle; [Sprint 87](../handoffs/2026-07-08-granitelib-sprint-87-fabric-mod-json-branding-cleanup.md): **`fabric.mod.json`** non-license + **`projectVersion` 2.3.22**). **Stage 4+** TBD. |
| **Date** | 2026-07-06 (Sprint 85) · 2026-07-07 (Sprint 86) · 2026-07-08 (Sprint 87) |
| **Prereq** | [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) (Sprint 84) · [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) (Sprint 83) |
| **Related** | [granitelib-distribution-adr.md](granitelib-distribution-adr.md) · [Sprint 85 handoff](../handoffs/2026-07-06-granitelib-sprint-85-branding-metadata-cleanup-plan.md) · [Sprint 86 handoff](../handoffs/2026-07-07-granitelib-sprint-86-readme-url-branding-cleanup.md) · [Sprint 87 handoff](../handoffs/2026-07-08-granitelib-sprint-87-fabric-mod-json-branding-cleanup.md) |

**Sprint 87** updated **`name`**, **`description`**, **`authors`**, and **`contact`**, and bumped **`projectVersion`**, without changing **`id`**, **`license`**, **`depends`**, **`entrypoints`**, **`mixins`**, or **`environment`**.

---

## 1. Purpose

Inventory **product-facing** surfaces that still read like the **upstream** **AuraSkills** / **Aurelium** **plugin** story, and plan an **ordered** cleanup so **Discount** / **GraniteLib** **identity** can shine **without** touching **legal** **attribution** (see §3).

**This is not** a license change and **not** permission to delete **vendor** files, **LICENSE** text, or **Javadoc** **upstream** **pointers** until [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) + **human** **sign-off**.

---

## 2. Current product-facing identity surfaces (audited, Sprint 85)

| Surface | Finding |
|---------|---------|
| **`README.md`** | **Sprint 86:** **FabricAuraSkills** title; **Discount152/FabricAuraSkills** clone and nav; **GraniteLib** JiJ called out; upstream **aurelium.dev** / wiki in **About** and **Upstream Bukkit API** as **lineage/reference**, not as this repo’s home. Hero **Spiget**/Bukkit Maven badges **removed** from main view. |
| **`gradle.properties`** | **Sprint 86+87:** **`releaseTitle`**, **`repoBaseUrl`** as in Sprint 86; **`projectVersion=2.3.22`** (Sprint 87). |
| **`fabric/src/main/resources/fabric.mod.json`** | **Sprint 87:** **`name`** `FabricAuraSkills`; **`description`** + **GraniteLib**; **`authors`** list **Archyx** (original) + **Discount152**; **`contact`:** homepage, sources, issues **→** this GitHub. **`id`** `auraskills`, **`license`** `GPL-3.0` **unchanged** (§3). |
| **`Changelog.md`** | Historical entries; **head** section can use **current** product naming in **future** releases without **rewriting** **old** entries. **— (C)** / **(B)** for **new** lines only by default. |
| **`docs/architecture/*`**, **`docs/handoffs/*`** | Mix: some links use **Discount152/FabricAuraSkills** GitHub; sprint text references **AuraSkills**/**Slate** for **history** — **(C)** or leave. |
| **`LICENSE.md`** | **GPL-3.0** full text — **(A)** **not** **branding**. |

**Output JAR name** (from Gradle): includes **FabricAuraSkills** — **product** naming; **align** with **`releaseTitle`** / **`fabric.mod.json`** **in** a **coordinated** **stage** (implementation **later**).

---

## 3. Legal / attribution surfaces — **not** “branding cleanup”

**Do not** treat these as optional **marketing** edits:

| Surface | Rule |
|---------|------|
| **`LICENSE.md`** (root), **`vendor/Slate/LICENSE.md`**, **`vendor/**` copyright **headers** | **(A)** — [license review plan](granitelib-license-notice-review-plan.md), [NOTICE strategy](granitelib-notice-attribution-strategy.md). |
| **`fabric.mod.json` `license`:** `GPL-3.0` | **(A)** — change **only** if **license** **policy** **changes**, not for **rebrand**. |
| **`GraniteFabricBootstrap` Javadoc** (Archy-X/Slate **link**) | **(A)/(E)** — **attribution** until **NOTICE** **absorbs** **lineage**. |
| **`repoBaseUrl`** | If it is the **factual** **upstream** **URL** for **lineage**, **moving** it may belong in **NOTICE** + **README** “**Upstream**” **first** — **not** silent **deletion**. |

---

## 4. README cleanup candidates (Sprint 86: first tranche done)

| Area | Issue | Direction (examples — **sign-off** required) |
|------|--------|-----------------------------------------------|
| **Title / tagline** | Reads as **upstream** **product** | State **Fabric** **dedicated-server** build; name **DiscountAuraSkills** or **approved** **trademark** **string**. |
| **Badges** | Point to **Archy-X** releases / **Bukkit** API | Replace with **this** **repo** **releases** if **accurate**; **or** keep **one** **“Upstream”** badge **set** **with** **label**. |
| **Links** | **aurelium.dev** as **primary** **home** | **“**Documentation**”** **may** **link** **upstream** **wiki** **if** still **true**; **add** **fork** **README** / **Discord** **if** **different**. |
| **Clone URL** | **`Archy-X/AuraSkills.git`** | **Must** **be** **`Discount152/FabricAuraSkills`** (or **actual** **remote**) **for** **this** **repo** — **factual** **fix**, **not** **cosmetic** **only**. |
| **API** **section** | **Bukkit** **Maven** **coordinates** | **Label** as **upstream** **API** **or** **trim** **if** **Fabric** **port** **does** **not** **ship** **same** **API** **surface**. — **Sprint 86:** trimmed to a **short** “Upstream Bukkit/Spigot API (reference)” **section**. |

---

## 5. `gradle.properties` cleanup candidates

| Key | Current (after Sprint 86) | Notes |
|-----|---------------------------|--------|
| **`releaseTitle`** | `FabricAuraSkills (MC 1.21.11)` | **Aligned** with **`fabric.mod.json` `name` `FabricAuraSkills`** (Sprint 87). |
| **`repoBaseUrl`** | `https://github.com/Discount152/FabricAuraSkills` | **Sprint 86** **applied** **fork** **canonical** URL. |

**`projectVersion`:** **2.3.20 → 2.3.21** (Sprint 86) → **2.3.22** (Sprint 87 **`fabric.mod.json`** + Changelog), each with a **Changelog** line.

---

## 6. `fabric.mod.json` cleanup candidates (non-license fields only)

| Field | After Sprint 87 | Notes |
|-------|-----------------|--------|
| **`id`** | `auraskills` | **Unchanged** — **mod** **ID** **migration** is a **separate** **decision**. |
| **`name`** | `FabricAuraSkills` | **Done** (launcher / mod list). |
| **`description`** | Fork-consistent; **GraniteLib**; **MC** version | **Done**. |
| **`authors`** | **Archyx** (original) + **Discount152** (Fabric) | **Done** — **no** **silent** **drop** of **upstream** **credit**. |
| **`contact`** | **homepage** / **sources** / **issues** | **Done** (Discount152/FabricAuraSkills). |
| **`license`** | `GPL-3.0` | **(A)** — **unchanged**. |

**Future** (optional): **`custom`**, **Modrinth**-specific **fields** on **publish** — **Stage** **6**; **`id`** only with **migration** **plan**.

---

## 7. Docs cleanup candidates

| Area | Notes |
|------|--------|
| **`docs/architecture/*`** | Already **mix** **FabricAuraSkills** **(GitHub)** **and** **DiscountAuraSkills** **wording**; **converge** **on** **one** **canonical** **name** **per** **section** **in** **implementation** **pass**. |
| **`docs/handoffs/*`** | **(C)** **historical** — **do** **not** **rewrite** **sprint** **history** **unless** **archiving** **policy** **says** **so**. |
| **`fabric/FABRIC_QUICKSTART.md`** (referenced in **description**) | **Re-audit** **when** **README** **changes** **for** **broken** **relative** **links** / **naming**. |

---

## 8. Links / URLs inventory (summary)

| URL / pattern | Role | Cleanup |
|---------------|------|---------|
| `https://github.com/Archy-X/AuraSkills` | Upstream **repo** | **README** **clone** **wrong** **for** **this** **tree**; **`repoBaseUrl`** **may** **need** **fork** **URL**. |
| `https://github.com/Discount152/FabricAuraSkills` | Used in **some** **docs** | **Preferred** **canonical** **for** **this** **fork** **where** **accurate**. |
| `aurelium.dev`, `wiki.aurelium.dev` | **Upstream** **docs** | **Keep** **as** **“**inspired** **by**/**derived**” **or** **remove** **from** **hero** **if** **misleading**. |
| `spigotmc.org/resources/81069` | **Plugin** **page** | **Fabric** **users** **may** **not** **apply** — **clarify** **or** **demote** **to** **lineage** **section**. |

---

## 9. Recommended replacement language (draft)

**Sprint 86** applied **factual** **README** / **`gradle.properties`** **wording** **without** **replacing** **NOTICE**-grade **attribution** (see [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md)). **Further** renames (e.g. mod listing strings) use **only** **after** **product** **+** **legal** **sign-off** where applicable:

- **Product** **name:** **DiscountAuraSkills** (or **approved** **string**) for **this** **Fabric** **fork**.
- **GraniteLib:** “**Embedded** **menu**/**definition** **library** (**`:granitelib-fabric`**, **`dev.granitelib.fabric`)** **Jar-in-Jar’d** **in** **the** **mod**.”
- **Lineage:** “**Derived** **from** **work** **by** **[upstream]**; **see** **Attribution** **/** **`NOTICE`**.”
- **Avoid:** **Implying** **this** **repo** **is** **the** **official** **Archy-X** **Fabric** **release** **unless** **true**.

---

## 10. Implementation staging

| Stage | Scope | Status / guardrails |
|-------|--------|----------------------|
| **1** | **README** **product** **language**, **badges**, **clone** **URL** | **Sprint 86** **done** — no **LICENSE**/**vendor** **edits**; **factual** **fork** **URL**; **upstream** **links** **labeled** **as** **lineage**/**API** **reference**. |
| **2** | **`gradle.properties`** **`releaseTitle`**, **`repoBaseUrl`** | **Sprint 86** **done**; **`projectVersion`** **→** **2.3.21** with **Changelog** **(see** **Sprint 86** **handoff).** |
| **3** | **`fabric.mod.json`** **non-license** **fields** | **Sprint 87** **done** — **`name`**, **`description`**, **`authors`**, **`contact`**; **`id`**, **`license`**, **`depends`**, **entrypoints**, **mixins** **unchanged**. **`projectVersion` 2.3.22**. |
| **4** | **Changelog** **head** / **release** **notes** **template** | **Ongoing** — **2.3.22** **entry** with **Sprint 87**. |
| **5** | **Root** **`NOTICE`** **finalization** | [granitelib-notice-attribution-strategy.md §10](granitelib-notice-attribution-strategy.md#10-review-checklist-before-any-root-notice-is-added) |
| **6** | **Modrinth** / **CurseForge** / **GitHub** **About** | [granitelib-distribution-adr.md](granitelib-distribution-adr.md) **external** **prereqs** |

**Order** can **shift**; **do** **not** **merge** **branding** **with** **license** **file** **deletions**.

---

## 11. Review / sign-off checklist (before **further** implementation PRs)

- [ ] **Legal** / **maintainer:** **Apache** **(vendor/Slate)** **+** **GPL** **(root)** **story** **unchanged** **or** **documented** **in** **future** **`NOTICE`**.  
- [ ] **Product:** **canonical** **repo** **URL**, **mod** **name**, **mod** **id** **(if** **changing)** **decided**.  
- [x] **README** clone URL matches the repository people actually clone — **Sprint 86** **→** **Discount152/FabricAuraSkills**.  
- [x] **`fabric.mod.json` `license`** **stays** **`GPL-3.0`** — **unchanged** in Sprints 86–87.  
- [x] **Changelog** **entry** for **branding** **metadata** / **version** **bump** — **2.3.21** (Sprint 86), **2.3.22** (Sprint 87).  
- [x] **Re-run** **:fabric:build** **+** **`verifyFabricReleaseBundle`** after **metadata** **changes** — see Sprint 86 and Sprint 87 handoffs.

---

## 12. Classification buckets (findings)

| Bucket | Meaning |
|--------|---------|
| **A** | **Legal** **attribution** — **LICENSE**, **`license`** **field**, **vendor** |
| **B** | **Product-facing** **metadata** — **README**, **`releaseTitle`**, **`name`**, **`description`** |
| **C** | **Historical** **handoffs** / **changelog** **body** |
| **D** | **Code** **packages** — **mostly** **separate** **from** **this** **plan** |
| **E** | **Vendor** / third-party names in dependencies or Javadoc |

---

## 13. Non-goals (Sprint 85)

- **No** edits to **README**, **`gradle.properties`**, **`fabric.mod.json`**, root **`LICENSE*`** files, or **`vendor/**`** in this sprint deliverable.  
- **No** **root** **`NOTICE`** **file** **creation**.  
- **No** **`fabric.mod.json` `license`** **value** **change** **for** **branding**.

---

## 14. Recommended Sprint 88

- **Root `NOTICE` PR** (after **signed-off** draft) — [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) §9–10 — **or** **external** **listings** / **Modrinth** **metadata** (Stage 6) when **publishing** **is** **in** **scope**.  
- **Or** **`mod` `id` migration** design (`auraskills` **→** **new** **id**) only if product accepts **save**/**depends** **break** **and** **has** a **communication** **plan** — **not** **default**.  
- **Or** **GraniteLib** / **port** **code** **work** with **branding** **on** **maintenance** **mode**.

*Sprint 85: plan. Sprint 86: README + `gradle.properties` + `2.3.21`. Sprint 87: `fabric.mod.json` + `2.3.22` ([handoff](../handoffs/2026-07-08-granitelib-sprint-87-fabric-mod-json-branding-cleanup.md)).*
