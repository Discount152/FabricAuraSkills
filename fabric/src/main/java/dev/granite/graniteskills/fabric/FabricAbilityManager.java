package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.ability.AbilityManager;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.ability.FabricAbilityImpl;
import dev.granite.graniteskills.fabric.ability.FabricAgilityAbilities;
import dev.granite.graniteskills.fabric.ability.FabricAlchemyAbilities;
import dev.granite.graniteskills.fabric.ability.FabricArcheryAbilities;
import dev.granite.graniteskills.fabric.ability.FabricBleedListener;
import dev.granite.graniteskills.fabric.ability.FabricDamagePipelineContributor;
import dev.granite.graniteskills.fabric.ability.FabricDefenseAbilities;
import dev.granite.graniteskills.fabric.ability.FabricEnchantingAbilities;
import dev.granite.graniteskills.fabric.ability.FabricEnduranceAbilities;
import dev.granite.graniteskills.fabric.ability.FabricExcavationAbilities;
import dev.granite.graniteskills.fabric.ability.FabricFarmingAbilities;
import dev.granite.graniteskills.fabric.ability.FabricFightingAbilities;
import dev.granite.graniteskills.fabric.ability.FabricFishingAbilities;
import dev.granite.graniteskills.fabric.ability.FabricForagingAbilities;
import dev.granite.graniteskills.fabric.ability.FabricForgingAbilities;
import dev.granite.graniteskills.fabric.ability.FabricHealingAbilities;
import dev.granite.graniteskills.fabric.ability.FabricMiningAbilities;
import dev.granite.graniteskills.fabric.damage.FabricDamageMeta;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class FabricAbilityManager extends AbilityManager {

    private final FabricGraniteSkills plugin;
    private final List<FabricDamagePipelineContributor> damageContributors = new ArrayList<>();

    public FabricAbilityManager(GraniteSkillsPlugin plugin) {
        super(plugin);
        this.plugin = (FabricGraniteSkills) plugin;
    }

    @Override
    public void registerAbilityImplementations() {
        damageContributors.clear();
        registerAndHook(new FabricFishingAbilities(plugin));
        registerAndHook(new FabricEnduranceAbilities(plugin));
        registerAndHook(new FabricAgilityAbilities(plugin));
        registerAndHook(new FabricAlchemyAbilities(plugin));
        registerAndHook(new FabricEnchantingAbilities(plugin));
        registerAndHook(new FabricHealingAbilities(plugin));
        registerAndHook(new FabricForgingAbilities(plugin));
        registerAndHook(new FabricFightingAbilities(plugin));
        FabricBleedListener.register(plugin);
        registerAndHook(new FabricArcheryAbilities(plugin));
        registerAndHook(new FabricFarmingAbilities(plugin));
        registerAndHook(new FabricForagingAbilities(plugin));
        registerAndHook(new FabricMiningAbilities(plugin));
        registerAndHook(new FabricExcavationAbilities(plugin));
        registerAndHook(new FabricDefenseAbilities(plugin));
    }

    private void registerAndHook(FabricAbilityImpl impl) {
        addImplToMap(impl);
        if (impl instanceof FabricDamagePipelineContributor contributor) {
            damageContributors.add(contributor);
        }
    }

    public void applyAbilityAttackDamage(FabricDamageMeta meta, ServerPlayer attacker) {
        for (FabricDamagePipelineContributor c : damageContributors) {
            c.contributeAttackDamage(plugin, meta, attacker);
        }
    }

    public void applyAbilityDefenseDamage(FabricDamageMeta meta, ServerPlayer victim) {
        for (FabricDamagePipelineContributor c : damageContributors) {
            c.contributeDefenseDamage(plugin, meta, victim);
        }
    }

    @Override
    public void sendAbilityMessage(User user, String message) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        if (plugin.configBoolean(Option.ACTION_BAR_ABILITY) && plugin.configBoolean(Option.ACTION_BAR_ENABLED)) {
            plugin.getUiProvider().getActionBarManager().sendAbilityActionBar(user, message);
        } else {
            if (message == null || message.isEmpty()) {
                return;
            }
            user.sendMessage(plugin.getPrefix(user.getLocale()) + message);
        }
    }
}
