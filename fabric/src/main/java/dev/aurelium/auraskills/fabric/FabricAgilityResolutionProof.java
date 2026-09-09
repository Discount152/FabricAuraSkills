package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.JumpingXpSource;
import dev.aurelium.auraskills.api.source.type.StatisticXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;

import java.util.List;
import java.util.stream.Collectors;

public final class FabricAgilityResolutionProof {

    private FabricAgilityResolutionProof() {
    }

    public static void emit(FabricAuraSkills plugin) {
        List<SkillSource<StatisticXpSource>> stats = plugin.getSkillManager().getSourcesOfType(StatisticXpSource.class);
        long agilityStatRows = stats.stream().filter(s -> FabricAgilitySkill.isAgility(s.skill())).count();
        String statCsv = stats.stream()
                .filter(s -> FabricAgilitySkill.isAgility(s.skill()))
                .filter(s -> FabricStatisticSourceResolver.resolveForAgility(
                        s.source().getStatistic(), s.source().getId().getKey()) != null)
                .map(s -> s.source().getId().getSimpleName())
                .sorted()
                .collect(Collectors.joining(","));
        List<SkillSource<JumpingXpSource>> jumps = plugin.getSkillManager().getSourcesOfType(JumpingXpSource.class);
        long agilityJumpRows = jumps.stream().filter(j -> FabricAgilitySkill.isAgility(j.skill())).count();
        String jumpCsv = jumps.stream()
                .filter(j -> FabricAgilitySkill.isAgility(j.skill()))
                .map(j -> j.source().getId().getSimpleName())
                .sorted()
                .collect(Collectors.joining(","));
        if (plugin.getSkillManager().isSourceEnabled(SourceTypes.STATISTIC)
                && Skills.AGILITY.isEnabled()
                && agilityStatRows > 0
                && statCsv.isEmpty()) {
            plugin.logger().warn("[AuraSkills] FAIL: agility StatisticXpSource rows exist (" + agilityStatRows
                    + ") but zero resolved to Minecraft stats; check FabricStatisticSourceResolver / YAML tokens.");
        }
        if (plugin.getSkillManager().isSourceEnabled(SourceTypes.JUMPING)
                && Skills.AGILITY.isEnabled()
                && agilityJumpRows > 0
                && jumpCsv.isEmpty()) {
            plugin.logger().warn("[AuraSkills] FAIL: agility JumpingXpSource rows exist (" + agilityJumpRows
                    + ") but none listed; check agility.yml jumping entries.");
        }
        FabricXpDiagnostics.logAgilitySourcesResolved(plugin, statCsv, jumpCsv);
        plugin.logger().info("[AuraSkills] agility_sources_resolved statistic_sources=[" + statCsv + "] jump_sources=["
                + jumpCsv + "]");
    }
}