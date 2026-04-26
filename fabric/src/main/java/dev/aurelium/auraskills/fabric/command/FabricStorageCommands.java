package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.storage.StorageProvider;
import dev.aurelium.auraskills.common.storage.file.FileStorageProvider;
import dev.aurelium.auraskills.common.storage.sql.SqlStorageProvider;
import dev.aurelium.auraskills.common.user.UserState;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bounded **storage** admin commands: **Sprint 63** — **info**, **save**, **reload** explainer; **Sprint
 * 64** — **mergeskills** / **removeallmodifiers** (Bukkit **console-only** for **confirm**; two-step; pre-userdata
 * backup; online refresh). No JDBC secrets in **info** output.
 */
public final class FabricStorageCommands {

    private static final String ARG_FROM = "from";
    private static final String ARG_TO = "to";

    private FabricStorageCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> storageLiteral() {
        SuggestionProvider<ServerCommandSource> skills =
                FabricCommandSuggestionProviders.enabledSkillKeys();
        return literal("storage")
                .then(
                        literal("info")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.STORAGE, false))
                                .executes(FabricStorageCommands::executeInfo))
                .then(
                        literal("save")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.STORAGE, false))
                                .executes(FabricStorageCommands::executeSave))
                .then(
                        literal("reload")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.STORAGE, false))
                                .executes(FabricStorageCommands::executeReload))
                .then(
                        literal("mergeskills")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.STORAGE, false))
                                .then(
                                        CommandManager.argument(ARG_FROM, StringArgumentType.word())
                                                .suggests(skills)
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_TO,
                                                                        StringArgumentType.word())
                                                                .suggests(skills)
                                                                .executes(
                                                                        FabricStorageCommands
                                                                                ::executeMergePreview)
                                                                .then(
                                                                        literal("confirm")
                                                                                .requires(
                                                                                        FabricCommandRequirements
                                                                                                .permissionOrNonPlayerSource(
                                                                                                        FabricCommandPermissions
                                                                                                                .STORAGE,
                                                                                                        false))
                                                                                .requires(
                                                                                        s ->
                                                                                                s.getEntity()
                                                                                                        == null)
                                                                                .executes(
                                                                                        FabricStorageCommands
                                                                                                ::executeMergeConfirm)))))
                .then(
                        literal("removeallmodifiers")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.STORAGE, false))
                                .executes(FabricStorageCommands::executeRemoveModifiersPreview)
                                .then(
                                        literal("confirm")
                                                .requires(
                                                        FabricCommandRequirements
                                                                .permissionOrNonPlayerSource(
                                                                        FabricCommandPermissions
                                                                                .STORAGE,
                                                                        false))
                                                .requires(s -> s.getEntity() == null)
                                                .executes(
                                                        FabricStorageCommands
                                                                ::executeRemoveModifiersConfirm)));
    }

    private static int executeInfo(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            String backend = describeBackend(p);
                            String shortImpl = p.getStorageProvider().getClass().getSimpleName();
                            String implFq = p.getStorageProvider().getClass().getName();
                            String dataRoot = p.getPluginFolder().getName();
                            int loaded = p.getUserManager().getUserMap().size();
                            String out =
                                    Text.translatable("auraskills.granite.command.storage.info_header")
                                            .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_backend",
                                                            backend,
                                                            shortImpl)
                                                    .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_fqcn",
                                                            implFq)
                                                    .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_data_root",
                                                            dataRoot)
                                                    .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_loaded",
                                                            loaded)
                                                    .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_secrets",
                                                            p.configBoolean(Option.SQL_ENABLED)
                                                                    ? "true"
                                                                    : "false")
                                                    .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_note")
                                                    .getString();
                            source.sendFeedback(() -> Text.literal(out), false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static String describeBackend(FabricAuraSkills p) {
        if (p.getStorageProvider() instanceof FileStorageProvider) {
            return "YAML (file / userdata)";
        }
        if (p.getStorageProvider() instanceof SqlStorageProvider) {
            return "SQL (pooled, MySQL-compatible)";
        }
        return "unknown";
    }

    private static int executeSave(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            try {
                                int n = p.saveAllUserProfiles();
                                source.sendFeedback(
                                        () ->
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".save_done",
                                                        n),
                                        true);
                                return 1;
                            } catch (Exception e) {
                                p.logger().severe("Storage save command failed", e);
                                source.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.storage.save_failed",
                                                String.valueOf(e.getMessage())));
                                return 0;
                            }
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeReload(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            source.sendFeedback(
                                    () ->
                                            Text.translatable(
                                                    "auraskills.granite.command.storage"
                                                            + ".reload_info"),
                                    false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeMergePreview(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String fromR = StringArgumentType.getString(ctx, ARG_FROM);
                            String toR = StringArgumentType.getString(ctx, ARG_TO);
                            Skill from = FabricCommandSkillParser.parse(p, fromR);
                            Skill to = FabricCommandSkillParser.parse(p, toR);
                            if (from == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".merge_unknown_from",
                                                        fromR));
                                return 0;
                            }
                            if (to == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".merge_unknown_to",
                                                        toR));
                                return 0;
                            }
                            if (from.getId().equals(to.getId())) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".merge_same_skill"));
                                return 0;
                            }
                            ServerCommandSource source = ctx.getSource();
                            source.sendFeedback(
                                    () ->
                                            Text.translatable(
                                                    "auraskills.granite.command.storage"
                                                            + ".merge_preview",
                                                    fromR,
                                                    toR,
                                                    from.getId().toString(),
                                                    to.getId().toString()),
                                    false);
                            FabricCommandConfirmation.registerPending(
                                    p,
                                    source,
                                    FabricCommandConfirmation.storageMergePendingKey(from, to));
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeMergeConfirm(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (ctx.getSource().getEntity() != null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".confirm_console_only"));
                                return 0;
                            }
                            String fromR = StringArgumentType.getString(ctx, ARG_FROM);
                            String toR = StringArgumentType.getString(ctx, ARG_TO);
                            Skill fromS = FabricCommandSkillParser.parse(p, fromR);
                            Skill toS = FabricCommandSkillParser.parse(p, toR);
                            if (fromS == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".merge_unknown_from",
                                                        fromR));
                                return 0;
                            }
                            if (toS == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".merge_unknown_to",
                                                        toR));
                                return 0;
                            }
                            if (fromS.getId().equals(toS.getId())) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".merge_same_skill"));
                                return 0;
                            }
                            if (!FabricCommandConfirmation.verifyPendingOrError(
                                    ctx.getSource(),
                                    FabricCommandConfirmation.storageMergePendingKey(
                                            fromS, toS))) {
                                return 0;
                            }
                            return runMergeSkillsConfirm(p, ctx.getSource(), fromS, toS);
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int runMergeSkillsConfirm(
            FabricAuraSkills p, ServerCommandSource source, Skill fromS, Skill toS) {
        final File preBackup;
        try {
            preBackup = p.getBackupProvider().saveBackupSync(true);
            if (preBackup == null) {
                source.sendError(
                        Text.translatable(
                                "auraskills.granite.command.storage.mutation_prereq_skipped"));
                return 0;
            }
        } catch (Exception e) {
            p.logger().severe("Pre-mutation storage backup (UserState) failed", e);
            source.sendError(
                    Text.translatable(
                            "auraskills.granite.command.storage.mutation_prereq_failed"));
            return 0;
        }

        StorageProvider storage = p.getStorageProvider();
        int missing = 0, merged = 0, noop = 0, applyFail = 0;
        try {
            List<UserState> list = storage.loadStates(false, false);
            for (UserState state : list) {
                Entry<Skill, Integer> fromE = findLevelEntry(state, fromS);
                Entry<Skill, Integer> toE = findLevelEntry(state, toS);
                if (fromE == null || toE == null) {
                    missing++;
                    continue;
                }
                if (fromE.getValue() <= toE.getValue()) {
                    noop++;
                    continue;
                }
                state.skillLevels().put(toE.getKey(), fromE.getValue());
                state
                        .skillXp()
                        .put(
                                toE.getKey(),
                                state.skillXp().getOrDefault(fromE.getKey(), 0.0));
                try {
                    storage.applyState(state);
                    merged++;
                } catch (Exception e) {
                    p.logger()
                            .severe(
                                    "mergeskills applyState failed for "
                                            + state.uuid()
                                            + ": "
                                            + e.getMessage(),
                                    e);
                    applyFail++;
                }
            }
        } catch (Exception e) {
            p.logger().severe("mergeskills loadStates/merge failed", e);
            source.sendError(
                    Text.translatable(
                            "auraskills.granite.command.storage.merge_failed",
                            String.valueOf(e.getMessage())));
            return 0;
        }
        FabricCommandStorageUtil.refreshOnlineUsersFromStorage(p);
        int finalMissing = missing;
        int finalNoop = noop;
        int finalMerge = merged;
        int finalFail = applyFail;
        String backupName = preBackup.getName();
        source.sendFeedback(
                () ->
                        Text.translatable(
                                "auraskills.granite.command.storage.merge_done",
                                fromS.getId().toString(),
                                toS.getId().toString(),
                                finalMerge,
                                finalFail,
                                finalMissing,
                                finalNoop,
                                backupName),
                true);
        source.sendFeedback(
                () -> Text.translatable("auraskills.granite.command.storage.mutation_online_note"),
                false);
        return 1;
    }

    private static @Nullable Entry<Skill, Integer> findLevelEntry(UserState state, Skill sk) {
        for (Entry<Skill, Integer> e : state.skillLevels().entrySet()) {
            if (e.getKey().getId().equals(sk.getId())) {
                return e;
            }
        }
        return null;
    }

    private static int executeRemoveModifiersPreview(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            source.sendFeedback(
                                    () ->
                                            Text.translatable(
                                                    "auraskills.granite.command.storage"
                                                            + ".remove_mod_preview"),
                                    false);
                            FabricCommandConfirmation.registerPending(
                                    p,
                                    source,
                                    FabricCommandConfirmation.STORAGE_PENDING_REMOVE_ALL_MODIFIERS);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeRemoveModifiersConfirm(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            if (ctx.getSource().getEntity() != null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".confirm_console_only"));
                                return 0;
                            }
                            if (!FabricCommandConfirmation.verifyPendingOrError(
                                    ctx.getSource(),
                                    FabricCommandConfirmation.STORAGE_PENDING_REMOVE_ALL_MODIFIERS)) {
                                return 0;
                            }
                            final File preBackup;
                            try {
                                preBackup = p.getBackupProvider().saveBackupSync(true);
                                if (preBackup == null) {
                                    ctx.getSource()
                                            .sendError(
                                                    Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".mutation_prereq_skipped"));
                                    return 0;
                                }
                            } catch (Exception e) {
                                p.logger()
                                        .severe("Pre-mutation storage backup (UserState) failed", e);
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".mutation_prereq_failed"));
                                return 0;
                            }
                            StorageProvider storage = p.getStorageProvider();
                            int success = 0, failed = 0, skipped = 0;
                            try {
                                List<UserState> list = storage.loadStates(false, false);
                                for (UserState state : list) {
                                    if (state.statModifiers().isEmpty()
                                            && state.traitModifiers().isEmpty()) {
                                        skipped++;
                                        continue;
                                    }
                                    UserState n =
                                            new UserState(
                                                    state.uuid(),
                                                    state.skillLevels(),
                                                    state.skillXp(),
                                                    Map.of(),
                                                    Map.of(),
                                                    state.mana());
                                    try {
                                        storage.applyState(n);
                                        success++;
                                    } catch (Exception e) {
                                        p.logger()
                                                .severe(
                                                        "removeallmodifiers applyState failed for "
                                                                + state.uuid()
                                                                + ": "
                                                                + e.getMessage(),
                                                        e);
                                        failed++;
                                    }
                                }
                            } catch (Exception e) {
                                p.logger().severe("removeallmodifiers failed", e);
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".remove_mod_failed",
                                                        String.valueOf(e.getMessage())));
                                return 0;
                            }
                            int fs = success, ff = failed, sk = skipped;
                            String bname = preBackup.getName();
                            ctx.getSource()
                                    .sendFeedback(
                                            () ->
                                                    Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".remove_mod_done",
                                                            fs,
                                                            ff,
                                                            sk,
                                                            bname),
                                            true);
                            FabricCommandStorageUtil.refreshOnlineUsersFromStorage(p);
                            ctx.getSource()
                                    .sendFeedback(
                                            () ->
                                                    Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".mutation_online_note"),
                                            false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }
}
