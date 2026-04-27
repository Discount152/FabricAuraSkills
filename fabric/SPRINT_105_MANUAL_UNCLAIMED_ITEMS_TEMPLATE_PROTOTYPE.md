# Sprint 105 — Manual validation: GraniteLib template-backed `unclaimed_items` prototype

## Purpose

Prototype a bundled **v0 YAML** template for the **unclaimed_items** menu. This surface exercises **inventory mutation** via the existing safe merge helpers (**`FabricUnclaimedItemsMerge`**) while keeping **single-item claim**, **claim all**, and **paging** behavior identical to the built-in menu when the template path succeeds. **Not** full Slate / `MenuFileManager` parity.

## Template path

- **`assets/auraskills/granite/templates/unclaimed_items.yml`**
- **`FabricUnclaimedItemsTemplateSupport.TEMPLATE_RESOURCE_PATH`**

## Placeholders supported

| Placeholder | Meaning |
|-------------|---------|
| `{item_name}` | Display name string of the **rendered** stack (`ItemStack#getName#getString`). |
| `{item_amount}` | Stack count (string). |
| `{page}` | Current page (**1-based**). |
| `{total_pages}` | Total pages (**1-based**, `maxPageIndex + 1`). |

Local expander only — not a global registry. Unresolved `{...}` after expansion → template path aborts → **built-in** menu.

## Dynamic claimable item behavior

- Grid item: YAML key **`claimable`** or **`action: claim_item`**.
- **36** slots, **exactly** **`FabricUnclaimedItemsMenuGui.builtinClaimableSlots()`** (**0…35**).
- **Display:** **Real** registered item from **`FabricUnclaimedItemsMenuGui.baseStackForUnclaimedEntry`** (same registry path as built-in). Template **`material`** and **`name`** on **`claimable`** are **ignored** for the displayed row (only **lore** lines from YAML are merged in).
- **Lore:** Existing lore from the registry item is kept; if YAML defines lore, an empty line is inserted when there was prior lore, then each expanded YAML lore line is appended.
- **Unknown / invalid** keys: **barrier** placeholder — same as built-in.

## Claim single item

- Slot index **0…35** maps to global list index **`page * 36 + slot`**.
- Handler: **`FabricUnclaimedItemsMenuGui.handleUnclaimedClick`** → **`FabricUnclaimedItemsMerge.mergeOneAtListIndex`** (unchanged). **Inventory full** path: same message + close as built-in.

## Claim all

- **`claim_all`** control (**slot 49**) → **`doClaimAllAndRefresh`** / **`mergeAllInOrder`** (unchanged).

## Paging

- **Previous** **48** / **next** **50** only when **`maxPage > 0`**; stacks shown only when the control is active (same as built-in).

## Fallback behavior

Template path returns **`false`** (warn log, no crash) on: missing resource, invalid YAML, wrong **`id`** / **`rows`**, missing **`claimable`** definition, wrong slot count/order, missing **`previous`/`next`/`claim_all`** keys or wrong actions/slots, unknown static control **material**, unresolved placeholders, or runtime exception. **`openBuiltIn`** then runs.

## Safety notes

- **No** changes to **`FabricUnclaimedItemsMerge`**, **`FabricItemRegistry.tryMergeRegisteredItemCount`**, or unclaimed list storage format.
- **No** direct `UserState` mutation in template code beyond what merge helpers already do.
- Rows are **copies** for display; list mutations happen only inside **`FabricUnclaimedItemsMerge`**.

## Files changed (typical)

- `fabric/.../FabricUnclaimedItemsMenuGui.java`, **`FabricUnclaimedItemsTemplateSupport.java`**, **`FabricUnclaimedItemsPlaceholders.java`**
- `assets/auraskills/granite/templates/unclaimed_items.yml`
- `slate-fabric/.../GraniteTemplateActionNames.java`, `GraniteTemplateItems.java`, tests
- `Changelog.md`, `gradle.properties`, parity / menu port docs

## Tests run

```bat
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

## Java validation checklist (operator)

Use **disposable** unclaimed data only.

1. **`/claimitems`** when list non-empty — menu opens.
2. **`/openmenu unclaimed_items`** — same.
3. Click **one** claimable stack — item merges; list updates; no dupes/loss.
4. **`/claimitems all`** — merges in order; empty list closes / no-op as before.
5. With **>36** entries — **prev/next** work.
6. **Full inventory** — single claim shows **inventory full** behavior (close + message).
7. No raw **`auraskills.granite.*`** in visible item text from template path (nav items use plain YAML strings in bundle; claim rows use real item names + expanded lore).
8. Negative test: break YAML locally → **built-in** menu still opens.

## Known limitations

- Template **`title`** is expanded from YAML (default **English** string); built-in path still uses **`MenuMessage.UNCLAIMED_ITEMS_TITLE`** + formatting.
- No **disk** override, **hot reload**, or full Slate action engine.
- **`custom_model_data`** on **`claimable`** in YAML is **not** applied to real reward stacks (would fight “real item” display).
- **Bedrock / Geyser:** deferred — **not** tested this sprint.

## Recommended next sprint

**Disk / config-folder template loading** or **hot reload** — major bundled menus now have v0 template prototypes; next phase is **lifecycle** and **override**, not another menu port.
