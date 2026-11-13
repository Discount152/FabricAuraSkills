package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.mana.ManaAbilities;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class FabricManaRegenTrait extends FabricTraitImpl {

    public FabricManaRegenTrait(FabricGraniteSkills plugin) {
        super(plugin, Traits.MANA_REGEN);
        startRegen(plugin);
    }

    @Override
    public double getBaseLevel(ServerPlayer player, Trait trait) {
        return Traits.MANA_REGEN.optionDouble("base");
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value) + "/s";
    }

    private void startRegen(FabricGraniteSkills plugin) {
        TaskRunnable task = new TaskRunnable() {
            @Override
            public void run() {
                if (!Traits.MANA_REGEN.isEnabled()) {
                    return;
                }
                for (ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
                    User user = plugin.getUserManager().getUser(player.getUUID());
                    double originalMana = user.getMana();
                    double maxMana = user.getMaxMana();
                    if (originalMana >= maxMana) {
                        continue;
                    }
                    if (user.getAbilityData(ManaAbilities.ABSORPTION).getBoolean("activated")) {
                        continue;
                    }
                    double regen = user.getEffectiveTraitLevel(Traits.MANA_REGEN);
                    double delta = Math.min(originalMana + regen, maxMana) - originalMana;
                    if (delta > 0) {
                        user.setMana(originalMana + delta);
                    }
                }
            }
        };
        plugin.getScheduler().timerSync(task, 0, 1, TimeUnit.SECONDS);
    }
}
