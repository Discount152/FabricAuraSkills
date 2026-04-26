package dev.granitelib.fabric.template;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteTemplateItemsTest {

    @Test
    void firstSlotPrefersItemKey() {
        GraniteTemplateItem back =
                new GraniteTemplateItem(
                        List.of(45),
                        "minecraft:arrow",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.BACK));
        GraniteTemplateItem other =
                new GraniteTemplateItem(
                        List.of(1),
                        "minecraft:stone",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.BACK));
        GraniteMenuTemplate t =
                new GraniteMenuTemplate("x", "t", 6, Optional.empty(), Map.of("back", back, "x", other), Optional.empty());
        assertEquals(45, GraniteTemplateItems.firstSlotByPreferredKeyOrActions(t, "back", List.of(GraniteTemplateActionNames.BACK)).orElseThrow());
    }

    @Test
    void firstSlotFallsBackToAction() {
        GraniteTemplateItem nav =
                new GraniteTemplateItem(
                        List.of(48),
                        "minecraft:arrow",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.PREVIOUS_PAGE));
        GraniteMenuTemplate t =
                new GraniteMenuTemplate(
                        "x",
                        "t",
                        6,
                        Optional.empty(),
                        Map.of("previous", nav),
                        Optional.empty());
        assertEquals(
                48,
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                                t, "missing", List.of(GraniteTemplateActionNames.PREVIOUS_PAGE))
                        .orElseThrow());
    }

    @Test
    void findSourceGridDefinition() {
        GraniteTemplateItem row =
                new GraniteTemplateItem(
                        List.of(10),
                        "minecraft:paper",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.SOURCE_ROW));
        GraniteMenuTemplate t =
                new GraniteMenuTemplate("sources", "s", 6, Optional.empty(), Map.of("foo", row), Optional.empty());
        assertNotNull(GraniteTemplateItems.findSourceGridDefinition(t));
        GraniteTemplateItem keyed =
                new GraniteTemplateItem(
                        List.of(11),
                        "minecraft:paper",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.SOURCE_ROW));
        GraniteMenuTemplate t2 =
                new GraniteMenuTemplate(
                        "sources", "s", 6, Optional.empty(), Map.of("source", keyed, "foo", row), Optional.empty());
        assertEquals(keyed, GraniteTemplateItems.findSourceGridDefinition(t2));
    }

    @Test
    void findLevelGridDefinition() {
        GraniteTemplateItem row =
                new GraniteTemplateItem(
                        List.of(10),
                        "minecraft:lime_dye",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.LEVEL_ROW));
        GraniteMenuTemplate t =
                new GraniteMenuTemplate("lp", "s", 6, Optional.empty(), Map.of("foo", row), Optional.empty());
        assertNotNull(GraniteTemplateItems.findLevelGridDefinition(t));
        GraniteTemplateItem keyed =
                new GraniteTemplateItem(
                        List.of(11),
                        "minecraft:lime_dye",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.LEVEL_ROW));
        GraniteMenuTemplate t2 =
                new GraniteMenuTemplate(
                        "lp", "s", 6, Optional.empty(), Map.of("level", keyed, "foo", row), Optional.empty());
        assertEquals(keyed, GraniteTemplateItems.findLevelGridDefinition(t2));
    }

    @Test
    void findSkillGridDefinition() {
        GraniteTemplateItem row =
                new GraniteTemplateItem(
                        List.of(10),
                        "minecraft:book",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.SKILL_ROW));
        GraniteMenuTemplate t =
                new GraniteMenuTemplate("skills", "s", 6, Optional.empty(), Map.of("foo", row), Optional.empty());
        assertNotNull(GraniteTemplateItems.findSkillGridDefinition(t));
        GraniteTemplateItem keyed =
                new GraniteTemplateItem(
                        List.of(11),
                        "minecraft:book",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.SKILL_ROW));
        GraniteMenuTemplate t2 =
                new GraniteMenuTemplate(
                        "skills", "s", 6, Optional.empty(), Map.of("skill", keyed, "foo", row), Optional.empty());
        assertEquals(keyed, GraniteTemplateItems.findSkillGridDefinition(t2));
    }

    @Test
    void findStatGridDefinition() {
        GraniteTemplateItem row =
                new GraniteTemplateItem(
                        List.of(10),
                        "minecraft:book",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.STAT_ROW));
        GraniteMenuTemplate t =
                new GraniteMenuTemplate("stats", "s", 6, Optional.empty(), Map.of("foo", row), Optional.empty());
        assertNotNull(GraniteTemplateItems.findStatGridDefinition(t));
        GraniteTemplateItem keyed =
                new GraniteTemplateItem(
                        List.of(11),
                        "minecraft:book",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.STAT_ROW));
        GraniteMenuTemplate t2 =
                new GraniteMenuTemplate(
                        "stats", "s", 6, Optional.empty(), Map.of("stat", keyed, "foo", row), Optional.empty());
        assertEquals(keyed, GraniteTemplateItems.findStatGridDefinition(t2));
    }

    @Test
    void findAbilityGridDefinition() {
        GraniteTemplateItem row =
                new GraniteTemplateItem(
                        List.of(11),
                        "minecraft:book",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.ABILITY_ROW));
        GraniteMenuTemplate t =
                new GraniteMenuTemplate("abilities", "a", 6, Optional.empty(), Map.of("foo", row), Optional.empty());
        assertNotNull(GraniteTemplateItems.findAbilityGridDefinition(t));
        GraniteTemplateItem keyed =
                new GraniteTemplateItem(
                        List.of(12),
                        "minecraft:book",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.ABILITY_ROW));
        GraniteMenuTemplate t2 =
                new GraniteMenuTemplate(
                        "abilities",
                        "a",
                        6,
                        Optional.empty(),
                        Map.of("ability", keyed, "foo", row),
                        Optional.empty());
        assertEquals(keyed, GraniteTemplateItems.findAbilityGridDefinition(t2));
    }

    @Test
    void findManaAbilityItemDefinition() {
        GraniteTemplateItem row =
                new GraniteTemplateItem(
                        List.of(31),
                        "minecraft:nether_star",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.MANA_ABILITY_ROW));
        GraniteMenuTemplate t =
                new GraniteMenuTemplate("abilities", "a", 6, Optional.empty(), Map.of("foo", row), Optional.empty());
        assertNotNull(GraniteTemplateItems.findManaAbilityItemDefinition(t));
        GraniteTemplateItem keyed =
                new GraniteTemplateItem(
                        List.of(32),
                        "minecraft:nether_star",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of(GraniteTemplateActionNames.MANA_ABILITY_ROW));
        GraniteMenuTemplate t2 =
                new GraniteMenuTemplate(
                        "abilities",
                        "a",
                        6,
                        Optional.empty(),
                        Map.of("mana_ability", keyed, "foo", row),
                        Optional.empty());
        assertEquals(keyed, GraniteTemplateItems.findManaAbilityItemDefinition(t2));
    }

    @Test
    void actionEquals() {
        GraniteTemplateItem i =
                new GraniteTemplateItem(
                        List.of(0),
                        "minecraft:stone",
                        Optional.empty(),
                        List.of(),
                        OptionalInt.empty(),
                        OptionalInt.empty(),
                        Optional.of("BACK"));
        assertTrue(GraniteTemplateItems.actionEquals(i, GraniteTemplateActionNames.BACK));
    }
}
