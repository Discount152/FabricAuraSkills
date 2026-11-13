package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootContext;
import dev.aurelium.auraskills.api.loot.LootPool;
import dev.aurelium.auraskills.api.loot.LootTable;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.source.type.FishingXpSource;
import dev.aurelium.auraskills.common.commands.CommandExecutor;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.loot.AbstractLootHandler;
import dev.aurelium.auraskills.common.loot.CommandLoot;
import dev.aurelium.auraskills.common.loot.SourceContext;
import dev.aurelium.auraskills.common.message.MessageKey;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricUser;
import dev.aurelium.auraskills.fabric.FabricXpHandlerUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fabric counterpart to Bukkit {@link dev.aurelium.auraskills.bukkit.loot.handler.FishingLootHandler}: rolls bonus loot
 * from {@code loot/fishing.yml} pools (rare / epic with Treasure Hunter / Epic Catch, open-water option, command loot).
 * Entity loot entries spawn the configured entity at the bobber.
 */
public final class FabricFishingLootHandler extends AbstractLootHandler {

    private final FabricAuraSkills plugin;

    public FabricFishingLootHandler(FabricAuraSkills plugin) {
        this.plugin = plugin;
    }

    /**
     * Called after a successful fishing XP match when vanilla increments the fish-caught stat (same timing as Bukkit
     * {@code PlayerFishEvent.State.CAUGHT_FISH}).
     */
    public static void tryRollBonusLoot(FabricAuraSkills plugin, ServerPlayer player, FishingHook bobber,
            ItemStack caughtItem, SkillSource<FishingXpSource> matchedSource) {
        if (plugin == null || caughtItem == null || caughtItem.isEmpty()) {
            return;
        }
        FabricFishingLootHandler h = new FabricFishingLootHandler(plugin);
        if (h.failsChecks(player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        Skill skill = matchedSource.skill();
        FabricLootManager lm = plugin.getFabricLootManager();
        LootTable table = lm.getLootTable(skill.getId());
        if (table == null || !table.checkRequirements(user.getUuid())) {
            return;
        }
        FishingXpSource matchedFishSource = matchedSource.source();
        XpSource contextXpSource = matchedFishSource;

        for (LootPool pool : table.getPools()) {
            if (Boolean.TRUE.equals(pool.getOption("require_open_water", Boolean.class, false)) && !bobber.isOpenWaterFishing()) {
                continue;
            }
            if (!pool.checkRequirements(user.getUuid())) {
                continue;
            }
            XpSource source = null;
            double chance = h.getCommonChance(pool, user);
            if ("rare".equals(pool.getName()) && Abilities.TREASURE_HUNTER.isEnabled()) {
                chance = h.getAbilityModifiedChance(chance, Abilities.TREASURE_HUNTER, user);
                source = getSourceWithLootPool(plugin, "rare", skill);
            } else if ("epic".equals(pool.getName()) && Abilities.EPIC_CATCH.isEnabled()) {
                chance = h.getAbilityModifiedChance(chance, Abilities.EPIC_CATCH, user);
                source = getSourceWithLootPool(plugin, "epic", skill);
            } else {
                source = matchedFishSource;
            }
            if (source == null) {
                continue;
            }
            if (h.isPoolUnobtainable(pool, source)) {
                continue;
            }
            if (ThreadLocalRandom.current().nextDouble() >= chance) {
                continue;
            }
            Loot selected = h.selectLoot(pool, new SourceContext(contextXpSource), user);
            if (selected == null) {
                continue;
            }
            if (selected instanceof FabricItemLoot itemLoot) {
                h.giveFishingItemLoot(player, bobber, itemLoot, source, skill);
            } else if (selected instanceof CommandLoot commandLoot) {
                h.giveFishingCommandLoot(player, bobber, commandLoot, source, skill);
            } else if (selected instanceof FabricEntityLoot entityLoot) {
                h.giveFishingEntityLoot(player, bobber, entityLoot, source, skill);
            }
            break;
        }
    }

    private static @Nullable FishingXpSource getSourceWithLootPool(FabricAuraSkills plugin, String lootPool, Skill skill) {
        for (SkillSource<FishingXpSource> entry : plugin.getSkillManager().getSourcesOfType(FishingXpSource.class)) {
            if (!entry.skill().equals(skill)) {
                continue;
            }
            String candidate = entry.source().getItem().lootPool();
            if (candidate == null) {
                continue;
            }
            if (lootPool.equals(candidate)) {
                return entry.source();
            }
        }
        return null;
    }

    private boolean failsChecks(ServerPlayer player) {
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE)
                && player.getAbilities().instabuild) {
            return true;
        }
        String worldName = FabricUser.worldNameCompat(player);
        return plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName);
    }

    private Loot selectLoot(LootPool pool, LootContext providedContext, User user) {
        UUID uuid = user.getUuid();
        return pool.rollLoot(loot -> {
            if (!loot.checkRequirements(uuid)) {
                return false;
            }
            if (providedContext instanceof SourceContext(XpSource providedSource)) {
                Set<LootContext> lootContexts = loot.getValues().getContexts().get("sources");
                if (lootContexts != null && providedSource != null) {
                    boolean matched = false;
                    for (LootContext context : lootContexts) {
                        if (context instanceof SourceContext(XpSource configuredSource)) {
                            if (configuredSource.equals(providedSource)) {
                                matched = true;
                                break;
                            }
                        }
                    }
                    return matched;
                }
            }
            return true;
        }).orElse(null);
    }

    private void giveFishingItemLoot(ServerPlayer player, FishingHook bobber, FabricItemLoot loot,
            XpSource source, Skill skill) {
        ItemStack template = FabricLootTemplateSupplier.supplyTemplate(loot.getItem());
        if (template == null || template.isEmpty()) {
            return;
        }
        int amount = generateAmount(loot.getMinAmount(), loot.getMaxAmount());
        if (amount <= 0) {
            return;
        }
        ItemStack drop = template.copy();
        drop.setCount(amount);

        Entity hooked = bobber.getHookedIn();
        FabricLootManager lm = plugin.getFabricLootManager();
        if (lm.toInventory(player.getMainHandItem())) {
            player.getInventory().placeItemBackInInventory(drop, Prediction.SERVER_ONLY);
            if (hooked instanceof ItemEntity ie) {
                ie.setItem(ItemStack.EMPTY);
            }
        } else if (hooked instanceof ItemEntity ie) {
            ie.setItem(drop);
            Vec3 toward = player.getEyePosition().subtract(ie.getX(), ie.getY(), ie.getZ()).normalize().scale(0.08);
            ie.setDeltaMovement(toward.x, Math.max(0.12, toward.y + 0.08), toward.z);
        } else {
            player.getInventory().placeItemBackInInventory(drop, Prediction.SERVER_ONLY);
        }

        attemptSendMessage(player, loot);
        giveLootXp(player, loot, source, skill);
    }

    private void giveFishingCommandLoot(ServerPlayer player, FishingHook bobber, CommandLoot loot,
            XpSource source, Skill skill) {
        Entity hooked = bobber.getHookedIn();
        if (hooked instanceof ItemEntity ie) {
            ie.setItem(ItemStack.EMPTY);
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        for (String command : loot.getCommands()) {
            String finalCommand = TextUtil.replace(command, "{player}", player.getName().getString());
            CommandExecutor executor = loot.getExecutor();
            if (executor == CommandExecutor.CONSOLE) {
                plugin.runConsoleCommand(finalCommand);
            } else {
                plugin.runPlayerCommand(user, finalCommand);
            }
        }
        attemptSendMessage(player, loot);
        giveLootXp(player, loot, source, skill);
    }

    private void giveFishingEntityLoot(ServerPlayer player, FishingHook bobber, FabricEntityLoot loot,
            XpSource source, Skill skill) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
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
        spawned.snapTo(bobber.getX(), bobber.getY(), bobber.getZ(), player.getYRot(), 0f);
        if (!level.addFreshEntity(spawned)) {
            return;
        }
        Entity hooked = bobber.getHookedIn();
        if (hooked instanceof ItemEntity itemEntity) {
            itemEntity.setItem(ItemStack.EMPTY);
        }
        attemptSendMessage(player, loot);
        giveLootXp(player, loot, source, skill);
    }

    private void attemptSendMessage(ServerPlayer player, Loot loot) {
        String message = loot.getValues().getMessage();
        if (message == null || message.isEmpty()) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        Locale locale = user.getLocale();
        MessageKey messageKey = MessageKey.of(message);
        String resolved = plugin.getMsg(messageKey, locale);
        if (resolved != null) {
            message = resolved;
        }
        user.sendMessage(message);
    }

    private void giveLootXp(ServerPlayer player, Loot loot, XpSource source, Skill skill) {
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.FISHING)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        Object xpObj = loot.getValues().getOptions().get("xp");
        double xp;
        if (xpObj instanceof Integer i) {
            xp = i;
        } else if (xpObj instanceof Double d) {
            xp = d;
        } else {
            xp = -1.0;
        }
        if (xp < 0 && source != null) {
            plugin.getLevelManager().addXp(user, skill, source, source.getXp());
        } else if (xp > 0) {
            plugin.getLevelManager().addXp(user, skill, source, xp);
        }
    }
}
