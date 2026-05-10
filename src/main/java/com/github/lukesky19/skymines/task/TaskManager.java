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
package com.github.lukesky19.skymines.task;

import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.mine.MineDataManager;
import com.github.lukesky19.skymines.mine.interfaces.Mine;
import com.github.lukesky19.skymines.player.CooldownManager;
import com.github.lukesky19.skymines.player.MineTimeManager;
import com.github.lukesky19.skymines.player.PlayerDataManager;
import com.github.lukesky19.skymines.player.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Manages {@link BukkitTask}s for the plugin.
 */
public class TaskManager {
    private final @NonNull SkyMines skyMines;
    private final @NonNull MineDataManager mineDataManager;
    private final @NonNull PlayerDataManager playerDataManager;
    private final @NonNull MineTimeManager mineTimeManager;
    private final @NonNull CooldownManager cooldownManager;
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
            @NonNull SkyMines skyMines,
            @NonNull MineDataManager mineDataManager,
            @NonNull PlayerDataManager playerDataManager,
            @NonNull MineTimeManager mineTimeManager,
            @NonNull CooldownManager cooldownManager) {
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
            Map<String, Mine> minesMap = new HashMap<>(mineDataManager.getMinesMap());
            Map<UUID, PlayerData> playerDataMap = new HashMap<>(playerDataManager.getPlayerDataMap());

            for(Map.Entry<String, Mine> mineEntry : minesMap.entrySet()) {
                String mineId = mineEntry.getKey();
                Mine mine = mineEntry.getValue();

                for(Map.Entry<UUID, PlayerData> playerDataEntry : playerDataMap.entrySet()) {
                    UUID uuid = playerDataEntry.getKey();

                    Player player = skyMines.getServer().getPlayer(uuid);
                    if(player == null || !player.isOnline() || !player.isConnected()) continue;

                    if(mine.isLocationInMine(player.getLocation())) {
                        long mineTime = mineTimeManager.getMineTime(uuid, mineId);
                        if(mineTime > 0) {
                            mineTimeManager.decrementMineTime(uuid, mineId, 1);
                        }
                    }
                }
            }

            for(Map.Entry<UUID, PlayerData> playerDataEntry : playerDataMap.entrySet()) {
                UUID uuid = playerDataEntry.getKey();
                PlayerData playerData = playerDataEntry.getValue();

                Player player = skyMines.getServer().getPlayer(uuid);
                if(player == null || !player.isOnline() || !player.isConnected()) continue;

                // Decrement block cooldowns
                List<Location> locationsOnCooldown = new ArrayList<>(playerData.getBlockDataOnCooldown().keySet().stream().toList());
                locationsOnCooldown.forEach(location -> {
                    cooldownManager.decrementLocationCooldown(uuid, location);
                });
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