package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.antiafk.AntiAfkManager;
import dev.aurelium.auraskills.common.antiafk.Check;
import dev.aurelium.auraskills.common.antiafk.CheckType;
import dev.aurelium.auraskills.common.antiafk.LogLocation;
import dev.aurelium.auraskills.common.ref.PlayerRef;
import dev.aurelium.auraskills.common.region.BlockPosition;

import java.lang.reflect.Constructor;

public final class FabricAntiAfkManager extends AntiAfkManager {

    public FabricAntiAfkManager(AuraSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void registerCheckEvents(Check check) {
    }

    @Override
    public void unregisterCheckEvents(Check check) {
    }

    @Override
    public LogLocation getLogLocation(PlayerRef ref) {
        return new LogLocation(new BlockPosition(0, 0, 0), "");
    }

    @Override
    protected Constructor<?> getCheckConstructor(Class<? extends Check> checkClass) throws NoSuchMethodException {
        throw new NoSuchMethodException("Fabric MVP: no anti-AFK check implementations");
    }

    @Override
    public CheckType[] getCheckTypes() {
        return new CheckType[0];
    }
}
