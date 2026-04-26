# Sprint 95 — GraniteLib Path A Spike 0: template package layout + read-only YAML prototype

| Field | Value |
|--------|--------|
| **Type** | **Architecture / code spike** (minimal; no production menu swaps) |
| **Baseline** | Sprint **94** plan accepted; two-JAR policy (**93**); built-in menus remain **partials** |
| **Bedrock / Geyser** | **Unchanged — deferred / not tested** |

## Purpose

Establish the **GraniteLib** (`:granitelib-fabric`, on-disk `slate-fabric/`) home for **Slate-style menu templates**: immutable model, **v0 YAML** schema, read-only loader + validation, and tests — **without** migrating Skills/Stats/Level Progression or changing player-facing behavior.

## Chosen package layout

Existing GraniteLib code uses **`dev.granitelib.fabric`** (not `dev.aurelium.slate.fabric`). Spike 0 **follows that convention** — no global rename.

| Package | Role |
|---------|------|
| `dev.granitelib.fabric.template` | Spike umbrella (`package-info`) |
| `dev.granitelib.fabric.template.model` | Immutable records: menu + fill + item |
| `dev.granitelib.fabric.template.load` | YAML parse, validation, exceptions |

**Deferred (empty this sprint):** `template.context`, `template.action` as subpackages — reserved for Sprint **96+** when context registry and action dispatch exist.

## Model classes

| Type | Fields / notes |
|------|----------------|
| **`GraniteMenuTemplate`** | `id`, `title`, `rows` (1–6), `Optional<GraniteTemplateFill> fill`, `Map<String, GraniteTemplateItem> items`, `Optional<String> sourceLabel` |
| **`GraniteTemplateFill`** | `material`, `Optional<String> displayName` |
| **`GraniteTemplateItem`** | `List<Integer> slots`, `material`, `Optional<String> displayName`, `List<String> lore`, `OptionalInt amount`, `OptionalInt customModelData`, `Optional<String> action` (placeholder string only) |

Templates are **immutable** (`Map.copyOf`, `List.copyOf` in records). **No** `ItemStack` resolution — `material` is a **string** for now.

## YAML schema v0

Documented example (also used in tests):

```yaml
id: sources
title: "Sources"
rows: 6
fill:
  material: minecraft:black_stained_glass_pane
  name: " "
items:
  back:
    material: minecraft:arrow
    slots: [45]
    name: "Back"
    lore:
      - "Return to the previous menu"
    action: back
  sort:
    material: minecraft:hopper
    slots: [49]
    name: "Sort"
    action: cycle_sort
```

**Rules:**

- **Required:** `id`, `title`, `rows` (integer **1–6** inclusive).
- **`fill`:** optional mapping; if present, **`material`** required; **`name`** optional (display).
- **`items`:** optional mapping of **item id → item mapping**.
- **Per item:** **`material`** required; **`slots`** (list or single int) **or** **`slot`** (int) required.
- **Optional on item:** `name` or `display_name`, `lore` (list or single string), `amount` (≥ 1), `custom_model_data` (≥ 0), `action` (string, not executed in Spike 0).

**Not** Slate-compatible YAML yet — intentional v0 subset.

## Loader behavior

- **Entry points:** `GraniteMenuTemplateYamlLoader#loadString`, `#loadPath`, `#loadReader`.
- **Library:** **SnakeYAML** `2.3`, declared as **`implementation` + `include(...)`** (JiJ) on **`granitelib-fabric`** so the standalone GraniteLib jar resolves YAML at runtime.
- **Validation:** collects **all** parse/field errors, then throws **`GraniteTemplateValidationException`** with **`errors()`** list.
- **Slot bounds:** for `rows` = R, valid slot indices **0 … (R×9 − 1)**.
- **No** mutation, **no** hot reload, **no** integration with `Fabric*MenuGui` in this sprint.

**Loom note:** `processIncludeJars` may log *`(2.3) is not valid semver`* for SnakeYAML — **benign**; build still succeeds.

## Test coverage

**Module:** `:granitelib-fabric` — `GraniteMenuTemplateYamlLoaderTest`:

- Minimal valid YAML (`id` / `title` / `rows`)
- `fill` + `items` + `lore` + `action`
- Multiple `slots`, singular `slot`
- `loadPath` from temp file
- Invalid `rows` (0, 7)
- Out-of-bounds slot
- Missing `id`, `title`, `rows`
- Root not a mapping
- Material string accepted without registry validation
- `custom_model_data` optional

**Commands run:**

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:test
.\gradlew -PfabricOnly=true :granitelib-fabric:build
.\gradlew -PfabricOnly=true :fabric:test
```

## Fallback philosophy (future wiring)

When templates drive real menus (post–Spike 0):

1. **Try** load + validate YAML template.
2. On **failure:** log **diagnostics** (path + `GraniteTemplateValidationException.errors()`).
3. **Fall back** to existing **built-in code** menu — **do not** crash the server.

Spike 0 **does not** wire this into `FabricAuraSkills`; it is **documented only**.

## Migration candidate comparison (Sprint 96)

| Candidate | Pros | Cons |
|-----------|------|------|
| **sources** | Sort + paging; strong stress test for dynamic grids; high visibility | More moving parts first |
| **stat_info** | Smaller, mostly static layout; fewer interactions | Less representative of list menus |

**Recommendation for Sprint 96:** **`stat_info`** — first **template-driven** menu behind a feature flag or dev-only path, keeping **built-in `FabricStatInfoMenuGui`** as fallback. **sources** is the **next** candidate once stat_info proves loader + screen glue.

## Recommended Sprint 96

1. **Wire** `GraniteMenuTemplateYamlLoader` from a **resource or config path** (read-only at open).
2. **Map** v0 template → existing **6×9** population (reuse `GraniteContainerMenus`) for **stat_info** only.
3. **Fallback** to current code if YAML missing or invalid.
4. **Still no** full placeholder engine, **no** hot reload, **no** Bedrock certification.

## Explicitly not implemented (Sprint 95)

- Migrating **skills**, **stats**, **level_progression**
- Replacing built-in menus or changing click behavior
- Placeholder pipeline, full Slate actions, context registry
- Hot reload / `MenuFileManager` lifecycle
- Bedrock/Geyser validation
- Global package/module rename to “GraniteLib” branding

## References

- **`SPRINT_94_GRANITELIB_SLATE_PARITY_PLAN.md`** — Path A phases **A–H**
- **`PARITY_COMPLETION_ROADMAP.md`** — honest partial vs full Slate parity

---

**Definition of done:** layout + model + loader + tests + docs; `:granitelib-fabric:test` and `:fabric:test` **PASS**; **Slate parity not claimed complete**.
