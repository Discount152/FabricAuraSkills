package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootContext;
import dev.aurelium.auraskills.api.loot.LootPool;
import dev.aurelium.auraskills.api.loot.LootTable;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityDamagers;
import dev.aurelium.auraskills.common.commands.CommandExecutor;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.loot.CommandLoot;
import dev.aurelium.auraskills.common.message.MessageKey;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.data.Pair;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricDamageCauseMapper;
import dev.aurelium.auraskills.fabric.FabricEntityDamageAttribution;
import dev.aurelium.auraskills.fabric.FabricEntityXpSourceLookup;
import dev.aurelium.auraskills.fabric.FabricUser;
import dev.aurelium.auraskills.fabric.FabricXpHandlerUtil;
import dev.aurelium.auraskills.fabric.loot.context.FabricMobContext;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bukkit {@code MobLootHandler} on Fabric: rolls the {@code mob} skill loot table (see {@code loot/mob.yml})
 * when a living entity dies to a player (melee or projectile), with the same context/damager/cause gating
 * and pool iteration semantics as the reference.
 */
public final class FabricMobLootHandler extends dev.aurelium.auraskills.common.loot.AbstractLootHandler {

    private final FabricAuraSkills plugin;
    private final Random random = new Random();

    private FabricMobLootHandler(FabricAuraSkills plugin) {
        this.plugin = plugin;
    }

