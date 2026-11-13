package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.level.LevelManager;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.requirement.FabricRequirementHandlers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 0.5f);
    }
}
