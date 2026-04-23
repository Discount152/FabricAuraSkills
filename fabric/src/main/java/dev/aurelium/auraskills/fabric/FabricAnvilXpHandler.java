package dev.aurelium.auraskills.fabric;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.AnvilXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricAnvilXpHandler {

    private FabricAnvilXpHandler() {
    }

    /**
     * Bukkit: {@code AnvilLeveler#onAnvilCombine} at {@code EventPriority.MONITOR} on the <strong>result</strong> slot
     * after {@code failsClickChecks} (plus {@code failsChecks}). Fabric: one-shot at
     * {@link net.minecraft.screen.AnvilScreenHandler#onTakeOutput} (only runs on a real output take; no preview).
     * Left/right/level cost for matching and {@code {repair_cost}}: prefer
     * {@link FabricAnvilTakeContext#takePending()} from {@code onTake} HEAD (pre-consumption); fallback to current slots
     * if missing (e.g. another mod alters the flow).
     */
    public static void onTakeOutput(ServerPlayerEntity player, ForgingScreenHandler handler) {
        if (!(handler instanceof AnvilScreenHandler anvil)) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        FabricAnvilTakeContext.Pending snap = FabricAnvilTakeContext.takePending();
        ItemStack left;
        ItemStack right;
        int repairCost;
        if (snap != null) {
            left = snap.left();
            right = snap.right();
            repairCost = snap.levelCost();
        } else {
            left = handler.getSlot(AnvilScreenHandler.INPUT_1_ID).getStack();
            right = handler.getSlot(AnvilScreenHandler.INPUT_2_ID).getStack();
            repairCost = anvil.getLevelCost();
        }
        if (left.isEmpty()) {
            return;
        }
        SkillSource<AnvilXpSource> skillSource = findAnvilSource(plugin, left, right);
        if (skillSource == null) {
            return;
        }
        AnvilXpSource source = skillSource.source();
        Skill skill = skillSource.skill();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.ANVIL)) {
            return;
        }
        double multiplier = getRepairCostMultiplier(plugin, source, skill, repairCost);
        User user = plugin.getUserManager().getUser(player.getUuid());
        plugin.getLevelManager().addXp(user, skill, source, multiplier * source.getXp());
    }

    private static SkillSource<AnvilXpSource> findAnvilSource(FabricAuraSkills plugin, ItemStack left, ItemStack right) {
        for (SkillSource<AnvilXpSource> entry : plugin.getSkillManager().getSourcesOfType(AnvilXpSource.class)) {
            AnvilXpSource source = entry.source();
            Skill skill = entry.skill();
            boolean leftMatches = FabricLootItemMatcher.passesApiItemFilter(plugin, left, source.getLeftItem(), skill);
            boolean rightMatches = FabricLootItemMatcher.passesApiItemFilter(plugin, right, source.getRightItem(), skill);
            if (leftMatches && rightMatches) {
                return entry;
            }
        }
        return null;
    }

    private static double getRepairCostMultiplier(FabricAuraSkills plugin, AnvilXpSource source, Skill skill, int repairCost) {
        double multiplier = 1;
        String multiplierString = source.getMultiplier();
        if (multiplierString != null) {
            multiplierString = TextUtil.replace(multiplierString, "{repair_cost}", String.valueOf(repairCost));
            try {
                multiplier = new Expression(multiplierString).evaluate().getNumberValue().doubleValue();
            } catch (EvaluationException | ParseException e) {
                plugin.logger().warn("Invalid multiplier for anvil source " + source.getId() + " in skill " + skill.getId());
                e.printStackTrace();
            }
        }
        return multiplier;
    }
}
