package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Reusable {@link com.mojang.brigadier.suggestion.SuggestionProvider}s (Java + Bedrock: server-side
 * suggestions as supported by the client).
 */
public final class FabricCommandSuggestionProviders {

    private FabricCommandSuggestionProviders() {}

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
}
