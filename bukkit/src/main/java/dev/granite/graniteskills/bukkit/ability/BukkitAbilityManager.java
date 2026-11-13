package dev.granite.graniteskills.bukkit.ability;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.skills.agility.AgilityAbilities;
import dev.granite.graniteskills.bukkit.skills.alchemy.AlchemyAbilities;
import dev.granite.graniteskills.bukkit.skills.archery.ArcheryAbilities;
import dev.granite.graniteskills.bukkit.skills.defense.DefenseAbilities;
import dev.granite.graniteskills.bukkit.skills.enchanting.EnchantingAbilities;
import dev.granite.graniteskills.bukkit.skills.endurance.EnduranceAbilities;
import dev.granite.graniteskills.bukkit.skills.excavation.ExcavationAbilities;
import dev.granite.graniteskills.bukkit.skills.farming.FarmingAbilities;
import dev.granite.graniteskills.bukkit.skills.fighting.FightingAbilities;
import dev.granite.graniteskills.bukkit.skills.fishing.FishingAbilities;
import dev.granite.graniteskills.bukkit.skills.foraging.ForagingAbilities;
import dev.granite.graniteskills.bukkit.skills.forging.ForgingAbilities;
import dev.granite.graniteskills.bukkit.skills.healing.HealingAbilities;
import dev.granite.graniteskills.bukkit.skills.mining.MiningAbilities;
import dev.granite.graniteskills.bukkit.user.BukkitUser;
import dev.granite.graniteskills.common.ability.AbilityManager;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitAbilityManager extends AbilityManager {

    private final GraniteSkills plugin;

    public BukkitAbilityManager(GraniteSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void registerAbilityImplementations() {
        registerAbilityImpl(new FishingAbilities(plugin));
        registerAbilityImpl(new EnduranceAbilities(plugin));
        registerAbilityImpl(new AgilityAbilities(plugin));
        registerAbilityImpl(new AlchemyAbilities(plugin));
        registerAbilityImpl(new EnchantingAbilities(plugin));
        registerAbilityImpl(new HealingAbilities(plugin));
        registerAbilityImpl(new ForgingAbilities(plugin));
        registerAbilityImpl(new FightingAbilities(plugin));
        registerAbilityImpl(new ArcheryAbilities(plugin));
        registerAbilityImpl(new FarmingAbilities(plugin));
        registerAbilityImpl(new ForagingAbilities(plugin));
        registerAbilityImpl(new MiningAbilities(plugin));
        registerAbilityImpl(new ExcavationAbilities(plugin));
        registerAbilityImpl(new DefenseAbilities(plugin));
    }

    @Override
    public void sendAbilityMessage(User user, String message) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player != null) {
            sendMessage(player, message);
        }
    }

    public void registerAbilityImpl(BukkitAbilityImpl abilityImpl) {
        addImplToMap(abilityImpl);
        Bukkit.getPluginManager().registerEvents(abilityImpl, plugin);
    }

    /**
     * Sends a message to the ability action bar if enabled, or the chat otherwise.
     *
     * @param player the player to send the message to
     * @param message the message to send
     */
    public void sendMessage(Player player, String message) {
        User user = plugin.getUser(player);
        if (plugin.configBoolean(Option.ACTION_BAR_ABILITY) && plugin.configBoolean(Option.ACTION_BAR_ENABLED)) {
            plugin.getUiProvider().getActionBarManager().sendAbilityActionBar(user, message);
        } else {
            if (message == null || message.isEmpty()) return; // Don't send empty message
            player.sendMessage(plugin.getPrefix(user.getLocale()) + message);
        }
    }

}
