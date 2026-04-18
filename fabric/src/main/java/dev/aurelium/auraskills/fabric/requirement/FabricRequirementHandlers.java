package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.math.RomanNumber;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricUser;
import dev.aurelium.auraskills.fabric.item.FabricSkillsItemReader;
import dev.aurelium.auraskills.fabric.trait.FabricTraitWorlds;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

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

    public static void register(FabricAuraSkills plugin) {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                plugin.getFabricRequirementManager().getErrorMessageTimer().remove(handler.player.getUuid()));

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld)) {
                return true;
            }
            if (FabricTraitWorlds.isInDisabledWorld(plugin, sp)) {
                return true;
            }
            if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED)) {
                return true;
            }
            if (plugin.configBoolean(Option.REQUIREMENT_ITEM_PREVENT_TOOL_USE)) {
                ItemStack main = sp.getMainHandStack();
                if (!main.isEmpty() && !FabricSkillsItemReader.meetsRequirements(plugin, sp, main, ModifierType.ITEM)) {
                    sendItemRequirementDenied(plugin, sp, main);
                    return false;
                }
            }
            if (!checkBlock(plugin, sp, Registries.BLOCK.getId(state.getBlock()), BlockContext.BREAK)) {
                return false;
            }
            return true;
        });

        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            if (FabricTraitWorlds.isInDisabledWorld(plugin, sp)) {
                return ActionResult.PASS;
            }
            if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED)) {
                return ActionResult.PASS;
            }
            ItemStack stack = sp.getStackInHand(hand);
            if (!(stack.getItem() instanceof BlockItem)) {
                return ActionResult.PASS;
            }
            ItemPlacementContext ctx = new ItemPlacementContext(sw, sp, hand, stack, hitResult);
            if (!ctx.canPlace()) {
                return ActionResult.PASS;
            }
            if (plugin.configBoolean(Option.REQUIREMENT_ITEM_PREVENT_BLOCK_PLACE)
                    && !FabricSkillsItemReader.meetsRequirements(plugin, sp, stack, ModifierType.ITEM)) {
                sendItemRequirementDenied(plugin, sp, stack);
                return ActionResult.FAIL;
            }
            BlockItem blockItem = (BlockItem) stack.getItem();
            Identifier placedId = Registries.BLOCK.getId(blockItem.getBlock());
            if (!checkBlock(plugin, sp, placedId, BlockContext.PLACE)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((PlayerEntity player, World world, Hand hand) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
                return ActionResult.PASS;
            }
            if (FabricTraitWorlds.isInDisabledWorld(plugin, sp)) {
                return ActionResult.PASS;
            }
            if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED) || !plugin.configBoolean(Option.REQUIREMENT_ITEM_PREVENT_INTERACT)) {
                return ActionResult.PASS;
            }
            ItemStack stack = sp.getStackInHand(hand);
            if (stack.isEmpty()) {
                return ActionResult.PASS;
            }
            if (!FabricSkillsItemReader.meetsRequirements(plugin, sp, stack, ModifierType.ITEM)) {
                sendItemRequirementDenied(plugin, sp, stack);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
                return ActionResult.PASS;
            }
            if (FabricTraitWorlds.isInDisabledWorld(plugin, sp)) {
                return ActionResult.PASS;
            }
            if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED) || !plugin.configBoolean(Option.REQUIREMENT_ITEM_PREVENT_WEAPON_USE)) {
                return ActionResult.PASS;
            }
            ItemStack stack = sp.getStackInHand(Hand.MAIN_HAND);
            if (stack.isEmpty()) {
                return ActionResult.PASS;
            }
            if (!FabricSkillsItemReader.meetsRequirements(plugin, sp, stack, ModifierType.ITEM)) {
                sendItemRequirementDenied(plugin, sp, stack);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    enum BlockContext {
        BREAK, PLACE, HARVEST
    }

    /**
     * @return false if the action must be denied
     */
    public static boolean checkBlock(FabricAuraSkills plugin, ServerPlayerEntity player, Identifier blockId, BlockContext ctx) {
        if (!plugin.configBoolean(Option.REQUIREMENT_BLOCKS_ENABLED)) {
            return true;
        }
        if (player.getAbilities().creativeMode && plugin.configBoolean(Option.REQUIREMENT_BLOCKS_BYPASS_IN_CREATIVE_MODE)) {
            return true;
        }
        if (plugin.configBoolean(Option.REQUIREMENT_BLOCKS_BYPASS_IF_OP)) {
            MinecraftServer server = player.getEntityWorld().getServer();
            if (server != null && server.getPlayerManager().isOperator(new PlayerConfigEntry(player.getGameProfile()))) {
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
                    User user = plugin.getUserManager().getUser(player.getUuid());
                    user.sendMessage(message);
                }
                return false;
            }
        }
        return true;
    }

    public static void sendItemRequirementDenied(FabricAuraSkills plugin, ServerPlayerEntity player, ItemStack item) {
        FabricRequirementManager manager = plugin.getFabricRequirementManager();
        if (manager == null) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        Locale locale = user.getLocale();
        Integer timer = manager.getErrorMessageTimer().get(player.getUuid());
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
                Identifier heldId = Registries.ITEM.getId(item.getItem());
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
        manager.getErrorMessageTimer().put(player.getUuid(), 8);
    }

    /**
     * @return false if the shot must be cancelled (Bukkit {@code EntityShootBowEvent} cancel).
     */
    public static boolean allowProjectileWeaponShot(FabricAuraSkills plugin, ServerPlayerEntity shooter, ItemStack weaponStack) {
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
