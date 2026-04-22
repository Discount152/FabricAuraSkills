# GraniteLib Sprint 80 — Package / public class rename plan

**Date:** 2026-07-01  
**Type:** **Documentation / planning only** (no Java, Gradle, YAML, or metadata renames in Sprint 80)

## Objective

After distribution ADR (Sprint 79), produce a **concrete, staged** plan for **Java package and public class identity** (GraniteLib vs historical Slate) so implementation can proceed **without** a monolithic rename.

## Deliverable

- [granitelib-package-public-class-rename-plan.md](../architecture/granitelib-package-public-class-rename-plan.md)

## Package rename inventory (summary)

- **`:granitelib-fabric`:** under `slate-fabric/src/main/java`, only **`dev.aurelium.slate.fabric`** (1 file). **No** `src/test/java` in this tree.
- **Not** in module: `…menu`, `…platform` subpackages; document when code lands and re-audit.
- **Target (illustrative):** e.g. **`dev.granitelib.fabric`**, pending org sign-off.

## Public class inventory (summary)

- **Found:** `SlateFabricBootstrap` (public) — candidate **`GraniteFabricBootstrap`** in Stage 3+.
- **N/A** on upstream (examples only / fork): `SlateFabricMenuRegistry`, `SlatePortManifest`, `FabricSlateGroupSlotMath`, `SlateMenuRuntime`, `AuraSkillsPackagedMenuIds` — re-audit if merged later.

## Recommended staging (summary)

1. Package moves inside **`slate-fabric`**.  
2. Consumer (`:fabric`, etc.) import updates.  
3. Public class renames.  
4. Docs / ADR / Javadoc.  
5. Optional physical `slate-fabric/` → `granitelib-fabric/`.  
6. License/notice + external-prep (ties to distribution ADR).

## Validation matrix (from plan)

- `:granitelib-fabric:test` · `:fabric:test` · `:fabric:build -PfabricOnly=true`  
- Dependency checks: no **`:granitelib-fabric` → `:fabric`** reverse edge.  
- **`verifyFabricReleaseBundle: OK`**, `granitelib-fabric-*.jar` nested.

**Do not** run **`:granitelib-platform`**.

## Behavior preservation (Sprint 80)

- **No** source, build, or behavior changes; planning docs and cross-links only.

## Guardrail ledger (preserved)

- No renames, no Gradle, no `fabric.mod.json`, no JiJ removal, no publish, no menu/ID/Phase1 changes, no menuapi, no DTOs, no license stripping, no removing references.

## Sprint 81 recommendation

- **Implement** Stages 1–3 for the **current** small `slate-fabric` tree **after** package root + public names are agreed.  
- **Or** re-audit the plan when a branch lands **substantive** `slate-fabric` code, then apply the same stages.

**Done:** this handoff + [granitelib-package-public-class-rename-plan.md](../architecture/granitelib-package-public-class-rename-plan.md). Cross-links added from ADRs and Sprint 77/78/79–related arch docs.

**Sprint 81 (implementation):** [2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md](2026-07-02-granitelib-sprint-81-package-bootstrap-rename.md) — Stages 1–3: **`dev.granitelib.fabric`**, **`GraniteFabricBootstrap`**, **`projectVersion` 2.3.20**.
