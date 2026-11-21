package dev.granite.graniteskills.common.storage.file;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.trait.TraitProvider;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.ref.PlayerRef;
import dev.granite.graniteskills.common.skill.SkillManager;
import dev.granite.graniteskills.common.ui.ActionBarIdleMode;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.user.UserManager;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlIdleModeFileTest {

    @TempDir
    Path temp;

    private static Field traitProvider;
    private static Object previousTraitProvider;

    @BeforeAll
    static void disableManaTraitLookup() throws Exception {
        traitProvider = Traits.class.getDeclaredField("provider");
        traitProvider.setAccessible(true);
        previousTraitProvider = traitProvider.get(Traits.MAX_MANA);
        traitProvider.set(Traits.MAX_MANA, (TraitProvider) java.lang.reflect.Proxy.newProxyInstance(
                TraitProvider.class.getClassLoader(),
                new Class<?>[]{TraitProvider.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isEnabled", "optionBoolean" -> false;
                    case "optionInt" -> 0;
                    case "optionDouble" -> 0.0;
                    case "optionString", "getDisplayName", "getMenuDisplay" -> "";
                    case "optionStringList" -> List.of();
                    case "optionMap" -> java.util.Map.of();
                    default -> null;
                }));
    }

    @AfterAll
    static void restoreManaTraitLookup() throws Exception {
        if (traitProvider != null) {
            traitProvider.set(Traits.MAX_MANA, previousTraitProvider);
        }
    }

    @Test
    void idleModeRoundTripsThroughTheUserFile() throws Exception {
        UserManager[] users = new UserManager[1];
        SkillManager[] skills = new SkillManager[1];
        GraniteSkillsPlugin plugin = (GraniteSkillsPlugin) java.lang.reflect.Proxy.newProxyInstance(
                GraniteSkillsPlugin.class.getClassLoader(),
                new Class<?>[]{GraniteSkillsPlugin.class},
                (proxy, method, args) -> {
                    if ("getUserManager".equals(method.getName())) {
                        return users[0];
                    }
                    if ("getSkillManager".equals(method.getName())) {
                        return skills[0];
                    }
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> "yaml-idle";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> null;
                        };
                    }
                    return null;
                });
        users[0] = new OnlineUsers(plugin);
        skills[0] = new SkillManager(plugin);
        FileStorageProvider storage = new FileStorageProvider(plugin, temp.toString());
        UUID uuid = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

        assertRoundTrip(storage, plugin, uuid, ActionBarIdleMode.OFF, "off");
        assertRoundTrip(storage, plugin, uuid, ActionBarIdleMode.ALWAYS, "always");
        assertRoundTrip(storage, plugin, uuid, ActionBarIdleMode.SMART, "smart");
    }

    private void assertRoundTrip(FileStorageProvider storage, GraniteSkillsPlugin plugin, UUID uuid, ActionBarIdleMode mode, String fileValue) throws Exception {
        User user = new TestUser(uuid, plugin);
        user.setActionBarIdleMode(mode);
        storage.save(user);

        String yaml = Files.readString(temp.resolve(uuid + ".yml"));
        System.out.println(yaml);
        boolean recorded = yaml.lines().anyMatch(line -> {
            String trimmed = line.trim();
            return trimmed.equals("idle_mode: " + fileValue)
                    || trimmed.equals("idle_mode: '" + fileValue + "'")
                    || trimmed.equals("idle_mode: \"" + fileValue + "\"");
        });
        assertTrue(recorded);

        User loaded = storage.loadRaw(uuid, null);
        assertEquals(mode, loaded.getActionBarIdleMode());
    }

    private static final class OnlineUsers extends UserManager {
        OnlineUsers(GraniteSkillsPlugin plugin) {
            super(plugin);
        }

        @Override
        public User instantiateUser(UUID uuid, PlayerRef ref) {
            return new TestUser(uuid, plugin());
        }

        @Override
        public List<User> getOnlineUsers() {
            return List.of();
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
}
