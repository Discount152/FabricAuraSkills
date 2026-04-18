package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.level.LevelManager;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.requirement.FabricRequirementHandlers;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.NotNull;

public final class FabricLevelManager extends LevelManager {

    private final FabricAuraSkills plugin;

    public FabricLevelManager(FabricAuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void loadXpRequirements() {
        new FabricXpRequirementsLoader(plugin, xpRequirements).load();
    }

    /**
     * Registers event-based XP handlers. Fishing uses {@link dev.aurelium.auraskills.fabric.mixin.FishingBobberEntityMixin}
     * (no explicit register call).
     */
    public void registerFabricXpHandlers() {
        FabricRequirementHandlers.register(plugin);
        FabricBrewingOwnerTracker.register();
        FabricBlockBreakHandler.register(plugin);
        FabricBlockInteractHandler.register(plugin);
        FabricEntityKillHandler.register(plugin);
        FabricCombatXpHandler.register(plugin);
        FabricStatisticXpHandler.start(plugin);
        FabricJumpingXpHandler.register(plugin);
    }

    @Override
    public void playLevelUpSound(@NotNull User user) {
        ServerPlayerEntity player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
    }
}
