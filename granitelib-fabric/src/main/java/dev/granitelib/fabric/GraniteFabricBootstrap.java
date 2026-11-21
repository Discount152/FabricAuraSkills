package dev.granitelib.fabric;

import dev.granitelib.fabric.menu.GraniteContainerMenus;

/**
 * Entry point for GraniteLib on Fabric.
 * <p>
 * Menu surface: {@link GraniteContainerMenus#openReadOnly9x6} and
 * {@link GraniteContainerMenus#openClickable9x6} (vanilla {@code GENERIC_9x6}).
 * Read-only YAML templates live in {@code dev.granitelib.fabric.template}.
 */
public final class GraniteFabricBootstrap {

    private GraniteFabricBootstrap() {
    }

    public static String status() {
        return "GraniteLib: 6×9 menus and YAML templates";
    }
}
