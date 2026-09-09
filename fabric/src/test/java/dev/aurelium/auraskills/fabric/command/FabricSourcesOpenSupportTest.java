package dev.aurelium.auraskills.fabric.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricSourcesOpenSupportTest {

    @Test
    void emptyWarningNotUsedWhenVersionValidSourcesExist() {
        assertTrue(FabricSourcesOpenSupport.shouldShowVisibleButNotRenderableWarning(3, 0));
    }

    @Test
    void emptyWarningUsedOnlyWhenTrulyEmpty() {
        assertFalse(FabricSourcesOpenSupport.shouldShowVisibleButNotRenderableWarning(0, 0));
        assertFalse(FabricSourcesOpenSupport.shouldShowVisibleButNotRenderableWarning(2, 2));
    }
}