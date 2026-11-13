package dev.granite.graniteskills.common.hooks;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.user.User;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.spongepowered.configurate.ConfigurationNode;

public class LuckPermsHook extends PermissionsHook {

    protected final LuckPerms luckPerms;

    public LuckPermsHook(GraniteSkillsPlugin plugin, ConfigurationNode config) {
        super(plugin, config);
        this.luckPerms = LuckPermsProvider.get();
    }

    @Override
    public void setPermission(User playerData, String permission, boolean value) {
        luckPerms.getUserManager().modifyUser(playerData.getUuid(), user ->
                user.data().add(Node.builder(permission).value(value).build()));
    }

    @Override
    public void unsetPermission(User playerData, String permission, boolean value) {
        luckPerms.getUserManager().modifyUser(playerData.getUuid(), user ->
                user.data().remove(Node.builder(permission).value(value).build()));
    }

    @Override
    public Class<? extends Hook> getTypeClass() {
        return PermissionsHook.class;
    }

}
