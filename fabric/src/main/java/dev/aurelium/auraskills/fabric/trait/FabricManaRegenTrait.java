package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class FabricManaRegenTrait extends FabricTraitImpl {

    public FabricManaRegenTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.MANA_REGEN);
        startRegen(plugin);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        return Traits.MANA_REGEN.optionDouble("base");
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value) + "/s";
    }

    private void startRegen(FabricAuraSkills plugin) {
        TaskRunnable task = new TaskRunnable() {
            @Override
            public void run() {
                if (!Traits.MANA_REGEN.isEnabled()) {
                    return;
                }
                for (ServerPlayerEntity player : plugin.getServer().getPlayerManager().getPlayerList()) {
                    User user = plugin.getUserManager().getUser(player.getUuid());
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
