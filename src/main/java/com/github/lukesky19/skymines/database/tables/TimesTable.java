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
import com.github.lukesky19.skymines.database.QueueManager;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to create and interface with the times table in the database.
 */
public class TimesTable {
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "skymines_times";

    /**
     * Default Constructor.
     * You should use {@link #TimesTable(QueueManager)} instead.
     * @deprecated You should use {@link #TimesTable(QueueManager)} instead.
     */
    @Deprecated
    public TimesTable() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param queueManager A {@link QueueManager} instance.
     */
    public TimesTable(@NotNull QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    /**
     * Creates the table in the database if it doesn't exist and any indexes that don't exist.
     */
    public @NotNull CompletableFuture<Void> createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "mine_id TEXT NOT NULL, " + // Unique
                "player_id LONG NOT NULL DEFAULT 0, " + // Unique
                "time LONG NOT NULL DEFAULT 0, " +
                "last_updated LONG NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (mine_id) REFERENCES mine_ids(mine_id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY (player_id) REFERENCES player_ids(player_id), " +
                "UNIQUE (mine_id, player_id))";
        String mineIdsIndexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_mine_ids ON " + tableName + "(mine_id)";
        String playerIdsIndexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_player_ids ON " + tableName + "(player_id)";

        return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, mineIdsIndexSql, playerIdsIndexSql)).thenAccept(result -> {});
    }

    /**
     * Get a {@link Map} mapping mine ids to mine time as a {@link Long}.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link Map} mapping mine ids to mine time as a {@link Long}.
     */
    public @NotNull CompletableFuture<@NotNull Map<String, Long>> loadMineTimes(@NotNull UUID uuid) {
        String selectSql = "SELECT mine_id, time FROM " + tableName + " WHERE player_id = ?";
        UUIDParameter uuidParameter = new UUIDParameter(uuid);

        return queueManager.queueReadTransaction(selectSql, List.of(uuidParameter), resultSet -> {
            Map<String, Long> mineTimes = new HashMap<>();

            try {
                while(resultSet.next()) {
                    String mineId = resultSet.getString("mine_id");
                    long mineTime = resultSet.getLong("time");

                    mineTimes.put(mineId, mineTime);
                }

                return mineTimes;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Save the time the player has access to a mine for.
     * @param uuid The {@link UUID} of the player.
     * @param mineId The id of the mine.
     * @param mineTimeSeconds The time in seconds.
     * @return A {@link CompletableFuture} containing a {@link Boolean} where true is successful, otherwise false.
     */
    public @NotNull CompletableFuture<Boolean> saveMineTime(@NotNull UUID uuid, @NotNull String mineId, long mineTimeSeconds) {
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (mine_id, player_id, time, last_updated) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (mine_id, player_id) DO UPDATE SET " +
                "time = ?, last_updated = ? WHERE last_updated < ?";

        StringParameter mineIdParameter = new StringParameter(mineId);
        UUIDParameter playerIdParameter = new UUIDParameter(uuid);
        LongParameter timeParameter = new LongParameter(mineTimeSeconds);
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        List<Parameter<?>> parameterList = List.of(mineIdParameter, playerIdParameter, timeParameter, lastUpdatedParameter, timeParameter, lastUpdatedParameter, lastUpdatedParameter);

        return queueManager.queueWriteTransaction(insertOrUpdateSql, parameterList).thenApply(result -> result > 0);
    }

    /**
     * Saves all mine time for a player to the database.
     * @param data A {@link Map} mapping mine ids to mine time as a {@link Long}.
     * @return A {@link CompletableFuture} containing a {@link List} of {@link Boolean} with the results.
     * The list will contain false if an operation failed.
     */
    public @NotNull CompletableFuture<List<Boolean>> saveMineTimes(@NotNull UUID uuid, @NotNull Map<String, Long> data) {
        List<List<Parameter<?>>> listOfParameterLists = new ArrayList<>();
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (mine_id, player_id, time, last_updated) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (mine_id, player_id) DO UPDATE SET " +
                "time = ?, last_updated = ? WHERE last_updated < ?";

        data.forEach((mineId, time) -> {
            StringParameter mineIdParameter = new StringParameter(mineId);
            UUIDParameter playerIdParameter = new UUIDParameter(uuid);
            LongParameter timeParameter = new LongParameter(time);
            LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

            List<Parameter<?>> parameterList = List.of(mineIdParameter, playerIdParameter, timeParameter, lastUpdatedParameter, timeParameter, lastUpdatedParameter, lastUpdatedParameter);

            listOfParameterLists.add(parameterList);
        });

        return queueManager.queueBulkWriteTransaction(insertOrUpdateSql, listOfParameterLists).thenApply(list -> {
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
    public @NotNull CompletableFuture<@NotNull List<@NotNull Boolean>> saveMineTimes(@NotNull Map<@NotNull String, Map<@NotNull UUID, @NotNull Long>> data) {
        List<List<Parameter<?>>> listOfParameterLists = new ArrayList<>();
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (mine_id, player_id, time, last_updated) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (mine_id, player_id) DO UPDATE SET " +
                "time = ?, last_updated = ? WHERE last_updated < ?";

        data.forEach((mineId, timesMap) -> {
            StringParameter mineIdParameter = new StringParameter(mineId);

            timesMap.forEach((uuid, time) -> {
                UUIDParameter playerIdParameter = new UUIDParameter(uuid);
                LongParameter timeParameter = new LongParameter(time);
                LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

                List<Parameter<?>> parameterList = List.of(mineIdParameter, playerIdParameter, timeParameter, lastUpdatedParameter, timeParameter, lastUpdatedParameter, lastUpdatedParameter);

                listOfParameterLists.add(parameterList);
            });
        });

        return queueManager.queueBulkWriteTransaction(insertOrUpdateSql, listOfParameterLists).thenApply(list -> {
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
     * Check if the table is in the legacy format or not.
     * @return A {@link CompletableFuture} of type {@link Boolean} where true is legacy and false is not.
     * @deprecated This method is planned for removal in version 3.2.0.0.
     */
    @Deprecated(since = "3.1.0.0", forRemoval = true)
    public @NotNull CompletableFuture<Boolean> isLegacyFormat() {
        String pragmaQuery = "PRAGMA table_info(" + tableName + ")";

        return queueManager.queueReadTransaction(pragmaQuery, resultSet -> {
            try {
                while(resultSet.next()) {
                    String columnName = resultSet.getString("name");
                    String dataType = resultSet.getString("type");

                    if ("time".equals(columnName) && "INTEGER".equalsIgnoreCase(dataType)) {
                        return true;
                    }
                }

                return false;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Gets all legacy data stored in the table.
     * @return A {@link CompletableFuture} containing a {@link Map} mapping mine ids to a {@link Map} mapping {@link UUID}s to mine time in seconds as an {@link Integer}.
     * @deprecated This method is planned for removal in version 3.2.0.0.
     */
    @Deprecated(since = "3.1.0.0", forRemoval = true)
    public @NotNull CompletableFuture<Map<String, Map<UUID, Integer>>> getLegacyData() {
        String selectSql = "SELECT * FROM skymines_times";

        return queueManager.queueReadTransaction(selectSql, resultSet -> {
            Map<String, Map<UUID, Integer>> mineTimesByMineId = new HashMap<>();
            try {
                while(resultSet.next()) {
                    String mineId = resultSet.getString("mine_id");
                    if(mineId == null) continue;

                    Map<UUID, Integer> playerMineTimes = mineTimesByMineId.getOrDefault(mineId, new HashMap<>());

                    UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                    int time = resultSet.getInt("time");

                    playerMineTimes.put(uuid, time);
                    mineTimesByMineId.put(mineId, playerMineTimes);
                }

                return mineTimesByMineId;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Remove the legacy table from the database
     * @deprecated This method is planned for removal in version 3.2.0.0.
     */
    @Deprecated(since = "3.1.0.0", forRemoval = true)
    public @NotNull CompletableFuture<Boolean> dropTable() {
        String dropTableSql = "DROP TABLE " + tableName;

        return queueManager.queueWriteTransaction(dropTableSql)
                .thenApply(result -> {
                    return result > 0;
                })
                .exceptionally(e -> {
                    return false;
                });
    }
}
