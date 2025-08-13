/*
    SkyMines offers different types mines to get resources from.
    Copyright (C) 2023 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skymines.manager.task;

import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.player.PlayerData;
import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.manager.mine.packet.CooldownManager;
import com.github.lukesky19.skymines.manager.mine.packet.MineTimeManager;
import com.github.lukesky19.skymines.manager.player.PlayerDataManager;
import com.github.lukesky19.skymines.mine.AbstractMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manages {@link BukkitTask}s for the plugin.
 */
public class TaskManager {
    private final @NotNull SkyMines skyMines;
    private final @NotNull MineDataManager mineDataManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull MineTimeManager mineTimeManager;
    private final @NotNull CooldownManager cooldownManager;
    /**
     * This task manages mine time and the reversion of blocks after the cooldown ends.
     */
    private @Nullable BukkitTask mineTask;
    /**
     * This task manages the periodic saving of player data.
     */
    private @Nullable BukkitTask saveTask;

    /**
     * Default Constructor.
     * You should use {@link #TaskManager(SkyMines, MineDataManager, PlayerDataManager, MineTimeManager, CooldownManager)} instead.
     * @deprecated You should use {@link #TaskManager(SkyMines, MineDataManager, PlayerDataManager, MineTimeManager, CooldownManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public TaskManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param mineDataManager A {@link MineDataManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param mineTimeManager A {@link MineTimeManager} instance.
     * @param cooldownManager A {@link CooldownManager} instance.
     */
    public TaskManager(
            @NotNull SkyMines skyMines,
            @NotNull MineDataManager mineDataManager,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull MineTimeManager mineTimeManager,
            @NotNull CooldownManager cooldownManager) {
        this.skyMines = skyMines;
        this.mineDataManager = mineDataManager;
        this.playerDataManager = playerDataManager;
        this.mineTimeManager = mineTimeManager;
        this.cooldownManager = cooldownManager;
    }

    /**
     * This task manages the decrement of mine time and block cooldown times.
     */
    public void startMineTask() {
        mineTask = skyMines.getServer().getScheduler().runTaskTimer(skyMines, () -> {
            Map<String, AbstractMine> minesMap = new HashMap<>(mineDataManager.getMinesMap());

            for(Map.Entry<String, AbstractMine> mineEntry : minesMap.entrySet()) {
                String mineId = mineEntry.getKey();
                AbstractMine mine = mineEntry.getValue();
                Map<UUID, PlayerData> playerDataMap = new HashMap<>(playerDataManager.getPlayerDataMap());

                for(Map.Entry<UUID, PlayerData> playerDataEntry : playerDataMap.entrySet()) {
                    UUID uuid = playerDataEntry.getKey();
                    PlayerData playerData = playerDataEntry.getValue();

                    Player player = skyMines.getServer().getPlayer(uuid);
                    if(player == null || !player.isOnline() || !player.isConnected()) continue;

                    if(mine.isLocationInMine(player.getLocation())) {
                        long mineTime = mineTimeManager.getMineTime(uuid, mineId);
                        if(mineTime > 0) {
                            mineTimeManager.decrementMineTime(uuid, mineId, 1);
                        }
                    }

                    // Decrement block cooldowns
                    List<Location> locationsOnCooldown = new ArrayList<>(playerData.getBlockDataOnCooldown().keySet().stream().toList());
                    locationsOnCooldown.forEach(location -> {
                        cooldownManager.decrementLocationCooldown(uuid, location);
                    });
                }
            }
        }, 20L, 20L);
    }

    /**
     * Stop the mine task.
     */
    public void stopMineTask() {
        if(mineTask != null && !mineTask.isCancelled()) {
            mineTask.cancel();
            mineTask = null;
        }
    }

    /**
     * This task saves player data every 15 minutes to the database.
     */
    public void startSaveTask() {
        saveTask = skyMines.getServer().getScheduler().runTaskTimer(skyMines, () -> {
            playerDataManager.savePlayerData();
        }, 20 * 60 * 15, 20 * 60 * 15); // 20 ticks * 60 seconds * 15 minutes = 18000 ticks
    }

    /**
     * Stop the save task.
     */
    public void stopSaveTask() {
        if(saveTask != null && !saveTask.isCancelled()) {
            saveTask.cancel();
            saveTask = null;
        }
    }
}
