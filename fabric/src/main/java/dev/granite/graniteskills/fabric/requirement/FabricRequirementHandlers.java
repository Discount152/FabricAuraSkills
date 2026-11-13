package dev.granite.graniteskills.fabric.requirement;

import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.math.RomanNumber;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.item.FabricSkillsItemReader;
import dev.granite.graniteskills.fabric.trait.FabricTraitWorlds;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Locale;
import java.util.Map;

/**
 * Block and hand-item requirement gates (Bukkit {@code RequirementListener}).
 * <p>
 * Harvest ({@code allow_harvest}) is enforced via targeted block mixins for vanilla harvest paths
 * (sweet berries, bee nest/hive honey, cave vine glow berries); other modded harvest flows are not covered.
 */
public final class FabricRequirementHandlers {

    private FabricRequirementHandlers() {
    }

    public static void register(FabricGraniteSkills plugin) {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                plugin.getFabricRequirementManager().getErrorMessageTimer().remove(handler.player.getUUID()));

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel)) {
                return true;
            }
            if (FabricTraitWorlds.isInDisabledWorld(plugin, sp)) {
                return true;
            }
            if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED)) {
                return true;
            }
            if (plugin.configBoolean(Option.REQUIREMENT_ITEM_PREVENT_TOOL_USE)) {
                ItemStack main = sp.getMainHandItem();
                if (!main.isEmpty() && !FabricSkillsItemReader.meetsRequirements(plugin, sp, main, ModifierType.ITEM)) {
                    sendItemRequirementDenied(plugin, sp, main);
                    return false;
                }
            }
            if (!checkBlock(plugin, sp, BuiltInRegistries.BLOCK.getKey(state.getBlock()), BlockContext.BREAK)) {
                return false;
            }
            return true;
        });

        UseBlockCallback.EVENT.register((Player player, Level world, InteractionHand hand, BlockHitResult hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel sw)) {
                return InteractionResult.PASS;
            }
            if (FabricTraitWorlds.isInDisabledWorld(plugin, sp)) {
                return InteractionResult.PASS;
            }
            if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED)) {
                return InteractionResult.PASS;
            }
            ItemStack stack = sp.getItemInHand(hand);
            if (!(stack.getItem() instanceof BlockItem)) {
                return InteractionResult.PASS;
            }
            BlockPlaceContext ctx = new BlockPlaceContext(sw, sp, hand, stack, hitResult);
            if (!ctx.canPlace()) {
                return InteractionResult.PASS;
            }
            if (plugin.configBoolean(Option.REQUIREMENT_ITEM_PREVENT_BLOCK_PLACE)
                    && !FabricSkillsItemReader.meetsRequirements(plugin, sp, stack, ModifierType.ITEM)) {
                sendItemRequirementDenied(plugin, sp, stack);
                return InteractionResult.FAIL;
            }
            BlockItem blockItem = (BlockItem) stack.getItem();
            Identifier placedId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
            if (!checkBlock(plugin, sp, placedId, BlockContext.PLACE)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            if (FabricTraitWorlds.isInDisabledWorld(plugin, sp)) {
                return InteractionResult.PASS;
            }
            if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED) || !plugin.configBoolean(Option.REQUIREMENT_ITEM_PREVENT_INTERACT)) {
                return InteractionResult.PASS;
            }
            ItemStack stack = sp.getItemInHand(hand);
            if (stack.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (!FabricSkillsItemReader.meetsRequirements(plugin, sp, stack, ModifierType.ITEM)) {
                sendItemRequirementDenied(plugin, sp, stack);
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            if (FabricTraitWorlds.isInDisabledWorld(plugin, sp)) {
                return InteractionResult.PASS;
            }
            if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED) || !plugin.configBoolean(Option.REQUIREMENT_ITEM_PREVENT_WEAPON_USE)) {
                return InteractionResult.PASS;
            }
            ItemStack stack = sp.getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (!FabricSkillsItemReader.meetsRequirements(plugin, sp, stack, ModifierType.ITEM)) {
                sendItemRequirementDenied(plugin, sp, stack);
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }

    enum BlockContext {
        BREAK, PLACE, HARVEST
    }

    /**
     * @return false if the action must be denied
     */
    public static boolean checkBlock(FabricGraniteSkills plugin, ServerPlayer player, Identifier blockId, BlockContext ctx) {
        if (!plugin.configBoolean(Option.REQUIREMENT_BLOCKS_ENABLED)) {
            return true;
        }
        if (player.getAbilities().instabuild && plugin.configBoolean(Option.REQUIREMENT_BLOCKS_BYPASS_IN_CREATIVE_MODE)) {
            return true;
        }
        if (plugin.configBoolean(Option.REQUIREMENT_BLOCKS_BYPASS_IF_OP)) {
            MinecraftServer server = player.level().getServer();
            if (server != null && server.getPlayerList().isOp(new NameAndId(player.getGameProfile()))) {
                return true;
            }
        }
        FabricBlockRequirement match = null;
        for (FabricBlockRequirement block : plugin.getFabricRequirementManager().getBlocks()) {
            if (block.blockId().equals(blockId)) {
                match = block;
                break;
            }
        }
        if (match == null) {
            return true;
        }
        if (ctx == BlockContext.BREAK && match.allowBreak()) {
            return true;
        }
        if (ctx == BlockContext.PLACE && match.allowPlace()) {
            return true;
        }
        if (ctx == BlockContext.HARVEST && match.allowHarvest()) {
            return true;
        }
        for (FabricRequirementNode node : match.nodes()) {
            if (!node.check(player)) {
                String message = node.getDenyMessage();
                if (message != null && !message.isEmpty()) {
                    User user = plugin.getUserManager().getUser(player.getUUID());
                    user.sendMessage(message);
                }
                return false;
            }
        }
        return true;
    }

    public static void sendItemRequirementDenied(FabricGraniteSkills plugin, ServerPlayer player, ItemStack item) {
        FabricRequirementManager manager = plugin.getFabricRequirementManager();
        if (manager == null) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        Locale locale = user.getLocale();
        Integer timer = manager.getErrorMessageTimer().get(player.getUUID());
        if (timer != null && timer != 0) {
            return;
        }
        StringBuilder requirementsString = new StringBuilder();
        Map<Skill, Integer> requirementMap = FabricSkillsItemReader.getRequirements(plugin, item, ModifierType.ITEM);
        for (Map.Entry<Skill, Integer> entry : requirementMap.entrySet()) {
            requirementsString.append(TextUtil.replace(plugin.getMsg(CommandMessage.ITEM_REQUIREMENT_ENTRY, locale),
                    "{skill}", entry.getKey().getDisplayName(locale), "{level}", RomanNumber.toRoman(entry.getValue(), plugin)));
        }
        if (!plugin.configBoolean(Option.REQUIREMENT_OVERRIDE_GLOBAL) || requirementMap.isEmpty()) {
            for (FabricGlobalRequirement global : manager.getGlobalRequirementsType(ModifierType.ITEM)) {
                Identifier heldId = BuiltInRegistries.ITEM.getKey(item.getItem());
                if (!global.itemId().equals(heldId)) {
                    continue;
                }
                for (Map.Entry<Skill, Integer> entry : global.requirements().entrySet()) {
                    requirementsString.append(TextUtil.replace(plugin.getMsg(CommandMessage.ITEM_REQUIREMENT_ENTRY, locale),
                            "{skill}", entry.getKey().getDisplayName(locale), "{level}", RomanNumber.toRoman(entry.getValue(), plugin)));
                }
            }
        }
        if (requirementsString.length() >= 2) {
            requirementsString.delete(requirementsString.length() - 2, requirementsString.length());
        }
        user.sendMessage(plugin.getPrefix(locale) + TextUtil.replace(plugin.getMsg(CommandMessage.ITEM_REQUIREMENT_USE, locale),
                "{requirements}", requirementsString.toString()));
        manager.getErrorMessageTimer().put(player.getUUID(), 8);
    }

    /**
     * @return false if the shot must be cancelled (Bukkit {@code EntityShootBowEvent} cancel).
     */
    public static boolean allowProjectileWeaponShot(FabricGraniteSkills plugin, ServerPlayer shooter, ItemStack weaponStack) {
        if (FabricTraitWorlds.isInDisabledWorld(plugin, shooter)) {
            return true;
        }
        if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED) || !plugin.configBoolean(Option.REQUIREMENT_ITEM_PREVENT_WEAPON_USE)) {
            return true;
        }
        if (weaponStack.isEmpty()) {
            return true;
        }
        if (!FabricSkillsItemReader.meetsRequirements(plugin, shooter, weaponStack, ModifierType.ITEM)) {
            sendItemRequirementDenied(plugin, shooter, weaponStack);
            return false;
        }
        return true;
    }
}
