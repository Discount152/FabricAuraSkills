package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.menu.MenuHelper;

/**
 * Slate menus are not available on Fabric in the MVP; return safe fallbacks.
 */
public final class FabricMenuHelper implements MenuHelper {

    @Override
    public String getFormat(String menuName, String formatName) {
        return "";
    }
}
