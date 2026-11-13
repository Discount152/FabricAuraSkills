package dev.granite.graniteskills.bukkit.antiafk;

import dev.granite.graniteskills.bukkit.antiafk.checks.*;
import dev.granite.graniteskills.common.antiafk.CheckType;

public enum BukkitCheckType implements CheckType {

    BLOCK_A(BlockA.class),
    DAMAGE_A(DamageA.class),
    DAMAGE_B(DamageB.class),
    DAMAGE_C(DamageC.class),
    ENTITY_A(EntityA.class),
    ENTITY_B(EntityB.class),
    ENTITY_C(EntityC.class),
    FISHING_A(FishingA.class);

    private final Class<? extends BukkitCheck> checkClass;

    BukkitCheckType(Class<? extends BukkitCheck> checkClass) {
        this.checkClass = checkClass;
    }

    @Override
    public Class<? extends BukkitCheck> getCheckClass() {
        return checkClass;
    }

}
