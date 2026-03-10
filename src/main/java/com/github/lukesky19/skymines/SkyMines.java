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
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIListener;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skylib.libs.bstats.bukkit.Metrics;
import com.github.lukesky19.skymines.commands.SkyMinesCommand;
import com.github.lukesky19.skymines.database.ConnectionManager;
import com.github.lukesky19.skymines.database.DatabaseManager;
import com.github.lukesky19.skymines.database.QueueManager;
import com.github.lukesky19.skymines.listeners.*;
import com.github.lukesky19.skymines.manager.bossbar.BossBarManager;
import com.github.lukesky19.skymines.manager.config.GUIConfigManager;
import com.github.lukesky19.skymines.manager.config.LocaleManager;
import com.github.lukesky19.skymines.manager.config.MineConfigManager;
import com.github.lukesky19.skymines.manager.config.SettingsManager;
import com.github.lukesky19.skymines.manager.hook.HookManager;
import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.manager.mine.MineManager;
import com.github.lukesky19.skymines.manager.mine.packet.CooldownManager;
import com.github.lukesky19.skymines.manager.mine.packet.MineTimeManager;
import com.github.lukesky19.skymines.manager.mine.world.BlocksManager;
import com.github.lukesky19.skymines.manager.player.PlayerDataManager;
import com.github.lukesky19.skymines.manager.task.TaskManager;
import com.github.lukesky19.skymines.mine.Mine;
import com.google.common.collect.ImmutableList;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.List;

/**
 * The main plugin class
 */
public class SkyMines extends SkyPlugin {
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private MineConfigManager mineConfigManager;
    private GUIConfigManager guiConfigManager;
    private UUIDGUIManager guiManager;
    private MineManager mineManager;
    private MineDataManager mineDataManager;
    private PlayerDataManager playerDataManager;
    private BossBarManager bossBarManager;
    private DatabaseManager databaseManager;
    private TaskManager taskManager;

    /**
     * Default Constructor
     */
    public SkyMines() {}

    /**
     * The method ran on plugin startup.
     */
    @Override
    public void onEnable() {
        if(!checkSkyLibVersion()) return;

        // Setup bstats
        int pluginId = 22278;
        new Metrics(this, pluginId);

        // Database Classes
        ConnectionManager connectionManager = new ConnectionManager(this);
        QueueManager queueManager = new QueueManager(connectionManager);
        databaseManager = new DatabaseManager(this, connectionManager, queueManager);

        // Config Classes
        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, settingsManager);
        mineConfigManager = new MineConfigManager(this, databaseManager);
        guiConfigManager = new GUIConfigManager(this);

        // Mine Data Classes
        mineDataManager = new MineDataManager();

        // PlayerData classes
        playerDataManager = new PlayerDataManager(this, databaseManager);
        bossBarManager = new BossBarManager(playerDataManager, mineDataManager);
        MineTimeManager mineTimeManager = new MineTimeManager(playerDataManager, bossBarManager);
        CooldownManager cooldownManager = new CooldownManager(this, playerDataManager);
        BlocksManager blocksManager = new BlocksManager(playerDataManager);
        HookManager hookManager = new HookManager(this);

        // Mine Classes
        mineManager = new MineManager(this, settingsManager, localeManager, mineConfigManager, mineDataManager, cooldownManager, mineTimeManager, playerDataManager, bossBarManager, blocksManager);

        // GUI Classes
        guiManager = new UUIDGUIManager();

        // Task Classes
        taskManager = new TaskManager(this, mineDataManager, playerDataManager, mineTimeManager, cooldownManager);

        // Register plugin command
        SkyMinesCommand skyMinesCommand = new SkyMinesCommand(this, localeManager, guiConfigManager, mineConfigManager, guiManager, mineDataManager, mineTimeManager, blocksManager, hookManager);

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
        pm.registerEvents(new HangingBreakListener(mineDataManager), this);
        pm.registerEvents(new HangingPlaceListener(mineDataManager), this);
        pm.registerEvents(new HopperMoveItemListener(mineDataManager), this);
        pm.registerEvents(new ItemFrameChangeListener(mineDataManager), this);
        pm.registerEvents(new UUIDGUIListener(guiManager), this);
        pm.registerEvents(new PlayerHarvestBlockListener(mineDataManager), this);
        pm.registerEvents(new PlayerInteractListener(mineDataManager), this);
        pm.registerEvents(new PlayerJoinListener(mineDataManager, playerDataManager, databaseManager), this);
        pm.registerEvents(new PlayerMoveListener(mineDataManager), this);
        pm.registerEvents(new PlayerQuitListener(playerDataManager, bossBarManager), this);
        pm.registerEvents(new PlayerTeleportListener(mineDataManager), this);
        pm.registerEvents(new StructureGrowListener(mineDataManager), this);

        reload(true);

        List<Player> onlinePlayers = ImmutableList.copyOf(this.getServer().getOnlinePlayers().stream().filter(player -> player.isOnline() && player.isConnected()).toList());
        onlinePlayers.forEach(player ->
                playerDataManager.loadPlayerData(player.getUniqueId()).thenAccept(v -> {
                    Mine mine = mineDataManager.getMineByLocation(player.getLocation());
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
        if(guiManager != null) guiManager.closeOpenGUIs(true);

        if(taskManager != null) {
            taskManager.stopMineTask();
            taskManager.stopSaveTask();
        }

        if(mineManager != null) {
            mineManager.clearMines(true);
        }

        if(playerDataManager != null) {
            List<Player> onlinePlayers = ImmutableList.copyOf(this.getServer().getOnlinePlayers());

            playerDataManager.savePlayerData().thenAccept(result -> {
                onlinePlayers.forEach(player -> bossBarManager.removeBossBar(player, player.getUniqueId()));

                if(databaseManager != null) databaseManager.handlePluginDisable();
            });
        }
    }

    /**
     * Reloads all plugin data.
     * @param onEnable Is the reload occurring during plugin enable?
     */
    public void reload(boolean onEnable) {
        guiManager.closeOpenGUIs(false);

        settingsManager.loadConfiguration();
        localeManager.loadConfiguration();
        guiConfigManager.reload();
        mineConfigManager.reload();
        mineManager.reload();

        if(!onEnable) {
            taskManager.stopMineTask();
            taskManager.stopSaveTask();

            // Show boss bars to players in mines
            for(Player onlinePlayer : this.getServer().getOnlinePlayers()) {
                Mine mine = mineDataManager.getMineByLocation(onlinePlayer.getLocation());
                if(mine == null) continue;
                if(mine.getMineId() == null) continue;

                bossBarManager.createAndShowBossBar(mine.getMineId(), onlinePlayer, onlinePlayer.getUniqueId());
            }
        }

        taskManager.startMineTask();
        taskManager.startSaveTask();
    }

    @Override
    public void reload() {
        this.reload(false);
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

            if(second >= 5) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtil.deserialize("SkyLib Version 1.5.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }
}
