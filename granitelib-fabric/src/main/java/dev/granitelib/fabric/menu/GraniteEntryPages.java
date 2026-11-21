package dev.granitelib.fabric.menu;

import java.util.List;

/**
 * Splits menu entries into pages. A later page starts after the previous page and does not
 * include those entries again.
 */
public final class GraniteEntryPages {

    private GraniteEntryPages() {
    }

    public static <T> List<T> page(List<T> entries, int page, int pageSize) {
        if (entries == null) {
            throw new IllegalArgumentException("entries are required");
        }
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("page size must be >= 1");
        }
        int start = page * pageSize;
        if (start >= entries.size()) {
            return List.of();
        }
        int end = Math.min(entries.size(), start + pageSize);
        return List.copyOf(entries.subList(start, end));
    }
}
