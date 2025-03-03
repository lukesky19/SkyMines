package com.github.lukesky19.skymines.listeners;

import com.github.lukesky19.skymines.manager.MineManager;
import com.github.lukesky19.skymines.mine.Mine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * This class listens to when a player connects to the server and passes the event to a mine if they are inside one.
 */
public class PlayerJoinListener implements Listener {
    private final MineManager mineManager;

    /**
     * Constructor
     * @param mineManager A MineManager instance.
     */
    public PlayerJoinListener(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    /**
     * Listens to when a player logs in and passes the event to the mine they are in (if any).
     * @param playerJoinEvent A PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Mine mine = mineManager.getMineByLocation(playerJoinEvent.getPlayer().getLocation());
        if(mine != null) {
            mine.handlePlayerJoinEvent(playerJoinEvent);
        }
    }
}
