# R8 acceptance: matrix deps, provenance, armor, automatic backup

## Problem

`AC-MATRIX-FINAL.depends_on` listed 156 ids while 272 in-scope required cases existed (~116 missing). Several cases were PASS on helper or suite smoke (`MysqlSuiteProbe` for `AC-MIGRATION-SQL`, `remaining-ability-probe` for retrieval/repairing/splash). Ledger and state also implied 2.3.239 execution for evidence that was mostly collected on **2.3.237** disposable-server runs.

## Fix

- `tools/upd_acceptance_r8.py` sets `depends_on` to every `required_ids` entry except `AC-MATRIX-FINAL` and `EXCLUDED BY EXISTING SCOPE`.
- Reopened `AC-MIGRATION-SQL`, `AC-ABILITY-RETRIEVAL`, `AC-ABILITY-REPAIRING`, `AC-ABILITY-SPLASHER`, `AC-XP-JUMP` as **NOT STARTED** without inventing PASS; ordinary `AC-MYSQL-*` round-trip PASS rows kept.
- `acceptance-state.json` gains `applicability_assessment` for **GraniteSkills 2.3.240** + **Granitelib 0.2.0-alpha.21** vs historical freezes.
- `AC-MYSQL-FAIL` references `F:\gs-lifecycle-server\scripts\mysql-fail-hash.ps1`.

## Armor stack ownership (investigation)

**Files:** `LivingEntityArmorRequirementMixin.java`, `EquippableArmorRequirementMixin.java`, `ArmorSlotRequirementMixin.java`.

**Verdict (Fabric 2.3.240):** No confirmed “steal stack still in hand” defect. Primary gates block equip before the hand is emptied (`EquippableArmorRequirementMixin` / `ArmorSlotRequirementMixin`). `LivingEntityArmorRequirementMixin` cancels failing `setItemSlot` and only calls `placeItemBackInInventory(stack.copy())` when the stack is **not** still in main/off hand (`stillInHand` check). That targets right-click paths that already emptied the hand — not shift-click/inventory paths.

**Gap:** No dedicated client join probe on 2.3.240 for unequip/swap edge cases (ledger still lists client equip check).

## Automatic backup `last_automatic_backup` ordering

**Fabric:** `FabricGraniteSkills.backupAutomatically()` writes `last_automatic_backup` only **after** `backupProvider.saveBackupSync(false)` succeeds (`fabric/src/main/java/.../FabricGraniteSkills.java` ~L275–278). Failed runs log a warning and do not advance the timestamp — retry on next interval is possible.

**Bukkit:** `GraniteSkills.backupAutomatically()` still sets `last_automatic_backup` **before** `saveBackupSync` (~L353–356). Hypothesis only for Bukkit; R8 scope is Fabric freeze.

## Verify

```powershell
cd F:\FabricAuraSkills\fabricauraskills
python tools\upd_acceptance_r8.py
python -c "import json; d=json.load(open('.cursor/granite-completion/acceptance-state.json')); m=next(c for c in d['cases'] if c['id']=='AC-MATRIX-FINAL'); print(len(m['depends_on']), d['applicability_assessment']['matrix_depends_on_after'])"
```

Re-run `mysql-migration-probe`, ability `*-event-probe`, and `matrix-final-partial-probe` on **2.3.240 + alpha.21** before claiming PASS on reopened cases or MATRIX.