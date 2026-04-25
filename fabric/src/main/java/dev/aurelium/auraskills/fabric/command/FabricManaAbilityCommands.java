package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit {@link dev.aurelium.auraskills.bukkit.commands.ManaAbilityCommand} — **Sprint 66.**
 * Subcommand of {@code %skills_alias%} and {@code /auraskills}. Does **not** register a
 * top-level root (reference is {@code /sk manaability …} only).
 * <p>
 * <strong>vs {@code /toggle} and {@code /abtoggle}:</strong> those toggle the **IDLE** action bar
 * line only ({@code ActionBarType.IDLE}, Sprint 48). This command **admin-resets a mana
 * ability cooldown** (and optional mana fill) for an <strong>other</strong> online player — same
 * Bukkit ACF {@code @Flags("other")} rule (no in-game self-target).
 * <p>
 * Bukkit fires {@code ManaAbilityRefreshEvent} when the cooldown is cleared; **api-bukkit** has no
 * Fabric analogue — cooldown is set to 0 in {@link User} data only (see
 * {@link dev.aurelium.auraskills.fabric.FabricTimerCountdown}).
 */
public final class FabricManaAbilityCommands {

    private static final String ARG_PLAYER = "player";
    private static final String ARG_ABILITY = "ability";
    private static final String ARG_FILL_MANA = "fill_mana";

    private FabricManaAbilityCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> manaAbilityRoot() {
        return literal("manaability")
                .then(
                        literal("resetcooldown")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions
                                                        .MANAABILITY_RESETCOOLDOWN,
                                                false))
                                .then(
                                        CommandManager.argument(
                                                        ARG_PLAYER, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_ABILITY,
                                                                        StringArgumentType.word())
                                                                .suggests(
                                                                        FabricCommandSuggestionProviders
                                                                                .enabledManaAbilities())
                                                                .executes(
                                                                        ctx ->
                                                                                executeReset(
                                                                                        ctx,
                                                                                        false))
                                                                .then(
                                                                        CommandManager.argument(
                                                                                        ARG_FILL_MANA,
                                                                                        bool())
                                                                                .executes(
                                                                                        ctx ->
                                                                                                executeReset(
                                                                                                        ctx,
                                                                                                        getBool(
                                                                                                                ctx,
                                                                                                                ARG_FILL_MANA)))))));
    }

    private static int executeReset(CommandContext<ServerCommandSource> ctx, boolean fillMana) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            String name =
                                    StringArgumentType.getString(ctx, ARG_PLAYER);
                            String abRaw = StringArgumentType.getString(ctx, ARG_ABILITY);
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(
                                                    source.getServer(), name)
                                            .orElse(null);
                            if (target == null) {
                                source.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.xp"
                                                        + ".player_not_found",
                                                name));
                                return 0;
                            }
                            if (source.getEntity() instanceof ServerPlayerEntity self
                                    && self.getUuid().equals(target.getUuid())) {
                                source.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command"
                                                        + ".manaability.other_target_only"));
                                return 0;
                            }
                            ManaAbility ability = FabricCommandManaAbilityParser.parse(p, abRaw);
                            if (ability == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMessageProvider()
                                                        .applyFormatting(
                                                                p.getMsg(
                                                                        CommandMessage
                                                                                .UNKNOWN_MANA_ABILITY,
                                                                        p.getDefaultLanguage()))));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMessageProvider()
                                                        .applyFormatting(
                                                                p.getMsg(
                                                                        CommandMessage
                                                                                .NO_PROFILE,
                                                                        p.getDefaultLanguage()))));
                                return 0;
                            }
                            Locale lo = user.getLocale();
                            ManaAbilityData data = user.getManaAbilityData(ability);
                            if (data.getCooldown() > 0 && !data.isActivated()) {
                                data.setCooldown(0);
                            }
                            if (fillMana) {
                                user.setMana(user.getMaxMana());
                            }
                            user.setShouldSave(true);
                            String line =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    TextUtil.replace(
                                                            p.getMsg(
                                                                    CommandMessage
                                                                            .MANAABILITY_RESETCOOLDOWN_COOLDOWN_RESET,
                                                                    lo),
                                                            "{player}",
                                                            target.getName().getString(),
                                                            "{ability}",
                                                            ability.getDisplayName(lo)));
                            String out = p.getPrefix(lo) + line;
                            source.sendFeedback(() -> Text.literal(out), true);
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
