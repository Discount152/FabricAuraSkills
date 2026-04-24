package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.fabric.FabricAuraSkillsMod;
import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import me.lucko.fabric.api.permissions.v0.Permissions;
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
                "Registering Brigadier: /auraskills (…), /skills, /sk|skill, /stats, /openmenu, leaderboard, /multiplier|/toggle|/abtoggle|/lang, /modifier, /trait, /item (modifier|trait|ignore|requirement|multiplier), /armor (modifier|trait|requirement|multiplier), /mana, /skilllevel, /xp — see fabric/COMMAND_PARITY_AUDIT.md");

        registerAuraskillsAndAliases(dispatcher);
        dispatcher.register(FabricOpenMenuCommands.openMenuRoot());
        dispatcher.register(FabricLeaderboardCommands.topRoot());
        dispatcher.register(FabricLeaderboardCommands.skillTopRoot());
        dispatcher.register(FabricLeaderboardCommands.rankRoot());
        dispatcher.register(FabricLeaderboardCommands.skillRankRoot());
        dispatcher.register(FabricLeaderboardCommands.updateLeaderboardsRoot());
        dispatcher.register(FabricPlayerUtilityCommands.multiplierRoot());
        dispatcher.register(FabricPlayerUtilityCommands.toggleRoot());
        dispatcher.register(FabricPlayerUtilityCommands.abToggleRoot());
        dispatcher.register(FabricPlayerUtilityCommands.langRoot());
        dispatcher.register(FabricPlayerModifierCommands.modifierCommandRoot());
        dispatcher.register(FabricPlayerModifierCommands.traitCommandRoot());
        dispatcher.register(FabricItemArmorModifierCommands.itemCommandRoot());
        dispatcher.register(FabricItemArmorModifierCommands.armorCommandRoot());
        dispatcher.register(aliasSkillsRoot("sk"));
        dispatcher.register(aliasSkillsRoot("skill"));
        dispatcher.register(FabricManaCommands.manaCommandRoot());
        dispatcher.register(FabricSkillLevelCommands.skillLevelRoot());
        dispatcher.register(FabricXpCommands.xpRoot());
    }

    /**
     * Bukkit {@code skills|sk|skill} shape: default opens **Skills** (permission like {@code
     * /skills}). **Sprint 44–45:** {@code openmenu}. **Sprint 46:** reuses the same
     * {@link FabricXpCommands#xpRoot()}, {@link FabricManaCommands#manaCommandRoot()}, and
     * {@link FabricSkillLevelCommands#skillLevelRoot()} <strong>builders</strong> (each
     * registration call produces a <strong>new</strong> node tree) so
     * {@code /sk|skill xp …}, {@code … mana …}, and {@code … skilllevel …} match the
     * standalone roots without duplicating executors. Permissions are unchanged on the shared
     * nodes.
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> aliasSkillsRoot(
            String alias) {
        return literal(alias)
                .executes(FabricBrigadierCommands::skillsAliasDefault)
                .then(FabricOpenMenuCommands.openMenuSubcommand())
                .then(FabricLeaderboardCommands.topRoot())
                .then(FabricLeaderboardCommands.skillTopRoot())
                .then(FabricLeaderboardCommands.rankRoot())
                .then(FabricLeaderboardCommands.skillRankRoot())
                .then(FabricLeaderboardCommands.updateLeaderboardsRoot())
                .then(FabricPlayerUtilityCommands.multiplierRoot())
                .then(FabricPlayerUtilityCommands.toggleRoot())
                .then(FabricPlayerUtilityCommands.abToggleRoot())
                .then(FabricPlayerUtilityCommands.langRoot())
                .then(FabricPlayerModifierCommands.modifierCommandRoot())
                .then(FabricPlayerModifierCommands.traitCommandRoot())
                .then(FabricItemArmorModifierCommands.itemCommandRoot())
                .then(FabricItemArmorModifierCommands.armorCommandRoot())
                .then(FabricXpCommands.xpRoot())
                .then(FabricManaCommands.manaCommandRoot())
                .then(FabricSkillLevelCommands.skillLevelRoot());
    }

    private static int skillsAliasDefault(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!Permissions.check(source, FabricCommandPermissions.SKILLS, true)) {
            source.sendError(Text.translatable("auraskills.granite.command.permission_denied"));
            return 0;
        }
        return skills(ctx);
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
                        .executes(FabricBrigadierCommands::save))
                .then(FabricManaCommands.manaCommandRoot())
                .then(FabricSkillLevelCommands.skillLevelRoot())
                .then(FabricXpCommands.xpRoot())
                .then(FabricOpenMenuCommands.openMenuSubcommand())
                .then(FabricLeaderboardCommands.topRoot())
                .then(FabricLeaderboardCommands.skillTopRoot())
                .then(FabricLeaderboardCommands.rankRoot())
                .then(FabricLeaderboardCommands.skillRankRoot())
                .then(FabricLeaderboardCommands.updateLeaderboardsRoot())
                .then(FabricPlayerUtilityCommands.multiplierRoot())
                .then(FabricPlayerUtilityCommands.toggleRoot())
                .then(FabricPlayerUtilityCommands.abToggleRoot())
                .then(FabricPlayerUtilityCommands.langRoot())
                .then(FabricPlayerModifierCommands.modifierCommandRoot())
                .then(FabricPlayerModifierCommands.traitCommandRoot())
                .then(FabricItemArmorModifierCommands.itemCommandRoot())
                .then(FabricItemArmorModifierCommands.armorCommandRoot()));
        dispatcher.register(literal("skills")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.SKILLS, true))
                .executes(FabricBrigadierCommands::skills));
        dispatcher.register(literal("stats")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.STATS, true))
                .executes(FabricBrigadierCommands::stats));
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

}
