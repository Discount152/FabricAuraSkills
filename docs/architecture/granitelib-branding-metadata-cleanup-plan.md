# GraniteLib / DiscountAuraSkills — branding & metadata cleanup plan

| Field | Value |
|-------|--------|
| **Status** | **Proposed** (planning only — **not** implementation in Sprint 85) |
| **Date** | 2026-07-06 (Sprint 85) |
| **Prereq** | [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) (Sprint 84) · [granitelib-license-notice-review-plan.md](granitelib-license-notice-review-plan.md) (Sprint 83) |
| **Related** | [granitelib-distribution-adr.md](granitelib-distribution-adr.md) · [Sprint 85 handoff](../handoffs/2026-07-06-granitelib-sprint-85-branding-metadata-cleanup-plan.md) |

**Sprint 85 does not edit** `README.md`, `gradle.properties`, `fabric.mod.json`, or other **metadata** unless maintainers **explicitly** approve a **separate** implementation PR.

---

## 1. Purpose

Inventory **product-facing** surfaces that still read like the **upstream** **AuraSkills** / **Aurelium** **plugin** story, and plan an **ordered** cleanup so **Discount** / **GraniteLib** **identity** can shine **without** touching **legal** **attribution** (see §3).

**This is not** a license change and **not** permission to delete **vendor** files, **LICENSE** text, or **Javadoc** **upstream** **pointers** until [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) + **human** **sign-off**.

---

## 2. Current product-facing identity surfaces (audited, Sprint 85)

| Surface | Finding |
|---------|---------|
| **`README.md`** | Title **“Fabric AuraSkills”**; center text “port of **AuraSkills**”; badges → **Archy-X/AuraSkills** releases, **Maven** `dev.aurelium`, **Spiget**/Spigot resource; links to **aurelium.dev**, **wiki.aurelium.dev**, **Discord**; **About** describes **AuraSkills** **plugin**; **Building** clones **`github.com/Archy-X/AuraSkills.git`** (upstream, not this fork); long **API** **Bukkit** **Maven** snippet. **— (B) cleanup** when **factual** fork story is ready. |
| **`gradle.properties`** | **`releaseTitle=Fabric AuraSkills (MC 1.21.11)`**; **`repoBaseUrl=https://github.com/Archy-X/AuraSkills`**. **— (B)** if **canonical** repo URL is **elsewhere** (e.g. **Discount** fork). |
| **`fabric/src/main/resources/fabric.mod.json`** | **`id`:** `auraskills`; **`name`:** `Fabric AuraSkills`; **`description`:** “Fabric AuraSkills — dedicated server…”; **`authors`:** “Archyx (original), Fabric port”. **`license`:** `GPL-3.0` — **do not change** **for branding** (§3). **No** `homepage` / `sources` / `issues` keys in current file — optional **adds** later for **accurate** links. **— (B)** for **name**, **description**, **authors** **wording** **only** after **sign-off**. |
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

## 4. README cleanup candidates (future — not executed in Sprint 85)

| Area | Issue | Direction (examples — **sign-off** required) |
|------|--------|-----------------------------------------------|
| **Title / tagline** | Reads as **upstream** **product** | State **Fabric** **dedicated-server** build; name **DiscountAuraSkills** or **approved** **trademark** **string**. |
| **Badges** | Point to **Archy-X** releases / **Bukkit** API | Replace with **this** **repo** **releases** if **accurate**; **or** keep **one** **“Upstream”** badge **set** **with** **label**. |
| **Links** | **aurelium.dev** as **primary** **home** | **“**Documentation**”** **may** **link** **upstream** **wiki** **if** still **true**; **add** **fork** **README** / **Discord** **if** **different**. |
| **Clone URL** | **`Archy-X/AuraSkills.git`** | **Must** **be** **`Discount152/FabricAuraSkills`** (or **actual** **remote**) **for** **this** **repo** — **factual** **fix**, **not** **cosmetic** **only**. |
| **API** **section** | **Bukkit** **Maven** **coordinates** | **Label** as **upstream** **API** **or** **trim** **if** **Fabric** **port** **does** **not** **ship** **same** **API** **surface**. |

---

## 5. `gradle.properties` cleanup candidates

