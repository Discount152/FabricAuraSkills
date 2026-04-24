package dev.granitelib.fabric;

import dev.granitelib.fabric.menu.GraniteContainerMenus;

/**
 * Entry point for the Fabric-backed GraniteLib bootstrap.
 * <p>
 * Bukkit Slate source (reference while porting) lives in the {@code vendor/Slate} git submodule
 * (<a href="https://github.com/Archy-X/Slate">Archy-X/Slate</a>). See {@code fabric/MENU_AND_COMMANDS_PORT.md}.
 * <p>
 * Menu surface: {@link GraniteContainerMenus#openReadOnly9x6} (read-only 6×9 chest using vanilla
 * {@code GENERIC_9X6} — client + Geyser see a standard large chest).
 */
public final class GraniteFabricBootstrap {

    private GraniteFabricBootstrap() {
    }

    public static String status() {
        return "granitelib-fabric: GraniteContainerMenus (read-only 9x6) + future Slate port";
    }
}
