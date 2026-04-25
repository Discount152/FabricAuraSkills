package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;
import net.minecraft.server.command.ServerCommandSource;

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
    public static SuggestionProvider<ServerCommandSource> builtInOpenMenuIds() {
        return (context, builder) -> {
            String remaining = builder.getRemaining();
            for (String id :
                    new String[] {
                        FabricMenuKeys.SKILLS,
                        FabricMenuKeys.STATS,
                        FabricMenuKeys.LEVEL_PROGRESSION,
                        FabricMenuKeys.STAT_INFO,
                        FabricMenuKeys.SOURCES,
                        FabricMenuKeys.UNCLAIMED_ITEMS
                    }) {
                String r = remaining.toLowerCase(Locale.ROOT);
                if (remaining.isEmpty() || id.startsWith(r)) {
                    builder.suggest(id);
                }
            }
            return builder.buildFuture();
        };
    }

    public static SuggestionProvider<ServerCommandSource> onlinePlayers() {
        return (CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) -> {
            String remaining = builder.getRemaining();
            var server = context.getSource().getServer();
            List<String> names = FabricCommandPlayerTarget.onlinePlayerNames(server);
            for (String n : FabricCommandPlayerTarget.filterPrefix(names, remaining)) {
                builder.suggest(n);
            }
            return builder.buildFuture();
        };
    }

    public static SuggestionProvider<ServerCommandSource> enabledSkillKeys() {
        return (CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) -> {
            FabricAuraSkills p = FabricAuraSkills.getInstance();
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
    public static SuggestionProvider<ServerCommandSource> enabledSkillKeysOrGlobal() {
        return (CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) -> {
            String remaining = builder.getRemaining();
            if (remaining.isEmpty()
                    || "global"
                            .toLowerCase(Locale.ROOT)
                            .startsWith(remaining.toLowerCase(Locale.ROOT))) {
                builder.suggest("global");
            }
            FabricAuraSkills p = FabricAuraSkills.getInstance();
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
    public static SuggestionProvider<ServerCommandSource> enabledStatKeys() {
        return (CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) -> {
            FabricAuraSkills p = FabricAuraSkills.getInstance();
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
    public static SuggestionProvider<ServerCommandSource> enabledTraitKeys() {
        return (CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) -> {
            FabricAuraSkills p = FabricAuraSkills.getInstance();
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
     * dev.aurelium.auraskills.fabric.item.FabricItemRegistry} (Sprint 55).
     */
    public static SuggestionProvider<ServerCommandSource> registeredItemKeys() {
        return (CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) -> {
            FabricAuraSkills p = FabricAuraSkills.getInstance();
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
                if (!id.getNamespace().equals(NamespacedId.AURASKILLS)) {
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

    public static SuggestionProvider<ServerCommandSource> languageCodes() {
        return (CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) -> {
            FabricAuraSkills p = FabricAuraSkills.getInstance();
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
    public static SuggestionProvider<ServerCommandSource> sourceSortTypes() {
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
    public static SuggestionProvider<ServerCommandSource> backupYmlInBackupsFolder() {
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
    public static SuggestionProvider<ServerCommandSource> enabledManaAbilities() {
        return (CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) -> {
            FabricAuraSkills p = FabricAuraSkills.getInstance();
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
    public static SuggestionProvider<ServerCommandSource> presetZipsInPresetsFolder() {
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
