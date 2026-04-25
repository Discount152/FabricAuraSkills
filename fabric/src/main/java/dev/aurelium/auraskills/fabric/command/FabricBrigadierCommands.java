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
 * <p>
 * **Sprint 65:** {@code skills|sk|skill} (Bukkit {@code %skills_alias%}) share one subtree from
 * {@link #skillsAliasSharedTree} so {@code /skills} cannot drift from {@code /sk} and {@code
 * /skill}.
 */
public final class FabricBrigadierCommands {

    private FabricBrigadierCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        FabricAuraSkillsMod.LOGGER.info(
                "Registering Brigadier (Sprint 65–71): /auraskills (…), /skills|/sk|/skill share mirror subtrees, /stats, /openmenu (Sprint 70: sources; Sprint 71: unclaimed_items), /claimitems (Sprint 71: menu + all), /sources, /resethealth, /transfer, /profile, /backup, /preset, /storage, leaderboard, /multiplier|/toggle|/abtoggle|/lang, /modifier, /trait, /item (…), /armor (…), /mana, /skilllevel, /manaability, /antiafk, /xp, + per-skill shortcuts (Sprint 68) — see fabric/COMMAND_PARITY_AUDIT.md");

        registerAuraskillsAndAliases(dispatcher);
        FabricSkillShortcutCommands.register(dispatcher);
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
        dispatcher.register(FabricClaimItemsCommands.claimItemsLiteral());
        dispatcher.register(FabricSourcesCommands.sourcesLiteral());
        dispatcher.register(FabricResetHealthCommands.resetHealthLiteral());
        dispatcher.register(FabricTransferCommands.transferLiteral());
        dispatcher.register(FabricProfileCommands.profileLiteral());
        dispatcher.register(FabricBackupCommands.backupLiteral());
        dispatcher.register(FabricPresetCommands.presetLiteral());
        dispatcher.register(FabricStorageCommands.storageLiteral());
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
    /**
     * Bukkit {@code skills|sk|skill} share the same ACF class; this tree must match for every
     * literal. **Sprint 65** — was missing from {@code /skills} (only {@code /sk|skill} and
     * {@code /auraskills} had the full set).
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource>
            skillsAliasSharedTree(
                    com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource>
                            root) {
        return root.then(FabricOpenMenuCommands.openMenuSubcommand())
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
                .then(FabricSkillLevelCommands.skillLevelRoot())
                .then(FabricManaAbilityCommands.manaAbilityRoot())
                .then(FabricAntiAfkCommands.antiafkRoot())
                .then(FabricClaimItemsCommands.claimItemsLiteral())
                .then(FabricSourcesCommands.sourcesLiteral())
                .then(FabricResetHealthCommands.resetHealthLiteral())
                .then(FabricTransferCommands.transferLiteral())
                .then(FabricProfileCommands.profileLiteral())
                .then(FabricBackupCommands.backupLiteral())
                .then(FabricPresetCommands.presetLiteral())
                .then(FabricStorageCommands.storageLiteral());
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> aliasSkillsRoot(
            String alias) {
        return skillsAliasSharedTree(
                literal(alias).executes(FabricBrigadierCommands::skillsAliasDefault));
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
                .then(FabricManaAbilityCommands.manaAbilityRoot())
                .then(FabricAntiAfkCommands.antiafkRoot())
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
                .then(FabricItemArmorModifierCommands.armorCommandRoot())
                .then(FabricClaimItemsCommands.claimItemsLiteral())
                .then(FabricSourcesCommands.sourcesLiteral())
                .then(FabricResetHealthCommands.resetHealthLiteral())
                .then(FabricTransferCommands.transferLiteral())
                .then(FabricProfileCommands.profileLiteral())
                .then(FabricBackupCommands.backupLiteral())
                .then(FabricPresetCommands.presetLiteral())
                .then(FabricStorageCommands.storageLiteral()));
        dispatcher.register(
                skillsAliasSharedTree(
                        literal("skills")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.SKILLS, true))
                                .executes(FabricBrigadierCommands::skills)));
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
