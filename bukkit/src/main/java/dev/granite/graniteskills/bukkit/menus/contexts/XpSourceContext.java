package dev.granite.graniteskills.bukkit.menus.contexts;

import dev.aurelium.slate.context.ContextProvider;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.jetbrains.annotations.Nullable;

public class XpSourceContext implements ContextProvider<XpSource> {

    private final GraniteSkills plugin;

    public XpSourceContext(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<XpSource> getType() {
        return XpSource.class;
    }

    @Override
    @Nullable
    public XpSource parse(String menuName, String input) {
        String[] split = input.split(" ");
        // Parse Skill from first part of input
        Skill skill = plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(split[0]));
        if (skill == null || !skill.isEnabled()) {
            return null;
        }
        // Parse Source from second part of input
        NamespacedId sourceId = NamespacedId.fromDefault(split[1]);
        // Find source from skill that matches id
        return skill.getSources().stream().filter(source -> source.getId().equals(sourceId)).findFirst().orElse(null);
    }

}
