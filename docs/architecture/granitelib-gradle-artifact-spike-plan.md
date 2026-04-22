# GraniteLib — Gradle artifact **spike plan** (context for forks & history)

**On upstream `main` (post–Sprint 77):** the **Gradle** rename for the library is **already implemented** for **`:granitelib-fabric` only** — see [granitelib-sprint-77-implementation.md](granitelib-sprint-77-implementation.md). This file is **not** a second plan you must run on `main`.

## Why this document exists

- **Fork / planning branches** used a **formal spike** to validate Loom `namedElements`, JiJ, and `verifyFabricReleaseBundle` before adopting **`:granitelib-fabric`** and sometimes **`:granitelib-platform`**. That work often described **two** subprojects and a **larger** nested-JAR matrix.

## Upstream (Discount152) today

- **One** subproject: **`slate-fabric/`** with Gradle id **`:granitelib-fabric`**. **No** `slate-platform/`.
- The **“validation matrix”** in fork docs that lists **`:granitelib-platform:build`** and platform-specific `verify` rules is **not** runnable on upstream until that module exists.

## If you re-spike on a two-module fork

Use the same ideas: `projectDir` mapping, `namedElements`, JiJ `include`, update `requireJar` for each embedded lib JAR, prove **no** reverse `lib → :fabric` edge. *Copy matrix lines only to repos that have both projects.*

*Sprint 78: fork vs upstream clarified.*