| Key | Current (Sprint 85) | Notes |
|-----|---------------------|--------|
| **`releaseTitle`** | `Fabric AuraSkills (MC 1.21.11)` | Align with **`fabric.mod.json` `name`** and **marketing** **decision** (e.g. **Discount** + **MC** **version**). |
| **`repoBaseUrl`** | `https://github.com/Archy-X/AuraSkills` | **If** **fork** **canonical** **is** **Discount152/FabricAuraSkills**, **update** **for** **release** **scripts**/**docs** **gen** — **verify** **CI** **consumers**. |

**Do not** change **`projectVersion`** or **`supportedMCVersions`** **for** **branding** **only**.

---

## 6. `fabric.mod.json` cleanup candidates (non-license fields only)

| Field | Current | Notes |
|-------|---------|--------|
| **`id`** | `auraskills` | **Mod** **ID** **change** **breaks** **worlds**/**depends** — **major** **decision**; **out of** **typical** **“**branding** **text**” **sprint**. |
| **`name`** | `Fabric AuraSkills` | **(B)** — **user-visible** **in** **launcher**. |
| **`description`** | “Fabric AuraSkills — …” | **(B)** — **mention** **GraniteLib** **JiJ** **if** **desired**; **keep** **accurate** **MC** **version**. |
| **`authors`** | Archyx + Fabric port | **(B)** — **add** **Discount** **maintainers** **as** **appropriate**; **do** **not** **remove** **“**original**” **without** **NOTICE**/**README** **attribution**. |
| **`license`** | `GPL-3.0` | **(A)** — **unchanged** **unless** **license** **changes**. |

**Optional** **future** **keys:** `links` / **Fabric** **metadata** **for** **homepage**, **sources**, **issues** — **point** to **this** **repo** **when** **ready**.

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

## 9. Recommended replacement language (draft — **not** applied)

Use **only** **after** **product** **+** **legal** **sign-off**:

- **Product** **name:** **DiscountAuraSkills** (or **approved** **string**) for **this** **Fabric** **fork**.
- **GraniteLib:** “**Embedded** **menu**/**definition** **library** (**`:granitelib-fabric`**, **`dev.granitelib.fabric`)** **Jar-in-Jar’d** **in** **the** **mod**.”
- **Lineage:** “**Derived** **from** **work** **by** **[upstream]**; **see** **Attribution** **/** **`NOTICE`**.”
- **Avoid:** **Implying** **this** **repo** **is** **the** **official** **Archy-X** **Fabric** **release** **unless** **true**.

---

## 10. Implementation staging (future)

| Stage | Scope | Guardrails |
|-------|--------|------------|
| **1** | **README** **product** **language**, **badges**, **clone** **URL** | **No** **LICENSE**/**vendor** **edits**; **factual** **accuracy** **first**. |
| **2** | **`gradle.properties`** **`releaseTitle`**, **`repoBaseUrl`** | **CI**/release **consumers** **checked**. |
| **3** | **`fabric.mod.json`** **non-license** **fields** | **`id`** **change** **only** **if** **explicit** **migration** **plan**; **`license`** **unchanged**. |
| **4** | **Changelog** **head** / **release** **notes** **template** | **History** **preserved**. |
| **5** | **Root** **`NOTICE`** **finalization** | [granitelib-notice-attribution-strategy.md §10](granitelib-notice-attribution-strategy.md#10-review-checklist-before-any-root-notice-is-added) |
| **6** | **Modrinth** / **CurseForge** / **GitHub** **About** | [granitelib-distribution-adr.md](granitelib-distribution-adr.md) **external** **prereqs** |

**Order** can **shift**; **do** **not** **merge** **branding** **with** **license** **file** **deletions**.

---

## 11. Review / sign-off checklist (before implementation PR)

- [ ] **Legal** / **maintainer:** **Apache** **(vendor/Slate)** **+** **GPL** **(root)** **story** **unchanged** **or** **documented** **in** **future** **`NOTICE`**.  
- [ ] **Product:** **canonical** **repo** **URL**, **mod** **name**, **mod** **id** **(if** **changing)** **decided**.  
- [ ] **README** clone URL matches the repository people actually clone (e.g. fork vs upstream).  
- [ ] **`fabric.mod.json` `license`** **stays** **`GPL-3.0`** **unless** **license** **project** **changes**.  
- [ ] **Changelog** **entry** **for** **user-visible** **name** **changes**.  
- [ ] **Re-run** **:fabric:build** **+** **`verifyFabricReleaseBundle`**.

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

## 14. Recommended Sprint 86

- **Implementation** **PR** **(one** **stage** **at** **a** **time):** **e.g.** **README** **factual** **fixes** **(clone** **URL**, **title)** **first**, **then** **`gradle.properties`**, **then** **`fabric.mod.json`** **strings** — **or** **wait** **for** **signed-off** **`NOTICE`** **before** **big** **user-facing** **renames**.  
- **Or** **legal:** **finalize** **`NOTICE`** **per** [granitelib-notice-attribution-strategy.md](granitelib-notice-attribution-strategy.md) **before** **wide** **metadata** **changes**.  
- **Or** **GraniteLib** **code** **port** **work** **unrelated** **to** **marketing**.

*Sprint 85: **plan** **+** **cross-links** **only**.*
