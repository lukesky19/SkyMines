package com.github.lukesky19.skymines.manager;

import com.github.lukesky19.skymines.SkyMines;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

/**
 * Handles the storing of player's time to access the mines.
 */
public class DatabaseManager {
    private final SkyMines skyMines;
    private final Connection connection;

    /**
     * Constructor
     * Creates the connection to the database and creates the table if it does not exist.
     * @param skyMines The SkyMines Plugin
     * @param path The path to the database.
     * @throws SQLException If the table creation fails.
     */
    public DatabaseManager(SkyMines skyMines, String path) throws SQLException {
        this.skyMines = skyMines;

        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        try(Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS skymines_times (" +
                    "id INTEGER NOT NULL, " +
                    "mine_id VARCHAR(50) NOT NULL, " +
                    "player_uuid VARCHAR(255) NOT NULL, " +
                    "time INTEGER NOT NULL, " +
                    "PRIMARY KEY('id' AUTOINCREMENT))");
        }
    }

    /**
     * Loads all player times stored in the database for the provided mine id.
     * @param mineId The id of the mine to load player times for.
     * @return A HashMap of player UUIDs and their mine time. May be empty.
     * @throws SQLException If the plugin fails to retrieve the data from the database.
     */
    @NotNull
    public HashMap<UUID, Integer> getPlayerTimesByMineId(String mineId) throws SQLException {
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM skymines_times WHERE mine_id = ?")) {
            preparedStatement.setString(1, mineId);

            ResultSet resultSet = preparedStatement.executeQuery();
            HashMap<UUID, Integer> playerTimes = new HashMap<>();

            while(resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                int time = resultSet.getInt("time");

                playerTimes.put(uuid, time);
            }

            return playerTimes;
        }
    }

    /**
     * Inserts a player's time to access a mine into the database.
     * @param mineId The mine id to save a player's time for.
     * @param playerUUID The UUID of the player.
     * @param time The time to insert.
     */
    public void insertPlayerTime(String mineId, String playerUUID, int time) {
        skyMines.getServer().getScheduler().runTaskAsynchronously(skyMines, () -> {
            try(PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO skymines_times (mine_id, player_uuid, time) VALUES (?, ?, ?)")) {
                insertStatement.setString(1, mineId);
                insertStatement.setString(2, playerUUID);
                insertStatement.setInt(3, time);

                insertStatement.executeUpdate();
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        });
    }

    /**
     * Sets the player's time to access a mine in the database.
     * @param mineId The mine id to save the player's time for.
     * @param playerUUID The UUID of the player.
     * @param time The time to set.
     */
    public void setPlayerTime(String mineId, String playerUUID, int time) {
        skyMines.getServer().getScheduler().runTaskAsynchronously(skyMines, () -> {
            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE skymines_times SET time = ? WHERE mine_id = ? AND player_uuid = ?")) {
                updateStatement.setInt(1, time);
                updateStatement.setString(2, mineId);
                updateStatement.setString(3, playerUUID);
                updateStatement.executeUpdate();
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        });
    }

    /**
     * Removes a player's time to access a mine from the database.
     * @param mineId The mine id to save the player's time for.
     * @param playerUUID The UUID of the player.
     */
    public void deletePlayerTime(String mineId, String playerUUID) {
        skyMines.getServer().getScheduler().runTaskAsynchronously(skyMines, () -> {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM skymines_times WHERE mine_id = ? AND player_uuid = ?")) {
                statement.setString(1, mineId);
                statement.setString(2, playerUUID);
                statement.executeUpdate();
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        });
    }

    /**
     * Closes the connection to the database.
     * @throws SQLException If the database connection fails to close
     */
    public void closeConnection() throws SQLException {
        if(connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
