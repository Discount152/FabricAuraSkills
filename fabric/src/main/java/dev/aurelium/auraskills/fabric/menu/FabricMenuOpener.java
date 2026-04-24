package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@FunctionalInterface
public interface FabricMenuOpener {

    void open(ServerPlayerEntity player, FabricAuraSkills plugin, Text title);
}
