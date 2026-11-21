package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.message.type.MenuMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.item.FabricUnclaimedItemsMerge;
import dev.granite.graniteskills.fabric.menu.FabricGraniteSkillsMenus;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

/**
 * Bukkit {@code SkillsRootCommand#onClaimItems} opens a Slate {@code UnclaimedItemsMenu}. **Sprint
 * 56** added a bounded **merge** command; **Sprint 71** adds a **Granite 6×9** menu with per-stack
 * claim + **claim all**, reusing {@link dev.granite.graniteskills.fabric.item.FabricUnclaimedItemsMerge}.
 */
public final class FabricClaimItemsCommands {

    private FabricClaimItemsCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> claimItemsLiteral() {
        return literal("claimitems")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.CLAIMITEMS, true))
                .then(literal("all").executes(FabricClaimItemsCommands::executeMergeAll))
                .executes(FabricClaimItemsCommands::executeDefault);
    }

    /** In-game: open menu when non-empty; on failure, merge. {@code /claimitems all}: one-shot merge. */
    private static int executeDefault(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(
                    FabricGraniteText.key("graniteskills.granite.command.claimitems.players_only"));
            return 0;
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            User user = p.getUserManager().getUser(player.getUUID());
                            if (user == null) {
                                source.sendFailure(
                                        Component.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            user.clearInvalidItems();
                            if (user.getUnclaimedItems().isEmpty()) {
                                String line =
                                        p.getPrefix(user.getLocale())
                                                + p.getMessageProvider()
                                                        .applyFormatting(
                                                                p.getMsg(
                                                                        CommandMessage
                                                                                .CLAIMITEMS_NO_ITEMS,
                                                                        user.getLocale()));
                                source.sendSuccess(() -> Component.literal(line), true);
                                return 1;
                            }
                            try {
                                FabricGraniteSkillsMenus.openUnclaimedItems(player, p, 0);
                            } catch (Throwable t) {
                                p.logger().warn("Failed to open unclaimed items menu", t);
                                return mergeAndFeedback(p, user, player, source);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            source.sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeMergeAll(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(
                    FabricGraniteText.key("graniteskills.granite.command.claimitems.players_only"));
            return 0;
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> mergeCommandBody(p, player, source))
                .orElseGet(
                        () -> {
                            source.sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int mergeCommandBody(
            FabricGraniteSkills p, ServerPlayer player, CommandSourceStack source) {
        User user = p.getUserManager().getUser(player.getUUID());
        if (user == null) {
            source.sendFailure(
                    Component.literal(
                            p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        return mergeAndFeedback(p, user, player, source);
    }

    private static int mergeAndFeedback(
            FabricGraniteSkills p, User user, ServerPlayer player, CommandSourceStack source) {
        user.clearInvalidItems();
        if (user.getUnclaimedItems().isEmpty()) {
            String line =
                    p.getPrefix(user.getLocale())
                            + p.getMessageProvider()
                                    .applyFormatting(
                                            p.getMsg(
                                                    CommandMessage.CLAIMITEMS_NO_ITEMS,
                                                    user.getLocale()));
            source.sendSuccess(() -> Component.literal(line), true);
            return 1;
        }
        int touch = FabricUnclaimedItemsMerge.mergeAllInOrder(p, user, player);
        int sizeAfter = user.getUnclaimedItems().size();
        if (sizeAfter == 0) {
            source.sendSuccess(
                    () -> FabricGraniteText.key(
                            "graniteskills.granite.command.claimitems" + ".all_claimed"),
                    true);
            return 1;
        }
        if (touch > 0) {
            source.sendSuccess(
                    () -> FabricGraniteText.key(
                            "graniteskills.granite.command.claimitems" + ".partial",
                            sizeAfter,
                            touch),
                    true);
            return 1;
        }
        source.sendSuccess(
                () -> Component.literal(
                        p.getMessageProvider()
                                .applyFormatting(
                                        p.getMsg(
                                                MenuMessage.INVENTORY_FULL,
                                                user.getLocale()))),
                true);
        return 0;
    }
}
