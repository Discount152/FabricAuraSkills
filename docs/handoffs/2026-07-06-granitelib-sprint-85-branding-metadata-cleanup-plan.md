# GraniteLib Sprint 85 — Branding and metadata cleanup plan

**Date:** 2026-07-06  
**Type:** Documentation and planning only — no edits to `README.md`, `gradle.properties`, `fabric.mod.json`, `LICENSE*`, `vendor/`, or a new root `NOTICE` in this sprint.

## Plan summary

- **Primary deliverable:** [granitelib-branding-metadata-cleanup-plan.md](../architecture/granitelib-branding-metadata-cleanup-plan.md) — classifies **product-facing** strings (README, Gradle `releaseTitle` / `repoBaseUrl`, `fabric.mod.json` **non-license** fields, new Changelog lines, host listings) vs **(A)** legal/attribution surfaces, with **staging** (plan §10), a **sign-off** checklist (§11), and **Sprint 86** options (§14).
- **Relationship to Sprint 84:** [granitelib-notice-attribution-strategy.md](../architecture/granitelib-notice-attribution-strategy.md) is still the **where legal breadcrumbs live** story; Sprint 85 is **which labels can come off the front of the box** and in **what order**, without deleting attribution.

## Surfaces audited (headline)

- **README.md** — upstream-shaped title, badges, aurelium.dev / Spigot / Maven references, `git clone` URL pointing at `Archy-X/AuraSkills` (factual fork mismatch risk for this tree).
- **gradle.properties** — `releaseTitle`, `repoBaseUrl`.
- **fabric.mod.json** — `id`, `name`, `description`, `authors`; **`license: GPL-3.0`** treated as out of scope for *branding* cleanups.
- **Changelog.md**, **docs/** — noted in the plan (historical vs new-release wording).

## Key cleanup candidates (implementation later, after sign-off)

- **Factual fix:** README clone URL and “home” links should match the **canonical** repository and support story for **this** project when maintainers are ready.
- **Product strings:** move user-visible naming from “Fabric AuraSkills” / upstream-only framing toward agreed **Discount** / product naming in README title, `releaseTitle`, and `fabric.mod.json` `name` / `description` where not legally constrained.
- **`mod` id** `auraskills`: changing it is a **breaking** / migration decision — not a default part of a light branding pass.

## Do-not-touch legal surfaces (repeat)

- Root **`LICENSE.md`**, **`vendor/Slate/**`**, per-file **copyrights**, **`fabric.mod.json` `license`**, and **GraniteFabricBootstrap** Javadoc upstream pointers until a **signed-off** `NOTICE` / README “Legal & attribution” section absorbs the story.
- **No** root **`NOTICE`** file in Sprint 85.

## Replacement language (draft)

- See plan **§9** for example wording: **DiscountAuraSkills**, **GraniteLib** as embedded menu framework, **inspired by / derived from** historical AuraSkills only where **accurate** and after review; avoid overclaiming.

## Staging (from plan §10)

1. README and docs **product language** (highest visibility).  
2. **`gradle.properties`** `repoBaseUrl` / `releaseTitle`.  
3. **`fabric.mod.json`** **non-license** fields (not `license`; `id` only with a migration plan).  
4. Changelog **head** / release notes for **new** entries.  
5. Root **`NOTICE`** and attribution finalization **after** legal sign-off ([notice strategy](../architecture/granitelib-notice-attribution-strategy.md)).  
6. External listings (Modrinth, CurseForge, GitHub About) when publishing.

## Behavior preservation

- **No** product metadata file mutations in this sprint; **no** runtime, Gradle, Java, or YAML changes.

## Guardrail ledger

- No **license** deletions, no **vendor** edits, no **link stripping** for attribution, no **`NOTICE`** at root, no **`fabric.mod.json` `license`** change for branding.

## Validation

- Docs-only sprint. Optional: `.\gradlew.bat` `:granitelib-fabric:test`, `:fabric:test`, `:fabric:build -PfabricOnly=true` — expect **BUILD SUCCESSFUL**, **`verifyFabricReleaseBundle: OK`**, **FabricAuraSkills-2.3.20.jar** (or current `projectVersion`).

## Sprint 86 recommendation (pick one or sequence)

- **Small implementation PR:** README + correct clone/home URLs + title only, after plan §11 checklist.  
- **`NOTICE` PR** only if legal/maintainer sign-off exists for a draft.  
- **GraniteLib** feature work, with branding implementation deferred.  
- **Mod id** migration design if product chooses to rename the published mod id.

**Done:** this handoff + [granitelib-branding-metadata-cleanup-plan.md](../architecture/granitelib-branding-metadata-cleanup-plan.md).
