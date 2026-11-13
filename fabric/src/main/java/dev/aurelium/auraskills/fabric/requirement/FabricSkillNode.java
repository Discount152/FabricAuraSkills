package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.level.ServerPlayer;

final class FabricSkillNode extends FabricRequirementNode {

    private final Skill skill;
    private final int level;

    FabricSkillNode(FabricAuraSkills plugin, Skill skill, int level, String message) {
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
