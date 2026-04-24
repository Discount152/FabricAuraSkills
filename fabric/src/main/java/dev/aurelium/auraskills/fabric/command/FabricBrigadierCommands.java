package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.MessageBuilder;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricAuraSkillsMod;
import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Single registration entry for Granite Skills Brigadier commands (**Sprint 39**).
 * Add new roots or {@code /auraskills} subcommands as private {@code register*} methods
 * or separate package-local classes as the tree grows.
 */
public final class FabricBrigadierCommands {

    private FabricBrigadierCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        FabricAuraSkillsMod.LOGGER.info(
                "Registering Brigadier: /auraskills (…), /skills, /stats, /mana — see fabric/COMMAND_PARITY_AUDIT.md");

        registerAuraskillsAndAliases(dispatcher);
        registerManaRoot(dispatcher);
    }

    private static void registerAuraskillsAndAliases(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("auraskills")
                .executes(FabricBrigadierCommands::auraskillsUsage)
                .then(literal("help")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.VERSION, true))
                        .executes(FabricBrigadierCommands::auraskillsHelp))
                .then(literal("version")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.VERSION, true))
                        .executes(FabricBrigadierCommands::version))
                .then(literal("skills")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.SKILLS, true))
                        .executes(FabricBrigadierCommands::skills))
                .then(literal("stats")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.STATS, true))
                        .executes(FabricBrigadierCommands::stats))
                .then(literal("reload")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.RELOAD, false))
                        .executes(FabricBrigadierCommands::reload))
                .then(literal("save")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.SAVE, false))
                        .executes(FabricBrigadierCommands::save)));
        dispatcher.register(literal("skills")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.SKILLS, true))
                .executes(FabricBrigadierCommands::skills));
        dispatcher.register(literal("stats")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.STATS, true))
                .executes(FabricBrigadierCommands::stats));
    }

    private static void registerManaRoot(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mana")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.MANA, true))
                .executes(FabricBrigadierCommands::manaSelf));
    }

    private static int auraskillsUsage(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource()
                .sendFeedback(
                        () -> Text.translatable("auraskills.granite.command.auraskills_usage"),
                        false);
        return 1;
    }

    private static int auraskillsHelp(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource()
                .sendFeedback(() -> Text.translatable("auraskills.granite.command.help"), false);
        return 1;
    }

    private static int version(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(p -> {
                    ctx.getSource()
                            .sendFeedback(
                                    () -> Text.literal("Fabric AuraSkills — data: "
                                            + p.getPluginFolder().getAbsolutePath()),
                                    false);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int skills(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(p -> {
                    ServerCommandSource source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                        source.sendError(Text.literal("Players only."));
                        return 0;
                    }
                    FabricAuraSkillsMenus.open(FabricAuraSkillsMenus.Id.SKILLS, player, p);
                    source.sendFeedback(() -> Text.translatable("auraskills.granite.menu.skills_opened"), true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int stats(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(p -> {
                    ServerCommandSource source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                        source.sendError(Text.literal("Players only."));
                        return 0;
                    }
                    FabricAuraSkillsMenus.open(FabricAuraSkillsMenus.Id.STATS, player, p);
                    source.sendFeedback(() -> Text.translatable("auraskills.granite.menu.stats_opened"), true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int reload(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    try {
                        p.reloadFromDisk();
                    } catch (Exception e) {
                        ctx.getSource().sendError(Text.literal("Reload failed: " + e.getMessage()));
                        return 0;
                    }
                    ctx.getSource().sendFeedback(() -> Text.literal("AuraSkills reloaded configs and skill data."), true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int save(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    int n;
                    try {
                        n = p.saveAllUserProfiles();
                    } catch (Exception e) {
                        ctx.getSource().sendError(Text.literal("Save failed: " + e.getMessage()));
                        return 0;
                    }
                    int count = n;
                    ctx.getSource()
                            .sendFeedback(() -> Text.literal("Saved " + count + " loaded player profile(s)."), true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    /**
     * Bukkit {@code /mana} default: show own mana (common {@link dev.aurelium.auraskills.common.commands.ManaCommand}).
     */
    private static int manaSelf(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    ServerCommandSource source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                        source.sendError(
                                Text.literal("Players only. (Console: future target selector / admin subcommands)"));
                        return 0;
                    }
                    User user = p.getUserManager().getUser(player.getUuid());
                    if (user == null) {
                        source.sendError(Text.literal(
                                p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
                        return 0;
                    }
                    MessageBuilder.create(p)
                            .locale(user.getLocale())
                            .prefix()
                            .message(
                                    CommandMessage.MANA_DISPLAY,
                                    "current",
                                    NumberUtil.format1(user.getMana()),
                                    "max",
                                    NumberUtil.format1(user.getMaxMana()))
                            .send(user);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }
}
