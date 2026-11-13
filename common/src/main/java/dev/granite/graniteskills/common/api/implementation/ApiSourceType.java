package dev.granite.graniteskills.common.api.implementation;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.source.SourceType;
import dev.granite.graniteskills.api.source.XpSourceParser;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;

public class ApiSourceType implements SourceType {

    private final GraniteSkillsPlugin plugin;
    private final NamespacedId id;
    private final XpSourceParser<?> parser;

    public ApiSourceType(GraniteSkillsPlugin plugin, NamespacedId id, XpSourceParser<?> parser) {
        this.plugin = plugin;
        this.id = id;
        this.parser = parser;
    }

    @Override
    public NamespacedId getId() {
        return id;
    }

    @Override
    public XpSourceParser<?> getParser() {
        return parser;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getSkillManager().isSourceEnabled(this);
    }

}
