package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.menu.MenuHelper;

/**
 * Slate menus are not available on Fabric in the MVP; return safe fallbacks.
 */
public final class FabricMenuHelper implements MenuHelper {

    @Override
    public String getFormat(String menuName, String formatName) {
        return "";
    }
}
