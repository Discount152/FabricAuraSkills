package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bounded **openmenu** for built-in **skills** and **stats** (Sprint 44), Bukkit
 * {@link dev.aurelium.auraskills.bukkit.commands.OpenMenuCommand}-style. Reuse the same {@code
 * literal("openmenu")…} for {@code /openmenu}, {@code /auraskills openmenu}, and {@code /sk
 * openmenu} / {@code /skill openmenu}.
 */
public final class FabricOpenMenuCommands {

    private static final String ARG_MENU = "menu";
    private static final String ARG_TARGET = "target";

    private FabricOpenMenuCommands() {}

    /**
     * Subcommand {@code openmenu <menu> [<target>]}: attach with {@code .then(…)} under a parent
     * root.
     */
    public static LiteralArgumentBuilder<ServerCommandSource> openMenuSubcommand() {
        return literal("openmenu")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.OPEN_MENU, true))
                .then(
                        CommandManager.argument(ARG_MENU, StringArgumentType.word())
                                .suggests(
                                        FabricCommandSuggestionProviders.builtInOpenMenuIds())
                                .executes(FabricOpenMenuCommands::executeForSelf)
                                .then(
                                        CommandManager.argument(
                                                        ARG_TARGET, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .executes(
                                                        FabricOpenMenuCommands::executeForTarget)));
    }

    /** Top-level {@code /openmenu …} (same tree; Fabric-only convenience). */
    public static LiteralArgumentBuilder<ServerCommandSource> openMenuRoot() {
        return openMenuSubcommand();
    }

    private static int executeForSelf(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(
                        p -> {
                            String rawMenu = StringArgumentType.getString(ctx, ARG_MENU);
                            Optional<String> resolved =
                                    FabricCommandBuiltInMenuId.resolveForOpenCommand(rawMenu);
                            if (resolved.isEmpty()) {
                                unknownMenu(ctx);
                                return 0;
                            }
                            String menu = resolved.get();
                            ServerCommandSource s = ctx.getSource();
                            if (!(s.getEntity() instanceof ServerPlayerEntity self)) {
                                s.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.openmenu.console_specify"));
                                return 0;
                            }
                            FabricAuraSkillsMenus.open(menu, self, p);
                            sendOpenFeedback(s, menu, self);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeForTarget(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(
                        p -> {
                            String rawMenu = StringArgumentType.getString(ctx, ARG_MENU);
                            Optional<String> resolved =
                                    FabricCommandBuiltInMenuId.resolveForOpenCommand(rawMenu);
                            if (resolved.isEmpty()) {
                                unknownMenu(ctx);
                                return 0;
                            }
                            String menu = resolved.get();
                            String name = StringArgumentType.getString(ctx, ARG_TARGET);
                            ServerCommandSource s = ctx.getSource();
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(s.getServer(), name)
                                            .orElse(null);
                            if (target == null) {
                                s.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.xp.player_not_found",
                                                name));
                                return 0;
                            }
                            FabricAuraSkillsMenus.open(menu, target, p);
                            sendOpenFeedback(s, menu, target);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static void sendOpenFeedback(
            ServerCommandSource source, String menuId, ServerPlayerEntity menuRecipient) {
        if (source.getEntity() instanceof ServerPlayerEntity srcPlayer
                && srcPlayer.getUuid().equals(menuRecipient.getUuid())) {
            if (FabricMenuKeys.SKILLS.equals(menuId)) {
                source.sendFeedback(
                        () -> Text.translatable("auraskills.granite.menu.skills_opened"), true);
            } else {
                source.sendFeedback(
                        () -> Text.translatable("auraskills.granite.menu.stats_opened"), true);
            }
        } else {
            source.sendFeedback(
                    () -> Text.translatable(
                            "auraskills.granite.command.openmenu.opened_for",
                            menuId,
                            menuRecipient.getName().getString()),
                    true);
        }
    }

    private static void unknownMenu(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource()
                .sendError(
                        Text.translatable("auraskills.granite.command.openmenu.unknown_built_in"));
    }
}
