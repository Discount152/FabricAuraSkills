package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Central entry for Granite vertical-slice menus (Sprint 28+). All temporary commands and future
 * Slate/Brigadier open paths should go through here so read-only 9×6 usage stays discoverable.
 */
public final class FabricAuraSkillsMenus {

    public enum Id {
        /** @see FabricSkillsMenuGui */
        SKILLS,
        /** @see FabricStatsMenuGui */
        STATS
    }

    private FabricAuraSkillsMenus() {
    }

    public static void open(Id id, ServerPlayerEntity player, FabricAuraSkills plugin) {
        switch (id) {
            case SKILLS -> FabricSkillsMenuGui.open(player, plugin);
            case STATS -> FabricStatsMenuGui.open(player, plugin);
        }
    }
}
