package dev.granite.graniteskills.fabric.antiafk;

import dev.granite.graniteskills.common.antiafk.Check;
import dev.granite.graniteskills.common.antiafk.CheckType;
import dev.granite.graniteskills.fabric.FabricAntiAfkManager;

public enum FabricCheckType implements CheckType {

    BLOCK_A(FabricAntiAfkManager.BlockA.class),
    DAMAGE_A(FabricAntiAfkManager.DamageA.class),
    DAMAGE_B(FabricAntiAfkManager.DamageB.class),
    DAMAGE_C(FabricAntiAfkManager.DamageC.class),
    ENTITY_A(FabricAntiAfkManager.EntityA.class),
    ENTITY_B(FabricAntiAfkManager.EntityB.class),
    ENTITY_C(FabricAntiAfkManager.EntityC.class),
    FISHING_A(FabricAntiAfkManager.FishingA.class);

    private final Class<? extends Check> checkClass;

    FabricCheckType(Class<? extends Check> checkClass) {
        this.checkClass = checkClass;
    }

    @Override
    public Class<? extends Check> getCheckClass() {
        return checkClass;
    }
}
