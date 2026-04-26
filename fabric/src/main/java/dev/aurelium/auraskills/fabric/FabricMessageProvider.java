package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.fabric.text.FabricTextFormatter;
import net.kyori.adventure.text.Component;

/**
 * Bukkit uses {@code TextFormatter#toComponent} for {@link #stringToComponent} (see {@code
 * BukkitMessageProvider}). Fabric previously used only {@link
 * net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer}, so MiniMessage tags from
 * YAML (e.g. {@code <green>}) were shown literally. This matches Bukkit: MiniMessage in, legacy
 * string out for {@link #applyFormatting} / {@link #componentToString} round-trips.
 */
public final class FabricMessageProvider extends MessageProvider {

    public FabricMessageProvider(FabricAuraSkills plugin) {
        super(plugin);
    }

    @Override
    public String applyFormatting(String message) {
        return FabricTextFormatter.toString(FabricTextFormatter.toComponent(message));
    }

    @Override
    public String componentToString(Component component) {
        return FabricTextFormatter.toString(component);
    }

    @Override
    public Component stringToComponent(String message) {
        return FabricTextFormatter.toComponent(message);
    }
}
