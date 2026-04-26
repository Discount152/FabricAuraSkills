package dev.aurelium.auraskills.common.ui;

import com.google.common.collect.Sets;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.hooks.PlaceholderHook;
import dev.aurelium.auraskills.common.message.type.ActionBarMessage;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.ui.UiProvider.FormatType;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class ActionBarManager {

    public static final int PAUSE_MS = 750;
    private static final long MS_PER_TICK = 50L;
    protected final AuraSkillsPlugin plugin;
    private final UiProvider uiProvider;

    private final Set<UUID> isPaused = Sets.newConcurrentHashSet();
    private final Set<UUID> isGainingXp = Sets.newConcurrentHashSet();
    private final Map<UUID, Integer> timer = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> currentAction = new ConcurrentHashMap<>();
    private final Map<Locale, String> idleMessageCache = new ConcurrentHashMap<>();
    private final Map<UUID, IdleActionBarState> idleSmartState = new ConcurrentHashMap<>();

    public ActionBarManager(AuraSkillsPlugin plugin, UiProvider uiProvider) {
        this.plugin = plugin;
        this.uiProvider = uiProvider;
        startTimerCountdown();
        startUpdatingIdleActionBar();
    }

    public void startTimerCountdown() {
        var task = new TaskRunnable() {
            @Override
            public void run() {
                if (!plugin.configBoolean(Option.ACTION_BAR_ENABLED)) {
                    return;
                }

                for (User user : plugin.getUserManager().getOnlineUsers()) {
                    UUID uuid = user.getUuid();
                    Integer time = timer.get(uuid);
                    if (time != null) {
                        if (time > 0) {
                            timer.put(uuid, time - 1);
                        }
                    } else {
                        timer.put(uuid, 0);
                    }
                }
            }
        };
        plugin.getScheduler().timerSync(task, 0, 2 * 50L, TimeUnit.MILLISECONDS);
    }

    public void startUpdatingIdleActionBar() {
        var task = new TaskRunnable() {
            @Override
            public void run() {
                if (!plugin.configBoolean(Option.ACTION_BAR_IDLE) || !plugin.configBoolean(Option.ACTION_BAR_ENABLED)) {
                    return;
                }
                long now = System.currentTimeMillis();
                for (User user : plugin.getUserManager().getOnlineUsers()) {
                    UUID uuid = user.getUuid();
                    if (!user.isActionBarEnabled(ActionBarType.IDLE)) {
                        continue;
                    }
                    if (plugin.getWorldManager().isDisabledWorld(getWorldName(user))) {
                        continue;
                    }

                    if (!currentAction.containsKey(uuid)) {
                        currentAction.put(uuid, 0);
                    }
                    if (isGainingXp.contains(uuid) || isPaused.contains(uuid)) {
                        continue;
                    }

                    if (user.getActionBarIdleMode() == ActionBarIdleMode.ALWAYS) {
                        sendIdleActionBarMessage(user);
                        continue;
                    }
                    tickSmartIdle(user, now);
                }
            }
        };
        if (plugin.configBoolean(Option.ACTION_BAR_UPDATE_ASYNC)) {
            plugin.getScheduler().timerAsync(task, 0, plugin.configInt(Option.ACTION_BAR_UPDATE_PERIOD) * 50L, TimeUnit.MILLISECONDS);
        } else {
            plugin.getScheduler().timerSync(task, 0, plugin.configInt(Option.ACTION_BAR_UPDATE_PERIOD) * 50L, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Short window after profile load / trait recalculation on join.
     */
    public void onIdleUserLoaded(User user) {
        extendIdleWindow(user, plugin.configInt(Option.ACTION_BAR_SHOW_ON_JOIN_TICKS));
    }

    /**
     * Dimension / server world change without relying on displayed HP/mana delta.
     */
    public void onIdleWorldChanged(User user) {
        extendIdleWindow(user, plugin.configInt(Option.ACTION_BAR_SHOW_ON_JOIN_TICKS));
        IdleActionBarState st = idleSmartState.get(user.getUuid());
        if (st != null) {
            st.lastWorldKey = null;
        }
    }

    /**
     * Combat or general player damage — extends the smart window so the bar stays readable in fights.
     */
    public void onIdleCombatOrDamage(User user) {
        extendIdleWindow(user, plugin.configInt(Option.ACTION_BAR_SHOW_AFTER_COMBAT_TICKS));
    }

    private void extendIdleWindow(User user, int ticks) {
        long now = System.currentTimeMillis();
        long add = Math.max(0, ticks) * MS_PER_TICK;
        IdleActionBarState st = idleSmartState.computeIfAbsent(user.getUuid(), u -> new IdleActionBarState());
        st.visibleUntilMs = Math.max(st.visibleUntilMs, now + add);
    }

    private void tickSmartIdle(User user, long now) {
        String world = getWorldName(user);
        int h = parsePositiveInt(getHp(user));
        int mh = parsePositiveInt(getMaxHp(user));
        int m = parsePositiveInt(getMana(user));
        int mm = parsePositiveInt(getMaxMana(user));

        IdleActionBarState st = idleSmartState.computeIfAbsent(user.getUuid(), u -> new IdleActionBarState());

        if (st.lastWorldKey == null) {
            st.lastWorldKey = world;
        } else if (!st.lastWorldKey.equals(world)) {
            st.lastWorldKey = world;
            int join = plugin.configInt(Option.ACTION_BAR_SHOW_ON_JOIN_TICKS);
            st.visibleUntilMs = Math.max(st.visibleUntilMs, now + (long) join * MS_PER_TICK);
        }

        // Sprint 81B: "stable" values are only updated when the smart window is *over* (see !inWindow
        // block below). While the window is open after a level-up, current h/max/m stays different
        // from those stables *every* tick. Extending on stable-diff must run only when we were
        // *not* already in a window, or we re-extend forever. Intra-window changes use inWin* below.
        boolean wasInWindow = now < st.visibleUntilMs;

        int healthTicks = Math.max(0, plugin.configInt(Option.ACTION_BAR_SHOW_AFTER_HEALTH_CHANGE_TICKS));
        int manaTicks = Math.max(0, plugin.configInt(Option.ACTION_BAR_SHOW_AFTER_MANA_CHANGE_TICKS));

        if (!st.hasStableDisplay) {
            st.stableH = h;
            st.stableMaxH = mh;
            st.stableM = m;
            st.stableMaxM = mm;
            st.inWinH = h;
            st.inWinMaxH = mh;
            st.inWinM = m;
            st.inWinMaxM = mm;
            st.hasStableDisplay = true;
        } else {
            boolean dHealth = h != st.stableH || mh != st.stableMaxH;
            boolean dMana = m != st.stableM || mm != st.stableMaxM;
            if (shouldExtendSmartIdleWindowFromStableDiff(wasInWindow, dHealth, dMana)) {
                int ext = pickExtendTicks(dHealth, dMana, healthTicks, manaTicks);
                if (ext > 0) {
                    st.visibleUntilMs = Math.max(st.visibleUntilMs, now + (long) ext * MS_PER_TICK);
                }
            }
        }

        boolean inWindow = now < st.visibleUntilMs;
        if (inWindow) {
            boolean hNow = h != st.inWinH || mh != st.inWinMaxH;
            boolean mNow = m != st.inWinM || mm != st.inWinMaxM;
            if (hNow || mNow) {
                int ext = pickExtendTicks(hNow, mNow, healthTicks, manaTicks);
                if (ext > 0) {
                    st.visibleUntilMs = Math.max(st.visibleUntilMs, now + (long) ext * MS_PER_TICK);
                }
            }
            st.inWinH = h;
            st.inWinMaxH = mh;
            st.inWinM = m;
            st.inWinMaxM = mm;
        }

        inWindow = now < st.visibleUntilMs;
        if (!inWindow) {
            st.stableH = h;
            st.stableMaxH = mh;
            st.stableM = m;
            st.stableMaxM = mm;
            if (plugin.configBoolean(Option.ACTION_BAR_HIDE_WHEN_FULL_AND_IDLE)
                    && h >= mh
                    && m >= mm
                    && mh > 0
                    && mm > 0) {
                return;
            }
            return;
        }

        int whileVis = Math.max(1, plugin.configInt(Option.ACTION_BAR_UPDATE_WHILE_VISIBLE_TICKS));
        if (now - st.lastSendMs < (long) whileVis * MS_PER_TICK) {
            return;
        }
        sendIdleActionBarMessage(user);
        st.lastSendMs = now;
    }

    private static int pickExtendTicks(boolean dHealth, boolean dMana, int healthTicks, int manaTicks) {
        if (dHealth && dMana) {
            return Math.max(healthTicks, manaTicks);
        } else if (dHealth) {
            return healthTicks;
        } else {
            return manaTicks;
        }
    }

    /**
     * "Stable" HP/mana are frozen until the smart visibility window ends. While the window is open,
     * they intentionally differ from current values, so a stable-vs-current check must not extend
     * the window on every tick (Sprint 81B).
     */
    static boolean shouldExtendSmartIdleWindowFromStableDiff(
            boolean wasInWindow, boolean dHealth, boolean dMana) {
        return (dHealth || dMana) && !wasInWindow;
    }

    private void sendIdleActionBarMessage(User user) {
        boolean formatLast = plugin.configBoolean(Option.ACTION_BAR_FORMAT_LAST);
        Locale locale = user.getLocale();

        String base;
        if (formatLast) {
            base = plugin.getMessageProvider().getRaw(ActionBarMessage.IDLE, locale);
        } else {
            String cache = idleMessageCache.get(locale);
            if (cache != null) {
                base = cache;
            } else {
                base = plugin.getMsg(ActionBarMessage.IDLE, locale);
                idleMessageCache.put(locale, base);
            }
        }

        String message = TextUtil.replace(base,
                "{hp}", getHp(user),
                "{max_hp}", getMaxHp(user),
                "{mana}", getMana(user),
                "{max_mana}", getMaxMana(user));
        message = replacePlaceholderApi(user, message);

        if (formatLast) {
            message = plugin.getMessageProvider().applyFormatting(message);
        }

        uiProvider.sendActionBar(user, message);
    }

    private static int parsePositiveInt(String raw) {
        if (raw == null || raw.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void sendXpActionBar(User user, Skill skill, double currentXp, double levelXp, double xpGained, int level, boolean maxed, double income) {
        ActionBarType sendType = maxed ? ActionBarType.MAXED : ActionBarType.XP;
        // Return if the type is disabled in config
        if (!plugin.configBoolean(sendType.getOption()) || !plugin.configBoolean(Option.ACTION_BAR_ENABLED)) {
            return;
        }

        // Return if player has it disabled
        if (!user.isActionBarEnabled(sendType)) {
            return;
        }

        UUID uuid = user.getUuid();
        if (isPaused.contains(uuid)) {
            return;
        }
        // Set timer and is gaining xp
        isGainingXp.add(uuid);
        timer.put(uuid, 20);

        // Increment action number
        int thisAction = currentAction.getOrDefault(uuid, 0) + 1;
        currentAction.put(uuid, thisAction);
        // Schedule timer task to update action bar
        plugin.getScheduler().timerSync(
                new TaskRunnable() {
                    @Override
                    public void run() {
                        if (!isGainingXp.contains(uuid)) {
                            cancel();
                            return;
                        }
                        // Cancel if a later action bar is sent
                        Integer actionNum = currentAction.get(uuid);
                        if (actionNum == null) {
                            cancel();
                            return;
                        }
                        if (thisAction != actionNum) {
                            cancel();
                            return;
                        }

                        String message = getXpActionBarMessage(user, skill, currentXp, levelXp, xpGained, level, maxed, income);
                        uiProvider.sendActionBar(user, message);
                    }
                }, 0, plugin.configInt(Option.ACTION_BAR_UPDATE_PERIOD) * 50L, TimeUnit.MILLISECONDS);
        // Schedule task to stop updating action bar
        plugin.getScheduler().scheduleSync(() -> {
            Integer timerNum = timer.get(uuid);
            if (timerNum != null) {
                if (timerNum.equals(0)) {
                    isGainingXp.remove(uuid);
                }
            }
        }, 41 * 50L, TimeUnit.MILLISECONDS);
    }

    public void resetActionBars() {
        isGainingXp.clear();
        timer.clear();
        currentAction.clear();
        isPaused.clear();
        idleSmartState.clear();
        clearMessageCache();
    }

    public void resetActionBar(User user) {
        UUID uuid = user.getUuid();
        isGainingXp.remove(uuid);
        timer.remove(uuid);
        currentAction.remove(uuid);
        isPaused.remove(uuid);
        idleSmartState.remove(uuid);
    }

    public void setPaused(User user, int time, TimeUnit timeUnit) {
        UUID uuid = user.getUuid();
        isPaused.add(uuid);
        Integer action = currentAction.get(uuid);
        if (action != null) {
            currentAction.put(uuid, action + 1);
        } else {
            currentAction.put(uuid, 0);
        }
        int thisAction = this.currentAction.get(uuid);
        plugin.getScheduler().scheduleSync(() -> {
            Integer actionBarCurrentAction = currentAction.get(uuid);
            if (actionBarCurrentAction != null) {
                if (thisAction == actionBarCurrentAction) {
                    isPaused.remove(uuid);
                }
            }
        }, time, timeUnit);
    }

    public void sendAbilityActionBar(User user, String message) {
        if (!user.isActionBarEnabled(ActionBarType.ABILITY)) return;

        boolean formatLast = plugin.configBoolean(Option.ACTION_BAR_FORMAT_LAST);

        String base;
        if (formatLast) {
            base = plugin.getMessageProvider().getRaw(ActionBarMessage.ABILITY, user.getLocale());
        } else {
            base = plugin.getMsg(ActionBarMessage.ABILITY, user.getLocale());
        }

        String actionBarText = TextUtil.replace(base,
                "{hp}", getHp(user),
                "{max_hp}", getMaxHp(user),
                "{mana}", getMana(user),
                "{max_mana}", getMaxMana(user),
                "{message}", message);
        actionBarText = replacePlaceholderApi(user, actionBarText);

        if (formatLast) {
            actionBarText = plugin.getMessageProvider().applyFormatting(actionBarText);
        }

        plugin.getUiProvider().sendActionBar(user, actionBarText);
        setPaused(user, PAUSE_MS, TimeUnit.MILLISECONDS);
    }

    private String getXpActionBarMessage(User user, Skill skill, double currentXp, double levelXp, double xpGained, int level, boolean maxed, double income) {
        ActionBarMessage messageKey = maxed ? ActionBarMessage.MAXED : ActionBarMessage.XP;
        Locale locale = user.getLocale();

        boolean formatLast = plugin.configBoolean(Option.ACTION_BAR_FORMAT_LAST);

        String base;
        if (formatLast) {
            base = plugin.getMessageProvider().getRaw(messageKey, locale);
        } else {
            base = plugin.getMsg(messageKey, locale);
        }

        // Replace built in placeholders
        String message = TextUtil.replace(base,
                "{hp}", getHp(user),
                "{max_hp}", getMaxHp(user),
                "{xp_gained}", xpGained > 0 ? "+" + NumberUtil.format1(xpGained) : NumberUtil.format1(xpGained),
                "{skill}", skill.getDisplayName(locale),
                "{current_xp}", plugin.getUiProvider().getFormat(FormatType.XP).format(currentXp),
                "{level_xp}", NumberUtil.format1(levelXp),
                "{skill_level}", String.valueOf(level),
                "{mana}", getMana(user),
                "{max_mana}", getMaxMana(user),
                "{income}", plugin.getUiProvider().getFormat(FormatType.MONEY).format(income));
        // Replace PlaceholderAPI placeholders
        message = replacePlaceholderApi(user, message);

        if (formatLast) {
            message = plugin.getMessageProvider().applyFormatting(message);
        }

        return message;
    }

    @NotNull
    public abstract String getHp(User user);

    @NotNull
    public abstract String getMaxHp(User user);

    @NotNull
    public abstract String getWorldName(User user);

    private String getMana(User user) {
        return String.valueOf((int) Math.floor(user.getMana()));
    }

    private String getMaxMana(User user) {
        return String.valueOf((int) Math.floor(user.getMaxMana()));
    }

    private String replacePlaceholderApi(User user, String message) {
        if (plugin.getHookManager().isRegistered(PlaceholderHook.class) && plugin.configBoolean(Option.ACTION_BAR_PLACEHOLDER_API)) {
            return plugin.getHookManager().getHook(PlaceholderHook.class).setPlaceholders(user, message);
        }
        return message;
    }

    public void clearMessageCache() {
        idleMessageCache.clear();
    }

    private static final class IdleActionBarState {
        long visibleUntilMs;
        long lastSendMs;
        int stableH;
        int stableMaxH;
        int stableM;
        int stableMaxM;
        boolean hasStableDisplay;
        int inWinH;
        int inWinMaxH;
        int inWinM;
        int inWinMaxM;
        @Nullable String lastWorldKey;
    }

}
