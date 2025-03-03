package com.github.lukesky19.skymines.listeners;

import com.github.lukesky19.skymines.manager.MineManager;
import com.github.lukesky19.skymines.mine.Mine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * This class listens to when a player teleports and passes the event to the mine the player was in and is now in (if any).
 */
public class PlayerTeleportListener implements Listener {
    private final MineManager mineManager;

    /**
     * Constructor
     * @param mineManager A MineManager instance.
     */
    public PlayerTeleportListener(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    /**
     * Listens to when a player teleports and passes the event to the mine the teleport events occurred in (if any)
     * @param playerTeleportEvent A PlayerTeleportEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent playerTeleportEvent) {
        Mine fromMine = mineManager.getMineByLocation(playerTeleportEvent.getFrom());
        if(fromMine != null) {
            fromMine.handlePlayerTeleportEvent(playerTeleportEvent);
        }

        Mine toMine = mineManager.getMineByLocation(playerTeleportEvent.getTo());
        if(toMine != null) {
            toMine.handlePlayerTeleportEvent(playerTeleportEvent);
        }
    }
}
