package dev.aurelium.auraskills.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FabricAuraSkillsMod implements DedicatedServerModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("auraskills");

    @Override
    public void onInitializeServer() {
        FabricAuraSkillsCommands.registerEarly();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                FabricAuraSkills plugin = new FabricAuraSkills(server);
                FabricAuraSkills.setInstance(plugin);
                plugin.enable();
            } catch (Exception e) {
                LOGGER.error("Failed to start Fabric AuraSkills", e);
                throw e;
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            FabricAuraSkills inst = FabricAuraSkills.getInstance();
            if (inst != null) {
                inst.disable();
            }
        });
    }
}
