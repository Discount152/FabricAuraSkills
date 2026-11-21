package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.menu.FabricMenuKeys;
import net.minecraft.commands.CommandSourceStack;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Reusable {@link com.mojang.brigadier.suggestion.SuggestionProvider}s (Java + Bedrock: server-side
 * suggestions as supported by the client).
 */
public final class FabricCommandSuggestionProviders {

    private FabricCommandSuggestionProviders() {}

    /**
     * Sprints 44+ — built-in menu ids for bounded {@code openmenu} (string-token contexts);
     * prefer literal subnodes on {@code openmenu} for tab completion (**Sprint 45**).
     */
    public static SuggestionProvider<CommandSourceStack> builtInOpenMenuIds() {
        return (context, builder) -> {
            String remaining = builder.getRemaining();
            for (String id :
                    new String[] {
                            FabricMenuKeys.SKILLS,
                            FabricMenuKeys.STATS,
                            FabricMenuKeys.LEVEL_PROGRESSION,
                            FabricMenuKeys.STAT_INFO,
                            FabricMenuKeys.SOURCES,
                            FabricMenuKeys.UNCLAIMED_ITEMS,
                            FabricMenuKeys.LEADERBOARD,
                            FabricMenuKeys.ABILITIES
                    }) {
                String r = remaining.toLowerCase(Locale.ROOT);
                if (remaining.isEmpty() || id.startsWith(r)) {
                    builder.suggest(id);
                }
            }
            return builder.buildFuture();
        };
    }

