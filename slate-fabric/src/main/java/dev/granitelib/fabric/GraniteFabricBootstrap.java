package dev.granitelib.fabric;

/**
 * Entry point for the Fabric-backed GraniteLib bootstrap.
 * <p>
 * Bukkit Slate source (reference while porting) lives in the {@code vendor/Slate} git submodule
 * (<a href="https://github.com/Archy-X/Slate">Archy-X/Slate</a>). See {@code fabric/MENU_AND_COMMANDS_PORT.md}.
 */
public final class GraniteFabricBootstrap {

    private GraniteFabricBootstrap() {
    }

    /** Placeholder until the menu engine is wired to Fabric screen handlers. */
    public static String status() {
        return "granitelib-fabric skeleton — port in progress";
    }
}
