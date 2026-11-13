package dev.granite.graniteskills.bukkit.mana;

import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.skills.archery.ChargedShot;
import dev.granite.graniteskills.bukkit.skills.defense.Absorption;
import dev.granite.graniteskills.bukkit.skills.excavation.Terraform;
import dev.granite.graniteskills.bukkit.skills.farming.Replenish;
import dev.granite.graniteskills.bukkit.skills.fighting.LightningBlade;
import dev.granite.graniteskills.bukkit.skills.fishing.SharpHook;
import dev.granite.graniteskills.bukkit.skills.foraging.Treecapitator;
import dev.granite.graniteskills.bukkit.skills.mining.SpeedMine;
import dev.granite.graniteskills.bukkit.user.BukkitUser;
import dev.granite.graniteskills.common.mana.ManaAbilityManager;
import dev.granite.graniteskills.common.message.type.ManaAbilityMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitManaAbilityManager extends ManaAbilityManager {

    private final GraniteSkills plugin;
    private final Map<Class<?>, ManaAbilityProvider> providerMap;

    public BukkitManaAbilityManager(GraniteSkills plugin) {
        super(plugin);
        this.plugin = plugin;
        this.providerMap = new ConcurrentHashMap<>();
    }

    public void registerProviders() {
        new TimerCountdown(plugin); // Start counting down cooldown and error timers
        registerProvider(new Replenish(plugin));
        registerProvider(new Treecapitator(plugin));
        registerProvider(new SpeedMine(plugin));
        registerProvider(new SharpHook(plugin));
        registerProvider(new Terraform(plugin));
        registerProvider(new ChargedShot(plugin));
        registerProvider(new Absorption(plugin));
        registerProvider(new LightningBlade(plugin));
    }

    private void registerProvider(ManaAbilityProvider provider) {
        providerMap.put(provider.getClass(), provider);
        Bukkit.getPluginManager().registerEvents(provider, plugin);
    }

    public <T extends ManaAbilityProvider> T getProvider(Class<T> clazz) {
        ManaAbilityProvider provider = providerMap.get(clazz);
        if (provider != null) {
            return clazz.cast(provider);
        }
        throw new IllegalArgumentException("Mana ability provider of type " + clazz.getSimpleName() + " not found!");
    }

    @Nullable
    public ManaAbilityProvider getProvider(ManaAbility manaAbility) {
        for (ManaAbilityProvider provider : providerMap.values()) {
            if (provider.getManaAbility().equals(manaAbility)) {
                return provider;
            }
        }
        return null;
    }

    @Override
    public void sendNotEnoughManaMessage(User user, double manaCost) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player == null) return;
        plugin.getAbilityManager().sendMessage(player, TextUtil.replace(plugin.getMsg(ManaAbilityMessage.NOT_ENOUGH_MANA, user.getLocale()),
                "{mana}", NumberUtil.format0(manaCost),
                "{current_mana}", String.valueOf(Math.round(user.getMana())),
                "{max_mana}", String.valueOf(Math.round(user.getMaxMana()))));
    }

    @Override
    public String getBaseDescription(ManaAbility manaAbility, User user, boolean formatted) {
        String desc = manaAbility.getDescription(user.getLocale(), formatted);
        ManaAbilityProvider provider = plugin.getManaAbilityManager().getProvider(manaAbility);
        if (provider != null) {
            desc = provider.replaceDescPlaceholders(desc, user);
        }
        return desc;
    }

    public Set<ManaAbility> getEnabledManaAbilities() {
        Set<ManaAbility> abilities = new LinkedHashSet<>();
        for (ManaAbilityProvider abilityProvider : providerMap.values()) {
            if (!abilityProvider.isDisabled()) {
                abilities.add(abilityProvider.getManaAbility());
            }
        }
        return abilities;
    }

}
