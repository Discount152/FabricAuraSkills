package dev.aurelium.auraskills.fabric.menu;

import net.minecraft.text.Text;

/**
 * Reusable paged list behavior for 6×9 Granite built-ins (Sprint 37+). The **content** area is capped to
 * slots < 45 (rows 0–4) so the **last row** (45–53) is free for next/previous controls when needed.
 * Navigation slots are still validated against the configured list range in YAML.
 */
public final class FabricListMenuPaging {

    /** List items never occupy this slot or higher: row 5 (slots 45–53) is reserved for paging UI. */
    public static final int LIST_CONTENT_MAX_EXCLUSIVE = 45;

    public static final int DEFAULT_PREV_PAGE_SLOT = 45;
    public static final int DEFAULT_NEXT_PAGE_SLOT = 53;

    public record ListContentRegion(int startInclusive, int endExclusive) {

        public int size() {
            return Math.max(0, endExclusive - startInclusive);
        }
    }

    private FabricListMenuPaging() {
    }

    /**
     * The scrollable list region, intersected with [0, 45) so the bottom row remains available for prev/next
     * buttons.
     */
    public static ListContentRegion listContentRegion(int listStart, int listEndExclusive) {
        int end = Math.min(listEndExclusive, LIST_CONTENT_MAX_EXCLUSIVE);
        int start = Math.min(listStart, end);
        return new ListContentRegion(start, end);
    }

    public static int maxPageIndex(int totalCount, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        if (totalCount <= 0) {
            return 0;
        }
        return (totalCount - 1) / pageSize;
    }

    public static int clampPage(int page, int totalCount, int pageSize) {
        int m = maxPageIndex(totalCount, pageSize);
        if (page < 0) {
            return 0;
        }
        if (page > m) {
            return m;
        }
        return page;
    }

    public static Text appendPageToTitleIfNeeded(Text baseTitle, int page, int maxPage) {
        return FabricMenuTitles.appendPageSuffixGray(baseTitle, page, maxPage);
    }
}