    public static void register(FabricAuraSkills plugin) {
        FabricMobLootHandler handler = new FabricMobLootHandler(plugin);
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> handler.onAfterDeath(entity, damageSource));
    }

    private void onAfterDeath(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource) {
        if (!(entity.getEntityWorld() instanceof ServerWorld world) || world.isClient()) {
            return;
        }
        if (entity instanceof ServerPlayerEntity) {
            return;
        }
        if (entity instanceof ArmorStandEntity) {
            return;
        }
        if (!plugin.isRuntimeReady()) {
            return;
        }
        Pair<ServerPlayerEntity, EntityDamagers> killer = FabricEntityDamageAttribution.resolveKillerOnDeath(
                damageSource, entity, entity.getEntityWorld().getServer());
        if (killer == null) {
            return;
        }
        ServerPlayerEntity player = killer.first();
        EntityDamagers eventDamager = killer.second();
        if (failsChecks(player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        LootTable table = plugin.getFabricLootManager().getLootTable(NamespacedId.fromDefault("mob"));
        if (table == null || !table.checkRequirements(player.getUuid())) {
            return;
        }
        DamageCause skillsCause = FabricDamageCauseMapper.toApiCause(damageSource);
        SkillSource<EntityXpSource> skillSource = FabricEntityXpSourceLookup.forMobLootSkill(plugin, entity, eventDamager, skillsCause);
        Skill skill = Skills.FIGHTING;
        if (skillSource != null) {
            skill = skillSource.skill();
        }
        String entityPath = FabricMobContext.forEntity(entity).path();
        label:
        for (LootPool pool : table.getPools()) {
            if (isPoolUnobtainableForMob(pool, entityPath)) {
                continue;
            }
            if (!pool.checkRequirements(player.getUuid())) {
                continue;
            }
            double chance = getCommonChance(pool, user);
            FabricMobContext context = new FabricMobContext(entityPath);
            if (random.nextDouble() >= chance) {
                continue;
            }
            Loot selected = selectMobLootFromPool(pool, context, user);
            switch (selected) {
                case null:
                    break label;
                case FabricItemLoot itemLoot:
                    giveMobItemLoot(world, player, itemLoot, entity, skill, table);
                    break;
                case CommandLoot commandLoot:
                    giveCommandLootInternal(player, commandLoot, null, skill);
                    break;
                default:
                    break;
            }
            break;
        }
    }

    private boolean isPoolUnobtainableForMob(LootPool pool, String entityPath) {
        for (Loot loot : pool.getLoot()) {
            Set<LootContext> ctxs = loot.getValues().getContexts().getOrDefault("mobs", Set.of());
            if (ctxs.isEmpty()) {
                return false;
            }
            for (LootContext c : ctxs) {
                if (c instanceof FabricMobContext(String p) && p.equalsIgnoreCase(entityPath)) {
                    return false;
                }
            }
        }
        return true;
    }

    private @Nullable Loot selectMobLootFromPool(LootPool pool, FabricMobContext providedContext, User user) {
        UUID uuid = user.getUuid();
        return pool.rollLoot(loot -> {
            if (!loot.checkRequirements(uuid)) {
                return false;
            }
            if (providedContext == null) {
                return true;
            }
            Set<LootContext> mobCtx = loot.getValues().getContexts().get("mobs");
            if (mobCtx != null) {
                boolean matched = false;
                for (LootContext c : mobCtx) {
                    if (c instanceof FabricMobContext(String configured)) {
                        if (configured.equalsIgnoreCase(providedContext.path())) {
                            matched = true;
                            break;
                        }
                    }
                }
                return matched;
            }
            return true;
        }).orElse(null);
    }

    private void giveMobItemLoot(ServerWorld world, ServerPlayerEntity player, FabricItemLoot loot, LivingEntity victim, Skill skill, LootTable table) {
        ItemStack template = FabricLootTemplateSupplier.supplyTemplate(loot.getItem());
        if (template == null || template.isEmpty()) {
            return;
        }
        ItemStack drop = applyDamageRange(template.copy(), loot.getMinDamage(), loot.getMaxDamage());
        if (drop == null || drop.isEmpty()) {
            return;
        }
        int amount = generateAmount(loot.getMinAmount(), loot.getMaxAmount());
        if (amount <= 0) {
            return;
        }
        drop.setCount(amount);
        Vec3d at = new Vec3d(victim.getX(), victim.getY() + 0.25, victim.getZ());
        FabricLootManager lm = plugin.getFabricLootManager();
        if (lm.toInventory(player.getMainHandStack())) {
            player.getInventory().offerOrDrop(drop);
        } else {
            ItemEntity itemEntity = new ItemEntity(world, at.x, at.y, at.z, drop);
            itemEntity.setToDefaultPickupDelay();
            Vec3d p = new Vec3d(player.getX(), player.getY(), player.getZ());
            Vec3d v = p.subtract(at).normalize().multiply(0.1);
            itemEntity.setVelocity(v.x, 0.1, v.z);
            world.spawnEntity(itemEntity);
        }
        attemptSendMessage(player, loot);
        giveLootItemXp(player, loot, null, skill);
    }

    private @Nullable ItemStack applyDamageRange(ItemStack drop, double minDamage, double maxDamage) {
        if (minDamage == 0.0 && maxDamage == 0.0) {
            return drop;
        }
        if (minDamage >= 0.0 && minDamage <= 1.0
                && maxDamage >= 0.0 && maxDamage <= 1.0
                && minDamage <= maxDamage
                && !drop.isEmpty()
                && drop.isDamageable()) {
            int durability = drop.getMaxDamage();
            int minD = (int) (durability * minDamage);
            int maxD = (int) (durability * maxDamage);
            int lo = Math.min(minD, maxD);
            int hi = Math.max(minD, maxD);
            int damage;
            if (lo == hi) {
                damage = hi;
            } else {
                damage = ThreadLocalRandom.current().nextInt(lo, hi);
            }
            drop.setDamage(damage);
        }
        return drop;
    }

    private void giveCommandLootInternal(ServerPlayerEntity player, CommandLoot loot, @Nullable XpSource source, Skill skill) {
        User user = plugin.getUserManager().getUser(player.getUuid());
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
        giveLootItemXp(player, loot, source, skill);
    }

    private void attemptSendMessage(ServerPlayerEntity player, Loot entry) {
        String message = entry.getValues().getMessage();
        if (message == null || message.isEmpty()) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        Locale locale = user.getLocale();
        MessageKey key = MessageKey.of(message);
        String resolved = plugin.getMsg(key, locale);
        if (resolved != null) {
            message = resolved;
        }
        user.sendMessage(message);
    }

    private void giveLootItemXp(ServerPlayerEntity player, Loot entry, @Nullable XpSource source, Skill skill) {
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.ENTITY)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        Object xpObj = entry.getValues().getOptions().get("xp");
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

    private boolean failsChecks(ServerPlayerEntity player) {
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            return true;
        }
        String worldName = FabricUser.worldNameCompat(player);
        return plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName);
    }

}
