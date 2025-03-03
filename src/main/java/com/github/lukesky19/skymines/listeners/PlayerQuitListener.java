package com.github.lukesky19.skymines.listeners;

import com.github.lukesky19.skymines.manager.MineManager;
import com.github.lukesky19.skymines.mine.Mine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * This class listens to when a player disconnects from the server and passes the event to a mine if they logged out inside one.
 */
public class PlayerQuitListener implements Listener {
    private final MineManager mineManager;

    /**
     * Constructor
     * @param mineManager A MineManager instance.
     */
    public PlayerQuitListener(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    /**
     * Listens to when a player logs out and passes the event to the mine they were in (if any).
     * @param playerQuitEvent A PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        Mine mine = mineManager.getMineByLocation(playerQuitEvent.getPlayer().getLocation());
        if(mine != null) {
            mine.handlePlayerQuitEvent(playerQuitEvent);
        }
    }
}
