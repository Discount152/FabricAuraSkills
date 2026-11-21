package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.fabric.FabricGraniteSkillsMod;
import dev.granite.graniteskills.fabric.menu.FabricGraniteSkillsMenus;
import dev.granite.graniteskills.fabric.menu.FabricMenuUserReadiness;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

/**
 * Single registration entry for Granite Skills Brigadier commands (**Sprint 39**).
 * Add new roots or {@code /graniteskills} subcommands as private {@code register*} methods
 * or separate package-local classes as the tree grows.
 * <p>
 * **Sprint 65:** {@code skills|sk|skill} (Bukkit {@code %skills_alias%}) share one subtree from
 * {@link #skillsAliasSharedTree} so {@code /skills} cannot drift from {@code /sk} and {@code
 * /skill}.
 */
public final class FabricBrigadierCommands {

    private FabricBrigadierCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        FabricGraniteSkillsMod.LOGGER.info(
                "Registering Brigadier: /graniteskills (…), /skills|/sk|/skill (mirrored; Granite **item**/**xp** only under these + /graniteskills — vanilla /item /xp unchanged; Sprint 84–85 root usage on item/storage/backup/preset; **Sprint 108–110** **templates** **export** / **status** + guarded **overwrite**), /stats, /openmenu, /claimitems, /sources, /resethealth, /transfer, /profile, /backup, /preset, /storage, leaderboards, /multiplier|/toggle|/abtoggle|/lang, /modifier, /trait, /armor, /mana, /skilllevel, /manaability, /antiafk, per-skill shortcuts — see fabric/COMMAND_PARITY_AUDIT.md");

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
        // Top-level /item and /xp are vanilla MC commands — register Granite trees only under
        // /graniteskills, /skills, /sk, /skill (see Sprint 84).
        dispatcher.register(FabricItemArmorModifierCommands.armorCommandRoot());
        dispatcher.register(aliasSkillsRoot("sk"));
        dispatcher.register(aliasSkillsRoot("skill"));
        dispatcher.register(FabricManaCommands.manaCommandRoot());
        dispatcher.register(FabricSkillLevelCommands.skillLevelRoot());
        // /xpdiag is not a vanilla command; register it top-level too so admins do not have to
        // remember the /graniteskills prefix (Sprint 146).
        dispatcher.register(FabricXpDiagnosticsCommands.xpDiagRoot());
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
     * {@code /graniteskills} had the full set).
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack>
            skillsAliasSharedTree(
                    com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack>
                            root) {
        return root.then(literal("help")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.VERSION, true))
                        .executes(FabricBrigadierCommands::graniteskillsHelp))
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
                        .requires(FabricCommandRequirements.permissionOrNonPlayerSource(
                                FabricCommandPermissions.RELOAD, false))
                        .executes(FabricBrigadierCommands::reload))
                .then(literal("save")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.SAVE, false))
                        .executes(FabricBrigadierCommands::save))
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
                .then(FabricXpDiagnosticsCommands.xpDiagRoot())
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
                .then(FabricStorageCommands.storageLiteral())
                .then(FabricGraniteTemplateCommands.templatesLiteral());
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> aliasSkillsRoot(
            String alias) {
        return skillsAliasSharedTree(
                literal(alias).executes(FabricBrigadierCommands::skillsAliasDefault));
    }

    private static int skillsAliasDefault(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!Permissions.check(source, FabricCommandPermissions.SKILLS, true)) {
            source.sendFailure(
                    FabricGraniteText.key("graniteskills.granite.command.permission_denied"));
            return 0;
        }
        return skills(ctx);
    }

    private static void registerAuraskillsAndAliases(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(skillsAliasSharedTree(
                literal("graniteskills").executes(FabricBrigadierCommands::graniteskillsUsage)));
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

    private static int graniteskillsUsage(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource()
                .sendSuccess(
                        () -> FabricGraniteText.key("graniteskills.granite.command.graniteskills_usage"),
                        false);
        return 1;
    }

    private static int graniteskillsHelp(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource()
                .sendSuccess(
                        () -> FabricGraniteText.key("graniteskills.granite.command.help"), false);
        return 1;
    }

    private static int version(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(p -> {
                    ctx.getSource()
                            .sendSuccess(
                                    () -> Component.literal("Fabric GraniteSkills — data: "
                                            + p.getPluginFolder().getAbsolutePath()),
                                    false);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    private static int skills(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(p -> {
                    CommandSourceStack source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(Component.literal("Players only."));
                        return 0;
                    }
                    if (FabricMenuUserReadiness.loadedUserOrWarn(p, player) == null) {
                        return 0;
                    }
                    FabricGraniteSkillsMenus.open(FabricGraniteSkillsMenus.Id.SKILLS, player, p);
                    source.sendSuccess(
                            () -> FabricGraniteText.key("graniteskills.granite.menu.skills_opened"), true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    private static int stats(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(p -> {
                    CommandSourceStack source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(Component.literal("Players only."));
                        return 0;
                    }
                    if (FabricMenuUserReadiness.loadedUserOrWarn(p, player) == null) {
                        return 0;
                    }
                    FabricGraniteSkillsMenus.open(FabricGraniteSkillsMenus.Id.STATS, player, p);
                    source.sendSuccess(
                            () -> FabricGraniteText.key("graniteskills.granite.menu.stats_opened"), true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    try {
                        p.reloadFromDisk();
                    } catch (Exception e) {
                        ctx.getSource().sendFailure(Component.literal("Reload failed: " + e.getMessage()));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal("GraniteSkills reloaded configs and skill data."), true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    private static int save(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    int n;
                    try {
                        n = p.saveAllUserProfiles();
                    } catch (Exception e) {
                        ctx.getSource().sendFailure(Component.literal("Save failed: " + e.getMessage()));
                        return 0;
                    }
                    int count = n;
                    ctx.getSource()
                            .sendSuccess(() -> Component.literal("Saved " + count + " loaded player profile(s)."), true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

}
