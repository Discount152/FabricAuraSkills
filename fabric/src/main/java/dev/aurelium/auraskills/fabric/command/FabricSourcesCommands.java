package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.source.SourceLoader;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricSourceMenuItems;
import dev.aurelium.auraskills.fabric.menu.FabricXpSourceMenuDisplay;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit {@code SkillsRootCommand#onSources} opens the Slate <strong>sources</strong> menu. **Sprint
 * 57** added a paged <strong>chat</strong> listing. **Sprint 69** — in-game players open a Fabric
 * 6×9 {@link dev.aurelium.auraskills.fabric.menu.FabricSourcesMenuGui}; console/RCON and fallback keep
 * the same chat listing. Shared list/open logic: {@link FabricSourcesOpenSupport}.
 */
public final class FabricSourcesCommands {

    private static final String ARG_SKILL = "skill";
    private static final String ARG_SORT = "sort";

    private FabricSourcesCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> sourcesLiteral() {
        return literal("sources")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SOURCES, true))
                .then(
                        CommandManager.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .executes(ctx -> run(ctx, FabricSourceSortType.ASCENDING))
                                .then(
                                        CommandManager.argument(
                                                        ARG_SORT, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .sourceSortTypes())
                                                .executes(
                                                        FabricSourcesCommands::runWithSortArg)))
                .then(literal("diagnose")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.RELOAD, false))
                        .then(CommandManager.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .executes(FabricSourcesCommands::runDiagnose)));
    }

    private static int runWithSortArg(CommandContext<ServerCommandSource> ctx) {
        String sortRaw = StringArgumentType.getString(ctx, ARG_SORT);
        return FabricSourceSortType.fromTokenStrict(sortRaw)
                .map(s -> run(ctx, s))
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(
                                            FabricGraniteText.key(
                                                    "auraskills.granite.command.sources.bad_sort",
                                                    sortRaw));
                            return 0;
                        });
    }

    private static int run(
            CommandContext<ServerCommandSource> ctx, FabricSourceSortType sort) {
        ServerCommandSource source = ctx.getSource();
        String skillRaw = StringArgumentType.getString(ctx, ARG_SKILL);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerPlayerEntity player =
                                    source.getEntity() instanceof ServerPlayerEntity sp
                                            ? sp
                                            : null;
                            Locale lo = localeFor(p, player);
                            Skill skill = FabricCommandSkillParser.parse(p, skillRaw);
                            if (skill == null) {
                                String body =
                                        p.getMessageProvider()
                                                .applyFormatting(
                                                        p.getMsg(CommandMessage.UNKNOWN_SKILL, lo));
                                source.sendError(Text.literal(body));
                                return 0;
                            }
                            // /sources has no “from level progression” list page: Back uses 0.
                            return FabricSourcesOpenSupport.runGuiOrChatFor(
                                    p, source, player, lo, skill, sort, 0);
                        })
                .orElseGet(
                        () -> {
                            source.sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int runDiagnose(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        String skillRaw = StringArgumentType.getString(ctx, ARG_SKILL);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    Skill skill = FabricCommandSkillParser.parse(p, skillRaw);
                    if (skill == null) {
                        source.sendError(Text.literal("Unknown skill: " + skillRaw));
                        return 0;
                    }

                    SourceLoader.SourceLoadDiagnostics diagnostics = p.getSkillManager().getSourceDiagnostics(skill);
                    if (diagnostics == null) {
                        source.sendError(Text.literal("No source diagnostics are available yet for " + skill.getId()));
                        return 0;
                    }

                    Locale lo = localeFor(p, source.getEntity() instanceof ServerPlayerEntity sp ? sp : null);
                    var loaded = p.getSkillManager().getSkill(skill);
                    int loadedCount = loaded.sources().size();
                    var visible = FabricSourcesListSupport.buildSorted(p, loaded, FabricSourceSortType.ASCENDING, lo);
                    int visibleCount = visible.size();
                    int renderedOnPage = Math.min(visibleCount, 28);
                    FabricSourcesRuntimeDiagnostics.RenderSnapshot snapshot =
                            FabricSourcesRuntimeDiagnostics.snapshotFor(skill);
                    int finalSurvivedCount = snapshot == null ? 0 : snapshot.survivedCount();
                    int finalRenderedCount = snapshot == null ? 0 : snapshot.renderedCount();

                    source.sendFeedback(() -> Text.literal("Source diagnostics written to server log for " + skill.getId().getSimpleName() + "."), false);
                    source.sendFeedback(
                            () -> Text.literal(
                                    "Summary: loaded="
                                            + loadedCount
                                            + ", versionValid="
                                            + diagnostics.versionValidSources()
                                            + ", visible="
                                            + visibleCount
                                            + ", rendered="
                                            + finalRenderedCount
                                            + ", survived="
                                            + finalSurvivedCount
                                            + ", rejected="
                                            + diagnostics.rejectedSources().size()),
                            false);

                    StringJoiner log = new StringJoiner(System.lineSeparator());
                    log.add("[/sources diagnose] skill=" + diagnostics.skillId());
                    log.add("diskFileExists=" + diagnostics.diskFileExists() + " path=" + diagnostics.diskPath());
                    log.add("diskSourceKeys=" + diagnostics.diskSourceKeyCount()
                            + ", embeddedFileExists=" + diagnostics.embeddedFileExists()
                            + ", embeddedSourceKeys=" + diagnostics.embeddedSourceKeyCount()
                            + ", mergedSourceKeys=" + diagnostics.mergedSourceKeyCount());
                    log.add("versionValid=" + diagnostics.versionValidSources()
                            + ", loadedSkillSources=" + loadedCount
                            + ", visible=" + visibleCount
                            + ", renderedPlanPage1=" + renderedOnPage
                            + ", renderedFinal=" + finalRenderedCount
                            + ", survivedFinal=" + finalSurvivedCount
                            + ", embeddedFallbackUsed=" + diagnostics.embeddedFallbackUsed());

                    for (XpSource xpSource : loaded.sources()) {
                        if (!xpSource.isVersionValid()) {
                            continue;
                        }
                        int idxInVisible = visible.indexOf(xpSource);
                        boolean listed = idxInVisible >= 0;
                        int slot = listed ? sourceSlotInPage(idxInVisible) : -1;
                        String sourceId = xpSource.getId().toString();
                        String sourceTypeStr =
                                xpSource.getType() == null ? "unknown" : xpSource.getType().getId().toString();
                        String rawName = xpSource.getDisplayName(lo);
                        String effectiveName = FabricXpSourceMenuDisplay.effectiveRowTitle(p, xpSource, lo);
                        boolean titleOk = effectiveName != null && !effectiveName.isBlank();
                        boolean menuStackOk = menuItemNonEmpty(p, xpSource);
                        String material = resolveDisplayMaterial(p, xpSource);
                        boolean renderable = listed && titleOk;
                        String reason =
                                renderable
                                        ? "ok"
                                        : !listed
                                                ? "not_listed_after_sort"
                                                : !titleOk
                                                        ? "empty_effective_title"
                                                        : "unknown";
                        log.add(
                                "source id="
                                        + sourceId
                                        + ", type="
                                        + sourceTypeStr
                                        + ", xp="
                                        + xpSource.getXp()
                                        + ", versionValid=true"
                                        + ", rawDisplayName="
                                        + rawName
                                        + ", effectiveDisplayName="
                                        + effectiveName
                                        + ", fabricTitleFallback="
                                        + FabricXpSourceMenuDisplay.usedFallbackFabricTitle(p, xpSource, lo)
                                        + ", listedInSortedMenuList="
                                        + listed
                                        + ", menuItemNonEmpty="
                                        + menuStackOk
                                        + ", materialResolved="
                                        + material
                                        + ", renderable="
                                        + renderable
                                        + ", reason="
                                        + reason
                                        + ", assignedSlot=" + slot);
                    }

                    if (snapshot != null) {
                        for (FabricSourcesRuntimeDiagnostics.RowResult row : snapshot.rows()) {
                            log.add("final row slot=" + row.plannedSlot()
                                    + ", plannedItem=" + row.plannedItemId()
                                    + ", plannedName=" + row.plannedDisplayName()
                                    + ", actualItem=" + row.actualItemIdAfterRender()
                                    + ", actualName=" + row.actualDisplayNameAfterRender()
                                    + ", survived=" + row.survivedFinalMutation());
                        }
                    } else {
                        log.add("final row inspection: unavailable (open /sources " + skill.getId().getSimpleName() + " first)");
                    }

                    if (!diagnostics.fatalError().isEmpty()) {
                        log.add("fatalError=" + diagnostics.fatalError());
                    }
                    if (diagnostics.rejectedSources().isEmpty()) {
                        log.add("rejectedSources=none");
                    } else {
                        for (Map.Entry<String, String> entry : diagnostics.rejectedSources().entrySet()) {
                            log.add("rejected source=" + entry.getKey() + " reason=" + entry.getValue());
                        }
                    }
                    p.logger().info(log.toString());
                    return 1;
                })
                .orElseGet(() -> {
                    source.sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int sourceSlotInPage(int indexInPage) {
        int gridCols = 7;
        int row = 1 + indexInPage / gridCols;
        int col = 1 + indexInPage % gridCols;
        return row * 9 + col;
    }

    private static boolean menuItemNonEmpty(FabricAuraSkills plugin, XpSource source) {
        if (plugin.getItemRegistry().getSourceMenuItems() instanceof FabricSourceMenuItems fsmi) {
            ItemStack menuItem = fsmi.getMenuItem(source);
            return menuItem != null && !menuItem.isEmpty();
        }
        return false;
    }

    private static String resolveDisplayMaterial(FabricAuraSkills plugin, XpSource source) {
        if (plugin.getItemRegistry().getSourceMenuItems() instanceof FabricSourceMenuItems fsmi) {
            ItemStack menuItem = fsmi.getMenuItem(source);
            if (menuItem != null && !menuItem.isEmpty()) {
                return Registries.ITEM.getId(menuItem.getItem()).toString();
            }
        }
        return "template_builtin_no_registry_item";
    }

    private static Locale localeFor(FabricAuraSkills p, @Nullable ServerPlayerEntity player) {
        if (player == null) {
            return p.getDefaultLanguage();
        }
        User user = p.getUserManager().getUser(player.getUuid());
        return user != null ? user.getLocale() : p.getDefaultLanguage();
    }
}
