package dev.granitelib.fabric.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraniteTemplateActionNamesTest {

    @Test
    void contractMatchesBundledYamlAuthors() {
        assertEquals("back", GraniteTemplateActionNames.BACK);
        assertEquals("stat_panel", GraniteTemplateActionNames.STAT_PANEL);
        assertEquals("source_row", GraniteTemplateActionNames.SOURCE_ROW);
        assertEquals("previous_page", GraniteTemplateActionNames.PREVIOUS_PAGE);
        assertEquals("next_page", GraniteTemplateActionNames.NEXT_PAGE);
        assertEquals("cycle_sort", GraniteTemplateActionNames.CYCLE_SORT);
    }
}
