package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.message.type.MenuMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.data.KeyIntPair;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit {@code SkillsRootCommand#onClaimItems} is menu-based (Slate). **Sprint 56** — a bounded
 * **Fabric** path that **merges** unclaimed stacks into the player inventory using the same
 * **{@link dev.aurelium.auraskills.fabric.item.FabricItemRegistry#tryMergeRegisteredItemCount}** logic
 * as {@code /item give} overflow.
 */
public final class FabricClaimItemsCommands {

    private FabricClaimItemsCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> claimItemsLiteral() {
        return literal("claimitems")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.CLAIMITEMS, true))
                .executes(FabricClaimItemsCommands::execute);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
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
                            int sizeBefore = user.getUnclaimedItems().size();
                            int touch = runClaim(p, user, player);
                            int sizeAfter = user.getUnclaimedItems().size();
                            if (sizeAfter == 0) {
                                source.sendFeedback(
                                        () -> Text.translatable(
                                                "auraskills.granite.command.claimitems"
                                                        + ".all_claimed"),
                                        true);
                                return 1;
                            }
                            if (touch > 0) {
                                source.sendFeedback(
                                        () -> Text.translatable(
                                                "auraskills.granite.command.claimitems"
                                                        + ".partial",
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
                                                                    user
                                                                            .getLocale()))),
                                    true);
                            return 0;
                        })
                .orElseGet(
                        () -> {
                            source.sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    /**
     * Merges unclaimed entries in-order; mutates the list. Returns a rough count of <strong>entry
     * operations</strong> that delivered at least one item (full or partial).
     */
    private static int runClaim(
            FabricAuraSkills p, User user, ServerPlayerEntity player) {
        int touch = 0;
        var list = user.getUnclaimedItems();
        for (int i = 0; i < list.size(); ) {
            KeyIntPair pair = list.get(i);
            final NamespacedId id;
            try {
                id = NamespacedId.fromDefault(pair.getKey());
            } catch (IllegalArgumentException ex) {
                list.remove(i);
                user.setShouldSave(true);
                continue;
            }
            if (!p.getFabricItemRegistry().containsItem(id)) {
                list.remove(i);
                user.setShouldSave(true);
                continue;
            }
            int n = pair.getValue();
            int notFit = p.getFabricItemRegistry().tryMergeRegisteredItemCount(player, id, n);
            if (notFit == 0) {
                list.remove(i);
                user.setShouldSave(true);
                touch++;
                continue;
            }
            if (notFit < n) {
                pair.setValue(notFit);
                user.setShouldSave(true);
                touch++;
            }
            i++;
        }
        return touch;
    }
}
