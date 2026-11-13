package dev.granite.graniteskills.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FabricGraniteSkillsMod implements DedicatedServerModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("graniteskills");

    @Override
    public void onInitializeServer() {
        FabricGraniteSkillsCommands.registerEarly();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                FabricGraniteSkills plugin = new FabricGraniteSkills(server);
                FabricGraniteSkills.setInstance(plugin);
                plugin.enable();
            } catch (Exception e) {
                LOGGER.error("Failed to start Fabric GraniteSkills", e);
                throw e;
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            FabricGraniteSkills inst = FabricGraniteSkills.getInstance();
            if (inst != null) {
                inst.disable();
            }
        });
    }
}
