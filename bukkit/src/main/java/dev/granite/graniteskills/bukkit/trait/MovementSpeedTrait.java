package dev.granite.graniteskills.bukkit.trait;

import dev.granite.graniteskills.api.event.user.UserLoadEvent;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.util.AttributeCompat;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class MovementSpeedTrait extends TraitImpl {

    MovementSpeedTrait(GraniteSkills plugin) {
        super(plugin, Traits.MOVEMENT_SPEED);
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        AttributeInstance attribute = player.getAttribute(AttributeCompat.movementSpeed);
        if (attribute == null) {
            return 100;
        }
        double totalValue = attribute.getValue();
        // Subtract the trait value
        double attributeRatio = 1000;
        double baseValue = totalValue - getValue(player, trait, attributeRatio);
        return baseValue * attributeRatio;
    }

    @Override
    protected void reload(Player player, Trait trait) {
        if (plugin.getScheduler().isFolia()) {
            plugin.getScheduler().executeAtEntity(player, (task) -> {
                reloadWalkSpeed(player, trait);
            });
        } else {
            reloadWalkSpeed(player, trait);
        }
    }

    private void reloadWalkSpeed(Player player, Trait trait) {
        double walkSpeedRatio = 500;
        double value = getValue(player, trait, walkSpeedRatio);

        if (!trait.isEnabled()) return;
        if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
            player.setWalkSpeed(0.2f);
            return;
        }
        double max = trait.optionDouble("max") / walkSpeedRatio;
        if (0.2 + value > max) {
            player.setWalkSpeed((float) (max));
            return;
        }

        player.setWalkSpeed(Math.min((float) (0.2 + value), 1f));
    }

    @EventHandler
    public void onLoad(UserLoadEvent event) {
        reload(event.getPlayer(), getTraits()[0]);
    }

    @Override
    public void changeWorld(PlayerChangedWorldEvent event, Trait trait) {
        if (!trait.equals(Traits.MOVEMENT_SPEED)) return;

        reload(event.getPlayer(), getTraits()[0]);
    }

    private double getValue(Player player, Trait trait, double ratio) {
        User user = plugin.getUser(player);
        return user.getBonusTraitLevel(trait) / ratio;
    }

}
