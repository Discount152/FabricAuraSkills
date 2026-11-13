package dev.granite.graniteskills.bukkit.menus.contexts;

import dev.aurelium.slate.context.ContextProvider;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.jetbrains.annotations.Nullable;

public class SkillContext implements ContextProvider<Skill> {

    private final GraniteSkills plugin;

    public SkillContext(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<Skill> getType() {
        return Skill.class;
    }

    @Nullable
    @Override
    public Skill parse(String menuName, String input) {
        Skill skill = plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(input));
        if (skill != null && skill.isEnabled()) {
            return skill;
        } else {
            return null;
        }
    }

}
