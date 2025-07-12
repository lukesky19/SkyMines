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
package com.github.lukesky19.skymines.database.tables;

import com.github.lukesky19.skylib.api.database.parameter.Parameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.StringParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.UUIDParameter;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.database.QueueManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to create and interface with the unlocked blocks table in the database.
 */
public class UnlockedBlocksTable {
    private final @NotNull SkyMines skyMines;
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "skymines_unlocked_blocks";

    /**
     * Default Constructor.
     * You should use {@link #UnlockedBlocksTable(SkyMines, QueueManager)} instead.
     * @deprecated You should use {@link #UnlockedBlocksTable(SkyMines, QueueManager)} instead.
     */
    @Deprecated
    public UnlockedBlocksTable() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public UnlockedBlocksTable(@NotNull SkyMines skyMines, @NotNull QueueManager queueManager) {
        this.skyMines = skyMines;
        this.queueManager = queueManager;
    }

    /**
     * Creates the table in the database if it doesn't exist and any indexes that don't exist.
     */
    public void createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "mine_id TEXT NOT NULL, " + // Unique
                "player_id LONG NOT NULL DEFAULT 0, " + // Unique
                "unlocked_blocks TEXT NOT NULL DEFAULT 0, " +
                "last_updated LONG NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (mine_id) REFERENCES mine_ids(mine_id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY (player_id) REFERENCES player_ids(player_id), " +
                "UNIQUE (mine_id, player_id))";
        String mineIdsIndexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_mine_ids ON " + tableName + "(mine_id);";
        String playerIdsIndexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_player_ids ON " + tableName + "(player_id);";

        queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, mineIdsIndexSql, playerIdsIndexSql));
    }

    /**
     * Get a {@link Map} mapping mine ids to unlocked blocks as a {@link List} of {@link BlockType}s.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link Map} mapping mine ids to unlocked blocks as a {@link List} of {@link BlockType}s.
     */
    public @NotNull CompletableFuture<@NotNull Map<String, List<BlockType>>> loadUnlockedBlocks(@NotNull UUID uuid) {
        String selectSql = "SELECT mine_id, unlocked_blocks FROM " + tableName + " WHERE player_id = ?";
        UUIDParameter uuidParameter = new UUIDParameter(uuid);

        return queueManager.queueReadTransaction(selectSql, List.of(uuidParameter), resultSet -> {
            Map<String, List<BlockType>> unlockedBlockTypesByMineId = new HashMap<>();

            try {
                while(resultSet.next()) {
                    String mineId = resultSet.getString("mine_id");
                    String unlockedBlocksJson = resultSet.getString("unlocked_blocks");

                    Gson gson = new Gson();

                    Type listType = new TypeToken<List<String>>() {}.getType();

                    List<String> unlockedBlockTypeNames = gson.fromJson(unlockedBlocksJson, listType);
                    List<BlockType> unlockedBlockTypes = unlockedBlockTypeNames.stream().map(blockTypeName -> {
                        Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(skyMines.getComponentLogger(), blockTypeName);
                        return optionalBlockType.orElse(null);
                    }).filter(Objects::nonNull).toList();

                    unlockedBlockTypesByMineId.put(mineId, new ArrayList<>(unlockedBlockTypes));
                }

                return unlockedBlockTypesByMineId;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Save the blocks the player has access to for a mine.
     * @param uuid The {@link UUID} of the player.
     * @param mineId The id of the mine.
     * @param unlockedBlocks A {@link List} of {@link BlockType}.
     * @return A {@link CompletableFuture} containing a {@link Boolean} where true is successful, otherwise false.
     */
    public @NotNull CompletableFuture<Boolean> saveUnlockedBlocks(@NotNull UUID uuid, @NotNull String mineId, @NotNull List<BlockType> unlockedBlocks) {
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (mine_id, player_id, unlocked_blocks, last_updated) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (mine_id, player_id) DO UPDATE SET " +
                "unlocked_blocks = ?, last_updated = ? WHERE last_updated < ?";

        Gson gson = new Gson();
        String jsonList = gson.toJson(unlockedBlocks);

        StringParameter mineIdParameter = new StringParameter(mineId);
        UUIDParameter playerIdParameter = new UUIDParameter(uuid);
        StringParameter unlockedBlocksParameter = new StringParameter(jsonList);
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        List<Parameter<?>> parameterList = List.of(mineIdParameter, playerIdParameter, unlockedBlocksParameter, lastUpdatedParameter, unlockedBlocksParameter, lastUpdatedParameter, lastUpdatedParameter);

        return queueManager.queueWriteTransaction(insertOrUpdateSql, parameterList).thenApply(result -> result > 0);
    }

    /**
     * Saves all mine time for a player to the database.
     * @param data A {@link Map} mapping mine ids to mine time as a {@link Long}.
     * @return A {@link CompletableFuture} containing a {@link List} of {@link Boolean} with the results.
     * The list will contain false if an operation failed.
     */
    public @NotNull CompletableFuture<List<Boolean>> saveUnlockedBlocks(@NotNull UUID uuid, @NotNull Map<String, List<BlockType>> data) {
        Map<String, List<Parameter<?>>> sqlStatementsAndParameters = new HashMap<>();
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (mine_id, player_id, unlocked_blocks, last_updated) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (mine_id, player_id) DO UPDATE SET " +
                "unlocked_blocks = ?, last_updated = ? WHERE last_updated < ?";

        data.forEach((mineId, blockTypeList) -> {
            Gson gson = new Gson();
            List<String> blockTypeNamesList = blockTypeList.stream().map(blockType -> blockType.getKey().toString()).toList();
            String jsonList = gson.toJson(blockTypeNamesList);

            StringParameter mineIdParameter = new StringParameter(mineId);
            UUIDParameter playerIdParameter = new UUIDParameter(uuid);
            StringParameter unlockedBlocksParameter = new StringParameter(jsonList);
            LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

            List<Parameter<?>> parameterList = List.of(mineIdParameter, playerIdParameter, unlockedBlocksParameter, lastUpdatedParameter, unlockedBlocksParameter, lastUpdatedParameter, lastUpdatedParameter);

            sqlStatementsAndParameters.put(insertOrUpdateSql, parameterList);
        });

        return queueManager.queueBulkWriteTransaction(sqlStatementsAndParameters).thenApply(list -> {
                    List<Boolean> results = new ArrayList<>();

                    list.forEach(rowsUpdated -> {
                        if(rowsUpdated > 0) {
                            results.add(true);
                        } else  {
                            results.add(false);
                        }
                    });

                    return results;
                }
        );
    }

    /**
     * Saves all mine time to the database for all mines and players.
     * @param data A {@link Map} mapping mine ids to a {@link Map} mapping {@link UUID} to mine time as a {@link Long}.
     * @return A {@link CompletableFuture} containing a {@link List} of {@link Boolean} with the results.
     * The list will contain false if an operation failed.
     */
    public @NotNull CompletableFuture<@NotNull List<@NotNull Boolean>> saveUnlockedBlocks(@NotNull Map<String, Map<UUID, List<BlockType>>> data) {
        Map<String, List<Parameter<?>>> sqlStatementsAndParameters = new HashMap<>();
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (mine_id, player_id, unlocked_blocks, last_updated) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (mine_id, player_id) DO UPDATE SET " +
                "unlocked_blocks = ?, last_updated = ? WHERE last_updated < ?";

        data.forEach((mineId, unlockedBlocksByPlayerId) -> {
            unlockedBlocksByPlayerId.forEach((uuid, unlockedBlocks) -> {
                Gson gson = new Gson();
                String jsonList = gson.toJson(unlockedBlocks);

                StringParameter mineIdParameter = new StringParameter(mineId);
                UUIDParameter playerIdParameter = new UUIDParameter(uuid);
                StringParameter unlockedBlocksParameter = new StringParameter(jsonList);
                LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

                List<Parameter<?>> parameterList = List.of(mineIdParameter, playerIdParameter, unlockedBlocksParameter, lastUpdatedParameter, unlockedBlocksParameter, lastUpdatedParameter, lastUpdatedParameter);

                sqlStatementsAndParameters.put(insertOrUpdateSql, parameterList);
            });
        });

        return queueManager.queueBulkWriteTransaction(sqlStatementsAndParameters).thenApply(list -> {
                    List<Boolean> results = new ArrayList<>();

                    list.forEach(rowsUpdated -> {
                        if(rowsUpdated > 0) {
                            results.add(true);
                        } else  {
                            results.add(false);
                        }
                    });

                    return results;
                }
        );
    }
}
