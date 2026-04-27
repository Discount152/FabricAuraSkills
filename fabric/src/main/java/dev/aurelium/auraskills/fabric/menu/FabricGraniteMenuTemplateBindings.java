package dev.aurelium.auraskills.fabric.menu;

import java.util.List;

/**
 * Maps each known Granite menu template id to the classpath anchor and bundled resource path used by {@link
 * FabricGraniteMenuTemplateLoader}. Keep aligned with {@link FabricGraniteBundledMenuTemplateIds#ALL}.
 */
public final class FabricGraniteMenuTemplateBindings {

    public record Binding(String menuId, Class<?> classpathAnchor, String bundledClasspathPath) {}

    public static final List<Binding> ALL =
            List.of(
                    new Binding(
                            "stat_info",
                            FabricStatInfoTemplateSupport.class,
                            FabricStatInfoTemplateSupport.TEMPLATE_RESOURCE_PATH),
                    new Binding(
                            "sources",
                            FabricSourcesTemplateSupport.class,
                            FabricSourcesTemplateSupport.TEMPLATE_RESOURCE_PATH),
                    new Binding(
                            "level_progression",
                            FabricLevelProgressionTemplateSupport.class,
                            FabricLevelProgressionTemplateSupport.TEMPLATE_RESOURCE_PATH),
                    new Binding(
                            "skills",
                            FabricSkillsTemplateSupport.class,
                            FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH),
                    new Binding(
                            "stats",
                            FabricStatsTemplateSupport.class,
                            FabricStatsTemplateSupport.TEMPLATE_RESOURCE_PATH),
                    new Binding(
                            "abilities",
                            FabricAbilitiesTemplateSupport.class,
                            FabricAbilitiesTemplateSupport.TEMPLATE_RESOURCE_PATH),
                    new Binding(
                            "leaderboard",
                            FabricLeaderboardTemplateSupport.class,
                            FabricLeaderboardTemplateSupport.TEMPLATE_RESOURCE_PATH),
                    new Binding(
                            "unclaimed_items",
                            FabricUnclaimedItemsTemplateSupport.class,
                            FabricUnclaimedItemsTemplateSupport.TEMPLATE_RESOURCE_PATH));

    private FabricGraniteMenuTemplateBindings() {}
}
