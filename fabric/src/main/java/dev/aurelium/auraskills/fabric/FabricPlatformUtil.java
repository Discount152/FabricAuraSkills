package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.util.PlatformUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class FabricPlatformUtil implements PlatformUtil {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public boolean isValidMaterial(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        Identifier id = parseId(input);
        return id != null && Registries.BLOCK.get(id) != null;
    }

    @Override
    public boolean isValidEntityType(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        Identifier id = parseId(input);
        return id != null && Registries.ENTITY_TYPE.get(id) != null;
    }

    @Override
    public String convertEntityName(String input) {
        return input == null ? "" : input.toLowerCase();
    }

    @Override
    public Component toComponent(String message) {
        return LEGACY.deserialize(message);
    }

    @Override
    public String toString(Component component) {
        return LEGACY.serialize(component);
    }

    private static @Nullable Identifier parseId(String input) {
        String s = input.toLowerCase();
        if (!s.contains(":")) {
            s = "minecraft:" + s;
        }
        return Identifier.tryParse(s);
    }
}
