package dev.granite.graniteskills.bukkit.trait;

import dev.granite.graniteskills.api.event.mana.ManaRegenerateEvent;
import dev.granite.graniteskills.api.mana.ManaAbilities;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ManaRegenTrait extends TraitImpl {

    ManaRegenTrait(GraniteSkills plugin) {
        super(plugin, Traits.MANA_REGEN);
        startRegen();
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        return Traits.MANA_REGEN.optionDouble("base");
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value) + "/s";
    }

    public void startRegen() {
        var task = new TaskRunnable() {
            @Override
            public void run() {
                if (!Traits.MANA_REGEN.isEnabled()) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    User user = plugin.getUser(player);
                    double originalMana = user.getMana();
                    double maxMana = user.getMaxMana();
                    if (originalMana < maxMana) {
                        if (!user.getAbilityData(ManaAbilities.ABSORPTION).getBoolean("activated")) {
                            double regen = user.getEffectiveTraitLevel(Traits.MANA_REGEN);
                            double finalRegen = Math.min(originalMana + regen, maxMana) - originalMana;
                            ManaRegenerateEvent event = new ManaRegenerateEvent(player, user.toApi(), finalRegen);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                user.setMana(originalMana + event.getAmount());
                            }
                        }
                    }
                }
            }
        };
        plugin.getScheduler().timerSync(task, 0, 1, TimeUnit.SECONDS);
    }

}
