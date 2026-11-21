package dev.granite.graniteskills.common.perf;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.skill.SkillProvider;
import dev.granite.graniteskills.api.skill.Skills;
import dev.granite.graniteskills.api.trait.TraitProvider;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.message.PlatformLogger;
import dev.granite.graniteskills.common.ref.PlayerRef;
import dev.granite.graniteskills.common.scheduler.Scheduler;
import dev.granite.graniteskills.common.scheduler.Task;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.scheduler.TaskStatus;
import dev.granite.graniteskills.common.storage.StorageProvider;
import dev.granite.graniteskills.common.ui.ActionBarManager;
import dev.granite.graniteskills.common.ui.UiProvider;
import dev.granite.graniteskills.common.user.AntiAfkLog;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.user.UserManager;
import dev.granite.graniteskills.common.user.UserState;
import dev.granite.graniteskills.common.util.data.KeyIntPair;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceWorkloadTest {

    private static Field skillProvider;
    private static Field traitProvider;
    private static Object previousSkillProvider;
    private static Object previousTraitProvider;

    @BeforeAll
    static void enableFarmingForTheWorkload() throws Exception {
        skillProvider = Skills.class.getDeclaredField("provider");
        skillProvider.setAccessible(true);
        previousSkillProvider = skillProvider.get(Skills.FARMING);
        skillProvider.set(Skills.FARMING, farmingEnabled());

        traitProvider = Traits.class.getDeclaredField("provider");
        traitProvider.setAccessible(true);
        previousTraitProvider = traitProvider.get(Traits.MAX_MANA);
        traitProvider.set(Traits.MAX_MANA, disabledTrait());
    }

    @AfterAll
    static void restoreProviders() throws Exception {
        if (skillProvider != null) {
            skillProvider.set(Skills.FARMING, previousSkillProvider);
        }
        if (traitProvider != null) {
            traitProvider.set(Traits.MAX_MANA, previousTraitProvider);
        }
    }

    @Test
    void fourPlayersFinishWithinTheBar() throws Exception {
        Harness harness = Harness.open();
        ExecutorService pool = Executors.newFixedThreadPool(PerformanceWorkload.PLAYERS * 3);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        CountDownLatch done = new CountDownLatch(PerformanceWorkload.PLAYERS * 3);
        long started = System.nanoTime();
        try {
            for (int i = 0; i < PerformanceWorkload.PLAYERS; i++) {
                User user = harness.user(UUID.randomUUID());
                List<KeyIntPair> unclaimed = user.getUnclaimedItems();
                for (int item = 0; item < PerformanceWorkload.CLAIM_STOCK; item++) {
                    unclaimed.add(new KeyIntPair("stone", 1));
                }
                AtomicBoolean halfway = new AtomicBoolean();
                AtomicBoolean savedHalfway = new AtomicBoolean();
                pool.execute(() -> runGame(harness, user, halfway, savedHalfway, errors, done));
                pool.execute(() -> runSaves(harness, user, halfway, savedHalfway, errors, done));
                pool.execute(() -> runUi(harness, user, errors, done));
            }
            assertTrue(done.await(PerformanceWorkload.DEADLINE_MS, TimeUnit.MILLISECONDS), "workload exceeded the deadline");
            long measuredMs = (System.nanoTime() - started) / 1_000_000L;
            System.out.println("measuredMs=" + measuredMs);
            assertTrue(errors.isEmpty(), "workload errors: " + errors);
            assertTrue(measuredMs < PerformanceWorkload.DEADLINE_MS);

            for (User user : harness.users.getOnlineUsers()) {
                assertEquals(PerformanceWorkload.EXPECTED_XP, user.getSkillXp(Skills.FARMING));
                assertTrue(user.getUnclaimedItems().isEmpty());
                harness.storage.saveSafely(user);
                List<Double> snapshots = harness.storage.snapshots.get(user.getUuid());
                assertTrue(snapshots.size() >= PerformanceWorkload.SAVES_DURING_XP + 1);
                assertEquals(PerformanceWorkload.EXPECTED_XP, snapshots.get(snapshots.size() - 1));
                boolean overlapped = false;
                for (int index = 0; index < snapshots.size() - 1; index++) {
                    double saved = snapshots.get(index);
                    assertTrue(saved <= PerformanceWorkload.EXPECTED_XP);
                    if (saved >= PerformanceWorkload.EXPECTED_XP / 2.0 && saved < PerformanceWorkload.EXPECTED_XP) {
                        overlapped = true;
                    }
                }
                assertTrue(overlapped);
            }
            Map<?, ?> windows = harness.windows();
            assertEquals(PerformanceWorkload.PLAYERS, windows.size());
            for (Object ticks : windows.values()) {
                assertEquals(PerformanceWorkload.UI_TICKS, ticks);
            }
            assertTrue(harness.storage.lockCount() <= PerformanceWorkload.PLAYERS);
        } finally {
            pool.shutdownNow();
            harness.close();
        }
    }

    private static void runGame(Harness harness, User user, AtomicBoolean halfway, AtomicBoolean savedHalfway,
            ConcurrentLinkedQueue<Throwable> errors, CountDownLatch done) {
        try {
            for (int award = 1; award <= PerformanceWorkload.XP_AWARDS; award++) {
                user.addSkillXp(Skills.FARMING, PerformanceWorkload.XP_EACH);
                if (award == PerformanceWorkload.XP_AWARDS / 2) {
                    halfway.set(true);
                    while (!savedHalfway.get()) {
                        Thread.onSpinWait();
                    }
                }
            }
            int delivered = 0;
            while (!user.getUnclaimedItems().isEmpty()) {
                delivered += dev.granite.graniteskills.common.item.UnclaimedClaim.recordDelivery(
                        user.getUnclaimedItems(), 0, 0);
            }
            if (delivered != PerformanceWorkload.CLAIM_STOCK) {
                throw new IllegalStateException("delivered " + delivered);
            }
        } catch (Throwable thrown) {
            errors.add(thrown);
            savedHalfway.set(true);
        } finally {
            done.countDown();
        }
    }

    private static void runSaves(Harness harness, User user, AtomicBoolean halfway, AtomicBoolean savedHalfway,
            ConcurrentLinkedQueue<Throwable> errors, CountDownLatch done) {
        try {
            int saves = 0;
            while (saves < PerformanceWorkload.SAVES_DURING_XP || !savedHalfway.get()) {
                harness.storage.saveSafely(user);
                saves++;
                double xp = user.getSkillXp(Skills.FARMING);
                if (halfway.get() && xp >= PerformanceWorkload.EXPECTED_XP / 2.0 && xp < PerformanceWorkload.EXPECTED_XP) {
                    savedHalfway.set(true);
                }
                if (saves > PerformanceWorkload.SAVES_DURING_XP * 100) {
                    throw new IllegalStateException("saves did not observe the halfway XP");
                }
            }
        } catch (Throwable thrown) {
            errors.add(thrown);
            savedHalfway.set(true);
        } finally {
            done.countDown();
        }
    }

    private static void runUi(Harness harness, User user, ConcurrentLinkedQueue<Throwable> errors, CountDownLatch done) {
        try {
            for (int open = 0; open < PerformanceWorkload.UI_OPENS; open++) {
                int ticks = 1 + (open % PerformanceWorkload.UI_TICKS);
                harness.bars.openSmartWindow(user, ticks);
            }
            harness.bars.openSmartWindow(user, PerformanceWorkload.UI_TICKS);
        } catch (Throwable thrown) {
            errors.add(thrown);
        } finally {
            done.countDown();
        }
    }

    private static TraitProvider disabledTrait() {
        return new TraitProvider() {
            @Override
            public boolean isEnabled(dev.granite.graniteskills.api.trait.Trait trait) {
                return false;
            }

            @Override
            public String getDisplayName(dev.granite.graniteskills.api.trait.Trait trait, Locale locale, boolean formatted) {
                return "";
            }

            @Override
            public String getMenuDisplay(dev.granite.graniteskills.api.trait.Trait trait, double value, Locale locale) {
                return "";
            }

            @Override
            public boolean optionBoolean(dev.granite.graniteskills.api.trait.Trait type, String key) {
                return false;
            }

            @Override
            public boolean optionBoolean(dev.granite.graniteskills.api.trait.Trait type, String key, boolean def) {
                return def;
            }

            @Override
            public int optionInt(dev.granite.graniteskills.api.trait.Trait type, String key) {
                return 0;
            }

            @Override
            public int optionInt(dev.granite.graniteskills.api.trait.Trait type, String key, int def) {
                return def;
            }

            @Override
            public double optionDouble(dev.granite.graniteskills.api.trait.Trait type, String key) {
                return 0;
            }

            @Override
            public double optionDouble(dev.granite.graniteskills.api.trait.Trait type, String key, double def) {
                return def;
            }

            @Override
            public String optionString(dev.granite.graniteskills.api.trait.Trait type, String key) {
                return "";
            }

            @Override
            public String optionString(dev.granite.graniteskills.api.trait.Trait type, String key, String def) {
                return def;
            }

            @Override
            public java.util.List<String> optionStringList(dev.granite.graniteskills.api.trait.Trait type, String key) {
                return java.util.List.of();
            }

            @Override
            public java.util.Map<String, Object> optionMap(dev.granite.graniteskills.api.trait.Trait type, String key) {
                return java.util.Map.of();
            }
        };
    }

    private static SkillProvider farmingEnabled() {
        return new SkillProvider() {
            @Override
            public boolean isEnabled(Skill skill) {
                return true;
            }

            @Override
            public java.util.List<dev.granite.graniteskills.api.ability.Ability> getAbilities(Skill skill) {
                return java.util.List.of();
            }

            @Override
            public dev.granite.graniteskills.api.mana.ManaAbility getManaAbility(Skill skill) {
                return null;
            }

            @Override
            public java.util.List<dev.granite.graniteskills.api.source.XpSource> getSources(Skill skill) {
                return java.util.List.of();
            }

            @Override
            public int getMaxLevel(Skill skill) {
                return 97;
            }

            @Override
            public String getDisplayName(Skill skill, Locale locale, boolean formatted) {
                return "Farming";
            }

            @Override
            public String getDescription(Skill skill, Locale locale, boolean formatted) {
                return "";
            }

            @Override
            public boolean optionBoolean(Skill type, String key) {
                return false;
            }

            @Override
            public boolean optionBoolean(Skill type, String key, boolean def) {
                return def;
            }

            @Override
            public int optionInt(Skill type, String key) {
                return 0;
            }

            @Override
            public int optionInt(Skill type, String key, int def) {
                return def;
            }

            @Override
            public double optionDouble(Skill type, String key) {
                return 0;
            }

            @Override
            public double optionDouble(Skill type, String key, double def) {
                return def;
            }

            @Override
            public String optionString(Skill type, String key) {
                return "";
            }

            @Override
            public String optionString(Skill type, String key, String def) {
                return def;
            }

            @Override
            public java.util.List<String> optionStringList(Skill type, String key) {
                return java.util.List.of();
            }

            @Override
            public java.util.Map<String, Object> optionMap(Skill type, String key) {
                return java.util.Map.of();
            }
        };
    }

    private static final class Harness {
        final GraniteSkillsPlugin plugin;
        final Scheduler scheduler;
        final TestUsers users;
        final MemoryStorage storage;
        final ActionBarManager bars;

        private Harness(GraniteSkillsPlugin plugin, Scheduler scheduler, TestUsers users, MemoryStorage storage, ActionBarManager bars) {
            this.plugin = plugin;
            this.scheduler = scheduler;
            this.users = users;
            this.storage = storage;
            this.bars = bars;
        }

        static Harness open() {
            Scheduler[] scheduler = new Scheduler[1];
            TestUsers[] users = new TestUsers[1];
            GraniteSkillsPlugin plugin = (GraniteSkillsPlugin) java.lang.reflect.Proxy.newProxyInstance(
                    GraniteSkillsPlugin.class.getClassLoader(),
                    new Class<?>[]{GraniteSkillsPlugin.class},
                    (proxy, method, args) -> {
                        if ("getScheduler".equals(method.getName())) {
                            return scheduler[0];
                        }
                        if ("getUserManager".equals(method.getName())) {
                            return users[0];
                        }
                        if ("configBoolean".equals(method.getName())) {
                            return false;
                        }
                        if ("configInt".equals(method.getName())) {
                            return 1;
                        }
                        if ("logger".equals(method.getName())) {
                            return (PlatformLogger) new PlatformLogger() {
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
                        if (method.isDefault()) {
                            return java.lang.reflect.InvocationHandler.invokeDefault(proxy, method, args);
                        }
                        if (method.getDeclaringClass() == Object.class) {
                            return switch (method.getName()) {
                                case "toString" -> "perf-fixture";
                                case "hashCode" -> System.identityHashCode(proxy);
                                case "equals" -> proxy == args[0];
                                default -> null;
                            };
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
            scheduler[0] = new HarnessScheduler(plugin);
            users[0] = new TestUsers(plugin);
            MemoryStorage storage = new MemoryStorage(plugin);
            ActionBarManager bars = new ActionBarManager(plugin, unusedUi()) {
                @Override
                public String getHp(User user) {
                    return "20";
                }

                @Override
                public String getMaxHp(User user) {
                    return "20";
                }

                @Override
                public String getWorldName(User user) {
                    return "world";
                }
            };
            return new Harness(plugin, scheduler[0], users[0], storage, bars);
        }

        User user(UUID uuid) {
            User user = new TestUser(uuid, plugin);
            users.addUser(user);
            return user;
        }

        Map<?, ?> windows() throws Exception {
            Field field = ActionBarManager.class.getDeclaredField("smartRemainingTicks");
            field.setAccessible(true);
            return (Map<?, ?>) field.get(bars);
        }

        void close() {
            scheduler.shutdown();
        }
    }

    private static UiProvider unusedUi() {
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
            }

            @Override
            public void sendXpBossBar(User user, Skill skill, double currentXp, double levelXp, double xpGained, int level, boolean maxed, double income) {
            }

            @Override
            public void sendTitle(User user, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            }
        };
    }

    private static final class HarnessScheduler extends Scheduler {
        private static final Task IDLE = new Task() {
            @Override
            public TaskStatus getStatus() {
                return TaskStatus.STOPPED;
            }

            @Override
            public void cancel() {
            }
        };

        HarnessScheduler(GraniteSkillsPlugin plugin) {
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
            return IDLE;
        }

        @Override
        public Task timerAsync(TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
            return IDLE;
        }
    }

    private static final class TestUsers extends UserManager {
        TestUsers(GraniteSkillsPlugin plugin) {
            super(plugin);
        }

        @Override
        public User instantiateUser(UUID uuid, PlayerRef ref) {
            return new TestUser(uuid, plugin());
        }

        @Override
        public List<User> getOnlineUsers() {
            return List.copyOf(playerDataMap.values());
        }

        private GraniteSkillsPlugin plugin() {
            try {
                Field field = UserManager.class.getDeclaredField("plugin");
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

    private static final class MemoryStorage extends StorageProvider {
        final Map<UUID, List<Double>> snapshots = new ConcurrentHashMap<>();

        MemoryStorage(GraniteSkillsPlugin plugin) {
            super(plugin);
        }

        @Override
        protected User loadRaw(UUID uuid, PlayerRef platformPlayer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UserState loadState(UUID uuid) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void applyState(UserState state) {
        }

        @Override
        public void save(User user) {
            snapshots.computeIfAbsent(user.getUuid(), id -> new java.util.concurrent.CopyOnWriteArrayList<>())
                    .add(user.getSkillXp(Skills.FARMING));
        }

        @Override
        public void delete(UUID uuid) {
        }

        @Override
        public List<UserState> loadStates(boolean ignoreOnline, boolean skipKeyValues, long previousFetchTime) {
            return List.of();
        }

        @Override
        public List<AntiAfkLog> loadAntiAfkLogs(UUID uuid) {
            return List.of();
        }

        int lockCount() throws Exception {
            Field field = StorageProvider.class.getDeclaredField("userLocks");
            field.setAccessible(true);
            return ((Map<?, ?>) field.get(this)).size();
        }
    }
}
