package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.message.MessageProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class FabricMessageProvider extends MessageProvider {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public FabricMessageProvider(FabricGraniteSkills plugin) {
        super(plugin);
    }

    @Override
    public String applyFormatting(String message) {
        Component c = LEGACY.deserialize(message);
        return LEGACY.serialize(c);
    }

    @Override
    public String componentToString(Component component) {
        return LEGACY.serialize(component);
    }

    @Override
    public Component stringToComponent(String message) {
        return LEGACY.deserialize(message);
    }
}
