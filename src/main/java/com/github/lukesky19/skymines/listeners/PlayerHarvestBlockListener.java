package com.github.lukesky19.skymines.listeners;

import com.github.lukesky19.skymines.manager.MineManager;
import com.github.lukesky19.skymines.mine.Mine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

/**
 * This class listens to when a player harvests a block and if that location is inside a mine, the event is passed to that mine.
 */
public class PlayerHarvestBlockListener implements Listener {
    private final MineManager mineManager;

    /**
     * Constructor
     * @param mineManager A MineManager instance.
     */
    public PlayerHarvestBlockListener(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    /**
     * Listens to when a player harvests a block and passes the event to the mine the block was harvested in (if any).
     * @param playerHarvestBlockEvent A PlayerHarvestBlockEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerHarvestBlock(PlayerHarvestBlockEvent playerHarvestBlockEvent) {
        Mine mine = mineManager.getMineByLocation(playerHarvestBlockEvent.getHarvestedBlock().getLocation());
        if(mine != null) {
            mine.handlePlayerHarvestBlockEvent(playerHarvestBlockEvent);
        }
    }
}
