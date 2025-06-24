/*
    SkyMines tracks blocks broken in specific regions, replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2025  lukeskywlker19

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
package com.github.lukesky19.skymines;

import com.github.lukesky19.skylib.libs.bstats.bukkit.Metrics;
import com.github.lukesky19.skymines.commands.SkyMinesCommand;
import com.github.lukesky19.skymines.configuration.LocaleManager;
import com.github.lukesky19.skymines.configuration.MineConfigManager;
import com.github.lukesky19.skymines.configuration.SettingsManager;
import com.github.lukesky19.skymines.listeners.*;
import com.github.lukesky19.skymines.manager.DatabaseManager;
import com.github.lukesky19.skymines.manager.MineManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * The main plugin class
 */
public class SkyMines extends JavaPlugin {
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private MineConfigManager mineConfigManager;
    private MineManager mineManager;
    private DatabaseManager databaseManager;

    /**
     * The method ran on plugin startup.
     */
    @Override
    public void onEnable() {
        // Setup bstats
        int pluginId = 22278;
        new Metrics(this, pluginId);

        // Setup Database/DatabaseManager
        try {
            if (!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdirs();
            }

            databaseManager = new DatabaseManager(this,getDataFolder().getAbsolutePath() + File.separator + "database.db");
        } catch (SQLException e) {
            this.getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }

        // Other classes
        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, settingsManager);
        mineManager = new com.github.lukesky19.skymines.manager.MineManager(this, localeManager, databaseManager);
        mineConfigManager = new MineConfigManager(this, mineManager);

        // Register plugin command
        SkyMinesCommand skyMinesCommand = new SkyMinesCommand(this, localeManager, mineManager);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands ->
                        commands.registrar().register(skyMinesCommand.createCommand(),
                                "Command to manage and use the SkyMines plugin.",
                                List.of("skymine", "mines", "mine")));

        // Register Listeners.
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BlockBreakListener(mineManager), this);
        pm.registerEvents(new BlockClickListener(mineManager), this);
        pm.registerEvents(new BlockDropListener(mineManager), this);
        pm.registerEvents(new BlockPlaceListener(mineManager), this);
        pm.registerEvents(new PlayerHarvestBlockListener(mineManager), this);
        pm.registerEvents(new ChunkLoadListener(mineManager), this);
        pm.registerEvents(new PlayerMoveListener(mineManager), this);
        pm.registerEvents(new PlayerTeleportListener(mineManager), this);
        pm.registerEvents(new PlayerJoinListener(mineManager), this);
        pm.registerEvents(new PlayerQuitListener(mineManager), this);

        reload();
    }

    /**
     * The method ran on plugin disable.
     */
    @Override
    public void onDisable() {
        mineManager.clearMines();

        try {
            databaseManager.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reloads all plugin data.
     */
    public void reload() {
        mineManager.clearMines();
        settingsManager.reload();
        localeManager.reload();
        mineConfigManager.reload();
    }
}
