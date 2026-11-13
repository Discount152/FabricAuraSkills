package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.source.type.BlockXpSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Matches {@link BlockXpSource#getStates()} and state-based XP multipliers (Paper {@code BlockLeveler} / {@code BlockLevelerHelper}).
 */
public final class FabricBlockStateMatcher {

    private FabricBlockStateMatcher() {
    }

    public static boolean matchesStates(BlockState state, BlockXpSource.BlockXpSourceState[] states) {
        if (states == null || states.length == 0) {
            return true;
        }
        Map<String, Object> blockMap = blockStateToMap(state);
        for (BlockXpSource.BlockXpSourceState spec : states) {
            if (spec == null) {
                continue;
            }
            boolean stateMatches = true;
            for (Map.Entry<String, Object> e : spec.getStateMap().entrySet()) {
                String key = e.getKey();
                Object yamlVal = e.getValue();
                if (!blockMap.containsKey(key)) {
                    stateMatches = false;
                    break;
                }
                if (!valuesEqual(blockMap.get(key), yamlVal)) {
                    stateMatches = false;
                    break;
                }
            }
            if (stateMatches) {
                return true;
            }
        }
        return false;
    }

    public static double getStateMultiplier(BlockState state, BlockXpSource source) {
        if (!source.hasStateMultiplier()) {
            return 1.0;
        }
        for (Property<?> prop : state.getBlock().getStateDefinition().getProperties()) {
            if (!state.hasProperty(prop)) {
                continue;
            }
            String n = prop.getName();
            if (("age".equals(n) || "pickles".equals(n)) && prop instanceof IntegerProperty ip) {
                return source.getStateMultiplier(n, state.getValue(ip));
            }
        }
        return source.getStateMultiplier("placeholder", 1.0);
    }

    private static Map<String, Object> blockStateToMap(BlockState state) {
        Map<String, Object> map = new HashMap<>();
        for (Property.Value<?> value : state.getValues().toList()) {
            map.put(value.property().getName(), comparableToConfigValue(value.value()));
        }
        return map;
    }

    private static Object comparableToConfigValue(Comparable<?> c) {
        if (c instanceof Boolean b) {
            return b;
        }
        if (c instanceof Integer i) {
            return i;
        }
        if (c instanceof Enum<?> en) {
            return en instanceof net.minecraft.util.StringRepresentable sid ? sid.getSerializedName() : en.name().toLowerCase();
        }
        return c.toString();
    }

    private static boolean valuesEqual(Object blockVal, Object yamlVal) {
        if (blockVal == null || yamlVal == null) {
            return Objects.equals(blockVal, yamlVal);
        }
        if (blockVal instanceof Number n1 && yamlVal instanceof Number n2) {
            return n1.doubleValue() == n2.doubleValue();
        }
        if (blockVal.equals(yamlVal)) {
            return true;
        }
        return blockVal.toString().equalsIgnoreCase(yamlVal.toString());
    }
}
