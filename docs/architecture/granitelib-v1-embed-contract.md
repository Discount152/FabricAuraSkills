# GraniteLib v1 — embed contract (upstream)

**Status:** Design / policy for **this** monorepo. **Not** a SemVer or standalone-publish guarantee.

## What “GraniteLib v1” means **here**

- The neutral menu / definition / plan work lives in Gradle module **`:granitelib-fabric`**, with sources under directory **`slate-fabric/`** (folder name **historical**; physical rename **deferred** by default per [granitelib-physical-directory-rename-decision.md](granitelib-physical-directory-rename-decision.md)) and Java package **`dev.granitelib.fabric`** for the current bootstrap (Sprint 81+). Further subpackages and types will appear as the port grows.
- The library is **not** published as a separate product line on upstream `main` today. It is **Jar-in-Jar’d** (embedded) inside the remapped **FabricAuraSkills** JAR: nested **`granitelib-fabric-*.jar`**, with **`verifyFabricReleaseBundle`** enforcing presence.
- **Sprint 77** did **not** change Java packages, classes, YAML, or gameplay behavior — only **Gradle** identity, JiJ, and verification. Runtime behavior of menus is unchanged by the module id alone.

## What is **not** in this repo’s embed story yet

- A second module **`:granitelib-platform`**: only appears when **`slate-platform/`** (or equivalent) is part of the build. *Some forks document two modules; that is not upstream today.*

## Relationship to a future standalone

When/if GraniteLib ships standalone, the split between **“library owns”** and **“mod owns”** in this doc is still the conceptual boundary; implementation details are in planning forks, the [package/identity ADR](granitelib-package-name-adr.md), the [rename plan](granitelib-package-public-class-rename-plan.md) (staged package/class work), the [license/notice plan](granitelib-license-notice-review-plan.md) (attribution vs branding), the [draft NOTICE strategy](granitelib-notice-attribution-strategy.md) (Sprint 84), the [branding & metadata cleanup plan](granitelib-branding-metadata-cleanup-plan.md) (Sprint 85 — product-facing strings, **not** `NOTICE`/vendor without sign-off), and the [distribution model ADR](granitelib-distribution-adr.md) (Milestone D — default recommendation: **JiJ now, external later** while naming/API mature).

## See also

- [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md)  
- [granitelib-renderer-facade.md](granitelib-renderer-facade.md) (design direction)  
- [Sprint 77 handoff](../handoffs/2026-06-28-granitelib-sprint-77-module-rename-implementation.md)  
- [Sprint 79 handoff — distribution ADR](../handoffs/2026-06-30-granitelib-sprint-79-distribution-adr.md)  
- [Sprint 80 handoff — rename plan](../handoffs/2026-07-01-granitelib-sprint-80-package-public-class-rename-plan.md)  
- [Sprint 81 handoff — package + bootstrap](../handoffs/2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md)  
- [Sprint 82 handoff — directory decision](../handoffs/2026-07-03-granitelib-sprint-82-directory-rename-decision.md)  
- [Sprint 83 handoff — license/notice plan](../handoffs/2026-07-04-granitelib-sprint-83-license-notice-review-plan.md)  
- [Sprint 84 handoff — NOTICE strategy](../handoffs/2026-07-05-granitelib-sprint-84-notice-attribution-strategy.md)  
- [Sprint 85 handoff — branding & metadata plan](../handoffs/2026-07-06-granitelib-sprint-85-branding-metadata-cleanup-plan.md)  
- [Sprint 86 handoff — README + repo URL branding](../handoffs/2026-07-07-granitelib-sprint-86-readme-url-branding-cleanup.md)  
- [Sprint 87 handoff — fabric.mod.json branding](../handoffs/2026-07-08-granitelib-sprint-87-fabric-mod-json-branding-cleanup.md)

*Sprint 78: placeholder replaced; embed story aligned with one-module upstream. Sprint 79: Milestone D ADR (JiJ first, external later as policy default). Sprint 80: package/public class rename plan (no code changes in that doc sprint). Sprint 81: `dev.granitelib.fabric` + `GraniteFabricBootstrap` in source. Sprint 82: physical directory rename decision (default defer). Sprint 83: license/notice review plan. Sprint 84: draft root NOTICE/attribution strategy (no new file at repo root). Sprint 85: branding & metadata inventory (planning). Sprint 86: README + `gradle.properties` fork URL/title; `2.3.21`. Sprint 87: `fabric.mod.json` non-license + `contact`; `2.3.22`; **`id`/`license`/deps/entrypoints** unchanged.*
