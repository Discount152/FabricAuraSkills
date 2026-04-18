package dev.aurelium.slate.fabric;

/**
 * Entry point for the Fabric-backed Slate port.
 * <p>
 * Bukkit Slate source (reference while porting) lives in the {@code vendor/Slate} git submodule
 * (<a href="https://github.com/Archy-X/Slate">Archy-X/Slate</a>). See {@code fabric/MENU_AND_COMMANDS_PORT.md}.
 */
public final class SlateFabricBootstrap {

    private SlateFabricBootstrap() {
    }

    /** Placeholder until the Slate engine is wired to Fabric screen handlers. */
    public static String status() {
        return "slate-fabric skeleton — port in progress";
    }
}
