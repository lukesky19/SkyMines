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
package com.github.lukesky19.skymines.manager.player;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.player.PlayerData;
import com.github.lukesky19.skymines.database.DatabaseManager;
import com.github.lukesky19.skymines.database.tables.TimesTable;
import com.github.lukesky19.skymines.database.tables.UnlockedBlocksTable;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages player data.
 */
public class PlayerDataManager {
    private final @NotNull SkyMines skyMines;
    private final @NotNull ComponentLogger logger;
    private final @NotNull DatabaseManager databaseManager;

    private final @NotNull Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    /**
     * Default Constructor.
     * You should use {@link #PlayerDataManager(SkyMines, DatabaseManager)} instead.
     * @deprecated You should use {@link #PlayerDataManager(SkyMines, DatabaseManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public PlayerDataManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public PlayerDataManager(@NotNull SkyMines skyMines, @NotNull DatabaseManager databaseManager) {
        this.skyMines = skyMines;
        this.logger = skyMines.getComponentLogger();
        this.databaseManager = databaseManager;
    }

    public @NotNull PlayerData getPlayerData(@NotNull UUID uuid) {
        @Nullable PlayerData playerData = playerDataMap.get(uuid);
        if(playerData == null) {
            logger.warn(AdventureUtil.serialize("No player data found for UUID " + uuid + ". Creating new player data."));

            playerData = new PlayerData();
            playerDataMap.put(uuid, playerData);
        }

        return playerData;
    }

    /**
     * Get the {@link Map} mapping {@link UUID} to {@link PlayerData}.
     * @return A {@link Map} mapping {@link UUID} to {@link PlayerData}
     */
    public @NotNull Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    /**
     * Creates {@link PlayerData} for the player.
     * @param uuid The {@link UUID} of the player.
     */
    public void createPlayerData(@NotNull UUID uuid) {
        PlayerData playerData = new PlayerData();
        playerDataMap.put(uuid, playerData);
    }

    /**
     * Save all player data for the player's uuid provided to the database.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} containing a {@link Boolean} where true means all data saved successfully and false for any errors.
     */
    public @NotNull CompletableFuture<Boolean> savePlayerData(@NotNull UUID uuid) {
        PlayerData playerData = playerDataMap.get(uuid);
        if(playerData == null) {
            skyMines.getComponentLogger().error(AdventureUtil.serialize("Failed to save player data for " + uuid + " as they have no player data stored."));
            return CompletableFuture.completedFuture(false);
        }

        TimesTable timesTable = databaseManager.getTimesTable();
        UnlockedBlocksTable unlockedBlocksTable = databaseManager.getUnlockedBlocksTable();

        CompletableFuture<List<Boolean>> timeFuture = timesTable.saveMineTimes(uuid, playerData.getMineTimesByMineIdMap());
        CompletableFuture<List<Boolean>> blocksFuture = unlockedBlocksTable.saveUnlockedBlocks(uuid, playerData.getUnlockedBlocksByMineIdMap());

        return timeFuture
                .thenCombine(blocksFuture, (timeResults, blockResults) -> {
                    boolean allTimesSuccessful = timeResults.stream().allMatch(result -> result);
                    boolean allBlocksSuccessful = blockResults.stream().allMatch(result -> result);
                    return allTimesSuccessful && allBlocksSuccessful;
                })
                .exceptionally(e -> {
                    skyMines.getComponentLogger().error(AdventureUtil.serialize("Failed to save player data for " + uuid + " due to: " + e.getMessage()));
                    return false;
                });
    }

    /**
     * Save all player data to the database.
     * @return A {@link CompletableFuture} containing a {@link Boolean} where true means all data saved successfully and false for any errors.
     */
    public @NotNull CompletableFuture<Boolean> savePlayerData() {
        TimesTable timesTable = databaseManager.getTimesTable();
        UnlockedBlocksTable unlockedBlocksTable = databaseManager.getUnlockedBlocksTable();

        List<CompletableFuture<Boolean>> saveFutures = new ArrayList<>();

        playerDataMap.forEach((uuid, playerData) -> {
            CompletableFuture<List<Boolean>> timeFuture = timesTable.saveMineTimes(uuid, playerData.getMineTimesByMineIdMap());
            CompletableFuture<List<Boolean>> blocksFuture = unlockedBlocksTable.saveUnlockedBlocks(uuid, playerData.getUnlockedBlocksByMineIdMap());

            CompletableFuture<Boolean> resultFuture = timeFuture
                    .thenCombine(blocksFuture, (timeResults, blockResults) -> {
                        boolean allTimesSuccessful = timeResults.stream().allMatch(result -> result);
                        boolean allBlocksSuccessful = blockResults.stream().allMatch(result -> result);
                        return allTimesSuccessful && allBlocksSuccessful;
                    })
                    .exceptionally(e -> {
                        skyMines.getComponentLogger().error(AdventureUtil.serialize("Failed to save player data for " + uuid + " due to: " + e.getMessage()));
                        return false;
                    });

            saveFutures.add(resultFuture);
        });

        return CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> saveFutures.stream().allMatch(CompletableFuture::join));
    }

    public @NotNull CompletableFuture<Void> loadPlayerData(@NotNull UUID uuid) {
        TimesTable timesTable = databaseManager.getTimesTable();
        UnlockedBlocksTable unlockedBlocksTable = databaseManager.getUnlockedBlocksTable();

        CompletableFuture<Map<String, Long>> timesFuture = timesTable.loadMineTimes(uuid);
        CompletableFuture<Map<String, List<BlockType>>> blocksFuture = unlockedBlocksTable.loadUnlockedBlocks(uuid);

        return timesFuture
                .thenCombine(blocksFuture, (timesMap, blockTypeMap) -> {
                    return new PlayerData(new HashMap<>(timesMap), new HashMap<>(blockTypeMap));
                })
                .thenAccept(playerData -> {
                    playerDataMap.put(uuid, playerData);
                })
                .exceptionally(e -> {
                    skyMines.getComponentLogger().error(AdventureUtil.serialize("Failed to load player data for " + uuid + " due to " + e.getMessage()));
                    return null;
                });
    }

    public @NotNull CompletableFuture<Void> unloadPlayerData(@NotNull UUID uuid) {
        return savePlayerData(uuid).thenAccept(result -> playerDataMap.remove(uuid));
    }
}
