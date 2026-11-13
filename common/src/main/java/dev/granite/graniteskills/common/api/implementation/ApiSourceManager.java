package dev.granite.graniteskills.common.api.implementation;

import dev.granite.graniteskills.api.config.ConfigNode;
import dev.granite.graniteskills.api.source.*;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.message.MessageKey;
import dev.granite.graniteskills.common.source.income.IncomeLoader;
import dev.granite.graniteskills.common.util.text.TextUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ApiSourceManager implements SourceManager {

    private final GraniteSkillsPlugin plugin;
    private final IncomeLoader incomeLoader;

    public ApiSourceManager(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
        this.incomeLoader = new IncomeLoader(plugin);
    }

    @Override
    @NotNull
    public <T extends XpSource> List<SkillSource<T>> getSourcesOfType(Class<T> typeClass) {
        return plugin.getSkillManager().getSourcesOfType(typeClass);
    }

    @Override
    @Nullable
    public <T extends XpSource> SkillSource<T> getSingleSourceOfType(Class<T> typeClass) {
        return plugin.getSkillManager().getSingleSourceOfType(typeClass);
    }

    @Override
    @Nullable
    public String getUnitName(XpSource source, Locale locale) {
        String unitName = plugin.getItemRegistry().getSourceMenuItems().getSourceUnit(source);
        if (unitName == null) {
            if (source instanceof CustomSource customSource) {
                unitName = customSource.getValues().getUnitName();
            }
        }
        if (unitName == null) return null;

        // Try to replace placeholders
        for (String keyStr : TextUtil.getPlaceholders(unitName)) {
            MessageKey key = MessageKey.of(keyStr);
            String message = plugin.getMsg(key, locale);
            unitName = TextUtil.replace(unitName, "{" + keyStr + "}", message);
        }
        return unitName;
    }

    @Override
    public SourceIncome loadSourceIncome(ConfigNode source) {
        return incomeLoader.loadSourceIncome(((ApiConfigNode) source).getBacking());
    }

}
