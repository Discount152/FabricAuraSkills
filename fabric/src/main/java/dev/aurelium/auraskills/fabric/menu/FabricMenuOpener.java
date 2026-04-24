package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@FunctionalInterface
public interface FabricMenuOpener {

    /**
     * @param title Resolved title for static list menus; may be empty when the GUI builds the title from
     *              config + context (e.g. level_progression, stat_info).
     * @param ctx   Use {@link FabricMenuContext#none()} for id-only opens; detail menus use {@link
     *              FabricMenuContext#ofSkill} or {@link FabricMenuContext#ofStat}.
     */
    void open(
            ServerPlayerEntity player, FabricAuraSkills plugin, Text title, FabricMenuContext context);
}
