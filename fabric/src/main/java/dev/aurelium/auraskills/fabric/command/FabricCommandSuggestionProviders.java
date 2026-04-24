package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;
import net.minecraft.server.command.ServerCommandSource;

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
                        FabricMenuKeys.STAT_INFO
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
}
