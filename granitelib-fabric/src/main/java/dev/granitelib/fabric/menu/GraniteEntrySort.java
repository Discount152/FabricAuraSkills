package dev.granitelib.fabric.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts the same menu entries into more than one order. The input list is not changed.
 */
public final class GraniteEntrySort {

    private GraniteEntrySort() {
    }

    public record Entry(String name, double value) {
    }

    public enum Order {
        NAME,
        VALUE
    }

    public static List<Entry> sort(List<Entry> entries, Order order) {
        List<Entry> sorted = new ArrayList<>(entries);
        if (order == Order.NAME) {
            sorted.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
        } else {
            sorted.sort(Comparator.comparingDouble(Entry::value).reversed().thenComparing(Entry::name));
        }
        return List.copyOf(sorted);
    }
}
