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

import com.github.lukesky19.skylib.api.database.AbstractDatabaseManager;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.database.tables.MineIdsTable;
import com.github.lukesky19.skymines.database.tables.PlayerIdsTable;
import com.github.lukesky19.skymines.database.tables.TimesTable;
import com.github.lukesky19.skymines.database.tables.UnlockedBlocksTable;
import org.jetbrains.annotations.NotNull;

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
    private final @NotNull PlayerIdsTable playerIdsTable;
    private final @NotNull MineIdsTable mineIdsTable;
    private final @NotNull TimesTable timesTable;
    private final @NotNull UnlockedBlocksTable unlockedBlocksTable;

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param connectionManager Α {@link ConnectionManager} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public DatabaseManager(@NotNull SkyMines skyMines, @NotNull ConnectionManager connectionManager, @NotNull QueueManager queueManager) {
        super(connectionManager, queueManager);

        playerIdsTable = new PlayerIdsTable(queueManager);
        mineIdsTable = new MineIdsTable(queueManager);
        timesTable = new TimesTable(queueManager);
        unlockedBlocksTable = new UnlockedBlocksTable(skyMines, queueManager);

        playerIdsTable.createTable();
        mineIdsTable.createTable();

        migrateTimesTable().thenAccept(result -> {
            timesTable.createTable();
            unlockedBlocksTable.createTable();
        });
    }

    /**
     * Get the {@link TimesTable}.
     * @return The {@link TimesTable}.
     */
    public @NotNull TimesTable getTimesTable() {
        return timesTable;
    }

    /**
     * Get the {@link UnlockedBlocksTable}.
     * @return The {@link UnlockedBlocksTable}.
     */
    public @NotNull UnlockedBlocksTable getUnlockedBlocksTable() {
        return unlockedBlocksTable;
    }

    /**
     * Migrates the legacy times data to the new times table.
     */
    private @NotNull CompletableFuture<Boolean> migrateTimesTable() {
        return timesTable.isLegacyFormat().thenCompose(legacyFormatResult -> {
            if (legacyFormatResult) {
                return timesTable.getLegacyData().thenCompose(legacyTimesByMineId ->
                        timesTable.dropTable().thenCompose(dropTableResult -> {
                            Map<String, Map<UUID, Long>> updatedData = legacyTimesByMineId.entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> entry.getValue().entrySet().stream()
                                                .collect(Collectors.toMap(
                                                        Map.Entry::getKey,
                                                        entryValue -> entryValue.getValue().longValue()
                                                ))
                                ));

                        return timesTable.createTable().thenCompose(v -> {
                            List<CompletableFuture<Boolean>> mineIdFutures = new ArrayList<>();
                            List<CompletableFuture<Boolean>> playerIdFutures = new ArrayList<>();

                            updatedData.forEach((mineId, playerData) -> {
                                mineIdFutures.add(mineIdsTable.insertMineId(mineId));
                                playerData.forEach((uuid, time) -> {
                                    playerIdFutures.add(playerIdsTable.insertPlayerId(uuid));
                                });
                            });

                            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                                    mineIdFutures.toArray(new CompletableFuture[0])
                            ).thenCombine(CompletableFuture.allOf(playerIdFutures.toArray(new CompletableFuture[0])), (v1, v2) -> null);

                            return allFutures.thenCompose(v1 -> timesTable.saveMineTimes(updatedData)
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
