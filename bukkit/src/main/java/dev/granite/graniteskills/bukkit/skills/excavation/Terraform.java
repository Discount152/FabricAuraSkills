package dev.granite.graniteskills.bukkit.skills.excavation;

import dev.granite.graniteskills.api.event.mana.TerraformBlockBreakEvent;
import dev.granite.graniteskills.api.mana.ManaAbilities;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.api.source.type.BlockXpSource;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.hooks.TownyHook;
import dev.granite.graniteskills.bukkit.mana.ReadiedManaAbility;
import dev.granite.graniteskills.bukkit.source.BlockLeveler;
import dev.granite.graniteskills.common.message.type.ManaAbilityMessage;
import dev.granite.graniteskills.common.source.SourceTag;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.LinkedList;

public class Terraform extends ReadiedManaAbility {

    public Terraform(GraniteSkills plugin) {
        super(plugin, ManaAbilities.TERRAFORM, ManaAbilityMessage.TERRAFORM_START, ManaAbilityMessage.TERRAFORM_END,
                new String[]{"SHOVEL"}, new Action[]{Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR});
    }

    @Override
    public void onActivate(Player player, User user) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    @Override
    public void onStop(Player player, User user) {

    }

    @Override
    public String replaceDescPlaceholders(String input, User user) {
        return TextUtil.replace(input, "{radius}", String.valueOf(4));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (isDisabled()) return;
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (failsChecks(player)) return;

        if (block.hasMetadata("block-ignore")) { // Compatibility fix
            return;
        }
        // AdvancedEnchantments compatibility fix
        if (block.hasMetadata("blockbreakevent-ignore")) {
            return;
        }
        if (!block.hasMetadata("GraniteSkills-Terraform") && event.getClass() == BlockBreakEvent.class) {
            applyTerraform(player, plugin.getUser(player), block);
        }
    }

    private void applyTerraform(Player player, User user, Block block) {
        // Check if block is applicable to ability
        var skillSource = plugin.getLevelManager().getLeveler(BlockLeveler.class).getSource(block, BlockXpSource.BlockTriggers.BREAK);
        if (skillSource == null) return;

        XpSource source = skillSource.source();

        if (!plugin.getSkillManager().hasTag(source, SourceTag.TERRAFORM_APPLICABLE)) return;
        // Apply if activated
        if (isActivated(user)) {
            terraformBreak(player, block);
            return;
        }
        // Checks if ability is ready
        if (isHoldingMaterial(player) && checkActivation(player)) {
            terraformBreak(player, block);
        }
    }

    private void terraformBreak(Player player, Block block) {
        Material material = block.getType();
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        LinkedList<Block> toCheck = new LinkedList<>();
        toCheck.add(block);

        int count = 0;
        int maxCount = manaAbility.optionInt("max_blocks", 61);
        if (manaAbility.optionBoolean("max_limit_durability", false)) {
            maxCount = getHoldingMaterialDurability(player, maxCount);
        }

        while ((block = toCheck.poll()) != null && count < maxCount) {
            if (block.getType() == material) {
                block.setMetadata("GraniteSkills-Terraform", new FixedMetadataValue(plugin, true));
                breakBlock(player, block);
                for (BlockFace face : faces) {
                    toCheck.add(block.getRelative(face));
                }
                count++;
            }
        }

        setHoldingMaterialDurability(player, count, manaAbility.optionDouble("durability_multiplier", 0));
    }

    private void breakBlock(Player player, Block block) {
        if (plugin.getHookManager().isRegistered(TownyHook.class) && !plugin.getHookManager().getHook(TownyHook.class).canBreak(player, block)) {
            block.removeMetadata("GraniteSkills-Terraform", plugin);
            return;
        }
        TerraformBlockBreakEvent event = new TerraformBlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            block.breakNaturally(player.getInventory().getItemInMainHand());
        }
        block.removeMetadata("GraniteSkills-Terraform", plugin);
    }

}
