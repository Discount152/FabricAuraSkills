package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.menu.MenuHelper;
import dev.granite.graniteskills.common.message.MessageKey;
import dev.granite.graniteskills.fabric.menu.FabricBuiltInMenuConfig;
import dev.granite.graniteskills.fabric.menu.FabricMenuYamlSlice;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Resolves a menu format name to the template string menus substitute, matching Bukkit
 * {@code LoadedMenu.formats()}.
 */
public final class FabricMenuHelper implements MenuHelper {

    private static final Map<String, String> BUILTIN_TEMPLATES = Map.of(
            "stat_reward_entry", "\n  {color}+{num} {symbol} {stat}",
            "money_reward_entry", "\n  <green>${amount}",
            "player_stat_entry", "{color}{symbol} {stat} <white>{level}",
            "trait_entry", "<#d0d0d0>{trait}: {color}{level}");

    private final @Nullable FabricGraniteSkills plugin;

    public FabricMenuHelper() {
        this(null);
    }

    public FabricMenuHelper(@Nullable FabricGraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getFormat(String menuName, String formatName) {
        if (formatName == null || formatName.isEmpty()) {
            return "";
        }
        String stored = storedFormat(menuName, formatName);
        if (stored == null || stored.isEmpty()) {
            stored = BUILTIN_TEMPLATES.get(formatName);
        }
        if (stored == null || stored.isEmpty()) {
            return formatName;
        }
        if (isTemplate(stored)) {
            return stored;
        }
        String resolved = resolveMessage(stored);
        if (resolved != null && !resolved.isEmpty() && !resolved.equals(stored)) {
            return resolved;
        }
        String builtin = BUILTIN_TEMPLATES.get(formatName);
        if (builtin != null) {
            return builtin;
        }
        return formatName;
    }

    private @Nullable String storedFormat(String menuName, String formatName) {
        if (plugin == null || menuName == null) {
            return null;
        }
        FabricBuiltInMenuConfig config = plugin.getBuiltInMenuConfig();
        if (config == null) {
            return null;
        }
        FabricMenuYamlSlice slice = config.getSlice(menuName, emptySlice());
        return slice.getFormatKey(formatName, null);
    }

    private @Nullable String resolveMessage(String key) {
        if (plugin == null || plugin.getMessageProvider() == null) {
            return null;
        }
        return plugin.getMessageProvider().getRaw(MessageKey.of(key), plugin.getDefaultLanguage());
    }

    private static boolean isTemplate(String value) {
        return value.indexOf('{') >= 0 || value.indexOf('<') >= 0 || value.indexOf('\n') >= 0;
    }

    private static FabricMenuYamlSlice emptySlice() {
        return new FabricMenuYamlSlice(true, "", 6, 9, Map.of(), 0, 54, 4, 49, "", "", "", 45, 53);
    }
}
