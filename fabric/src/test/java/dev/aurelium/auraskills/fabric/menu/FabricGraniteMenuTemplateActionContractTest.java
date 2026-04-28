package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricGraniteMenuTemplateActionContractTest {

    private record MenuCase(String menuId, String resourcePath) {}

    private static final List<MenuCase> BUNDLED = List.of(
            new MenuCase("stat_info", FabricStatInfoTemplateSupport.TEMPLATE_RESOURCE_PATH),
            new MenuCase("sources", FabricSourcesTemplateSupport.TEMPLATE_RESOURCE_PATH),
            new MenuCase("level_progression", FabricLevelProgressionTemplateSupport.TEMPLATE_RESOURCE_PATH),
            new MenuCase("skills", FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH),
            new MenuCase("stats", FabricStatsTemplateSupport.TEMPLATE_RESOURCE_PATH),
            new MenuCase("abilities", FabricAbilitiesTemplateSupport.TEMPLATE_RESOURCE_PATH),
            new MenuCase("leaderboard", FabricLeaderboardTemplateSupport.TEMPLATE_RESOURCE_PATH),
            new MenuCase("unclaimed_items", FabricUnclaimedItemsTemplateSupport.TEMPLATE_RESOURCE_PATH));

    @Test
    void bundledYamlTemplatesPassActionContract() throws Exception {
        for (MenuCase c : BUNDLED) {
            GraniteMenuTemplate template = loadBundled(c.resourcePath, c.menuId + ".yml");
            Optional<String> bad =
                    FabricGraniteMenuTemplateActionContract.validateTemplateActions(c.menuId, template);
            assertTrue(bad.isEmpty(), c.menuId + ": " + bad.orElse(""));
        }
    }

    @Test
    void skillsYamlWithActionTypoFailsContract() throws Exception {
        String yaml = readBundled(FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH);
        String broken = yaml.replace("action: skill_row", "action: skill_ro");
        GraniteMenuTemplate template = GraniteMenuTemplateYamlLoader.loadString(broken, "skills.yml");
        Optional<String> bad = FabricGraniteMenuTemplateActionContract.validateTemplateActions("skills", template);
        assertTrue(bad.isPresent());
    }

    @Test
    void navigationAndRowCategoryHelpers() {
        assertTrue(
                FabricGraniteMenuTemplateActionContract.isNavigationNormalized(
                        GraniteTemplateActionNames.BACK));
        assertTrue(
                FabricGraniteMenuTemplateActionContract.isNavigationNormalized(
                        GraniteTemplateActionNames.CLOSE));
        assertTrue(
                FabricGraniteMenuTemplateActionContract.isRowOrPanelNormalized(
                        GraniteTemplateActionNames.SKILL_ROW));
        assertFalse(FabricGraniteMenuTemplateActionContract.isNavigationNormalized(GraniteTemplateActionNames.SKILL_ROW));
        assertFalse(
                FabricGraniteMenuTemplateActionContract.isRowOrPanelNormalized(GraniteTemplateActionNames.BACK));
    }

    @Test
    void unknownMenuIdSkipsValidation() {
        GraniteMenuTemplate t =
                GraniteMenuTemplateYamlLoader.loadString(
                        "id: custom\n"
                                + "title: x\n"
                                + "rows: 6\n"
                                + "items: {}\n",
                        "custom.yml");
        assertEquals(Optional.empty(), FabricGraniteMenuTemplateActionContract.validateTemplateActions("custom", t));
    }

    private static GraniteMenuTemplate loadBundled(String resourcePath, String label) throws Exception {
        return GraniteMenuTemplateYamlLoader.loadString(readBundled(resourcePath), label);
    }

    private static String readBundled(String resourcePath) throws Exception {
        try (InputStream in =
                FabricGraniteMenuTemplateActionContractTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(in, "missing " + resourcePath);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}