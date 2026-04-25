# Sprint 71 — manual validation — Unclaimed Items menu (Fabric)

**Build / version:** `projectVersion` **2.3.99** (see root `gradle.properties`).

## Bukkit reference (in-repo)

- **Class:** `bukkit/.../UnclaimedItemsMenu.java` (Slate `InventoryProvider`).
- **Shape:** `SmartInventory` **6×9** (54 container slots), **one stack per list index** in order (`user.getUnclaimedItems().get(slot)` for `slot` 0..53). **No YAML** in repo for this menu; title/lore from **`MenuMessage.UNCLAIMED_ITEMS_TITLE`** and **`MenuMessage.CLICK_TO_CLAIM`**.
- **Click:** `ItemUtils.addItemToInventory` (Bukkit) — on full failure, `MenuMessage.INVENTORY_FULL` and **close**; on partial, update `KeyIntPair` and **re-init**; on empty list, **close**.
- **Menu registration:** opened from `SkillsRootCommand#onClaimItems` when the list is non-empty (not via `MenuRegistrar` file load in the paths inspected for this sprint).

## Fabric implementation

- **GUI:** `FabricUnclaimedItemsMenuGui` + `GraniteContainerMenus#openClickable9x6` (read-only top grid + click handler).
- **Content:** rows 0–3 = **36** slots of display stacks (from `User#getUnclaimedItems` subList for the page). Row 4 unused (air). Row 5: **48** prev, **49** claim all, **50** next (when paging applies).
- **Merge logic:** `FabricUnclaimedItemsMerge#mergeAllInOrder` (Sprint 56 loop extracted) and `#mergeOneAtListIndex` for per-slot claim — both use **`FabricItemRegistry#tryMergeRegisteredItemCount`** (no remove unless merge succeeds or key invalid).
- **YAML:** **not** loaded; built-in only → **Partial** vs Slate/MenuFileManager.

## Commands tested (checklist)

| Root | Path | Notes |
|------|------|--------|
| (none) | `/claimitems`, `/claimitems all` | |
| | `/openmenu unclaimed_items`, `/openmenu unclaimed_items <player>` | **Self** in-game: first form. **Console:** second form only. |
| `/auraskills` | `... claimitems`, `... openmenu unclaimed_items …` | |
| `/skills`, `/sk`, `/skill` | same subcommands | **Sprint 65** shared tree |

**Permissions:** `auraskills.command.claimitems` (unchanged); `openmenu` → `auraskills.command.openmenu`.

## Player behavior

- **`/claimitems`:** if unclaimed is **empty** → same chat line as Bukkit `CLAIMITEMS_NO_ITEMS` (prefix + message). If **non-empty** → open menu.
- **`/claimitems all`:** one-shot merge + same feedback as Sprint 56 (all claimed / partial / inventory full).
- **Per-stack click** (menu): merges that entry; on **full** inventory for the whole stack → `MenuMessage.INVENTORY_FULL`, **close**; else **refresh** (page clamped). If list becomes **empty** → **close** (no empty GUI).
- **Claim all (slot 49):** runs `mergeAllInOrder`; if nothing left, **close**; else **refresh** on clamped page.

## Console / RCON

- **`/claimitems` / `all`:** **players only** (unchanged).
- **`/openmenu unclaimed_items`:** must specify **`<player>`** (online). Error key: `auraskills.granite.command.openmenu.unclaimed_console` when run from console without the self form.

## openmenu behavior

- **`/openmenu unclaimed_items`:** in-game self; empty list → feedback with no-items line (no GUI).
- **`/openmenu unclaimed_items <player>`:** opens **on the target** (same pattern as **skills**/**stats**). If GUI **throws** → merge on server + `claimitems` merge feedback (all / partial / full).

## Paging

- **36** items per page. After removing/claiming, **page is clamped** if the list shrinks (e.g. last item on a page removed).

## YAML / Slate limitations (explicit)

- No `unclaimed_items.yml` (none in reference inventory).
- No Slate **ClickableItem** / **InventoryManager** — Granite handler only.
- Bukkit can show up to **54** stacks on one page; Fabric uses **36** + **claim all** + paging.

## Java dedicated server — suggested steps

1. Register an item, grant overflow to unclaimed (`/item give` with full inv), or use existing test data.
2. Run `/claimitems` — menu opens; **click** one stack — inventory updates; unclaimed list updates.
3. Fill inventory; try one stack that **cannot** fit — **inventory full** line + **close**; unclaimed count unchanged for that key except partial merge cases.
4. **Claim all** with mixed space — remains in unclaimed for overflow (partial line on `/claimitems all` path).
5. **Paging:** add &gt;36 distinct unclaimed **entries** (or same id split — list order preserved); verify **prev/next** and **page title** `list_paging_subtitle`.
6. `/claimitems all` and `/openmenu unclaimed_items` (self + from console to another player).

## Bedrock / Geyser / Floodgate

- **Not run-time tested in this sprint.**
- **Risk:** inventory **click** / **container** screen parity varies on Geyser; treat as **NRP** — use **`/claimitems all`** or Java if clicks misbehave.
- **No claim** of Bedrock verification in release notes.

## Item-loss / duplication safety (review)

- [ ] No stack removed unless merge returned **0 not-fit** (full) or **partial** (`notFit < n`), or key **invalid** (pruned).
- [ ] `tryMergeRegisteredItemCount` is the only delivery path; overflow stays in `KeyIntPair` on partial.
- [ ] `mergeAll` never deletes without at least one item merged, except invalid keys (same as Sprint 56).
- [ ] No shift-click of top items (handler blocks top inventory moves in `GraniteClickable9x6Handler`).

## Remaining menu gaps (out of scope here)

- Full Slate, **MenuFileManager**, other menus (**leaderboard**, **abilities**), etc. — see **`MENU_AND_COMMANDS_PORT.md`**.

## Risks / caveats

- **Page size** differs from Bukkit (36 vs 54) — list order is preserved, navigation differs.
- **bedrock** untested; document **NRP** for production.

## Build

- `.\gradlew -PfabricOnly=true :fabric:test` (passing at sprint close).
