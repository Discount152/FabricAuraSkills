package dev.granite.graniteskills.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public final class FabricGraniteSkillsCommands {

    private FabricGraniteSkillsCommands() {
    }

    public static void registerEarly() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerRoot(dispatcher, "graniteskills");
    }

    private static void registerRoot(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        dispatcher.register(literal(root)
                .then(literal("version")
                        .requires(src -> Permissions.check(src, "graniteskills.command.version", true))
                        .executes(FabricGraniteSkillsCommands::version))
                .then(literal("skills")
                        .requires(src -> Permissions.check(src, "graniteskills.command.skills", true))
                        .executes(FabricGraniteSkillsCommands::skills))
                .then(literal("reload")
                        .requires(src -> Permissions.check(src, "graniteskills.command.reload", false))
                        .executes(FabricGraniteSkillsCommands::reload))
                .then(literal("save")
                        .requires(src -> Permissions.check(src, "graniteskills.command.save", false))
                        .executes(FabricGraniteSkillsCommands::save)));
    }

    private static int version(CommandContext<CommandSourceStack> ctx) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            ctx.getSource().sendFailure(Component.literal("Granite Skills is still starting."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Granite Skills — data: " + plugin.getPluginFolder().getAbsolutePath()), false);
        return 1;
    }

    private static int skills(CommandContext<CommandSourceStack> ctx) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            ctx.getSource().sendFailure(Component.literal("Granite Skills is still starting."));
            return 0;
        }
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        var user = plugin.getUserManager().getUser(player.getUUID());
        StringBuilder sb = new StringBuilder("Skills: ");
        for (var skill : plugin.getSkillManager().getSkillValues()) {
            sb.append(skill.name().toLowerCase()).append(" L").append(user.getSkillLevel(skill)).append("  ");
        }
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            ctx.getSource().sendFailure(Component.literal("Granite Skills is still starting."));
            return 0;
        }
        try {
            plugin.reloadFromDisk();
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Reload failed: " + e.getMessage()));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Granite Skills reloaded configs and skill data."), true);
        return 1;
    }

    private static int save(CommandContext<CommandSourceStack> ctx) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            ctx.getSource().sendFailure(Component.literal("Granite Skills is still starting."));
            return 0;
        }
        int n;
        try {
            n = plugin.saveAllUserProfiles();
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Save failed: " + e.getMessage()));
            return 0;
        }
        int count = n;
        ctx.getSource().sendSuccess(() -> Component.literal("Saved " + count + " loaded player profile(s)."), true);
        return 1;
    }
}
