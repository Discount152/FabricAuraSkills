package dev.aurelium.auraskills.fabric.item;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.data.KeyIntPair;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Safe merge paths for {@link User#getUnclaimedItems()} (**Sprint 56** / **Sprint 71**). All work is
 * synchronous on the server thread; lists are mutated in-place with {@link User#setShouldSave} when
 * changed.
 */
public final class FabricUnclaimedItemsMerge {

    /** One stack was fully merged, partially merged, or an invalid entry was dropped. */
    public enum MergeOne {
        /** Full removal or partial remainder; refresh UI. */
        CHANGED,
        /** Count &gt; 0 and nothing could be added (inventory full for the whole stack). */
        INVENTORY_FULL,
    }

    private FabricUnclaimedItemsMerge() {}

    /**
     * Merges unclaimed entries in-order; mutates the list. Returns a rough count of <strong>entry
     * operations</strong> that delivered at least one item (full or partial) — same contract as
     * historical {@code FabricClaimItemsCommands#runClaim}.
     */
    public static int mergeAllInOrder(FabricAuraSkills p, User user, ServerPlayerEntity player) {
        int touch = 0;
        var list = user.getUnclaimedItems();
        for (int i = 0; i < list.size(); ) {
            KeyIntPair pair = list.get(i);
            final NamespacedId id;
            try {
                id = NamespacedId.fromDefault(pair.getKey());
            } catch (IllegalArgumentException ex) {
                list.remove(i);
                user.setShouldSave(true);
                continue;
            }
            if (!p.getFabricItemRegistry().containsItem(id)) {
                list.remove(i);
                user.setShouldSave(true);
                continue;
            }
            int n = pair.getValue();
            int notFit = p.getFabricItemRegistry().tryMergeRegisteredItemCount(player, id, n);
            if (notFit == 0) {
                list.remove(i);
                user.setShouldSave(true);
                touch++;
                continue;
            }
            if (notFit < n) {
                pair.setValue(notFit);
                user.setShouldSave(true);
                touch++;
            }
            i++;
        }
        return touch;
    }

    /**
     * Tries to merge the unclaimed entry at {@code listIndex} into {@code player}'s main+hotbar. Does
     * <strong>not</strong> remove or shrink the stack unless at least one item was added.
     */
    public static MergeOne mergeOneAtListIndex(
            FabricAuraSkills p, User user, ServerPlayerEntity player, int listIndex) {
        var list = user.getUnclaimedItems();
        if (listIndex < 0 || listIndex >= list.size()) {
            return MergeOne.CHANGED;
        }
        KeyIntPair pair = list.get(listIndex);
        final NamespacedId id;
        try {
            id = NamespacedId.fromDefault(pair.getKey());
        } catch (IllegalArgumentException ex) {
            list.remove(listIndex);
            user.setShouldSave(true);
            return MergeOne.CHANGED;
        }
        if (!p.getFabricItemRegistry().containsItem(id)) {
            list.remove(listIndex);
            user.setShouldSave(true);
            return MergeOne.CHANGED;
        }
        int n = pair.getValue();
        int notFit = p.getFabricItemRegistry().tryMergeRegisteredItemCount(player, id, n);
        if (notFit == 0) {
            list.remove(listIndex);
            user.setShouldSave(true);
            return MergeOne.CHANGED;
        }
        if (notFit < n) {
            pair.setValue(notFit);
            user.setShouldSave(true);
            return MergeOne.CHANGED;
        }
        return MergeOne.INVENTORY_FULL;
    }
}
