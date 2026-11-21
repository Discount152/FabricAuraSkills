package dev.granite.graniteskills.fabric.region;

import java.util.Set;

/**
 * Pure placed-block set operations. Piston and break hooks call these so the rule can be tested
 * without a world.
 */
public final class PlacedBlockMoves {

    private PlacedBlockMoves() {
    }

    public static boolean move(Set<String> positions, String from, String to) {
        if (positions == null || from == null || to == null || from.equals(to)) {
            return false;
        }
        if (!positions.remove(from)) {
            return false;
        }
        positions.add(to);
        return true;
    }
}
