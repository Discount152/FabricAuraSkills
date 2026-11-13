package dev.granite.graniteskills.fabric.requirement;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

final class FabricSkillNode extends FabricRequirementNode {

    private final Skill skill;
    private final int level;

    FabricSkillNode(FabricGraniteSkills plugin, Skill skill, int level, String message) {
        super(plugin, message);
        this.skill = skill;
        this.level = level;
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (skill == null) {
            return true;
        }
        return plugin.getUserManager().getUser(player.getUUID()).getSkillLevel(skill) >= level;
    }
}
