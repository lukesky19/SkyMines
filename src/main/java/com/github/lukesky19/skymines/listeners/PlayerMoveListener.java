package com.github.lukesky19.skymines.listeners;

import com.github.lukesky19.skymines.manager.MineManager;
import com.github.lukesky19.skymines.mine.Mine;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * This class listens to when a player moves from one location to another and passes the event to a mine if they logged out inside one.
 */
public class PlayerMoveListener implements Listener {
    private final MineManager mineManager;

    /**
     * Constructor
     * @param mineManager A MineManager instance.
     */
    public PlayerMoveListener(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    /**
     * Listens to when a player moves from one block to another and passes the event to the mine they are in (if any).
     * @param playerMoveEvent A PlayerMoveEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        Location from = playerMoveEvent.getFrom();
        Location to = playerMoveEvent.getTo();

        Location blockFrom = new Location(from.getWorld(), from.getBlockX(), from.getBlockY(), from.getBlockZ());
        Location blockTo = new Location(to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ());

        Mine fromMine = mineManager.getMineByLocation(blockFrom);
        if(fromMine != null) {
            fromMine.handlePlayerMoveEvent(playerMoveEvent);
        }

        Mine toMine = mineManager.getMineByLocation(blockTo);
        if(toMine != null) {
            toMine.handlePlayerMoveEvent(playerMoveEvent);
        }
    }
}
