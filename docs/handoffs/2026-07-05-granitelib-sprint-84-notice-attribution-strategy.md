# GraniteLib Sprint 84 — Draft root NOTICE / attribution strategy

**Date:** 2026-07-05  
**Type:** **Documentation** — **no** `NOTICE` file at repo root in Sprint 84, **no** license/attribution deletions, **no** vendor or source edits

## Strategy summary

- **Deliverable:** [granitelib-notice-attribution-strategy.md](../architecture/granitelib-notice-attribution-strategy.md) — **Draft** (not legal advice).  
- **Goal:** a **shelf** for **attribution** (future root `NOTICE` **outline** + **inventory**) so **product** docs can later adopt **GraniteLib / Discount** **identity** without **deleting** **GPL/Apache** / **vendor** **obligations**.  
- **Key distinction:** **NOTICE** = **legal/history**; **README/Modrinth** = **product** (cleanup **after** sign-off).

## Inventory highlights

- **Root `LICENSE.md`:** **GPL-3.0** — keep.  
- **Root `NOTICE`:** still **absent**; **§3 + §9** of strategy doc = **draft** **outline** only.  
- **`vendor/Slate/LICENSE.md`:** **Apache-2.0** (Isaac Montagne **copyright** in boilerplate) — must **remain** **findable**; **summarize** or **link** in future **`NOTICE`**.  
- **`fabric.mod.json` `license`:** `GPL-3.0` — keep **field** until policy **changes** **elsewhere**.  
- **`GraniteFabricBootstrap` Javadoc** — keep **Slate** **upstream** **reference** until **NOTICE**/README **absorbs** the **lineage** story.

## Candidate NOTICE contents (abstract)

- **Pointer** to **`LICENSE.md`**; **vendored** `vendor/Slate/**` with **link** to **`vendor/Slate/LICENSE.md`**; **factual** **fork/lineage**; **optional** **trademark** disclaimer; **list** of **license** file **paths**.  
- **Not** **written** to disk in Sprint 84.

## Do-not-remove-yet list (repeat)

- `LICENSE.md` (root)  
- `vendor/Slate/LICENSE.md`, `vendor/**` **copyright** **headers**  
- `fabric.mod.json` **`license`:** `GPL-3.0`  
- `GraniteFabricBootstrap` Javadoc **attribution**  
- README / `gradle.properties` **upstream** **strings** — **reword**/**move** only **after** **review** (not **silent** **delete**)

## Review checklist

- See [granitelib-notice-attribution-strategy.md §10](../architecture/granitelib-notice-attribution-strategy.md#10-review-checklist-before-any-root-notice-is-added) before **any** real **`NOTICE` PR**.

## Behavior preservation

- **No** **file** **creates** **except** this **doc** + **handoff** and **arch** **cross-links** in **Sprint 84**.

## Guardrail ledger

- No **delete** **licenses**; no **vendor** **edits**; no **Java/Gradle/YAML**/**`fabric.mod.json`**; no **new** **root** **`NOTICE`**.

## Validation

- **Docs** only; **optional** Gradle: **2.3.20** build, **`verifyFabricReleaseBundle: OK`**.

## Sprint 85 recommendation

- **Add** **root** **`NOTICE`** (implementation PR) **if** **signed off**, **or** **branding** pass on **README**/**metadata** **coordinated** with this **strategy**, **or** **GraniteLib** **feature** work.

**Done:** this handoff + [granitelib-notice-attribution-strategy.md](../architecture/granitelib-notice-attribution-strategy.md).
