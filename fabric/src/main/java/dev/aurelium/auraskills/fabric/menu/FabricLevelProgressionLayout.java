package dev.aurelium.auraskills.fabric.menu;

import java.util.List;
import java.util.Set;

/**
 * Sprint 117: owner-approved snake path for bundled {@code level_progression.yml}. Row 1 (slots 0-8) stays empty;
 * 24 level cells per page follow {@link #LEVEL_PATH_SLOTS}; bottom row controls are fixed slots.
 */
public final class FabricLevelProgressionLayout {

    public static final int PAGE_SIZE = 24;

    /** First path slot; level 1 on page 1 starts here (row 2, column 1 in 1-based menu terms). */
    public static final int LEVEL_PATH_FIRST_SLOT = 9;

    public static final int SLOT_BACK = 45;
    public static final int SLOT_SOURCES = 48;
    public static final int SLOT_ABILITIES = 49;
    public static final int SLOT_PREVIOUS_PAGE = 52;
    public static final int SLOT_NEXT_PAGE = 53;

    public static final List<Integer> LEVEL_PATH_SLOTS = List.of(
            9, 18, 27, 36,
            37, 38,
            29, 20, 11,
            12, 13,
            22, 31, 40,
            41, 42,
            33, 24, 15,
            16, 17,
            26, 35, 44);

    public static final Set<Integer> LEVEL_PATH_SLOT_SET = Set.copyOf(LEVEL_PATH_SLOTS);

    private FabricLevelProgressionLayout() {
    }
}
