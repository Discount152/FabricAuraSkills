package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.message.type.ActionBarMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class FabricBossBarManager {

    private final FabricGraniteSkills plugin;
    private final NumberFormat xpFormat = new DecimalFormat("#,###.##");
    private final NumberFormat percentFormat = new DecimalFormat("#,###.#");
    private final NumberFormat moneyFormat = new DecimalFormat("#,###.##");
    private final Map<UUID, ServerBossEvent> bars = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> generation = new ConcurrentHashMap<>();

    public FabricBossBarManager(FabricGraniteSkills plugin) {
        this.plugin = plugin;
    }

    public void loadOptions() {
    }

    public NumberFormat getXpFormat() {
        return xpFormat;
    }

    public NumberFormat getPercentFormat() {
        return percentFormat;
    }

    public NumberFormat getMoneyFormat() {
        return moneyFormat;
    }

    public void send(User user, Skill skill, double currentXp, double levelXp, double xpGained, int level,
            boolean maxed, double income) {
        if (!plugin.configBoolean(Option.BOSS_BAR_ENABLED)) {
            return;
        }
        if (maxed && !plugin.configBoolean(Option.BOSS_BAR_DISPLAY_MAXED)) {
            return;
        }
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        int every = Math.max(1, plugin.configInt(Option.BOSS_BAR_UPDATE_EVERY));
        int tick = generation.merge(player.getUUID(), 1, Integer::sum);
        if (tick % every != 0) {
            return;
        }
        float progress = levelXp <= 0 ? 1f : (float) Math.min(1.0, Math.max(0.0, currentXp / levelXp));
        Locale locale = user.getLocale();
        ActionBarMessage key = maxed
                ? (income > 0 ? ActionBarMessage.BOSS_BAR_INCOME_MAXED : ActionBarMessage.BOSS_BAR_MAXED)
                : (income > 0 ? ActionBarMessage.BOSS_BAR_INCOME : ActionBarMessage.BOSS_BAR_XP);
        double percent = levelXp <= 0 ? 100 : (currentXp / levelXp) * 100.0;
        String text = strip(TextUtil.replace(plugin.getMsg(key, locale),
                "{skill}", skill.getDisplayName(locale, false),
                "{level}", String.valueOf(level),
                "{xp_gained}", xpFormat.format(xpGained),
                "{current_xp}", xpFormat.format(currentXp),
                "{level_xp}", xpFormat.format(levelXp),
                "{percent}", percentFormat.format(percent),
                "{income}", moneyFormat.format(income)));
        BossEvent.BossBarColor color = colorFor(skill);
        ServerBossEvent bar = bars.computeIfAbsent(player.getUUID(), id -> {
            ServerBossEvent created = new ServerBossEvent(UUID.randomUUID(), Component.literal(text), color,
                    BossEvent.BossBarOverlay.PROGRESS);
            created.addPlayer(player);
            return created;
        });
        bar.setName(Component.literal(text));
        bar.setProgress(progress);
        bar.setColor(color);
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        int seen = tick;
        long stayMillis = Math.max(1, plugin.configInt(Option.BOSS_BAR_STAY_TIME)) * 50L;
        plugin.getScheduler().scheduleSync(() -> {
            if (generation.getOrDefault(player.getUUID(), -1) != seen) {
                return;
            }
            ServerBossEvent current = bars.remove(player.getUUID());
            if (current != null) {
                current.removeAllPlayers();
            }
        }, stayMillis, TimeUnit.MILLISECONDS);
    }

    public void clear(ServerPlayer player) {
        ServerBossEvent bar = bars.remove(player.getUUID());
        if (bar != null) {
            bar.removeAllPlayers();
        }
        generation.remove(player.getUUID());
    }

    private BossEvent.BossBarColor colorFor(Skill skill) {
        for (String entry : plugin.configStringList(Option.BOSS_BAR_FORMAT)) {
            String[] parts = entry.split(" ");
            if (parts.length >= 2 && parts[0].equalsIgnoreCase(skill.name())) {
                try {
                    return BossEvent.BossBarColor.valueOf(parts[1].toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                    return BossEvent.BossBarColor.GREEN;
                }
            }
        }
        return BossEvent.BossBarColor.GREEN;
    }

    private static String strip(String input) {
        return input.replaceAll("<[^>]+>", "");
    }
}
