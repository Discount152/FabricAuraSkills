package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface FabricMenuOpener {

    /**
     * @param title Resolved title for static list menus; may be empty when the GUI builds the title from
     *              config + context (e.g. level_progression, stat_info).
     * @param ctx   Use {@link FabricMenuContext#none()} for id-only opens; list menus (Skills, Stats) may use
     *              {@link FabricMenuContext#withListPage(int)}. Detail menus use {@link
     *              FabricMenuContext#ofSkill} or {@link FabricMenuContext#ofStat}.
     */
    void open(
            ServerPlayer player, FabricGraniteSkills plugin, Component title, FabricMenuContext context);
}
