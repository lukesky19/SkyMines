/*
    SkyNodes places a random configured schematic after a set period of time.
    Copyright (C) 2023  lukeskywlker19

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
package com.github.lukesky19.skynodes;

import com.github.lukesky19.skynodes.commands.SkyNodeCommand;
import com.github.lukesky19.skynodes.managers.*;
import com.github.lukesky19.skynodes.records.Messages;
import com.github.lukesky19.skynodes.records.Settings;
import com.github.lukesky19.skynodes.records.SkyNode;
import com.github.lukesky19.skynodes.listeners.NodeBlockBreakListener;
import com.github.lukesky19.skynodes.records.SkyTask;
import com.github.lukesky19.skynodes.utils.ConfigurateUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SkyNodes extends JavaPlugin {
    // Variables
    ConfigManager cfgMgr;
    NodeManager nodeMgr;
    SkyTaskManager taskMgr;
    MessagesManager msgsMgr;
    SettingsManager settingsMgr;
    ConfigurateUtil confUtil;
    NodeBlockBreakListener nodeBlockBreakListener;
    SchematicManager schemMgr;
    SkyNodeCommand skyNodeCmd;
    Settings settings;
    Messages messages;
    List<BukkitTask> bukkitTasks = new ArrayList<>();
    final MiniMessage mm = MiniMessage.miniMessage();
    Logger logger;
    BukkitAudiences audiences;

    // Getter(s)
    public ConfigManager getCfgMgr() {
        return cfgMgr;
    }
    public NodeManager getNodeMgr() {
        return nodeMgr;
    }
    public SkyTaskManager getTaskMgr() {
        return taskMgr;
    }
    public MessagesManager getMsgsMgr() {
        return msgsMgr;
    }
    public SettingsManager getSettingsMgr() {
        return settingsMgr;
    }
    public ConfigurateUtil getConfUtil() {
        return confUtil;
    }
    public SchematicManager getSchemMgr() {
        return schemMgr;
    }
    public SkyNodeCommand getSkyNodeCmd() {
        return skyNodeCmd;
    }
    public @NonNull BukkitAudiences getAudiences() {
        if(this.audiences == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.audiences;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger = this.getLogger();

        if(Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core") == null) {
            logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(mm.deserialize("<red>Multiverse-Core not found. Disabling...")));
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // Check if WorldEdit or FastAsyncWorldEdit is enabled.
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null || Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") == null) {
            logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(mm.deserialize("<red>WorldEdit or FastAsyncWorldEdit not found. Disabling...")));
            Bukkit.getPluginManager().disablePlugin(this);
        }

        this.audiences = BukkitAudiences.create(this);

        confUtil = new ConfigurateUtil(this);
        cfgMgr = new ConfigManager(this);
        msgsMgr = new MessagesManager(this);
        settingsMgr = new SettingsManager(this);
        nodeMgr = new NodeManager(this);
        taskMgr = new SkyTaskManager(this);
        schemMgr = new SchematicManager(this);
        skyNodeCmd = new SkyNodeCommand(this);
        nodeBlockBreakListener = new NodeBlockBreakListener(this);

        // Set skynodes command executor and tabcompleter.
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setExecutor(skyNodeCmd);
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setTabCompleter(skyNodeCmd);
        // Register blockBreakListener.
        Bukkit.getPluginManager().registerEvents(nodeBlockBreakListener, this);

        reload();
    }

    @Override
    public void onDisable() {
        if(!bukkitTasks.isEmpty()) {
            for (BukkitTask currentTask : bukkitTasks) {
                if (currentTask != null) {
                    currentTask.cancel();
                }
            }
            bukkitTasks = new ArrayList<>();
        }

        if(this.audiences != null) {
            this.audiences.close();
            this.audiences = null;
        }
    }

    public void reload() {
        cfgMgr.reloadConfig();
        settingsMgr.reloadSettings();
        settings = settingsMgr.getSettings();
        msgsMgr.reloadMessages();
        messages = msgsMgr.getMessages();
        nodeMgr.setAllSkyNodes(new ArrayList<>());
        taskMgr.createSkyTasks();
        startTasks();
    }

    private void startTasks() {
        // Cancel previous BukkitTask if it exists.
        if(!bukkitTasks.isEmpty()) {
            for (BukkitTask currentTask : bukkitTasks) {
                if (currentTask != null) {
                    currentTask.cancel();
                }
            }
            bukkitTasks = new ArrayList<>();
        }

        for(SkyTask skyTask : taskMgr.getSkyTasksList()) {
            String taskId = skyTask.taskId();
            int delaySeconds = skyTask.delaySeconds();
            List<SkyNode> skyNodes = skyTask.skyNodes();

            BukkitTask bukkitTask = new BukkitRunnable() {
                @Override
                public void run() {
                    SkyNode randSkyNode = skyNodes.get(new Random().nextInt(skyNodes.size()));
                    try {
                        schemMgr.paste(taskId, randSkyNode.nodeId(), randSkyNode.nodeWorld(), randSkyNode.blockVector3(), randSkyNode.nodeSchems(), randSkyNode.region(), randSkyNode.safeLocation(), null);
                    } finally {
                        if (settings.debug()) {
                            logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(
                                    mm.deserialize(messages.nodePasteSuccess(),
                                            Placeholder.parsed("taskid", taskId),
                                            Placeholder.parsed("nodeid", randSkyNode.nodeId()))));
                        }
                    }
                }
            }.runTaskTimer(this,20L*60,20L*delaySeconds);
            bukkitTasks.add(bukkitTask);
        }

        if(settings.debug()) {
            logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(messages.startTasksSuccess()));
        }
    }
}
