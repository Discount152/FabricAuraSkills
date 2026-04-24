package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public final class FabricAuraSkillsCommands {

    private FabricAuraSkillsCommands() {
    }

    public static void registerEarly() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        FabricAuraSkillsMod.LOGGER.info("Registering /auraskills and /skills|stats (version, help, skills/stats GUI, reload, save)");
        dispatcher.register(literal("auraskills")
                .executes(FabricAuraSkillsCommands::auraskillsUsage)
                .then(literal("help")
                        .requires(src -> Permissions.check(src, "auraskills.command.version", true))
                        .executes(FabricAuraSkillsCommands::auraskillsHelp))
                .then(literal("version")
                        .requires(src -> Permissions.check(src, "auraskills.command.version", true))
                        .executes(FabricAuraSkillsCommands::version))
                .then(literal("skills")
                        .requires(src -> Permissions.check(src, "auraskills.command.skills", true))
                        .executes(FabricAuraSkillsCommands::skills))
                .then(literal("stats")
                        .requires(src -> Permissions.check(src, "auraskills.command.stats", true))
                        .executes(FabricAuraSkillsCommands::stats))
                .then(literal("reload")
                        .requires(src -> Permissions.check(src, "auraskills.command.reload", false))
                        .executes(FabricAuraSkillsCommands::reload))
                .then(literal("save")
                        .requires(src -> Permissions.check(src, "auraskills.command.save", false))
                        .executes(FabricAuraSkillsCommands::save)));
        dispatcher.register(literal("skills")
                .requires(src -> Permissions.check(src, "auraskills.command.skills", true))
                .executes(FabricAuraSkillsCommands::skills));
        dispatcher.register(literal("stats")
                .requires(src -> Permissions.check(src, "auraskills.command.stats", true))
                .executes(FabricAuraSkillsCommands::stats));
    }

    private static int auraskillsUsage(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(
                () -> Text.literal("Use: /auraskills <version|help|skills|stats|reload|save>  —  /skills / /stats = GUIs (temporary)"),
                false);
        return 1;
    }

    private static int auraskillsHelp(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(
                () -> Text.literal("Fabric AuraSkills: /auraskills version | /skills (GUI) | /stats (GUI) | reload | save (staff)"),
                false);
        return 1;
    }

    private static int version(CommandContext<ServerCommandSource> ctx) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
            return 0;
        }
        ctx.getSource().sendFeedback(() -> Text.literal("Fabric AuraSkills — data: " + plugin.getPluginFolder().getAbsolutePath()), false);
        return 1;
    }

    private static int skills(CommandContext<ServerCommandSource> ctx) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
            return 0;
        }
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Players only."));
            return 0;
        }
        // GraniteLib read-only 9×6. Temporary: /skills + /auraskills skills.
        FabricAuraSkillsMenus.open(FabricAuraSkillsMenus.Id.SKILLS, player, plugin);
        source.sendFeedback(() -> Text.translatable("auraskills.granite.menu.skills_opened"), true);
        return 1;
    }

    private static int stats(CommandContext<ServerCommandSource> ctx) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
            return 0;
        }
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Players only."));
            return 0;
        }
        FabricAuraSkillsMenus.open(FabricAuraSkillsMenus.Id.STATS, player, plugin);
        source.sendFeedback(() -> Text.translatable("auraskills.granite.menu.stats_opened"), true);
        return 1;
    }

    private static int reload(CommandContext<ServerCommandSource> ctx) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
            return 0;
        }
        try {
            plugin.reloadFromDisk();
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("Reload failed: " + e.getMessage()));
            return 0;
        }
        ctx.getSource().sendFeedback(() -> Text.literal("AuraSkills reloaded configs and skill data."), true);
        return 1;
    }

    private static int save(CommandContext<ServerCommandSource> ctx) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
            return 0;
        }
        int n;
        try {
            n = plugin.saveAllUserProfiles();
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("Save failed: " + e.getMessage()));
            return 0;
        }
        int count = n;
        ctx.getSource().sendFeedback(() -> Text.literal("Saved " + count + " loaded player profile(s)."), true);
        return 1;
    }
}
