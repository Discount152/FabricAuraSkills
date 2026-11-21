package dev.granitelib.fabric.menu;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Previous-menu ids for one player. The newest place is the menu currently open.
 * Skill, page, and sort travel with the place so Back can reopen that view.
 */
public final class GraniteMenuHistory {

    /**
     * One open menu. {@code skillKey} is the skill id's simple name, {@code page} is the menu's page,
     * and {@code sort} is a mode or sort token. Empty strings mean the menu does not use that field.
     */
    public record Place(String menuId, String skillKey, int page, String sort) {
        public Place {
            if (skillKey == null) {
                skillKey = "";
            }
            if (sort == null) {
                sort = "";
            }
            if (page < 0) {
                page = 0;
            }
        }

        public static Place of(String menuId) {
            return new Place(menuId, "", 0, "");
        }
    }

    private static final ConcurrentHashMap<UUID, Deque<Place>> OPEN = new ConcurrentHashMap<>();

    private GraniteMenuHistory() {
    }

    public static void push(UUID playerId, String menuId) {
        if (menuId == null || menuId.isBlank()) {
            return;
        }
        remember(playerId, Place.of(menuId));
    }

    /**
     * Records {@code place}. Reopening the same menu id replaces the current place so paging
     * and sort changes stay on one history entry.
     */
    public static void remember(UUID playerId, Place place) {
        if (playerId == null || place == null || place.menuId() == null || place.menuId().isBlank()) {
            return;
        }
        Deque<Place> stack = OPEN.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        if (!stack.isEmpty() && place.menuId().equals(stack.peekLast().menuId())) {
            stack.removeLast();
        }
        stack.addLast(place);
    }

    public static String current(UUID playerId) {
        Place place = currentPlace(playerId);
        return place == null ? null : place.menuId();
    }

    public static Place currentPlace(UUID playerId) {
        Deque<Place> stack = OPEN.get(playerId);
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.peekLast();
    }

    public static String back(UUID playerId) {
        Place place = backPlace(playerId);
        return place == null ? null : place.menuId();
    }

    /**
     * Drops the current menu and returns the place now showing. The root place stays when it is
     * the only entry.
     */
    public static Place backPlace(UUID playerId) {
        Deque<Place> stack = OPEN.get(playerId);
        if (stack == null || stack.size() < 2) {
            return null;
        }
        stack.removeLast();
        return stack.peekLast();
    }

    public static void clear(UUID playerId) {
        OPEN.remove(playerId);
    }
}
