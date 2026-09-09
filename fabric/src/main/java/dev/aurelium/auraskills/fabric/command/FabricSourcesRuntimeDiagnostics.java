package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.skill.Skill;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FabricSourcesRuntimeDiagnostics {

    private static final Map<String, RenderSnapshot> LAST_RENDER_BY_SKILL = new ConcurrentHashMap<>();

    private FabricSourcesRuntimeDiagnostics() {
    }

    public static void capture(Skill skill, List<PlannedRow> plannedRows, Inventory inventory) {
        List<RowResult> results = new ArrayList<>(plannedRows.size());
        int survived = 0;
        for (PlannedRow planned : plannedRows) {
            ItemStack actual = inventory.getStack(planned.plannedSlot());
            String actualItemId = itemId(actual);
            String actualName = displayName(actual);
            boolean rowSurvived = planned.plannedItemId().equals(actualItemId);
            if (rowSurvived) {
                survived++;
            }
            results.add(new RowResult(
                    planned.plannedSlot(),
                    planned.plannedItemId(),
                    planned.plannedDisplayName(),
                    actualItemId,
                    actualName,
                    rowSurvived));
        }
        LAST_RENDER_BY_SKILL.put(skill.getId().toString(), new RenderSnapshot(results, survived));
    }

    public static @Nullable RenderSnapshot snapshotFor(Skill skill) {
        return LAST_RENDER_BY_SKILL.get(skill.getId().toString());
    }

    public static PlannedRow plannedRow(int slot, ItemStack stack) {
        return new PlannedRow(slot, itemId(stack), displayName(stack));
    }

    private static String itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "minecraft:air";
        }
        return Registries.ITEM.getId(stack.getItem()).toString();
    }

    private static String displayName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "<empty>";
        }
        if (stack.contains(DataComponentTypes.ITEM_NAME)) {
            var text = stack.get(DataComponentTypes.ITEM_NAME);
            if (text != null) {
                return text.getString();
            }
        }
        return stack.getName().getString();
    }

    public record PlannedRow(int plannedSlot, String plannedItemId, String plannedDisplayName) {
    }

    public record RowResult(
            int plannedSlot,
            String plannedItemId,
            String plannedDisplayName,
            String actualItemIdAfterRender,
            String actualDisplayNameAfterRender,
            boolean survivedFinalMutation) {
    }

    public record RenderSnapshot(List<RowResult> rows, int survivedCount) {
        public RenderSnapshot {
            rows = List.copyOf(rows);
        }

        public int renderedCount() {
            return rows.size();
        }

        public String toSummaryLine() {
            return String.format(
                    Locale.ROOT,
                    "rendered=%d, survived=%d",
                    renderedCount(),
                    survivedCount);
        }
    }
}