package dev.granite.graniteskills.common.source.type;

import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.source.type.GrindstoneXpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.Source;

public class GrindstoneSource extends Source implements GrindstoneXpSource {

    private final String multiplier;

    public GrindstoneSource(GraniteSkillsPlugin plugin, SourceValues values, String multiplier) {
        super(plugin, values);
        this.multiplier = multiplier;
    }

    @Override
    public String getMultiplier() {
        return multiplier;
    }

}
