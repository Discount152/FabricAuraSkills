package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.data.KeyIntPair;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.granitelib.fabric.menu.GraniteContainerMenus;
import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.GraniteTemplateItems;
import dev.granitelib.fabric.template.GraniteTemplateResources;
import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.load.GraniteTemplateValidationException;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Sprint 105: template-backed {@code unclaimed_items}. Claimable slots show **real** registered {@link ItemStack}s;
 * YAML template supplies **additional** lore lines only (material/name on {@code claimable} are ignored for rows).
 * Clicks delegate to {@link FabricUnclaimedItemsMenuGui#handleUnclaimedClick}.
 */
public final class FabricUnclaimedItemsTemplateSupport {

    public static final String TEMPLATE_RESOURCE_PATH = "assets/auraskills/granite/templates/unclaimed_items.yml";

    private static final String MENU_LOG = "unclaimed_items template";

    private FabricUnclaimedItemsTemplateSupport() {}

    public static boolean tryOpen(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            User user,
            int page0,
            int maxPage0,
            List<KeyIntPair> sub,
            Locale lo) {
        try {
            return tryOpenInner(player, plugin, page0, maxPage0, sub, lo);
        } catch (RuntimeException e) {
            plugin.logger().warn("unclaimed_items template render failed; using built-in menu", e);
            return false;
        }
    }

    private static boolean tryOpenInner(
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            int page0,
            int maxPage0,
            List<KeyIntPair> sub,
            Locale lo) {
        GraniteMenuTemplate template;
        try (InputStream in =
                GraniteTemplateResources.openClasspath(FabricUnclaimedItemsTemplateSupport.class, TEMPLATE_RESOURCE_PATH)) {
            if (in == null) {
                plugin.logger().warn(
                        "unclaimed_items template missing from classpath ("
                                + TEMPLATE_RESOURCE_PATH
                                + "); using built-in menu");
                return false;
            }
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            template = GraniteMenuTemplateYamlLoader.loadString(yaml, TEMPLATE_RESOURCE_PATH);
        } catch (IOException e) {
            plugin.logger().warn("unclaimed_items template read failed; using built-in menu", e);
            return false;
        } catch (GraniteTemplateValidationException e) {
            plugin.logger().warn(
                    "unclaimed_items template invalid (" + String.join("; ", e.errors()) + "); using built-in menu");
            return false;
        }

        if (!"unclaimed_items".equalsIgnoreCase(template.id())) {
            plugin.logger().warn(
                    "unclaimed_items template id must be 'unclaimed_items', got '"
                            + template.id()
                            + "'; using built-in menu");
            return false;
        }
        if (template.rows() != 6) {
            plugin.logger().warn(
                    "unclaimed_items template rows must be 6 for 6×9 menu, got "
                            + template.rows()
                            + "; using built-in menu");
            return false;
        }

        GraniteTemplateItem claimableDef = GraniteTemplateItems.findClaimableGridDefinition(template);
        if (claimableDef == null) {
            plugin.logger().warn(
                    "unclaimed_items template: no claimable grid (key 'claimable' or action claim_item); using built-in menu");
            return false;
        }
        if (claimableDef.slots().size() != FabricUnclaimedItemsMenuGui.ITEMS_PER_PAGE) {
            plugin.logger().warn(
                    "unclaimed_items template: claimable item must have exactly "
                            + FabricUnclaimedItemsMenuGui.ITEMS_PER_PAGE
                            + " slots; got "
                            + claimableDef.slots().size()
                            + "; using built-in menu");
            return false;
        }
        if (!claimableDef.slots().equals(FabricUnclaimedItemsMenuGui.builtinClaimableSlots())) {
            plugin.logger().warn(
                    "unclaimed_items template: claimable slots must be 0..35 in order; using built-in menu");
            return false;
        }

        boolean claimableKeyed = claimableDef == template.items().get("claimable");
        boolean claimRowAction = GraniteTemplateItems.actionEquals(claimableDef, GraniteTemplateActionNames.CLAIM_ITEM);
        if (!claimableKeyed && !claimRowAction) {
            plugin.logger().warn(
                    "unclaimed_items template: claimable grid must use YAML key 'claimable' or action claim_item; using built-in menu");
            return false;
        }

        Optional<Integer> prevSlot =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "previous", List.of(GraniteTemplateActionNames.PREVIOUS_PAGE));
        Optional<Integer> nextSlot =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "next", List.of(GraniteTemplateActionNames.NEXT_PAGE));
        Optional<Integer> claimAllSlot =
                GraniteTemplateItems.firstSlotByPreferredKeyOrActions(
                        template, "claim_all", List.of(GraniteTemplateActionNames.CLAIM_ALL));
        if (prevSlot.isEmpty() || nextSlot.isEmpty() || claimAllSlot.isEmpty()) {
            plugin.logger().warn(
                    "unclaimed_items template: missing navigation item (previous/next/claim_all); using built-in menu");
            return false;
        }
        if (prevSlot.get() != FabricUnclaimedItemsMenuGui.SLOT_PREV
                || claimAllSlot.get() != FabricUnclaimedItemsMenuGui.SLOT_CLAIM_ALL
                || nextSlot.get() != FabricUnclaimedItemsMenuGui.SLOT_NEXT) {
            plugin.logger().warn(
                    "unclaimed_items template: nav slots must match built-in (prev "
                            + FabricUnclaimedItemsMenuGui.SLOT_PREV
                            + ", claim_all "
                            + FabricUnclaimedItemsMenuGui.SLOT_CLAIM_ALL
                            + ", next "
                            + FabricUnclaimedItemsMenuGui.SLOT_NEXT
                            + "); using built-in menu");
            return false;
        }

        GraniteTemplateItem prevItem = template.items().get("previous");
        GraniteTemplateItem nextItem = template.items().get("next");
        GraniteTemplateItem claimAllItem = template.items().get("claim_all");
        if (prevItem == null || nextItem == null || claimAllItem == null) {
            plugin.logger().warn(
                    "unclaimed_items template: require YAML keys previous, next, claim_all; using built-in menu");
            return false;
        }
        if (!GraniteTemplateItems.actionEquals(prevItem, GraniteTemplateActionNames.PREVIOUS_PAGE)
                || !GraniteTemplateItems.actionEquals(nextItem, GraniteTemplateActionNames.NEXT_PAGE)
                || !GraniteTemplateItems.actionEquals(claimAllItem, GraniteTemplateActionNames.CLAIM_ALL)) {
            plugin.logger().warn(
                    "unclaimed_items template: navigation actions must be previous_page, next_page, claim_all; using built-in menu");
            return false;
        }

        int prev = prevSlot.get();
        int next = nextSlot.get();
        int claimAll = claimAllSlot.get();

        Map<String, String> globalPh = buildGlobalPlaceholders(page0, maxPage0);

        String titleExpanded = FabricUnclaimedItemsPlaceholders.expand(template.title(), globalPh);
        Optional<Text> titleBaseOpt =
                FabricGraniteTemplateStacks.toLiteralNoUnresolved(titleExpanded, plugin, MENU_LOG, "title");
        if (titleBaseOpt.isEmpty()) {
            return false;
        }
        Text pageTitle =
                FabricListMenuPaging.appendPageToTitleIfNeeded(titleBaseOpt.get(), page0, maxPage0);

        Optional<ItemStack> fillBase =
                FabricGraniteTemplateStacks.buildFillStack(template.fill(), globalPh, plugin, MENU_LOG);
        if (template.fill().isPresent() && fillBase.isEmpty()) {
            return false;
        }

        List<ItemStack> rowStacks = new ArrayList<>(sub.size());
        for (KeyIntPair pair : sub) {
            Optional<ItemStack> row = buildClaimableRowStack(plugin, claimableDef, pair, lo, globalPh);
            if (row.isEmpty()) {
                return false;
            }
            rowStacks.add(row.get());
        }

        Optional<ItemStack> claimAllStack =
                FabricGraniteTemplateStacks.buildTemplateItemStack(
                        claimAllItem, globalPh, plugin, MENU_LOG, "items.claim_all");
        if (claimAllStack.isEmpty()) {
            return false;
        }

        Optional<ItemStack> prevStackOpt = Optional.empty();
        Optional<ItemStack> nextStackOpt = Optional.empty();
        if (maxPage0 > 0) {
            if (page0 > 0) {
                Optional<ItemStack> ps =
                        FabricGraniteTemplateStacks.buildTemplateItemStack(
                                prevItem, globalPh, plugin, MENU_LOG, "items.previous");
                if (ps.isEmpty()) {
                    return false;
                }
                prevStackOpt = ps;
            }
            if (page0 < maxPage0) {
                Optional<ItemStack> ns =
                        FabricGraniteTemplateStacks.buildTemplateItemStack(
                                nextItem, globalPh, plugin, MENU_LOG, "items.next");
                if (ns.isEmpty()) {
                    return false;
                }
                nextStackOpt = ns;
            }
        }

        final int fPage = page0;
        final int fMax = maxPage0;
        final Optional<ItemStack> fPrevOpt = prevStackOpt;
        final Optional<ItemStack> fNextOpt = nextStackOpt;

        GraniteContainerMenus.openClickable9x6(
                player,
                pageTitle,
                inv -> {
                    if (fillBase.isPresent()) {
                        ItemStack fill = fillBase.get();
                        for (int s = 0; s < FabricMenuSlotLayout.TOP_SLOTS; s++) {
                            inv.setStack(s, fill.copy());
                        }
                    } else {
                        for (int s = 0; s < FabricMenuSlotLayout.TOP_SLOTS; s++) {
                            inv.setStack(s, ItemStack.EMPTY);
                        }
                    }
                    for (int i = 0; i < rowStacks.size(); i++) {
                        inv.setStack(claimableDef.slots().get(i), rowStacks.get(i).copy());
                    }
                    inv.setStack(claimAll, claimAllStack.get().copy());
                    if (fMax > 0) {
                        fPrevOpt.ifPresent(st -> inv.setStack(prev, st.copy()));
                        fNextOpt.ifPresent(st -> inv.setStack(next, st.copy()));
                    }
                },
                (topSlot, pl, b, t) ->
                        FabricUnclaimedItemsMenuGui.handleUnclaimedClick(pl, plugin, fPage, fMax, topSlot));
        return true;
    }

    private static Map<String, String> buildGlobalPlaceholders(int page0, int maxPage0) {
        Map<String, String> m = new HashMap<>();
        m.put("page", String.valueOf(page0 + 1));
        m.put("total_pages", String.valueOf(maxPage0 + 1));
        return m;
    }

    private static Optional<ItemStack> buildClaimableRowStack(
            FabricAuraSkills plugin,
            GraniteTemplateItem claimableDef,
            KeyIntPair pair,
            Locale lo,
            Map<String, String> globalPh) {
        ItemStack show = FabricUnclaimedItemsMenuGui.baseStackForUnclaimedEntry(plugin, pair, lo);
        Map<String, String> rowPh = new HashMap<>(globalPh);
        rowPh.put("item_name", show.getName().getString());
        rowPh.put("item_amount", String.valueOf(show.getCount()));

        List<Text> lore = new ArrayList<>();
        if (show.contains(DataComponentTypes.LORE) && show.get(DataComponentTypes.LORE) != null) {
            lore.addAll(show.get(DataComponentTypes.LORE).lines());
        }
        if (!claimableDef.lore().isEmpty()) {
            if (!lore.isEmpty()) {
                lore.add(Text.empty());
            }
            for (String loreRaw : claimableDef.lore()) {
                String expanded = FabricUnclaimedItemsPlaceholders.expand(loreRaw, rowPh);
                Optional<Text> line =
                        FabricGraniteTemplateStacks.toLiteralNoUnresolved(
                                expanded, plugin, MENU_LOG, "items.claimable.lore");
                if (line.isEmpty()) {
                    return Optional.empty();
                }
                lore.add(line.get());
            }
        }
        if (!lore.isEmpty()) {
            show.set(DataComponentTypes.LORE, new LoreComponent(lore));
        }
        return Optional.of(show);
    }
}
