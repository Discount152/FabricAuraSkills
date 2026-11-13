package dev.granite.graniteskills.common.source.type;

import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.source.type.JumpingXpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.Source;

public class JumpingSource extends Source implements JumpingXpSource {

    private final int interval;

    public JumpingSource(GraniteSkillsPlugin plugin, SourceValues values, int interval) {
        super(plugin, values);
        this.interval = interval;
    }

    @Override
    public int getInterval() {
        return interval;
    }

}
