package dev.granite.graniteskills.bukkit.loot.handler;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.event.loot.LootDropEvent;
import dev.granite.graniteskills.api.loot.Loot;
import dev.granite.graniteskills.api.loot.LootPool;
import dev.granite.graniteskills.api.loot.LootTable;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.skill.Skills;
import dev.granite.graniteskills.api.source.SkillSource;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.api.source.type.FishingXpSource;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.loot.type.EntityLoot;
import dev.granite.graniteskills.bukkit.loot.type.ItemLoot;
import dev.granite.graniteskills.bukkit.source.FishingLeveler;
import dev.granite.graniteskills.bukkit.util.VersionUtils;
import dev.granite.graniteskills.common.loot.CommandLoot;
import dev.granite.graniteskills.common.loot.SourceContext;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

public class FishingLootHandler extends LootHandler implements Listener {

    private final Random random = new Random();

    public FishingLootHandler(GraniteSkills plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        if (failsChecks(player, player.getLocation(), false)) return;

        if (!(event.getCaught() instanceof Item)) return;
        if (!event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) return;
        if (event.getExpToDrop() == 0) return;

        User user = plugin.getUser(player);
        UUID uuid = player.getUniqueId();

        ItemStack originalItem = ((Item) event.getCaught()).getItemStack();

        var originalSource = plugin.getLevelManager().getLeveler(FishingLeveler.class).getSource(originalItem);

        Skill skill = originalSource != null ? originalSource.skill() : Skills.FISHING;

        LootTable table = plugin.getLootManager().getLootTable(skill);
        if (table == null || !table.checkRequirements(uuid)) return;

        for (LootPool pool : table.getPools()) {
            // Check if in open water
            if (pool.getOption("require_open_water", Boolean.class, false) && VersionUtils.isAtLeastVersion(16, 5)) {
                if (!event.getHook().isInOpenWater()) continue;
            }

            if (!pool.checkRequirements(uuid)) {
                continue;
            }

            // Calculate chance for pool
            XpSource source = null;
            double chance = getCommonChance(pool, user);

            LootDropEvent.Cause cause = LootDropEvent.Cause.FISHING_OTHER_LOOT;
            if (pool.getName().equals("rare") && Abilities.TREASURE_HUNTER.isEnabled()) {
                chance = getAbilityModifiedChance(chance, Abilities.TREASURE_HUNTER, user);
                source = getSourceWithLootPool("rare", skill);
                cause = LootDropEvent.Cause.TREASURE_HUNTER;
            } else if (pool.getName().equals("epic") && Abilities.EPIC_CATCH.isEnabled()) {
                chance = getAbilityModifiedChance(chance, Abilities.EPIC_CATCH, user);
                source = getSourceWithLootPool("epic", skill);
                cause = LootDropEvent.Cause.EPIC_CATCH;
            } else if (originalSource != null) {
                source = originalSource.source();
            }

            if (source == null) continue;

            // Skip if pool has no loot matching the source
            if (isPoolUnobtainable(pool, source)) {
                continue;
            }

            if (random.nextDouble() < chance) { // Pool is selected
                XpSource contextSource = originalSource != null ? originalSource.source() : null;
                Loot selectedLoot = selectLoot(pool, new SourceContext(contextSource), user);
                // Give loot
                if (selectedLoot == null) { // Continue iterating pools
                    continue;
                }
                if (selectedLoot instanceof ItemLoot itemLoot) {
                    giveFishingItemLoot(player, itemLoot, event, source, skill, cause, table);
                } else if (selectedLoot instanceof CommandLoot commandLoot) {
                    giveFishingCommandLoot(player, commandLoot, event, source, skill);
                } else if (selectedLoot instanceof EntityLoot entityLoot) {
                    giveFishingEntityLoot(player, entityLoot, event, source, skill, cause);
                }
                break; // Stop iterating pools
            }
        }
    }

    @Nullable
    private FishingXpSource getSourceWithLootPool(String lootPool, Skill skill) {
        for (SkillSource<FishingXpSource> entry : plugin.getSkillManager().getSourcesOfType(FishingXpSource.class)) {
            if (!entry.skill().equals(skill)) continue;

            String candidate = entry.source().getItem().lootPool();
            if (candidate == null) continue;

            if (lootPool.equals(candidate)) {
                return entry.source();
            }
        }
        return null;
    }

}
