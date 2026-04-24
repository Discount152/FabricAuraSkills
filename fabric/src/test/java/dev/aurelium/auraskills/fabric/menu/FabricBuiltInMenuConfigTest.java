package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricBuiltInMenuConfigTest {

    @Test
    void parse_graniteTitleKey() throws IOException {
        String yaml = "granite:\n  title_key: test.custom.title\n  size: {rows: 6, columns: 9}\n";
        ConfigurationNode root = load(yaml);
        FabricMenuYamlSlice s =
                FabricBuiltInMenuConfig.parse(root, FabricMenuYamlSlice.defaultSkills(), null, "t.yml");
        assertEquals("test.custom.title", s.titleTranslationKey());
        assertTrue(s.enabled());
    }

    @Test
    void parse_graniteDisabled() throws IOException {
        String yaml = "granite:\n  enabled: false\n";
        ConfigurationNode root = load(yaml);
        FabricMenuYamlSlice s =
                FabricBuiltInMenuConfig.parse(root, FabricMenuYamlSlice.defaultStats(), null, "t.yml");
        assertFalse(s.enabled());
    }

    @Test
    void parse_missingGranite_returnsDefault() throws IOException {
        String yaml = "other: 1\n";
        ConfigurationNode root = load(yaml);
        FabricMenuYamlSlice d = FabricMenuYamlSlice.defaultSkills();
        FabricMenuYamlSlice s = FabricBuiltInMenuConfig.parse(root, d, null, "t.yml");
        assertEquals(d.titleTranslationKey(), s.titleTranslationKey());
    }

    @Test
    void parse_mergesAllFormatKeys() throws IOException {
        String yaml = "granite:\n  format:\n    list_open_detail: a.b.c\n    back: x.y.z\n";
        ConfigurationNode root = load(yaml);
        FabricMenuYamlSlice d = FabricMenuYamlSlice.defaultLevelProgression();
        FabricMenuYamlSlice s = FabricBuiltInMenuConfig.parse(root, d, null, "t.yml");
        assertEquals("a.b.c", s.getFormatKey("list_open_detail", null));
        assertEquals("x.y.z", s.getFormatKey("back", null));
        assertEquals("auraskills.granite.menu.level_progression_lore_level", s.getFormatKey("lore_level", null));
    }

    @Test
    void parse_layoutListRange() throws IOException {
        String yaml = "granite:\n  layout:\n    list_start_slot: 9\n    list_end_slot_exclusive: 18\n";
        ConfigurationNode root = load(yaml);
        FabricMenuYamlSlice s =
                FabricBuiltInMenuConfig.parse(root, FabricMenuYamlSlice.defaultSkills(), null, "t.yml");
        assertEquals(9, s.listStartSlot());
        assertEquals(18, s.listEndSlotExclusive());
        assertEquals(4, s.mainSlot());
        assertEquals(49, s.backSlot());
    }

    @Test
    void parse_layoutInvalidListRange_usesDefaultListRange() throws IOException {
        String yaml = "granite:\n  layout:\n    list_start_slot: 20\n    list_end_slot_exclusive: 10\n";
        ConfigurationNode root = load(yaml);
        FabricMenuYamlSlice d = FabricMenuYamlSlice.defaultSkills();
        FabricMenuYamlSlice s = FabricBuiltInMenuConfig.parse(root, d, null, "t.yml");
        assertEquals(d.listStartSlot(), s.listStartSlot());
        assertEquals(d.listEndSlotExclusive(), s.listEndSlotExclusive());
    }

    @Test
    void parse_layoutMainEqualsBack_usesDefaultMainAndBack() throws IOException {
        String yaml = "granite:\n  layout:\n    main_slot: 4\n    back_slot: 4\n";
        ConfigurationNode root = load(yaml);
        FabricMenuYamlSlice d = FabricMenuYamlSlice.defaultLevelProgression();
        FabricMenuYamlSlice s = FabricBuiltInMenuConfig.parse(root, d, null, "t.yml");
        assertEquals(d.mainSlot(), s.mainSlot());
        assertEquals(d.backSlot(), s.backSlot());
    }

    @Test
    void parse_itemsListMainBack() throws IOException {
        String yaml =
                "granite:\n  items:\n    list: minecraft:writable_book\n    main: minecraft:emerald\n    back: minecraft:stick\n";
        ConfigurationNode root = load(yaml);
        FabricMenuYamlSlice s =
                FabricBuiltInMenuConfig.parse(root, FabricMenuYamlSlice.defaultSkills(), null, "t.yml");
        assertEquals("minecraft:writable_book", s.listItemId());
        assertEquals("minecraft:emerald", s.mainItemId());
        assertEquals("minecraft:stick", s.backItemId());
    }

    @Test
    void parse_itemsInvalidIdFormat_usesDefaultList() throws IOException {
        String yaml = "granite:\n  items:\n    list: '!!! not an id'\n";
        ConfigurationNode root = load(yaml);
        FabricMenuYamlSlice d = FabricMenuYamlSlice.defaultStats();
        FabricMenuYamlSlice s = FabricBuiltInMenuConfig.parse(root, d, null, "t.yml");
        assertEquals(d.listItemId(), s.listItemId());
    }

    private static ConfigurationNode load(String yaml) throws IOException {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .source(() -> new BufferedReader(new StringReader(yaml)))
                .build();
        return loader.load();
    }
}
