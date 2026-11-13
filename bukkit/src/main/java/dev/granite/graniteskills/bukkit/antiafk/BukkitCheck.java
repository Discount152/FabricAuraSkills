package dev.granite.graniteskills.bukkit.antiafk;

import dev.granite.graniteskills.common.antiafk.AntiAfkManager;
import dev.granite.graniteskills.common.antiafk.Check;
import dev.granite.graniteskills.common.antiafk.CheckData;
import dev.granite.graniteskills.common.antiafk.CheckType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import static dev.granite.graniteskills.bukkit.ref.BukkitPlayerRef.wrap;

public class BukkitCheck extends Check implements Listener {

    public BukkitCheck(CheckType type, AntiAfkManager manager) {
        super(type, manager);
    }

    protected CheckData getCheckData(Player player) {
        return getCheckData(wrap(player));
    }

    protected void logFail(Player player) {
        logFail(wrap(player));
    }

}
