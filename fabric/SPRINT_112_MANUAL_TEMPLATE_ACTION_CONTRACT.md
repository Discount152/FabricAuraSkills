# Sprint 112 manual -- Granite menu template action contract

**Artifact:** `FabricAuraSkills-2.3.139.jar` (with `granitelib-fabric`).

## What shipped

- **`GraniteTemplateActionNames` (GraniteLib):** `normalize(String)` (trim + `Locale.ROOT` lowercase, blank/null -> null), `isKnown(String)`, `allKnownNormalized()` (immutable set, 17 v0 actions).
- **`GraniteTemplateItems`:** `findFirstByAction` / `findAllByAction` matching normalized actions; grid discovery helpers delegate the scan branch to `findFirstByAction`.
- **`FabricGraniteMenuTemplateActionContract`:** For each bundled menu id (`stat_info`, `sources`, `level_progression`, `skills`, `stats`, `abilities`, `leaderboard`, `unclaimed_items`), maps **allowed** normalized actions (same inventory as bundled YAML) and requires each to appear on at least one item. Unknown menu id -> validation skipped (`Optional.empty()`). Optional helpers: `isNavigationNormalized`, `isRowOrPanelNormalized`.
- **Fabric template supports:** All eight `Fabric*TemplateSupport.tryOpenInner` methods call `validateTemplateActions` immediately after template id + rows checks; on failure: `logger.warn(menu + ": " + reason + "; using built-in menu"); return false;`.

## Operator notes

- Typos or experimental `action` strings on disk/bundled YAML that are **unknown** or **not allowed for that menu**, or **missing** a required action, force the **built-in Java** menu path for that open (same strict fallback style as other template failures).
- **Tests:** `FabricGraniteMenuTemplateActionContractTest` loads all eight bundled YAMLs via classpath + `GraniteMenuTemplateYamlLoader.loadString`; regression replaces `skill_row` with `skill_ro` in skills YAML and expects validation failure.

## Build

`gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build`
