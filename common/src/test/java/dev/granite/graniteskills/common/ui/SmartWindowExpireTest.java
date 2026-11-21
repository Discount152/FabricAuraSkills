package dev.granite.graniteskills.common.ui;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.TraitProvider;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.message.PlatformLogger;
import dev.granite.graniteskills.common.scheduler.Scheduler;
import dev.granite.graniteskills.common.scheduler.Task;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.scheduler.TaskStatus;
import dev.granite.graniteskills.common.trait.TraitManager;
import dev.granite.graniteskills.common.user.User;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartWindowExpireTest {

    private static Field traitProvider;
    private static Object previousTraitProvider;

    @BeforeAll
    static void disableManaTraitLookup() throws Exception {
        traitProvider = Traits.class.getDeclaredField("provider");
        traitProvider.setAccessible(true);
        previousTraitProvider = traitProvider.get(Traits.MAX_MANA);
        traitProvider.set(Traits.MAX_MANA, disabledTrait());
    }

    @AfterAll
    static void restoreManaTraitLookup() throws Exception {
        if (traitProvider != null) {
            traitProvider.set(Traits.MAX_MANA, previousTraitProvider);
        }
    }

    @Test
    void stableHealthAndManaDoesNotExtendTheSmartWindow() throws Exception {
        Harness harness = Harness.open();
        try {
            User user = new TestUser(UUID.randomUUID(), harness.plugin);
            user.setMana(10);
            int afterJoin = refresh(harness.bars, user);
            int afterStable = refresh(harness.bars, user);
            System.out.println("afterJoin=" + afterJoin + " afterStable=" + afterStable);
            assertEquals(afterJoin - 1, afterStable);
            assertTrue(afterStable < ActionBarManager.SMART_HEALTH_TICKS);
            assertTrue(afterStable < ActionBarManager.SMART_MANA_TICKS);

            harness.bars.hp = "19";
            int afterHealthChange = refresh(harness.bars, user);
            System.out.println("afterHealthChange=" + afterHealthChange);
            assertTrue(afterHealthChange > afterStable);
            assertEquals(ActionBarManager.SMART_HEALTH_TICKS - 1, afterHealthChange);
        } finally {
            harness.close();
        }
    }

    @Test
    void alwaysStaysVisibleWhileIdleIsAllowed() throws Exception {
        Harness harness = Harness.open();
        try {
            User user = new TestUser(UUID.randomUUID(), harness.plugin);
            user.setMana(10);
            user.setLocale(Locale.ENGLISH);
            user.setActionBarIdleMode(ActionBarIdleMode.ALWAYS);
            harness.users.addUser(user);
            TaskRunnable idle = harness.scheduler.syncTasks.get(1);

            idle.run();
            clearSmartWindow(harness.bars, user.getUuid());
            harness.bars.lines.clear();
            idle.run();
            System.out.println("alwaysLine=" + harness.bars.lines);
            assertEquals(1, harness.bars.lines.size());
            assertEquals("20/20 10/20", harness.bars.lines.get(0));

            user.setActionBarIdleMode(ActionBarIdleMode.SMART);
            harness.bars.lines.clear();
            idle.run();
            assertEquals(0, harness.bars.lines.size());

            user.setActionBarIdleMode(ActionBarIdleMode.ALWAYS);
            harness.idleAllowed[0] = false;
            idle.run();
            assertEquals(0, harness.bars.lines.size());
        } finally {
            harness.close();
        }
    }

    @Test
    void offSendsNoIdleLine() throws Exception {
        Harness harness = Harness.open();
        try {
            User user = new TestUser(UUID.randomUUID(), harness.plugin);
            user.setMana(10);
            user.setLocale(Locale.ENGLISH);
            user.setActionBarIdleMode(ActionBarIdleMode.OFF);
            harness.users.addUser(user);
            TaskRunnable idle = harness.scheduler.syncTasks.get(1);

            idle.run();
            System.out.println("offLines=" + harness.bars.lines);
            assertEquals(false, user.isActionBarEnabled(ActionBarType.IDLE));
            assertEquals(0, harness.bars.lines.size());

            user.setActionBarIdleMode(ActionBarIdleMode.ALWAYS);
            idle.run();
            assertEquals(1, harness.bars.lines.size());
            assertEquals("20/20 10/20", harness.bars.lines.get(0));

            user.setActionBarIdleMode(ActionBarIdleMode.OFF);
            harness.bars.lines.clear();
            idle.run();
            assertEquals(0, harness.bars.lines.size());
        } finally {
            harness.close();
        }
    }

    @Test
    void idleSkipsWhileXpOrAbilityLineIsActive() throws Exception {
        Harness harness = Harness.open();
        try {
            User user = new TestUser(UUID.randomUUID(), harness.plugin);
            user.setMana(10);
            user.setLocale(Locale.ENGLISH);
            user.setActionBarIdleMode(ActionBarIdleMode.ALWAYS);
            harness.users.addUser(user);
            TaskRunnable idle = harness.scheduler.syncTasks.get(1);

            idle.run();
            assertEquals(1, harness.bars.lines.size());
            harness.bars.lines.clear();

            harness.bars.sendXpActionBar(user, dev.granite.graniteskills.api.skill.Skills.FARMING, 1, 10, 1, 1, false, 0);
            idle.run();
            System.out.println("duringXp=" + harness.bars.lines);
            assertEquals(0, harness.bars.lines.size());

            harness.bars.resetActionBar(user);
            idle.run();
            assertEquals(1, harness.bars.lines.size());
            harness.bars.lines.clear();

            harness.bars.sendAbilityActionBar(user, "Bleed");
            assertEquals(1, harness.bars.lines.size());
            harness.bars.lines.clear();
            idle.run();
            System.out.println("duringAbility=" + harness.bars.lines);
            assertEquals(0, harness.bars.lines.size());

            harness.bars.resetActionBar(user);
            idle.run();
            assertEquals("20/20 10/20", harness.bars.lines.get(0));
        } finally {
            harness.close();
        }
    }

    @Test
    void joinAndWorldChangeOpen100SmartTicks() throws Exception {
        Harness harness = Harness.open();
        try {
            User user = new TestUser(UUID.randomUUID(), harness.plugin);
            user.setMana(10);
            int afterJoin = refresh(harness.bars, user);
            System.out.println("join=" + afterJoin);
            assertEquals(ActionBarManager.SMART_JOIN_TICKS - 1, afterJoin);

            int fallen = afterJoin;
            while (fallen > 90) {
                fallen = refresh(harness.bars, user);
            }
            System.out.println("beforeWorldChange=" + fallen);
            assertTrue(fallen < ActionBarManager.SMART_JOIN_TICKS - 1);

            harness.bars.world = "nether";
            int afterWorldChange = refresh(harness.bars, user);
            System.out.println("worldChange=" + afterWorldChange);
            assertEquals(ActionBarManager.SMART_JOIN_TICKS - 1, afterWorldChange);
            assertTrue(afterWorldChange > fallen);
        } finally {
            harness.close();
        }
    }

    private static int refresh(ActionBarManager bars, User user) throws Exception {
        Method refresh = ActionBarManager.class.getDeclaredMethod("refreshSmartWindow", User.class, UUID.class);
        refresh.setAccessible(true);
        refresh.invoke(bars, user, user.getUuid());
        Field field = ActionBarManager.class.getDeclaredField("smartRemainingTicks");
        field.setAccessible(true);
        return ((Map<UUID, Integer>) field.get(bars)).get(user.getUuid());
    }

    private static void clearSmartWindow(ActionBarManager bars, UUID uuid) throws Exception {
        Field field = ActionBarManager.class.getDeclaredField("smartRemainingTicks");
        field.setAccessible(true);
        ((Map<UUID, Integer>) field.get(bars)).put(uuid, 0);
    }

    private static TraitProvider disabledTrait() {
        return new TraitProvider() {
            @Override
            public boolean isEnabled(Trait trait) {
                return false;
            }

            @Override
            public String getDisplayName(Trait trait, Locale locale, boolean formatted) {
                return "";
            }

            @Override
            public String getMenuDisplay(Trait trait, double value, Locale locale) {
                return "";
            }

            @Override
            public boolean optionBoolean(Trait type, String key) {
                return false;
            }

            @Override
            public boolean optionBoolean(Trait type, String key, boolean def) {
                return def;
            }

            @Override
            public int optionInt(Trait type, String key) {
                return 0;
            }

            @Override
            public int optionInt(Trait type, String key, int def) {
                return def;
            }

            @Override
            public double optionDouble(Trait type, String key) {
                return 0;
            }

            @Override
            public double optionDouble(Trait type, String key, double def) {
                return def;
            }

            @Override
            public String optionString(Trait type, String key) {
                return "";
            }

            @Override
            public String optionString(Trait type, String key, String def) {
                return def;
            }

            @Override
            public java.util.List<String> optionStringList(Trait type, String key) {
                return java.util.List.of();
            }

            @Override
            public java.util.Map<String, Object> optionMap(Trait type, String key) {
                return java.util.Map.of();
            }
        };
    }

    private static final class Harness {
        final GraniteSkillsPlugin plugin;
        final IdleScheduler scheduler;
        final OnlineUsers users;
        final boolean[] idleAllowed;
        final TestBars bars;

        private Harness(GraniteSkillsPlugin plugin, IdleScheduler scheduler, OnlineUsers users, boolean[] idleAllowed, TestBars bars) {
            this.plugin = plugin;
            this.scheduler = scheduler;
            this.users = users;
            this.idleAllowed = idleAllowed;
            this.bars = bars;
        }

        static Harness open() {
            Scheduler[] scheduler = new Scheduler[1];
            TraitManager[] traits = new TraitManager[1];
            OnlineUsers[] users = new OnlineUsers[1];
            boolean[] idleAllowed = {true};
            UiProvider[] ui = new UiProvider[1];
            GraniteSkillsPlugin plugin = (GraniteSkillsPlugin) java.lang.reflect.Proxy.newProxyInstance(
                    GraniteSkillsPlugin.class.getClassLoader(),
                    new Class<?>[]{GraniteSkillsPlugin.class},
                    (proxy, method, args) -> {
                        if ("getScheduler".equals(method.getName())) {
                            return scheduler[0];
                        }
                        if ("getTraitManager".equals(method.getName())) {
                            return traits[0];
                        }
                        if ("getUserManager".equals(method.getName())) {
                            return users[0];
                        }
                        if ("getWorldManager".equals(method.getName())) {
                            return (dev.granite.graniteskills.common.region.WorldManager) new dev.granite.graniteskills.common.region.WorldManager() {
                                @Override
                                public boolean isBlockedWorld(String worldName) {
                                    return false;
                                }

                                @Override
                                public boolean isDisabledWorld(String worldName) {
                                    return false;
                                }
                            };
                        }
                        if ("getHookManager".equals(method.getName())) {
                            return new dev.granite.graniteskills.common.hooks.HookManager();
                        }
                        if ("getMsg".equals(method.getName())) {
                            return "{hp}/{max_hp} {mana}/{max_mana}";
                        }
                        if ("getUiProvider".equals(method.getName())) {
                            return ui[0];
                        }
                        if ("configBoolean".equals(method.getName())) {
                            if (args[0] == Option.ACTION_BAR_IDLE) {
                                return idleAllowed[0];
                            }
                            return args[0] == Option.ACTION_BAR_ENABLED
                                    || args[0] == Option.ACTION_BAR_XP
                                    || args[0] == Option.ACTION_BAR_ABILITY;
                        }
                        if ("configInt".equals(method.getName())) {
                            return args[0] == Option.ACTION_BAR_UPDATE_PERIOD ? 1 : 0;
                        }
                        if ("logger".equals(method.getName())) {
                            return quietLogger();
                        }
                        if (method.isDefault()) {
                            return java.lang.reflect.InvocationHandler.invokeDefault(proxy, method, args);
                        }
                        if (method.getDeclaringClass() == Object.class) {
                            return switch (method.getName()) {
                                case "toString" -> "smart-window";
                                case "hashCode" -> System.identityHashCode(proxy);
                                case "equals" -> proxy == args[0];
                                default -> null;
                            };
                        }
                        return null;
                    });
            scheduler[0] = new IdleScheduler(plugin);
            users[0] = new OnlineUsers(plugin);
            traits[0] = new TraitManager(plugin) {
                @Override
                public double getBaseLevel(User user, Trait trait) {
                    return 20;
                }

                @Override
                public void registerTraitHandler(dev.granite.graniteskills.api.trait.TraitHandler traitHandler) {
                }

                @Override
                public String getMenuDisplay(Trait trait, double value, Locale locale, java.text.NumberFormat format) {
                    return "";
                }
            };
            TestBars bars = new TestBars(plugin, new java.util.ArrayList<>());
            ui[0] = recordingUi(bars.lines);
            return new Harness(plugin, (IdleScheduler) scheduler[0], users[0], idleAllowed, bars);
        }

        void close() {
            scheduler.shutdown();
        }
    }

    private static UiProvider recordingUi(java.util.List<String> lines) {
        return new UiProvider() {
            @Override
            public ActionBarManager getActionBarManager() {
                return null;
            }

            @Override
            public java.text.NumberFormat getFormat(FormatType type) {
                return java.text.NumberFormat.getIntegerInstance();
            }

            @Override
            public void sendActionBar(User user, String message) {
                lines.add(message);
            }

            @Override
            public void sendXpBossBar(User user, Skill skill, double currentXp, double levelXp, double xpGained, int level, boolean maxed, double income) {
            }

            @Override
            public void sendTitle(User user, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            }
        };
    }

    private static PlatformLogger quietLogger() {
        return new PlatformLogger() {
            @Override
            public void info(String message) {
            }

            @Override
            public void warn(String message) {
            }

            @Override
            public void warn(String message, Throwable throwable) {
            }

            @Override
            public void severe(String message) {
            }

            @Override
            public void severe(String message, Throwable throwable) {
            }

            @Override
            public void debug(String message) {
            }
        };
    }

    private static final class TestBars extends ActionBarManager {
        String hp = "20";
        String world = "world";
        final java.util.List<String> lines;

        TestBars(GraniteSkillsPlugin plugin, java.util.List<String> lines) {
            super(plugin, new UiProvider() {
                @Override
                public ActionBarManager getActionBarManager() {
                    return null;
                }

                @Override
                public java.text.NumberFormat getFormat(FormatType type) {
                    return java.text.NumberFormat.getIntegerInstance();
                }

                @Override
                public void sendActionBar(User user, String message) {
                    lines.add(message);
                }

                @Override
                public void sendXpBossBar(User user, Skill skill, double currentXp, double levelXp, double xpGained, int level, boolean maxed, double income) {
                }

                @Override
                public void sendTitle(User user, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
                }
            });
            this.lines = lines;
        }

        @Override
        public String getHp(User user) {
            return hp;
        }

        @Override
        public String getMaxHp(User user) {
            return "20";
        }

        @Override
        public String getWorldName(User user) {
            return world;
        }
    }

    private static final class IdleScheduler extends Scheduler {
        private static final Task IDLE = new Task() {
            @Override
            public TaskStatus getStatus() {
                return TaskStatus.STOPPED;
            }

            @Override
            public void cancel() {
            }
        };

        final java.util.List<TaskRunnable> syncTasks = new java.util.ArrayList<>();

        IdleScheduler(GraniteSkillsPlugin plugin) {
            super(plugin);
        }

        @Override
        public Task executeSync(Runnable runnable) {
            return IDLE;
        }

        @Override
        public Task scheduleSync(Runnable runnable, long delay, TimeUnit timeUnit) {
            return IDLE;
        }

        @Override
        public Task timerSync(TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
            syncTasks.add(runnable);
            return IDLE;
        }

        @Override
        public Task timerAsync(TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
            return IDLE;
        }
    }

    private static final class OnlineUsers extends dev.granite.graniteskills.common.user.UserManager {
        OnlineUsers(GraniteSkillsPlugin plugin) {
            super(plugin);
        }

        @Override
        public User instantiateUser(UUID uuid, dev.granite.graniteskills.common.ref.PlayerRef ref) {
            return new TestUser(uuid, plugin());
        }

        @Override
        public java.util.List<User> getOnlineUsers() {
            return java.util.List.copyOf(playerDataMap.values());
        }

        private GraniteSkillsPlugin plugin() {
            try {
                Field field = dev.granite.graniteskills.common.user.UserManager.class.getDeclaredField("plugin");
                field.setAccessible(true);
                return (GraniteSkillsPlugin) field.get(this);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static final class TestUser extends User {
        TestUser(UUID uuid, GraniteSkillsPlugin plugin) {
            super(uuid, plugin);
        }

        @Override
        public String getUsername() {
            return "player";
        }

        @Override
        public void sendMessage(String message) {
        }

        @Override
        public void sendMessage(Component component) {
        }

        @Override
        public double getPermissionMultiplier(Skill skill) {
            return 1;
        }

        @Override
        public boolean hasSkillPermission(Skill skill) {
            return true;
        }

        @Override
        public void setCommandLocale(Locale locale) {
        }

        @Override
        public int getPermissionJobLimit() {
            return 0;
        }

        @Override
        public String getWorld() {
            return "world";
        }

        @Override
        public boolean hasPermission(String permission) {
            return false;
        }

        @Override
        public boolean canSelectJob(Skill skill) {
            return false;
        }
    }
}
