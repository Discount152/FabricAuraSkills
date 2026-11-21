package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.command.FabricSourceSortType;
import dev.granitelib.fabric.menu.GraniteLoadedMenuRegistry;
import dev.granitelib.fabric.menu.GraniteMenuContext;
import dev.granitelib.fabric.menu.GraniteMenuListener;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import net.minecraft.server.level.ServerPlayer;

/**
 * Library registry façade for the eight built-in menus. Ordinary opens route through the
 * TemplateSupport / gameplay GUI path (dynamic data, paging, sorting, claims, navigation).
 * {@link #loadDefinition} updates both the library registry and
 * {@link FabricGraniteMenuTemplateRegistry} so the next ordinary open uses the new template.
 */
public final class FabricGraniteLibMenuBridge {

    private static final String[] MENU_IDS = {
            FabricMenuKeys.SKILLS,
            FabricMenuKeys.STATS,
            FabricMenuKeys.LEVEL_PROGRESSION,
            FabricMenuKeys.STAT_INFO,
            FabricMenuKeys.SOURCES,
            FabricMenuKeys.ABILITIES,
            FabricMenuKeys.LEADERBOARD,
            FabricMenuKeys.UNCLAIMED_ITEMS
    };

    private final FabricGraniteSkills plugin;
    private final GraniteLoadedMenuRegistry registry = new GraniteLoadedMenuRegistry();
    private final Opener opener = new Opener();
    private String lastOpenedId = "";

    public FabricGraniteLibMenuBridge(FabricGraniteSkills plugin) {
        this.plugin = plugin;
    }

    public void register() {
        syncLoadedDefinitions();
    }

    /** Copies the menu-template registry into the library registry that tracks definitions. */
    public void syncLoadedDefinitions() {
        for (String id : MENU_IDS) {
            plugin.menuTemplateRegistry().getLoadedTemplate(id).ifPresent(loaded ->
                    registry.load(loaded.template(), opener));
        }
    }

    /**
     * Replaces one loaded definition. Ordinary TemplateSupport opens and the next {@link #open}
     * use this template (items, actions, title).
     */
    public void loadDefinition(GraniteMenuTemplate template) {
        registry.load(template, opener);
        plugin.menuTemplateRegistry().putOverride(
                template.id(),
                new FabricGraniteMenuTemplateLoader.LoadedTemplate(template, "bridge-override"));
    }

    public GraniteLoadedMenuRegistry registry() {
        return registry;
    }

    public String lastOpenedId() {
        return lastOpenedId;
    }

    /**
     * Records that an ordinary gameplay open used this library-backed menu id.
     * Called from {@link FabricGraniteSkillsMenus} so command/hub opens update
     * {@link #lastOpenedId()} without requiring a static-layout bridge render.
     */
    public void markOpened(String id) {
        lastOpenedId = id == null ? "" : id;
    }

    /**
     * Opens a menu via the gameplay TemplateSupport path so paging/sort/claims/row actions work.
     * Goes through {@link GraniteLoadedMenuRegistry#open} so the loaded definition is the open source,
     * then dispatches to TemplateSupport / {@code *MenuGui} (not static {@code GraniteMenuLayout.place}).
     */
    public void open(String id, ServerPlayer player, FabricMenuContext context, String sort, int page) {
        int safePage = Math.max(0, page);
        FabricMenuContext ctx = context == null ? FabricMenuContext.none() : context;
        String key = id == null ? "" : id.toLowerCase(java.util.Locale.ROOT);
        markOpened(key);
        if (!key.isEmpty() && registry.template(key) != null) {
            registry.open(
                    key,
                    new GraniteMenuContext<>(
                            new Request(player, ctx, sort == null ? "" : sort, safePage), safePage));
            return;
        }
        openGameplay(key, player, ctx, sort == null ? "" : sort, safePage);
    }

    /** Gameplay open used by {@link Opener} and as fallback when the id is not in the library registry. */
    void openGameplay(String key, ServerPlayer player, FabricMenuContext ctx, String sort, int page) {
        switch (key) {
            case FabricMenuKeys.SKILLS ->
                    FabricGraniteSkillsMenus.openGameplay(
                            FabricMenuKeys.SKILLS, player, plugin, ctx.withListPage(page));
            case FabricMenuKeys.STATS ->
                    FabricGraniteSkillsMenus.openGameplay(
                            FabricMenuKeys.STATS, player, plugin, ctx.withListPage(page));
            case FabricMenuKeys.LEVEL_PROGRESSION -> {
                Skill skill = ctx.skill();
                if (skill != null) {
                    FabricGraniteSkillsMenus.openGameplayLevelProgression(player, plugin, ctx);
                }
            }
            case FabricMenuKeys.STAT_INFO -> {
                Stat stat = ctx.stat();
                if (stat != null) {
                    FabricGraniteSkillsMenus.openGameplayStatInfo(stat, player, plugin, ctx.listPage());
                }
            }
            case FabricMenuKeys.SOURCES -> {
                Skill skill = ctx.skill();
                if (skill != null) {
                    FabricGraniteSkillsMenus.openGameplaySources(
                            skill, player, plugin, parseSort(sort), page, ctx.listPage());
                }
            }
            case FabricMenuKeys.ABILITIES -> {
                Skill skill = ctx.skill();
                if (skill != null) {
                    FabricGraniteSkillsMenus.openGameplayAbilities(skill, player, plugin, ctx.listPage());
                }
            }
            case FabricMenuKeys.LEADERBOARD -> {
                FabricLeaderboardMenuGui.Mode mode = parseLeaderboardMode(sort);
                FabricGraniteSkillsMenus.openGameplayLeaderboard(
                        player, plugin, mode, ctx.skill(), Math.max(1, page));
            }
            case FabricMenuKeys.UNCLAIMED_ITEMS ->
                    FabricGraniteSkillsMenus.openGameplayUnclaimedItems(player, plugin, page);
            default -> plugin.logger().warn("GraniteLib menu bridge: unknown menu id '" + key + "'");
        }
    }

    private static FabricSourceSortType parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return FabricSourceSortType.ASCENDING;
        }
        try {
            return FabricSourceSortType.valueOf(sort.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return FabricSourceSortType.ASCENDING;
        }
    }

    private static FabricLeaderboardMenuGui.Mode parseLeaderboardMode(String sort) {
        if (sort == null || sort.isBlank()) {
            return FabricLeaderboardMenuGui.Mode.POWER;
        }
        try {
            return FabricLeaderboardMenuGui.Mode.valueOf(sort.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return FabricLeaderboardMenuGui.Mode.POWER;
        }
    }

    private final class Opener implements GraniteMenuListener {
        @Override
        public void onOpen(GraniteMenuTemplate template) {
            lastOpenedId = template.id();
        }

        @Override
        public void onOpen(GraniteMenuTemplate template, GraniteMenuContext<?> context) {
            lastOpenedId = template.id();
            if (context == null || !(context.value() instanceof Request request)) {
                return;
            }
            openGameplay(
                    template.id().toLowerCase(java.util.Locale.ROOT),
                    request.player(),
                    request.context(),
                    request.sort(),
                    request.page());
        }

        @Override
        public void onReplaced() {
        }
    }

    private record Request(ServerPlayer player, FabricMenuContext context, String sort, int page) {
    }
}
