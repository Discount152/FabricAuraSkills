# GraniteLib Sprint 79 — JiJ vs external dependency (Milestone D ADR)

**Date:** 2026-06-30  
**Type:** **Documentation / decision only** (no Java, Gradle, YAML, `fabric.mod.json`, or publish)

## Objective

Record **Milestone D**: whether GraniteLib stays **Jar-in-Jar** inside DiscountAuraSkills, becomes an **external** required mod/library, or follows a **phased** approach — using an ADR the whole doc set can point to.

## ADR summary

- **Deliverable:** [granitelib-distribution-adr.md](../architecture/granitelib-distribution-adr.md) — **Proposed** status, 2026-06-30.
- **Current fact:** Upstream **embeds** a nested **`granitelib-fabric-*.jar`**; **`verifyFabricReleaseBundle`** requires that nested JAR; one-way graph **`:fabric` → `:granitelib-fabric`**.

## Options considered

| Option | One-line |
|--------|----------|
| **A** | JiJ only — current implementation. |
| **B** | External required dependency — future standalone mod/Maven. |
| **C** | **JiJ now, external later** — phased (recommended). |
| **D** | Both — deferred; high conflict risk. |

## Recommended distribution model

**Option C (JiJ now, external later).**

- Keeps **install simplicity** and the **working** build.
- Defer **external** until **package root, Maven group, public API, license/notice** are far enough along that a “library product” is not half-baked.
- **Does not** remove or relax JiJ in a doc-only sprint; external is a **future** migration with its own build/metadata/docs work.

## Consequences

- Product narrative: GraniteLib is **shipped with** FabricAuraSkills **for now**; standalone identity is **planned**, not current for consumers.
- Engineering later: a real external move **rewrites** dependency edges, metadata, `verifyFabricReleaseBundle` expectations, and user docs — tracked as a follow-on epic, not Sprint 79.

## Migration plan (when executed — not in Sprint 79)

See ADR “Migration plan” — prerequisites, build/publish, metadata, install docs, verification changes, and only then JiJ retirement if policy says so.

## Open questions (carried in ADR)

- SemVer for a future standalone GraniteLib line; exact metadata keys per host; compatibility matrix; whether Option D is ever wanted.

## Behavior preservation (Sprint 79)

- **No** build, runtime, or metadata changes. **JiJ remains** the enforced model in code.

## Guardrail ledger (preserved)

- No Java / Gradle / YAML edits in Sprint 79 deliverables.  
- No `fabric.mod.json` changes, no remove-JiJ, no publish, no new `:granitelib-platform`, no package/class/dir renames, no menu/ID/Phase1 behavior changes, no DTOs, no menuapi, no license stripping.

## Recommended Sprint 80

**Path A (user preference after distribution ADR):** **Package rename planning** — package root, public class strategy, and alignment with [granitelib-package-name-adr.md](../architecture/granitelib-package-name-adr.md) (no implementation required until product approves).  

**Path B:** If product wants external sooner, a **Sprint 80+** would scope prerequisites: Maven group, `fabric.mod.json` / Modrinst wiring, and CI — **after** ADR accepted and likely **after** naming clarity.

**Default suggestion:** **Sprint 80 — package / public API rename planning** (docs + ADR follow-ups) before investing in full external packaging.

**Done (Sprint 80, docs):** [2026-07-01-granitelib-sprint-80-package-public-class-rename-plan.md](2026-07-01-granitelib-sprint-80-package-public-class-rename-plan.md) and [granitelib-package-public-class-rename-plan.md](../architecture/granitelib-package-public-class-rename-plan.md).

## Files touched (Sprint 79)

| File | Change |
|------|--------|
| [granitelib-distribution-adr.md](../architecture/granitelib-distribution-adr.md) | **New** — full Milestone D ADR |
| [granitelib-standalone-extraction-checklist.md](../architecture/granitelib-standalone-extraction-checklist.md) | Pointer to ADR; distribution line updated |
| [granitelib-v1-embed-contract.md](../architecture/granitelib-v1-embed-contract.md) | See-also to distribution ADR |
| [granitelib-package-name-adr.md](../architecture/granitelib-package-name-adr.md) | Cross-link; JiJ row defers to distribution ADR |
| [granitelib-verification-report.md](../architecture/granitelib-verification-report.md) | Note + link to current JiJ/distribution story |
| [granitelib-sprint-77-implementation.md](../architecture/granitelib-sprint-77-implementation.md) | Related link to ADR + Sprint 79 handoff |
