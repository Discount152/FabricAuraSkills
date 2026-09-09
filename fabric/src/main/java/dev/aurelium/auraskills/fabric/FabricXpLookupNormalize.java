package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FabricXpLookupNormalize {

    public static final int MAX_CANDIDATE_CSV_CHARS = 2000;

    private FabricXpLookupNormalize() {
    }

    public static String blockBreakKey(BlockState state) {
        if (state == null || state.isAir()) {
            return "block:minecraft:air";
        }
        return "block:" + Registries.BLOCK.getId(state.getBlock()).toString();
    }

    public static String blockPathOnly(BlockState state) {
        if (state == null || state.isAir()) {
            return "air";
        }
        return Registries.BLOCK.getId(state.getBlock()).getPath();
    }

    public static String itemKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "item:-";
        }
        return "item:" + Registries.ITEM.getId(stack.getItem()).toString();
    }

    public static String damageCauseKey(DamageCause cause) {
        if (cause == null) {
            return "damage_cause:-";
        }
        return "damage_cause:" + cause.name().toLowerCase(Locale.ROOT);
    }

    public static String damageRuntimeSummary(DamageSource source) {
        String dmgType = source.getTypeRegistryEntry()
                .getKey()
                .map(k -> k.getValue().toString())
                .orElse("-");
        DamageCause ac = FabricDamageCauseMapper.toApiCause(source);
        String att = source.getAttacker() != null
                ? Registries.ENTITY_TYPE.getId(source.getAttacker().getType()).toString()
                : "-";
        String dir = source.getSource() != null
                ? Registries.ENTITY_TYPE.getId(source.getSource().getType()).toString()
                : "-";
        return "damage_type=" + dmgType + ";" + damageCauseKey(ac) + ";attacker=" + att + ";direct=" + dir;
    }

    public static List<String> normalizedKeysForBreak(BlockState state) {
        List<String> k = new ArrayList<>(2);
        k.add(blockBreakKey(state));
        k.add("block_path:" + blockPathOnly(state));
        return k;
    }

    public static List<String> normalizedKeysForDamage(DamageCause cause) {
        List<String> k = new ArrayList<>(1);
        k.add(damageCauseKey(cause));
        return k;
    }

    public static String blockBreakCandidateSourceIdsCsv(FabricAuraSkills plugin) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return "-";
        }
        for (SkillSource<BlockXpSource> entry : plugin.getSkillManager().getSourcesOfType(BlockXpSource.class)) {
            BlockXpSource bs = entry.source();
            BlockXpSource.BlockTriggers[] triggers = bs.getTriggers();
            if (triggers == null || triggers.length == 0) {
                continue;
            }
            boolean hasBreak = false;
            for (BlockXpSource.BlockTriggers t : triggers) {
                if (t == BlockXpSource.BlockTriggers.BREAK) {
                    hasBreak = true;
                    break;
                }
            }
            if (!hasBreak) {
                continue;
            }
            ids.add("[" + entry.skill().getId() + "]" + bs.getId());
        }
        return joinTruncate(ids);
    }

    public static int blockBreakCandidateCount(FabricAuraSkills plugin) {
        if (plugin == null || !plugin.isRuntimeReady()) {
            return 0;
        }
        int n = 0;
        for (SkillSource<BlockXpSource> entry : plugin.getSkillManager().getSourcesOfType(BlockXpSource.class)) {
            BlockXpSource bs = entry.source();
            BlockXpSource.BlockTriggers[] triggers = bs.getTriggers();
            if (triggers == null) {
                continue;
            }
            for (BlockXpSource.BlockTriggers t : triggers) {
                if (t == BlockXpSource.BlockTriggers.BREAK) {
                    n++;
                    break;
                }
            }
        }
        return n;
    }

    public static String damageXpCandidateSourceIdsCsv(FabricAuraSkills plugin) {
        Set<String> ids = new LinkedHashSet<>();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return "-";
        }
        for (SkillSource<DamageXpSource> entry : plugin.getSkillManager().getSourcesOfType(DamageXpSource.class)) {
            ids.add("[" + entry.skill().getId() + "]" + entry.source().getId());
        }
        return joinTruncate(ids);
    }

    public static int damageXpCandidateCount(FabricAuraSkills plugin) {
        if (plugin == null || !plugin.isRuntimeReady()) {
            return 0;
        }
        int n = 0;
        for (SkillSource<DamageXpSource> ignored : plugin.getSkillManager().getSourcesOfType(DamageXpSource.class)) {
            n++;
        }
        return n;
    }

    public static String joinKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return "-";
        }
        return String.join("|", keys);
    }

    public static String joinTruncate(Set<String> orderedUnique) {
        if (orderedUnique.isEmpty()) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : orderedUnique) {
            if (!sb.isEmpty()) {
                sb.append('|');
            }
            if (sb.length() + s.length() > MAX_CANDIDATE_CSV_CHARS - 50) {
                sb.append("...(truncated)");
                break;
            }
            sb.append(s);
        }
        return sb.toString();
    }
}