    public static SuggestionProvider<CommandSourceStack> onlinePlayers() {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            String remaining = builder.getRemaining();
            var server = context.getSource().getServer();
            List<String> names = FabricCommandPlayerTarget.onlinePlayerNames(server);
            for (String n : FabricCommandPlayerTarget.filterPrefix(names, remaining)) {
                builder.suggest(n);
            }
            return builder.buildFuture();
        };
    }

    /**
     * {@code /openmenu leaderboard <token>}: online player names and enabled skill keys (**Sprint
     * 72**).
     */
    public static SuggestionProvider<CommandSourceStack> onlinePlayersAndEnabledSkillKeys() {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            onlinePlayers().getSuggestions(context, builder);
            enabledSkillKeys().getSuggestions(context, builder);
            return builder.buildFuture();
        };
    }

    public static SuggestionProvider<CommandSourceStack> enabledSkillKeys() {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            FabricGraniteSkills p = FabricGraniteSkills.getInstance();
            if (p == null) {
                return CompletableFuture.completedFuture(builder.build());
            }
            String remaining = builder.getRemaining();
            for (Skill s : p.getSkillManager().getEnabledSkills()) {
                String k = s.getId().getKey();
                if (remaining.isEmpty()
                        || k.toLowerCase().startsWith(remaining.toLowerCase())) {
                    builder.suggest(k);
                }
            }
            return builder.buildFuture();
        };
    }

    /**
     * Bukkit {@code @skills_global} — enabled skill keys plus {@code global} (item held multiplier).
     * **Sprint 53**
     */
    public static SuggestionProvider<CommandSourceStack> enabledSkillKeysOrGlobal() {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            String remaining = builder.getRemaining();
            if (remaining.isEmpty()
                    || "global"
                            .toLowerCase(Locale.ROOT)
                            .startsWith(remaining.toLowerCase(Locale.ROOT))) {
                builder.suggest("global");
            }
            FabricGraniteSkills p = FabricGraniteSkills.getInstance();
            if (p == null) {
                return builder.buildFuture();
            }
            for (Skill s : p.getSkillManager().getEnabledSkills()) {
                String k = s.getId().getKey();
                if (remaining.isEmpty()
                        || k.toLowerCase().startsWith(remaining.toLowerCase())) {
                    builder.suggest(k);
                }
            }
            return builder.buildFuture();
        };
    }

    /** Enabled stats (registry key) for {@code openmenu stat_info …} (**Sprint 45**). */
    public static SuggestionProvider<CommandSourceStack> enabledStatKeys() {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            FabricGraniteSkills p = FabricGraniteSkills.getInstance();
            if (p == null) {
                return CompletableFuture.completedFuture(builder.build());
            }
            String remaining = builder.getRemaining();
            for (Stat s : p.getStatRegistry().getValues()) {
                if (!s.isEnabled()) {
                    continue;
                }
                String k = s.getId().getKey();
                if (remaining.isEmpty()
                        || k.toLowerCase().startsWith(remaining.toLowerCase())) {
                    builder.suggest(k);
                }
            }
            return builder.buildFuture();
        };
    }

    /** Bukkit {@code @lang} completion (**Sprint 48**). */
    /** Enabled traits (registry key) for {@code trait} commands (**Sprint 50**). */
    public static SuggestionProvider<CommandSourceStack> enabledTraitKeys() {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            FabricGraniteSkills p = FabricGraniteSkills.getInstance();
            if (p == null) {
                return CompletableFuture.completedFuture(builder.build());
            }
            String remaining = builder.getRemaining();
            for (Trait t : p.getTraitRegistry().getValues()) {
                if (!t.isEnabled()) {
                    continue;
                }
                String k = t.getId().getKey();
                if (remaining.isEmpty()
                        || k.toLowerCase().startsWith(remaining.toLowerCase())) {
                    builder.suggest(k);
                }
            }
            return builder.buildFuture();
        };
    }

    /**
     * Bukkit {@code @item_keys} — ids currently registered in {@link
     * dev.granite.graniteskills.fabric.item.FabricItemRegistry} (Sprint 55).
     */
    public static SuggestionProvider<CommandSourceStack> registeredItemKeys() {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            FabricGraniteSkills p = FabricGraniteSkills.getInstance();
            if (p == null) {
                return CompletableFuture.completedFuture(builder.build());
            }
            String remaining = builder.getRemaining();
            for (NamespacedId id : p.getFabricItemRegistry().getIds()) {
                String simple = id.getSimpleName();
                if (remaining.isEmpty()
                        || simple.toLowerCase(Locale.ROOT)
                                .startsWith(remaining.toLowerCase(Locale.ROOT))) {
                    builder.suggest(simple);
                }
                if (!id.getNamespace().equals(NamespacedId.GRANITESKILLS)) {
                    String full = id.toString();
                    if (remaining.isEmpty()
                            || full.toLowerCase(Locale.ROOT)
                                    .startsWith(remaining.toLowerCase(Locale.ROOT))) {
                        builder.suggest(full);
                    }
                }
            }
            return builder.buildFuture();
        };
    }

    public static SuggestionProvider<CommandSourceStack> languageCodes() {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            FabricGraniteSkills p = FabricGraniteSkills.getInstance();
            if (p == null) {
                return CompletableFuture.completedFuture(builder.build());
            }
            String remaining = builder.getRemaining();
            for (String code : p.getMessageProvider().getLanguageCodes()) {
                if (remaining.isEmpty()
                        || code.toLowerCase(Locale.ROOT).startsWith(remaining.toLowerCase(Locale.ROOT))) {
                    builder.suggest(code);
                }
            }
            return builder.buildFuture();
        };
    }

    /**
     * Bukkit {@code @sort_types} (reference {@code CommandRegistrar}) for {@code /sources} (**Sprint
     * 57**).
     */
    public static SuggestionProvider<CommandSourceStack> sourceSortTypes() {
        return (context, builder) -> {
            String remaining = builder.getRemaining();
            for (FabricSourceSortType t : FabricSourceSortType.values()) {
                String name = t.name().toLowerCase(Locale.ROOT);
                if (remaining.isEmpty()
                        || name.startsWith(remaining.toLowerCase(Locale.ROOT))) {
                    builder.suggest(name);
                }
            }
            return builder.buildFuture();
        };
    }

    /** **Sprint 61** — basenames of {@code backups/*.yml} for guarded {@code /backup load}. */
    public static SuggestionProvider<CommandSourceStack> backupYmlInBackupsFolder() {
        return (context, builder) -> {
            FabricCommandPluginState.runtimeReadyOrEmpty()
                    .ifPresent(
                            p -> {
                                File dir = new File(p.getPluginFolder(), "backups");
                                if (!dir.isDirectory()) {
                                    return;
                                }
                                String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                                File[] list = dir.listFiles((d, n) -> n.endsWith(".yml"));
                                if (list == null) {
                                    return;
                                }
                                Arrays.sort(
                                        list, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                                for (File f : list) {
                                    String n = f.getName();
                                    if (remaining.isEmpty() || n.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                                        builder.suggest(n);
                                    }
                                }
                            });
            return builder.buildFuture();
        };
    }

    /**
     * Bukkit {@code @mana_abilities} ({@code CommandRegistrar}) for {@code /… manaability
     * resetcooldown} (**Sprint 66**).
     */
    public static SuggestionProvider<CommandSourceStack> enabledManaAbilities() {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            FabricGraniteSkills p = FabricGraniteSkills.getInstance();
            if (p == null) {
                return CompletableFuture.completedFuture(builder.build());
            }
            String remaining = builder.getRemaining();
            for (ManaAbility a : p.getManaAbilityRegistry().getValues()) {
                if (!a.isEnabled()) {
                    continue;
                }
                String k = a.getId().getKey();
                if (remaining.isEmpty()
                        || k.toLowerCase(Locale.ROOT)
                                .startsWith(remaining.toLowerCase(Locale.ROOT))) {
                    builder.suggest(k);
                }
            }
            return builder.buildFuture();
        };
    }

    /** **Sprint 62** — basenames of {@code presets/*.zip} for {@code /preset load|info}. */
    public static SuggestionProvider<CommandSourceStack> presetZipsInPresetsFolder() {
        return (context, builder) -> {
            FabricCommandPluginState.runtimeReadyOrEmpty()
                    .ifPresent(
                            p -> {
                                File dir = new File(p.getPluginFolder(), "presets");
                                if (!dir.isDirectory()) {
                                    return;
                                }
                                String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                                File[] list = dir.listFiles((d, n) -> n.endsWith(".zip"));
                                if (list == null) {
                                    return;
                                }
                                Arrays.sort(list, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                                for (File f : list) {
                                    String n = f.getName();
                                    if (FabricPresetNameValidator.validateZipFileName(n).isPresent()) {
                                        continue;
                                    }
                                    if (remaining.isEmpty()
                                            || n.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                                        builder.suggest(n);
                                    }
                                }
                            });
            return builder.buildFuture();
        };
    }
}
