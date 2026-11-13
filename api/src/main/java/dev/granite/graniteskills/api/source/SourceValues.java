package dev.granite.graniteskills.api.source;

import dev.granite.graniteskills.api.GraniteSkillsApi;
import dev.granite.graniteskills.api.registry.NamespacedId;
import org.jetbrains.annotations.Nullable;

public class SourceValues {

    private final GraniteSkillsApi api;
    private final SourceType type;
    private final NamespacedId id;
    private final double xp;
    @Nullable
    private final String displayName;
    @Nullable
    private final String unitName;
    private final SourceIncome income;

    public SourceValues(GraniteSkillsApi api, SourceType type, NamespacedId id, double xp,
            @Nullable String displayName, @Nullable String unitName, SourceIncome income) {
        this.api = api;
        this.type = type;
        this.id = id;
        this.xp = xp;
        this.displayName = displayName;
        this.unitName = unitName;
        this.income = income;
    }

    public GraniteSkillsApi getApi() {
        return api;
    }

    public SourceType getType() {
        return type;
    }

    public NamespacedId getId() {
        return id;
    }

    public double getXp() {
        return xp;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public String getUnitName() {
        return unitName;
    }

    public SourceIncome getIncome() {
        return income;
    }

}
