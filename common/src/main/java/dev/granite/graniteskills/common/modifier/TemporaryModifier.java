package dev.granite.graniteskills.common.modifier;

import dev.granite.graniteskills.api.util.GraniteSkillsModifier;
import org.jetbrains.annotations.NotNull;

public record TemporaryModifier(
        GraniteSkillsModifier<?> modifier,
        long expirationTime
) implements Comparable<TemporaryModifier> {

    @Override
    public int compareTo(@NotNull TemporaryModifier other) {
        return Long.compare(this.expirationTime, other.expirationTime);
    }

}
