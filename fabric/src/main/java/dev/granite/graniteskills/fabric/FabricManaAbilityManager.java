package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.common.mana.ManaAbilityManager;
import dev.granite.graniteskills.common.user.User;
import org.jetbrains.annotations.NotNull;

public final class FabricManaAbilityManager extends ManaAbilityManager {

    private final FabricGraniteSkills plugin;

    public FabricManaAbilityManager(FabricGraniteSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    /**
     * Port Bukkit {@link dev.granite.graniteskills.bukkit.mana.BukkitManaAbilityManager#registerProviders()} here.
     * {@link FabricSharpHook} is wired via {@code ServerPlayerEntitySharpHookMixin} (not listed below).
     * {@link FabricChargedShot} (bow draw + {@code LivingEntity#damage} mixin for projectile hits; toggle on main-hand bow swing).
     * {@link FabricTerraform} chains horizontal same-block breaks via {@code PlayerBlockBreakEvents.BEFORE}.
     * {@link FabricAbsorption} uses {@code ServerLivingEntityEvents.ALLOW_DAMAGE} (shield ready: block attack only).
     * {@link FabricTreecapitator} — no WorldEdit wand exclusion (Bukkit {@code Treecapitator#materialMatches}).
     * Each provider, after a successful activation and mana consumption, should call
     * {@link FabricManaAbilityUseXpHandler#onManaAbilityActivated}.
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
        return manaAbility.getDescription(user.getLocale(), formatted);
    }
}
