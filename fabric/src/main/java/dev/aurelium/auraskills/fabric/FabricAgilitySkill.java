package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;

public final class FabricAgilitySkill {

    private FabricAgilitySkill() {
    }

    public static boolean isAgility(Skill skill) {
        return skill != null && Skills.AGILITY.getId().equals(skill.getId());
    }
}