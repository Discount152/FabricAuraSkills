package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.message.MessageProvider;
import dev.granite.graniteskills.fabric.text.FabricTextFormatter;
import net.kyori.adventure.text.Component;

public final class FabricMessageProvider extends MessageProvider {

    public FabricMessageProvider(FabricGraniteSkills plugin) {
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
