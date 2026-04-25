package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.message.type.MenuMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.item.FabricUnclaimedItemsMerge;
import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit {@code SkillsRootCommand#onClaimItems} opens a Slate {@code UnclaimedItemsMenu}. **Sprint
 * 56** added a bounded **merge** command; **Sprint 71** adds a **Granite 6×9** menu with per-stack
 * claim + **claim all**, reusing {@link dev.aurelium.auraskills.fabric.item.FabricUnclaimedItemsMerge}.
 */
public final class FabricClaimItemsCommands {

    private FabricClaimItemsCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> claimItemsLiteral() {
        return literal("claimitems")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.CLAIMITEMS, true))
                .then(literal("all").executes(FabricClaimItemsCommands::executeMergeAll))
                .executes(FabricClaimItemsCommands::executeDefault);
    }

    /** In-game: open menu when non-empty; on failure, merge. {@code /claimitems all}: one-shot merge. */
    private static int executeDefault(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(
                    Text.translatable("auraskills.granite.command.claimitems.players_only"));
            return 0;
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            User user = p.getUserManager().getUser(player.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
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
                                source.sendFeedback(() -> Text.literal(line), true);
                                return 1;
                            }
                            try {
                                FabricAuraSkillsMenus.openUnclaimedItems(player, p, 0);
                            } catch (Throwable t) {
                                p.logger().warn("Failed to open unclaimed items menu", t);
                                return mergeAndFeedback(p, user, player, source);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            source.sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeMergeAll(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(
                    Text.translatable("auraskills.granite.command.claimitems.players_only"));
            return 0;
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> mergeCommandBody(p, player, source))
                .orElseGet(
                        () -> {
                            source.sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int mergeCommandBody(
            FabricAuraSkills p, ServerPlayerEntity player, ServerCommandSource source) {
        User user = p.getUserManager().getUser(player.getUuid());
        if (user == null) {
            source.sendError(
                    Text.literal(
                            p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        return mergeAndFeedback(p, user, player, source);
    }

    private static int mergeAndFeedback(
            FabricAuraSkills p, User user, ServerPlayerEntity player, ServerCommandSource source) {
        user.clearInvalidItems();
        if (user.getUnclaimedItems().isEmpty()) {
            String line =
                    p.getPrefix(user.getLocale())
                            + p.getMessageProvider()
                                    .applyFormatting(
                                            p.getMsg(
                                                    CommandMessage.CLAIMITEMS_NO_ITEMS,
                                                    user.getLocale()));
            source.sendFeedback(() -> Text.literal(line), true);
            return 1;
        }
        int touch = FabricUnclaimedItemsMerge.mergeAllInOrder(p, user, player);
        int sizeAfter = user.getUnclaimedItems().size();
        if (sizeAfter == 0) {
            source.sendFeedback(
                    () -> Text.translatable(
                            "auraskills.granite.command.claimitems" + ".all_claimed"),
                    true);
            return 1;
        }
        if (touch > 0) {
            source.sendFeedback(
                    () -> Text.translatable(
                            "auraskills.granite.command.claimitems" + ".partial",
                            sizeAfter,
                            touch),
                    true);
            return 1;
        }
        source.sendFeedback(
                () -> Text.literal(
                        p.getMessageProvider()
                                .applyFormatting(
                                        p.getMsg(
                                                MenuMessage.INVENTORY_FULL,
                                                user.getLocale()))),
                true);
        return 0;
    }
}
