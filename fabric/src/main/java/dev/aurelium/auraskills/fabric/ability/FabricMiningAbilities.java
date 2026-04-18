package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.damage.DamageModifier;
import dev.aurelium.auraskills.api.damage.DamageType;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.damage.FabricDamageMeta;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricMiningAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    public FabricMiningAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.LUCKY_MINER, Abilities.MINER, Abilities.PICK_MASTER, Abilities.HARDENED_ARMOR, Abilities.STAMINA);
    }

    /**
     * Used by block levelers when deciding direct-drop behavior (ported from Bukkit {@code MiningAbilities}).
     */
    public boolean dropsMineralDirectly(BlockState state) {
        String p = Registries.BLOCK.getId(state.getBlock()).getPath();
        return p.contains("diamond_ore") || p.contains("redstone_ore") || p.contains("emerald_ore")
                || p.contains("coal_ore") || p.contains("lapis_ore") || p.contains("nether_quartz_ore")
                || p.contains("nether_gold_ore") || p.contains("iron_ore") || p.contains("gold_ore")
                || p.contains("copper_ore");
    }

    @Override
    public void contributeAttackDamage(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayerEntity attacker) {
        if (meta.getDamageType() != DamageType.PICKAXE) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUuid());
        meta.addAttackModifier(pickMaster(attacker, user));
    }

    private DamageModifier pickMaster(ServerPlayerEntity player, User user) {
        var ability = Abilities.PICK_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }
}
