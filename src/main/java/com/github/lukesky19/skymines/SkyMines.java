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
package com.github.lukesky19.skymines;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.libs.bstats.bukkit.Metrics;
import com.github.lukesky19.skymines.commands.SkyMinesCommand;
import com.github.lukesky19.skymines.configuration.GUIConfigManager;
import com.github.lukesky19.skymines.configuration.LocaleManager;
import com.github.lukesky19.skymines.configuration.MineConfigManager;
import com.github.lukesky19.skymines.configuration.SettingsManager;
import com.github.lukesky19.skymines.database.ConnectionManager;
import com.github.lukesky19.skymines.database.DatabaseManager;
import com.github.lukesky19.skymines.database.QueueManager;
import com.github.lukesky19.skymines.listeners.*;
import com.github.lukesky19.skymines.manager.bossbar.BossBarManager;
import com.github.lukesky19.skymines.manager.gui.GUIManager;
import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.manager.mine.MineManager;
import com.github.lukesky19.skymines.manager.mine.packet.CooldownManager;
import com.github.lukesky19.skymines.manager.mine.packet.MineTimeManager;
import com.github.lukesky19.skymines.manager.mine.world.BlocksManager;
import com.github.lukesky19.skymines.manager.player.PlayerDataManager;
import com.github.lukesky19.skymines.manager.task.TaskManager;
import com.github.lukesky19.skymines.mine.AbstractMine;
import com.google.common.collect.ImmutableList;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The main plugin class
 */
public class SkyMines extends JavaPlugin {
    // Config
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private MineConfigManager mineConfigManager;
    private GUIConfigManager guiConfigManager;

    // GUI
    private GUIManager guiManager;

    // Mine
    private MineManager mineManager;
    private MineDataManager mineDataManager;

    // Player Data
    private PlayerDataManager playerDataManager;
    private BossBarManager bossBarManager;

    // Database
    private DatabaseManager databaseManager;

    // Task
    private TaskManager taskManager;

    // Economy
    private Economy economy;

    /**
     * Get the {@link Economy} for the server.
     * @return The server's {@link Economy}.
     */
    public @NotNull Economy getEconomy() {
        return this.economy;
    }

    /**
     * The method ran on plugin startup.
     */
    @Override
    public void onEnable() {
        if(!checkSkyLibVersion()) return;
        // Check for and set up Vault/Economy.
        if(!setupEconomy()) return;

        // Setup bstats
        int pluginId = 22278;
        new Metrics(this, pluginId);

        // Config Classes
        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, settingsManager);
        mineConfigManager = new MineConfigManager(this);
        guiConfigManager = new GUIConfigManager(this);

        // Database Classes
        ConnectionManager connectionManager = new ConnectionManager(this);
        QueueManager queueManager = new QueueManager(connectionManager);
        databaseManager = new DatabaseManager(this, connectionManager, queueManager);

        // Mine Data Classes
        mineDataManager = new MineDataManager();

        // PlayerData classes
        playerDataManager = new PlayerDataManager(this, databaseManager);
        bossBarManager = new BossBarManager(playerDataManager, mineDataManager);
        MineTimeManager mineTimeManager = new MineTimeManager(playerDataManager, bossBarManager);
        CooldownManager cooldownManager = new CooldownManager(this, playerDataManager);
        BlocksManager blocksManager = new BlocksManager(playerDataManager);

        // Mine Classes
        mineManager = new MineManager(this, localeManager, mineConfigManager, mineDataManager, cooldownManager, mineTimeManager, bossBarManager, blocksManager);

        // GUI Classes
        guiManager = new GUIManager(this);

        // Task Classes
        taskManager = new TaskManager(this, mineDataManager, playerDataManager, mineTimeManager, cooldownManager);

