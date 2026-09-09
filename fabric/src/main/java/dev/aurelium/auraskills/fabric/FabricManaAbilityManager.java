package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.common.mana.ManaAbilityManager;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.menu.FabricManaAbilityDescPlaceholders;
import org.jetbrains.annotations.NotNull;

public final class FabricManaAbilityManager extends ManaAbilityManager {

    private final FabricAuraSkills plugin;

    public FabricManaAbilityManager(FabricAuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    /**
     * Port Bukkit {@link dev.aurelium.auraskills.bukkit.mana.BukkitManaAbilityManager#registerProviders()} here.
     * All listed providers (plus {@link FabricSharpHook} via {@code ServerPlayerEntitySharpHookMixin}) call
     * {@link FabricManaAbilityUseXpHandler#onManaAbilityActivated} after mana consumption, matching
     * {@code ManaAbilityUseLeveler} on {@code ManaAbilityActivateEvent} for their ability.
     * <ul>
     *   <li>{@link FabricTimerCountdown} — cooldown / error tick only, not an activation path.
     *   <li>{@link FabricAbsorption} — block-attacks while shield-ready, duration ability.
     *   <li>{@link FabricReplenish} — crop bone-meal
     *   <li>{@link FabricSpeedMine} — insta-break ore
     *   <li>{@link FabricTerraform} — linked horizontal break
     *   <li>{@link FabricTreecapitator} — tree (no Bukkit WorldEdit wand exclusion; see that class)
     *   <li>{@link FabricLightningBlade} — click attack
     *   <li>{@link FabricChargedShot} — bow shot after pull (damage mixin applies multiplier; XP on fire)
     * </ul>
     */
    public void registerProviders() {
        new FabricTimerCountdown(plugin);
        FabricAbsorption.register(plugin);
        FabricReplenish.register(plugin);
        FabricSpeedMine.register(plugin);
        FabricTerraform.register(plugin);
        FabricTreecapitator.register(plugin);
        FabricLightningBlade.register(plugin);
        FabricChargedShot.register(plugin);
    }

    @Override
    public void sendNotEnoughManaMessage(User user, double manaCost) {
        user.sendMessage("Not enough mana.");
    }

    @Override
    public @NotNull String getBaseDescription(ManaAbility manaAbility, User user, boolean formatted) {
        String desc = manaAbility.getDescription(user.getLocale(), formatted);
        return FabricManaAbilityDescPlaceholders.applyAbilitySpecificStatic(manaAbility, desc);
    }
}
