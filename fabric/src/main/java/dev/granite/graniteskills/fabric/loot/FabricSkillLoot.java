package dev.granite.graniteskills.fabric.loot;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.loot.Loot;
import dev.granite.graniteskills.api.loot.LootContext;
import dev.granite.graniteskills.api.loot.LootPool;
import dev.granite.graniteskills.api.loot.LootTable;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.skill.Skills;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.common.commands.CommandExecutor;
import dev.granite.graniteskills.common.loot.CommandLoot;
import dev.granite.graniteskills.common.loot.SourceContext;
import dev.granite.graniteskills.common.source.SourceTag;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rolls a skill loot table after a block award. Fishing keeps its own handler.
 * Excavation rare/epic pools mirror Bukkit {@code ExcavationLootProvider}
 * (Metal Detector / Lucky Spades).
 */
public final class FabricSkillLoot extends dev.granite.graniteskills.common.loot.AbstractLootHandler {

    public FabricSkillLoot() {
    }

    public static void tryRoll(FabricGraniteSkills plugin, ServerPlayer player, ServerLevel level, BlockPos pos,
            User user, Skill skill, XpSource source) {
        if (plugin == null || user == null || skill == null || source == null) {
            return;
        }
        LootTable table = plugin.getFabricLootManager().getLootTable(skill.getId());
        if (table == null || !table.checkRequirements(user.getUuid())) {
            return;
        }
        FabricSkillLoot handler = new FabricSkillLoot();
        for (LootPool pool : table.getPools()) {
            if (!pool.checkRequirements(user.getUuid()) || handler.isPoolUnobtainable(pool, source)) {
                continue;
            }
            if (!handler.isApplicable(plugin, pool, skill, source)) {
                continue;
            }
            double chance = handler.getPoolChance(plugin, pool, user, skill);
            if (ThreadLocalRandom.current().nextDouble() >= chance) {
                continue;
            }
            Loot selected = select(pool, source, user);
            if (selected instanceof FabricItemLoot itemLoot) {
                giveItem(plugin, player, level, pos, itemLoot);
            } else if (selected instanceof CommandLoot commandLoot) {
                giveCommand(plugin, player, commandLoot);
            } else if (selected instanceof FabricEntityLoot entityLoot) {
                giveEntity(player, level, pos, entityLoot);
            }
            break;
        }
    }

    /**
     * Bukkit {@code ExcavationLootProvider#isApplicable}: epic needs Lucky Spades tag; rare needs Metal Detector tag.
     */
    public boolean isApplicable(FabricGraniteSkills plugin, LootPool pool, Skill skill, XpSource source) {
        if (skill != Skills.EXCAVATION) {
            return true;
        }
        if ("rare".equals(pool.getName())) {
            return plugin.getSkillManager().hasTag(source, SourceTag.METAL_DETECTOR_APPLICABLE);
        }
        if ("epic".equals(pool.getName())) {
            return plugin.getSkillManager().hasTag(source, SourceTag.LUCKY_SPADES_APPLICABLE);
        }
        return true;
    }

    /**
     * Bukkit {@code ExcavationLootProvider#getChance}: Metal Detector boosts rare, Lucky Spades boosts epic.
     */
    public double getPoolChance(FabricGraniteSkills plugin, LootPool pool, User user, Skill skill) {
        double chance = getCommonChance(pool, user);
        if (skill != Skills.EXCAVATION) {
            return chance;
        }
        if ("rare".equals(pool.getName()) && Abilities.METAL_DETECTOR.isEnabled()) {
            return getAbilityModifiedChance(chance, Abilities.METAL_DETECTOR, user);
        }
        if ("epic".equals(pool.getName()) && Abilities.LUCKY_SPADES.isEnabled()) {
            return getAbilityModifiedChance(chance, Abilities.LUCKY_SPADES, user);
        }
        return chance;
    }

    private static Loot select(LootPool pool, XpSource source, User user) {
        UUID uuid = user.getUuid();
        return pool.rollLoot(loot -> {
            if (!loot.checkRequirements(uuid)) {
                return false;
            }
            Set<LootContext> contexts = loot.getValues().getContexts().get("sources");
            if (contexts == null) {
                return true;
            }
            for (LootContext context : contexts) {
                if (context instanceof SourceContext(XpSource configured) && configured.equals(source)) {
                    return true;
                }
            }
            return false;
        }).orElse(null);
    }

    /** Inclusive amount inside the configured min and max. Equal bounds stay constant. */
    public static int rollAmount(int minAmount, int maxAmount) {
        int min = Math.min(minAmount, maxAmount);
        int max = Math.max(minAmount, maxAmount);
        if (min < 1) {
            min = 1;
        }
        if (max < min) {
            max = min;
        }
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(max - min + 1) + min;
    }

    private static void giveItem(FabricGraniteSkills plugin, ServerPlayer player, ServerLevel level, BlockPos pos,
            FabricItemLoot itemLoot) {
        ItemStack template = FabricLootTemplateSupplier.supplyTemplate(itemLoot.getItem());
        if (template == null || template.isEmpty()) {
            return;
        }
        ItemStack drop = template.copy();
        drop.setCount(rollAmount(itemLoot.getMinAmount(), itemLoot.getMaxAmount()));
        boolean toInventory = plugin.getFabricLootManager().toInventory(player.getMainHandItem());
        deliver(player, level, pos, drop, toInventory);
    }

    /**
     * Puts the stack in the player's inventory when requested. A remainder, or the whole stack when
     * inventory delivery is off, is dropped at the position. Fishing does not use this path.
     */
    public static void deliver(ServerPlayer player, ServerLevel level, BlockPos pos, ItemStack drop,
            boolean toInventory) {
        if (drop == null || drop.isEmpty()) {
            return;
        }
        if (!toInventory || player == null) {
            Block.popResource(level, pos, drop);
            return;
        }
        int wanted = drop.getCount();
        Item item = drop.getItem();
        ItemStack template = drop.copy();
        int before = count(player, item);
        player.getInventory().add(drop);
        int stored = Math.max(0, count(player, item) - before);
        int leftover = wanted - stored;
        if (leftover <= 0) {
            return;
        }
        ItemStack rest = template.copy();
        rest.setCount(leftover);
        Block.popResource(level, pos, rest);
    }

    private static int count(ServerPlayer player, Item item) {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void giveCommand(FabricGraniteSkills plugin, ServerPlayer player, CommandLoot loot) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        String name = player.getName().getString();
        for (String command : loot.getCommands()) {
            String resolved = TextUtil.replace(command, "{player}", name);
            if (loot.getExecutor() == CommandExecutor.CONSOLE || user == null) {
                plugin.runConsoleCommand(resolved);
            } else {
                plugin.runPlayerCommand(user, resolved);
            }
        }
    }

    private static void giveEntity(ServerPlayer player, ServerLevel level, BlockPos pos, FabricEntityLoot loot) {
        String raw = loot.getEntityId();
        Identifier id = Identifier.parse(raw.indexOf(':') >= 0 ? raw : "minecraft:" + raw);
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (type == null) {
            return;
        }
        Entity spawned = type.create(level, EntitySpawnReason.TRIGGERED);
        if (spawned == null) {
            return;
        }
        Vec3 at = Vec3.atCenterOf(pos);
        spawned.snapTo(at.x, at.y, at.z, player.getYRot(), 0f);
        level.addFreshEntity(spawned);
    }
}
