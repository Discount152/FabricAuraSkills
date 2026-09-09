package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.level.LevelManager;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.loot.FabricBlockLootHandler;
import dev.aurelium.auraskills.fabric.loot.FabricMobLootHandler;
import dev.aurelium.auraskills.fabric.region.FabricPlacedBlockBreakHandler;
import dev.aurelium.auraskills.fabric.region.FabricRegionLifecycle;
import dev.aurelium.auraskills.fabric.requirement.FabricRequirementHandlers;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SourceType;
import dev.aurelium.auraskills.api.source.XpSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Registers event-based XP handlers. Fishing XP runs from {@link dev.aurelium.auraskills.fabric.mixin.FishingBobberEntityMixin}
     * at {@link net.minecraft.entity.projectile.FishingBobberEntity#use} RETURN via {@link FabricFishingXpHandler#onBobberUseReturn} (Sprint 136; no {@code PlayerEntity#increaseStat} hook).
     * (no explicit register call). Player config death resets use {@link FabricPlayerDeathHandler};
     * block break bonus loot / {@code override_vanilla_loot} use {@link FabricBlockLootHandler};
     * Bukkit {@code MobLootHandler} is {@link FabricMobLootHandler}; placed blocks / {@code check_replace} use
     * {@link dev.aurelium.auraskills.fabric.region.FabricRegionManager} and integrity follow-up
     * {@link dev.aurelium.auraskills.fabric.region.FabricRegionBlockIntegrity} (pistons / fall / tree / tall-support).
     * {@link FabricDoubleDrop#register} runs before {@link FabricPlacedBlockBreakHandler} so
     * {@code isPlacedBlock} is still set on break (Bukkit {@code DoubleDropTrait} vs {@code RegionBlockListener} order).
     */
    public void registerFabricXpHandlers() {
        FabricRegionLifecycle.register(plugin);
        FabricRequirementHandlers.register(plugin);
        FabricBrewingOwnerTracker.register();
        FabricBlockBreakPreCapture.register(plugin);
        FabricExternalBlockBreakXp.register(plugin);
        FabricBlockBreakHandler.register(plugin);
        FabricCropInteractionHarvestXp.register(plugin);
        FabricBlockInteractHandler.register(plugin);
        FabricEntityKillHandler.register(plugin);
        FabricMobLootHandler.register(plugin);
        FabricPlayerDeathHandler.register(plugin);
        FabricBlockLootHandler.register(plugin);
        FabricCombatXpHandler.register(plugin);
        FabricStatisticXpHandler.start(plugin);
        FabricJumpingXpHandler.register(plugin);
        FabricAgilityResolutionProof.emit(plugin);
        FabricDoubleDrop.register(plugin);
        FabricPlacedBlockBreakHandler.register(plugin);
    }

    @Override
    public void addXp(User user, Skill skill, @Nullable XpSource source, double amount) {
        if (user == null) {
            return;
        }
        ServerPlayerEntity player = plugin.getServer().getPlayerManager().getPlayer(user.getUuid());
        double beforeSkillXp = user.getSkillXp(skill);
        int beforeLevel = user.getSkillLevel(skill);
        if (player != null && source != null) {
            FabricXpDiagnostics.logDecision(
                    plugin,
                    "level_manager_addxp",
                    player,
                    skill,
                    source.getType(),
                    source.getId().toString(),
                    amount,
                    true,
                    "addxp_called");
        }
        super.addXp(user, skill, source, amount);
        if (FabricXpDiagnostics.isEnabled() && player != null && amount != 0) {
            SourceType diagType = source != null ? source.getType() : null;
            String sid = source != null ? source.getId().toString() : "-";
            double afterSkillXp = user.getSkillXp(skill);
            int afterLevel = user.getSkillLevel(skill);
            boolean mutated =
                    Math.abs(afterSkillXp - beforeSkillXp) > 1e-9 || afterLevel != beforeLevel;
            FabricXpDiagnostics.logAddXpMutation(
                    plugin,
                    player,
                    skill,
                    sid,
                    diagType,
                    amount,
                    beforeSkillXp,
                    afterSkillXp,
                    beforeLevel,
                    afterLevel,
                    mutated,
                    "-",
                    "-");
        }
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
