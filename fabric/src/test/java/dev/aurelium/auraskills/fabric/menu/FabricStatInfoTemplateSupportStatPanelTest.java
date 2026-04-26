package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricStatInfoTemplateSupportStatPanelTest {

    @Test
    void statKeyIsStatPanel() {
        GraniteTemplateItem def = new GraniteTemplateItem(
                List.of(0), "minecraft:book", Optional.empty(), List.of(), OptionalInt.empty(), OptionalInt.empty(), Optional.empty());
        assertTrue(FabricStatInfoTemplateSupport.isStatPanelItem("stat", def));
    }

    @Test
    void statPanelActionMarksPanel() {
        GraniteTemplateItem def = new GraniteTemplateItem(
                List.of(0),
                "minecraft:book",
                Optional.empty(),
                List.of(),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(GraniteTemplateActionNames.STAT_PANEL));
        assertTrue(FabricStatInfoTemplateSupport.isStatPanelItem("main", def));
    }

    @Test
    void backItemIsNotStatPanel() {
        GraniteTemplateItem def = new GraniteTemplateItem(
                List.of(45),
                "minecraft:arrow",
                Optional.empty(),
                List.of(),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(GraniteTemplateActionNames.BACK));
        assertFalse(FabricStatInfoTemplateSupport.isStatPanelItem("back", def));
    }
}
