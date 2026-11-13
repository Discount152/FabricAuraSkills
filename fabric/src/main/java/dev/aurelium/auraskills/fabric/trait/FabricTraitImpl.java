package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public abstract class FabricTraitImpl implements FabricTraitHandler {

    protected final FabricAuraSkills plugin;
    private final Trait[] traits;

    protected FabricTraitImpl(FabricAuraSkills plugin, Trait... traits) {
        this.plugin = plugin;
        this.traits = traits;
    }

    @Override
    public Trait[] getTraits() {
        return traits;
    }

    @Override
    public void onReload(ServerPlayer player, User user, Trait trait) {
        reload(player, trait);
    }

    protected void reload(ServerPlayer player, Trait trait) {
    }

    @Override
    public void onPlayerChangedServerWorld(ServerPlayer player, ServerLevel from, ServerLevel to) {
        if (from == null || from == to) {
            return;
        }
        for (Trait trait : traits) {
            onWorldChanged(player, from, to, trait);
        }
    }

    protected void onWorldChanged(ServerPlayer player, ServerLevel from, ServerLevel to, Trait trait) {
    }
}