        // Register plugin command
        SkyMinesCommand skyMinesCommand = new SkyMinesCommand(this, localeManager, guiConfigManager, mineConfigManager, guiManager, mineDataManager, mineTimeManager, blocksManager);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands ->
                        commands.registrar().register(skyMinesCommand.createCommand(),
                                "Command to manage and use the SkyMines plugin.",
                                List.of("skymine", "mines", "mine")));

        // Register Listeners
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BlockBreakListener(mineDataManager), this);
        pm.registerEvents(new BlockDropListener(mineDataManager), this);
        pm.registerEvents(new BlockFertilizeListener(mineDataManager), this);
        pm.registerEvents(new BlockFromToListener(mineDataManager), this);
        pm.registerEvents(new BlockPlaceListener(mineDataManager), this);
        pm.registerEvents(new BucketListener(mineDataManager), this);
        pm.registerEvents(new ChunkLoadListener(mineDataManager), this);
        pm.registerEvents(new EntityChangeBlockListener(mineDataManager), this);
        pm.registerEvents(new ExplosionListener(this, mineDataManager), this);
        pm.registerEvents(new InventoryListener(guiManager), this);
        pm.registerEvents(new PlayerHarvestBlockListener(mineDataManager), this);
        pm.registerEvents(new PlayerInteractListener(mineDataManager), this);
        pm.registerEvents(new PlayerJoinListener(mineDataManager, playerDataManager), this);
        pm.registerEvents(new PlayerMoveListener(mineDataManager), this);
        pm.registerEvents(new PlayerQuitListener(playerDataManager, bossBarManager), this);
        pm.registerEvents(new PlayerTeleportListener(mineDataManager), this);
        pm.registerEvents(new StructureGrowListener(mineDataManager), this);

        reload();

        List<Player> onlinePlayers = ImmutableList.copyOf(this.getServer().getOnlinePlayers().stream().filter(player -> player.isOnline() && player.isConnected()).toList());
        onlinePlayers.forEach(player ->
                playerDataManager.loadPlayerData(player.getUniqueId()).thenAccept(v -> {
                    AbstractMine mine = mineDataManager.getMineByLocation(player.getLocation());
                    if(mine != null) {
                        mine.createAndShowBossBar(player, player.getUniqueId());
                    }
        }));
    }

    /**
     * The method ran on plugin disable.
     */
    @Override
    public void onDisable() {
        guiManager.closeOpenGUIs(true);

        if(taskManager != null) {
            taskManager.stopMineTask();
            taskManager.stopSaveTask();
        }

        if(mineManager != null) {
            mineManager.clearMines(true);
        }

        playerDataManager.savePlayerData().thenAccept(result -> {
            if(playerDataManager != null) {
                List<Player> onlinePlayers = ImmutableList.copyOf(this.getServer().getOnlinePlayers().stream().filter(player -> player.isOnline() && player.isConnected()).toList());
                onlinePlayers.forEach(player -> bossBarManager.removeBossBar(player, player.getUniqueId()));
            }

            if(databaseManager != null) databaseManager.handlePluginDisable();
        });
    }

    /**
     * Reloads all plugin data.
     */
    public void reload() {
        guiManager.closeOpenGUIs(false);

        settingsManager.reload();
        localeManager.reload();
        guiConfigManager.reload();
        mineConfigManager.reload();
        mineManager.reload();

        taskManager.startMineTask();
        taskManager.startSaveTask();
    }

    /**
     * Checks if the Server has the proper SkyLib version.
     * @return true if it does, false if not.
     */
    private boolean checkSkyLibVersion() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin skyLib = pluginManager.getPlugin("SkyLib");
        if (skyLib != null) {
            String version = skyLib.getPluginMeta().getVersion();
            String[] splitVersion = version.split("\\.");
            int second = Integer.parseInt(splitVersion[1]);

            if(second >= 3) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtil.serialize("SkyLib Version 1.3.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }

    /**
     * Checks for Vault as a dependency and sets up the Economy instance.
     */
    private boolean setupEconomy() {
        if(getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();

                return true;
            }
        }

        this.getComponentLogger().error(MiniMessage.miniMessage().deserialize("<red>SkyShop has been disabled due to no Vault dependency found!</red>"));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }
}
