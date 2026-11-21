# Hardened Armor Fabric port

## Problem
`Abilities.HARDENED_ARMOR` was registered on Fabric but never cancelled armor durability loss. Bukkit cancels `PlayerItemDamageEvent` for armor with chance `value/100`.

## Root cause
No Fabric durability intercept existed for humanoid armor slots.

## Fix
- `FabricMiningAbilities.shouldCancelArmorDamage` mirrors the Bukkit chance check and armor detection via `DataComponents.EQUIPPABLE`.
- `ItemStackHardenedArmorMixin` cancels `hurtAndBreak(..., EquipmentSlot)` for `HUMANOID_ARMOR` when the chance hits.

## Verify
Dedicated-server `hardened-armor-probe-result.txt`: unlock gate, non-armor gate, L1 3% / L2 6% scaling, mixin damage 0 on hit and 1 on miss. Jar `GraniteSkills-2.3.227.jar`.
