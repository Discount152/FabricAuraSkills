package dev.granitelib.fabric;

import dev.granitelib.fabric.menu.GraniteContainerMenus;

/**
 * Entry point for the Fabric-backed GraniteLib bootstrap.
 * <p>
 * Bukkit Slate source (reference while porting) lives in the {@code vendor/Slate} git submodule
 * (<a href="https://github.com/Archy-X/Slate">Archy-X/Slate</a>). See {@code fabric/MENU_AND_COMMANDS_PORT.md}.
 * <p>
 * Menu surface: {@link GraniteContainerMenus#openReadOnly9x6},
 * {@link GraniteContainerMenus#openClickable9x6} (vanilla {@code GENERIC_9X6} — Geyser-friendly
 * large chest). Path A Spike 0 adds read-only YAML templates in {@code dev.granitelib.fabric.template}
 * (not wired to screens yet — see {@code fabric/SPRINT_95_GRANITELIB_TEMPLATE_SPIKE0.md}).
 */
public final class GraniteFabricBootstrap {

    private GraniteFabricBootstrap() {
    }

    public static String status() {
        return "granitelib-fabric: GraniteClickable9x6Handler (optional top-slot clicks) + future Slate";
    }
}
