# R8 armor ownership + automatic backup findings (2.3.240)

## Armor stack ownership — mitigated (confirmed by code)

`LivingEntityArmorRequirementMixin` cancels failed equip at HEAD. It restores via
`placeItemBackInInventory(stack.copy())` **only when** the rejected stack is no longer
in main/off hand (`ItemStack.isSameItemSameComponents`). If the caller still owns the
stack (inventory/shift-click paths), it does not duplicate.

Verdict: prior duplicate-restore defect addressed; remaining risk is equip-path coverage
on a real Java client (AC-CLIENT-JAVA still BLOCKED).

## Automatic backup failure/retry ordering — Fabric fixed; Bukkit still wrong

Fabric `FabricGraniteSkills.backupAutomatically()`:
1. `backupProvider.saveBackupSync(false)`
2. only then write `last_automatic_backup` + save meta.yml

So a failed backup does not advance the interval timestamp (can retry).

Bukkit `GraniteSkills.backupAutomatically()` still writes `last_automatic_backup`
**before** `saveBackupSync` — confirmed defect on Bukkit path, out of Fabric pair scope
unless Bukkit parity is required.

## Evidence pointers

- Mixin: `fabric/.../LivingEntityArmorRequirementMixin.java`
- Fabric backup: `FabricGraniteSkills.backupAutomatically` (after-success meta write)
- Bukkit backup: `bukkit/.../GraniteSkills.java` lines ~338–357 (before-success meta write)
