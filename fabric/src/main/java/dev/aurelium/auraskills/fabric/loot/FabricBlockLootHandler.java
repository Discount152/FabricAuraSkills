package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootContext;
import dev.aurelium.auraskills.api.loot.LootDropCause;
import dev.aurelium.auraskills.api.loot.LootPool;
import dev.aurelium.auraskills.api.loot.LootTable;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.commands.CommandExecutor;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.loot.CommandLoot;
import dev.aurelium.auraskills.common.loot.SkillLootProvider;
import dev.aurelium.auraskills.common.loot.SourceContext;
import dev.aurelium.auraskills.common.message.MessageKey;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricBlockXpSourceLookup;
import dev.aurelium.auraskills.fabric.FabricUser;
import dev.aurelium.auraskills.fabric.FabricXpHandlerUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bukkit {@code BlockLootHandler} on Fabric. Rolls block skill loot in {@link PlayerBlockBreakEvents#BEFORE};
 * applies item/command drops in {@link PlayerBlockBreakEvents#AFTER} so {@code override_vanilla_loot} can clear
 * vanilla {@link ItemEntity} instances at the block without deleting the skill roll’s own drops (Bukkit uses
 * {@code setDropItems(false)} before vanilla spawns).
 */
public final class FabricBlockLootHandler extends dev.aurelium.auraskills.common.loot.AbstractLootHandler {

    private static final ThreadLocal<@Nullable DeferredBlockLoot> PENDING_LOOT = new ThreadLocal<>();

    private final FabricAuraSkills plugin;
    private final Random random = new Random();
    private final Map<Skill, SkillLootProvider> lootProviders = new ConcurrentHashMap<>();

    private FabricBlockLootHandler(FabricAuraSkills plugin) {
        this.plugin = plugin;
        lootProviders.put(Skills.EXCAVATION, new FabricExcavationLootProvider(plugin, this));
        lootProviders.put(Skills.MINING, new SkillLootProvider(plugin, this) {
            @Override
            public LootDropCause getCause(LootPool pool) {
                return FabricLootCauses.MINING_OTHER_LOOT;
            }
        });
        lootProviders.put(Skills.FORAGING, new SkillLootProvider(plugin, this) {
            @Override
            public LootDropCause getCause(LootPool pool) {
                return FabricLootCauses.FORAGING_OTHER_LOOT;
            }
        });
        lootProviders.put(Skills.FARMING, new SkillLootProvider(plugin, this) {
            @Override
            public LootDropCause getCause(LootPool pool) {
                return FabricLootCauses.FARMING_OTHER_LOOT;
            }
        });
    }

    public static void register(FabricAuraSkills plugin) {
        FabricBlockLootHandler handler = new FabricBlockLootHandler(plugin);
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            PENDING_LOOT.remove();
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return true;
            }
            if (plugin == null || !plugin.isRuntimeReady()) {
                return true;
            }
            handler.prepareBlockLoot(sw, sp, pos, state);
            return true;
        });
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            DeferredBlockLoot deferred = PENDING_LOOT.get();
            PENDING_LOOT.remove();
            if (deferred == null) {
                return;
            }
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return;
            }
            if (!deferred.matches(sw, pos, sp)) {
                return;
            }
            if (deferred.overrideVanilla()) {
                removeFreshItemDropsNearBlock(sw, pos);
            }
            deferred.apply(handler);
        });
    }

    private void prepareBlockLoot(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockState state) {
        SkillSource<BlockXpSource> original = FabricBlockXpSourceLookup.findForBreak(plugin, state);
        if (original == null) {
            return;
        }
        BlockXpSource source = original.source();
        Skill skill = original.skill();
        if (!skill.isEnabled()) {
            return;
        }
        if (plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED) && source.checkReplace()
                && plugin.getFabricRegionManager().isPlacedBlock(world, pos)) {
            return;
        }
        if (failsChecks(player)) {
            return;
        }
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.BLOCK)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        UUID uuid = user.getUuid();
        SkillLootProvider provider = lootProviders.get(skill);
        LootTable table = plugin.getFabricLootManager().getLootTable(skill);
        if (table == null || !table.checkRequirements(uuid)) {
            return;
        }
        for (LootPool pool : table.getPools()) {
            if (provider != null && !provider.isApplicable(pool, source)) {
                continue;
            }
            if (isPoolUnobtainable(pool, source)) {
                continue;
            }
            if (!pool.checkRequirements(uuid)) {
                continue;
            }
            double chance;
            if (provider != null) {
                chance = provider.getChance(pool, user);
            } else {
                chance = getCommonChance(pool, user);
            }
            LootDropCause cause = provider != null ? provider.getCause(pool) : FabricLootCauses.UNKNOWN;
            DeferredBlockLoot deferred = trySelectBlockLoot(table, pool, world, pos, player, chance, source, skill, cause);
            if (deferred != null) {
                PENDING_LOOT.set(deferred);
                return;
            }
        }
    }

    private @Nullable DeferredBlockLoot trySelectBlockLoot(LootTable table, LootPool pool, ServerWorld world, BlockPos pos,
            ServerPlayerEntity player, double chance, XpSource originalSource, Skill skill, LootDropCause cause) {
        if (random.nextDouble() >= chance) {
            return null;
        }
        Loot selectedLoot = selectLootFromPool(pool, new SourceContext(originalSource), plugin.getUserManager().getUser(player.getUuid()));
        if (selectedLoot == null) {
            return null;
        }
        boolean override = pool.overridesVanillaLoot();
        if (selectedLoot instanceof FabricItemLoot itemLoot) {
            return new DeferredBlockLoot(world, pos.toImmutable(), player.getUuid(), skill, table, cause, itemLoot, null, override);
        } else if (selectedLoot instanceof CommandLoot commandLoot) {
            return new DeferredBlockLoot(world, pos.toImmutable(), player.getUuid(), skill, table, cause, null, commandLoot, override);
        }
        return null;
    }

    private @Nullable Loot selectLootFromPool(LootPool pool, SourceContext providedContext, User user) {
        UUID uuid = user.getUuid();
        return pool.rollLoot(entry -> {
            if (!entry.checkRequirements(uuid)) {
                return false;
            }
            if (providedContext instanceof SourceContext(XpSource providedSource)) {
                Set<LootContext> lootContexts = entry.getValues().getContexts().get("sources");
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

    private void giveBlockItemLoot(ServerWorld world, BlockPos pos, ServerPlayerEntity player, FabricItemLoot loot, Skill skill,
            LootTable table) {
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
        FabricLootManager lm = plugin.getFabricLootManager();
        Vec3d at = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (lm.toInventory(player.getMainHandStack())) {
            player.getInventory().offerOrDrop(drop);
        } else {
            ItemEntity entity = new ItemEntity(world, at.x, at.y, at.z, drop);
            entity.setToDefaultPickupDelay();
            Vec3d p = new Vec3d(player.getX(), player.getY(), player.getZ());
            Vec3d v = p.subtract(at).normalize().multiply(0.1);
            entity.setVelocity(v.x, 0.1, v.z);
            world.spawnEntity(entity);
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

    private void giveCommandLoot(ServerPlayerEntity player, CommandLoot loot, Skill skill) {
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
        giveLootItemXp(player, loot, null, skill);
    }

    private void attemptSendMessage(ServerPlayerEntity player, Loot entry) {
        String message = entry.getValues().getMessage();
        if (message == null || message.isEmpty()) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        Locale locale = user.getLocale();
        MessageKey messageKey = MessageKey.of(message);
        String resolved = plugin.getMsg(messageKey, locale);
        if (resolved != null) {
            message = resolved;
        }
        user.sendMessage(message);
    }

    private void giveLootItemXp(ServerPlayerEntity player, Loot entry, @Nullable XpSource source, Skill skill) {
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.BLOCK)) {
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

    private static void removeFreshItemDropsNearBlock(ServerWorld world, BlockPos pos) {
        Vec3d c = pos.toCenterPos();
        Box box = new Box(c, c).expand(0.35);
        for (ItemEntity e : world.getNonSpectatingEntities(ItemEntity.class, box)) {
            e.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private record DeferredBlockLoot(
            ServerWorld world,
            BlockPos pos,
            UUID playerId,
            Skill skill,
            LootTable table,
            @SuppressWarnings("unused") LootDropCause cause,
            @Nullable FabricItemLoot itemLoot,
            @Nullable CommandLoot commandLoot,
            boolean overrideVanilla) {

        boolean matches(ServerWorld sw, BlockPos p, ServerPlayerEntity player) {
            return world == sw && pos.equals(p) && playerId.equals(player.getUuid());
        }

        void apply(FabricBlockLootHandler h) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerId);
            if (player == null) {
                return;
            }
            if (itemLoot != null) {
                h.giveBlockItemLoot(world, pos, player, itemLoot, skill, table);
            } else if (commandLoot != null) {
                h.giveCommandLoot(player, commandLoot, skill);
            }
        }
    }
}
