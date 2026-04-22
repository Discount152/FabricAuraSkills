# GraniteLib Sprint 83 — License / notice review plan

**Date:** 2026-07-04  
**Type:** **Documentation / planning** (no license deletion, no Java/Gradle/metadata edits in Sprint 83 by default)

## Plan summary

- **Deliverable:** [granitelib-license-notice-review-plan.md](../architecture/granitelib-license-notice-review-plan.md) — **Proposed** checklist separating **(A) legal/attribution** from **(C) product-facing** copy; external-publish prerequisites; **`vendor/Slate`** called out; root **`NOTICE`** gap noted.
- **Goal:** support **breadcrumb / branding** cleanup and future **public** / **external** GraniteLib **without** throwing away **required** **license** text.

## Search terms used (examples)

`AuraSkills`, `Aurelium`, `Slate`, `Archy-X`, `Modrinth`, `CurseForge`, `Spigot`, `Bukkit`, `Paper`, `plugin`, `LICENSE`, `NOTICE`, `copyright`, and targeted `rg` for `vendor/Slate` copyrights.

## Classification buckets (A–E)

| Bucket | Short |
|--------|--------|
| **A** | License files, `vendor/**` headers, required attribution |
| **B** | Historical handoffs |
| **C** | README, `gradle.properties` marketing, `fabric.mod.json` user strings |
| **D** | Java package/class identity (e.g. `dev.aurelium.auraskills` mod vs `dev.granitelib`) |
| **E** | Real dependency / coordinate names (keep as facts) |

## Preliminary findings (see plan §13)

- Root **`LICENSE.md`** (GPL-3.0 text). **`README.md`**, **`gradle.properties`**, **`fabric.mod.json`** = strong **(C)** + upstream pointers. **`vendor/Slate`**: many **(A)** copyrights. **`GraniteFabricBootstrap`**: Javadoc to **Slate** repo — **(A)-leaning** port reference. **No** root **`NOTICE`**: evaluate later.

## What must not be removed yet

- **`LICENSE.md`**, **`vendor/Slate/**` including **`LICENSE.md`** and per-file **copyrights**, and **`fabric.mod.json` `license`** field integrity — until **review** and **replacement** strategy exists.

## Future cleanup (after human/legal pass)

- **(C)**: user-visible strings and metadata that **factualize** the **Discount** fork.  
- **(A)**: optional **`NOTICE`** aggregating **third-party**; **do not** delete **Slate**/**upstream** **credit** if licenses require it.

## Behavior preservation (Sprint 83)

- **No** runtime, build, or file **deletion** in the doc sprint.

## Guardrail ledger

- No removal of **licenses** / **attribution** / **old links** as part of *this* sprint’s intent; no Java/Gradle **changes**; no publish.

## Validation

- **Docs-only;** optional Gradle: expect **2.3.20** JAR, **`verifyFabricReleaseBundle: OK`**.

## Sprint 84 recommendation

- **`NOTICE` draft** or a **branding + legal sign-off** implementation sprint for **README** / **`fabric.mod.json`** / **`gradle.properties`**, **or** continue **GraniteLib** port; **re-audit** this plan before **standalone** **Maven**/host publish.

**Done:** this handoff + [granitelib-license-notice-review-plan.md](../architecture/granitelib-license-notice-review-plan.md).

**Next (Sprint 84):** [granitelib-notice-attribution-strategy.md](../architecture/granitelib-notice-attribution-strategy.md) · [2026-07-05-granitelib-sprint-84-notice-attribution-strategy.md](2026-07-05-granitelib-sprint-84-notice-attribution-strategy.md) — **draft** root `NOTICE` **outline** (no file at repo root in Sprint 84).
