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
package com.github.lukesky19.skymines.database;

import com.github.lukesky19.skylib.common.api.database.AbstractDatabaseManager;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.database.tables.MineIdsTable;
import com.github.lukesky19.skymines.database.tables.PlayerIdsTable;
import com.github.lukesky19.skymines.database.tables.TimesTable;
import com.github.lukesky19.skymines.database.tables.UnlockedBlocksTable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This class manages access to the database table classes and migrating the database as needed.
 */
public class DatabaseManager extends AbstractDatabaseManager {
    private final @NonNull PlayerIdsTable playerIdsTable;
    private final @NonNull MineIdsTable mineIdsTable;
    private final @NonNull TimesTable timesTable;
    private final @NonNull UnlockedBlocksTable unlockedBlocksTable;

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param connectionManager Α {@link ConnectionManager} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public DatabaseManager(@NonNull SkyMines skyMines, @NonNull ConnectionManager connectionManager, @NonNull QueueManager queueManager) {
        super(connectionManager, queueManager);

        playerIdsTable = new PlayerIdsTable(queueManager);
        mineIdsTable = new MineIdsTable(queueManager);
        timesTable = new TimesTable(queueManager);
        unlockedBlocksTable = new UnlockedBlocksTable(skyMines, queueManager);

        playerIdsTable.createTable();
        mineIdsTable.createTable();

        migrateTimesTable().thenAccept(_ -> {
            timesTable.createTable();
            unlockedBlocksTable.createTable();
        });
    }

    /**
     * Get the {@link PlayerIdsTable}.
     * @return The {@link PlayerIdsTable}.
     */
    public @NonNull PlayerIdsTable getPlayerIdsTable() {
        return playerIdsTable;
    }

    /**
     * Get the {@link MineIdsTable}.
     * @return The {@link MineIdsTable}.
     */
    public @NonNull MineIdsTable getMineIdsTable() {
        return mineIdsTable;
    }

    /**
     * Get the {@link TimesTable}.
     * @return The {@link TimesTable}.
     */
    public @NonNull TimesTable getTimesTable() {
        return timesTable;
    }

    /**
     * Get the {@link UnlockedBlocksTable}.
     * @return The {@link UnlockedBlocksTable}.
     */
    public @NonNull UnlockedBlocksTable getUnlockedBlocksTable() {
        return unlockedBlocksTable;
    }

    /**
     * Migrates the legacy times data to the new times table.
     */
    private @NonNull CompletableFuture<Boolean> migrateTimesTable() {
        return timesTable.isLegacyFormat().thenCompose(legacyFormatResult -> {
            if (legacyFormatResult) {
                return timesTable.getLegacyData().thenCompose(legacyTimesByMineId ->
                        timesTable.dropTable().thenCompose(_ -> {
                            Map<String, Map<UUID, Long>> updatedData = legacyTimesByMineId.entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> entry.getValue().entrySet().stream()
                                                .collect(Collectors.toMap(
                                                        Map.Entry::getKey,
                                                        entryValue -> entryValue.getValue().longValue()
                                                ))
                                ));

                        return timesTable.createTable().thenCompose(_ -> {
                            List<CompletableFuture<Boolean>> mineIdFutures = new ArrayList<>();
                            List<CompletableFuture<Boolean>> playerIdFutures = new ArrayList<>();

                            updatedData.forEach((mineId, playerData) -> {
                                mineIdFutures.add(mineIdsTable.insertMineId(mineId));
                                playerData.forEach((uuid, _) -> {
                                    playerIdFutures.add(playerIdsTable.insertPlayerId(uuid));
                                });
                            });

                            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                                    mineIdFutures.toArray(new CompletableFuture[0])
                            ).thenCombine(CompletableFuture.allOf(playerIdFutures.toArray(new CompletableFuture[0])), (_, _) -> null);

                            return allFutures.thenCompose(_ -> timesTable.saveMineTimes(updatedData)
                                    .thenApply(results -> !results.contains(false)));
                        });
                }));
            } else {
                // If not a legacy format, return a completed future with true (no migration needed)
                return CompletableFuture.completedFuture(true);
            }
        });
    }
}
