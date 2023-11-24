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
import com.github.lukesky19.skynodes.records.Node;
import com.github.lukesky19.skynodes.listeners.NodeBlockBreakListener;
import com.github.lukesky19.skynodes.records.Task;
import com.github.lukesky19.skynodes.utils.ConfigurateUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class SkyNodes extends JavaPlugin {
    // Variables
    ConfigManager cfgMgr;
    NodeManager nodeMgr;
    MessagesManager msgsMgr;
    SettingsManager settingsMgr;
    TaskManager taskMgr;
    ConfigurateUtil confUtil;
    NodeBlockBreakListener nodeBlockBreakListener;
    SchematicManager schemMgr;
    SkyNodeCommand skyNodeCmd;
    Settings settings;
    Messages messages;
    BukkitTask bukkitTask;
    final MiniMessage mm = MiniMessage.miniMessage();
    ComponentLogger logger;

    // Getter(s)
    public ConfigManager getCfgMgr() {
        return cfgMgr;
    }
    public NodeManager getNodeMgr() {
        return nodeMgr;
    }
    public MessagesManager getMsgsMgr() {
        return msgsMgr;
    }
    public SettingsManager getSettingsMgr() {
        return settingsMgr;
    }
    public TaskManager getTaskMgr() {
        return taskMgr;
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

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Store plugin main instance.
        logger = this.getComponentLogger();
        confUtil = new ConfigurateUtil(this);
        cfgMgr = new ConfigManager(this);
        msgsMgr = new MessagesManager(this);
        settingsMgr = new SettingsManager(this);
        nodeMgr = new NodeManager(this);
        taskMgr = new TaskManager(this);
        schemMgr = new SchematicManager(this);
        skyNodeCmd = new SkyNodeCommand(this);
        nodeBlockBreakListener = new NodeBlockBreakListener(this);

        reload();

        // Check if WorldEdit or FastAsyncWorldEdit is enabled.
        if (!Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("WorldEdit")).isEnabled() || !Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit")).isEnabled()) {
            logger.error(messages.missingWorldEditorFastAsyncWorldEditMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // Start the task of pasting nodes.
        try {
            startTasks();
            logger.info(messages.reloadMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Set skynodes command executor and tabcompleter.
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setExecutor(skyNodeCmd);
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setTabCompleter(skyNodeCmd);
        // Register blockBreakListener.
        Bukkit.getPluginManager().registerEvents(nodeBlockBreakListener, this);
    }

    public void reload() {
        cfgMgr.reloadConfig();
        settingsMgr.reloadSettings();
        settings = settingsMgr.getSettings();
        msgsMgr.reloadMessages();
        messages = msgsMgr.getMessages();
        taskMgr.reloadTasks();
    }

    public void startTasks() {
        // Cancel previous BukkitTask if it exists.
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                ArrayList<Task> tasksList = taskMgr.getTasksList();
                for (Task task : tasksList) {
                    Node currentNode = task.nodeList().get(new Random().nextInt(task.nodeList().size()));
                    try {
                        schemMgr.pasteFromConfig(currentNode);
                        logger.info(mm.deserialize(messages.consoleNodePasteSuccessMessage(), Placeholder.parsed("nodeid", currentNode.nodeId())));
                    } catch (Exception e) {
                        logger.info(mm.deserialize(messages.consoleNodePasteFailureMessage(), Placeholder.parsed("nodeid", currentNode.nodeId())));
                        throw new RuntimeException(e);
                    }
                }
            }
        }.runTaskTimer(this,1,20L*settings.timeDelay());
    }
}
