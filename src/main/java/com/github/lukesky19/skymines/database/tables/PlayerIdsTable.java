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

import com.github.lukesky19.skylib.api.database.parameter.impl.UUIDParameter;
import com.github.lukesky19.skymines.database.QueueManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to create and interface with the player ids table in the database.
 */
public class PlayerIdsTable {
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "skymines_player_ids";

    /**
     * Default Constructor.
     * You should use {@link #PlayerIdsTable(QueueManager)} instead.
     * @deprecated You should use {@link #PlayerIdsTable(QueueManager)} instead.
     */
    @Deprecated
    public PlayerIdsTable() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param queueManager A {@link QueueManager} instance.
     */
    public PlayerIdsTable(@NotNull QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    /**
     * Creates the table in the database if it doesn't exist and any indexes that don't exist.
     */
    public void createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (player_id TEXT NOT NULL UNIQUE)";
        String indexCreationSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_player_id ON " + tableName + "(player_id)";

        queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, indexCreationSql));
    }

    /**
     * Insert a player id into the player ids table.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} of type {@link Boolean} where true is successful, otherwise false. If the player id already exists it will also return false.
     */
    public @NotNull CompletableFuture<Boolean> insertPlayerId(@NotNull UUID uuid) {
        String insertMineIdSql = "INSERT INTO " + tableName + " (player_id) VALUES (?) ON CONFLICT (player_id) DO NOTHING";

        UUIDParameter playerIdParameter = new UUIDParameter(uuid);

        return queueManager.queueWriteTransaction(insertMineIdSql, List.of(playerIdParameter)).thenApply(result -> result > 0);
    }
}
