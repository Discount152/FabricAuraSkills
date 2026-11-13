package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.ability.AbilityManager;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.ability.FabricAbilityImpl;
import dev.aurelium.auraskills.fabric.ability.FabricBleedListener;
import dev.aurelium.auraskills.fabric.ability.FabricAgilityAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricAlchemyAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricArcheryAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricDamagePipelineContributor;
import dev.aurelium.auraskills.fabric.ability.FabricDefenseAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricEnchantingAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricEnduranceAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricExcavationAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricFarmingAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricFightingAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricFishingAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricForgingAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricForagingAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricHealingAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricMiningAbilities;
import dev.aurelium.auraskills.fabric.damage.FabricDamageMeta;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

public final class FabricAbilityManager extends AbilityManager {

    private final FabricAuraSkills plugin;
    private final List<FabricDamagePipelineContributor> damageContributors = new ArrayList<>();

    public FabricAbilityManager(AuraSkillsPlugin plugin) {
        super(plugin);
        this.plugin = (FabricAuraSkills) plugin;
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
