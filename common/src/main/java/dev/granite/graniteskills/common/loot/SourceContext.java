package dev.granite.graniteskills.common.loot;

import dev.granite.graniteskills.api.loot.LootContext;
import dev.granite.graniteskills.api.source.XpSource;

public record SourceContext(XpSource source) implements LootContext {

    @Override
    public String getName() {
        return source.getId().toString();
    }

}
