# GraniteLib — renderer façade (design, **upstream**)

**Status:** Design / boundary doc. **Sprint 77** did **not** implement or move façade code; it only changed **Gradle** project id **`:granitelib-fabric`** (see [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md)).

## What stayed the same on upstream

- **Java** source paths and packages under **`slate-fabric/src/...`**. The façade concept (if/when built) would still be **slate-fabric** / GraniteLib **logic** in that tree, with **`fabric`** owning Minecraft rendering, stacks, and Phase1.
- **One** library module in the build: **`:granitelib-fabric`**. There is no **`:granitelib-platform`** on **this** `main` until a `slate-platform` (or equivalent) subproject is added (forks may differ).

## Fork / planning

Longer design prose and more modules may exist on other branches. When merging docs from forks, **do not** assume two GraniteLib modules exist in this repo.

## See also

- [granitelib-layout-definition-alignment.md](granitelib-layout-definition-alignment.md)  
- [granitelib-v1-embed-contract.md](granitelib-v1-embed-contract.md)

*Sprint 78: replaced one-line placeholder with upstream-scoped summary.*
