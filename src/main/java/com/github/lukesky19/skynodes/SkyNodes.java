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
import com.github.lukesky19.skynodes.listeners.SkyNodeBlockBreakListener;
import com.github.lukesky19.skynodes.records.SkyTask;
import com.github.lukesky19.skynodes.utils.ConfigLoaderUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SkyNodes extends JavaPlugin {
    ConfigLoaderUtil configLoaderUtil;
    SettingsManager settingsManager;
    MessagesManager messagesManager;
    SkyNodeManager skyNodeManager;
    SkyTaskManager skyTaskManager;
    SchematicManager schematicManager;

    Settings settings;
    Messages messages;
    List<BukkitTask> bukkitTasks = new ArrayList<>();
    final MiniMessage mm = MiniMessage.miniMessage();
    BukkitAudiences audiences;

    public @NonNull BukkitAudiences getAudiences() {
        if(this.audiences == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.audiences;
    }

    @Override
    public void onEnable() {
        Logger logger = this.getLogger();

        // Check if WorldEdit or FastAsyncWorldEdit is enabled.
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null || Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") == null) {
            logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(mm.deserialize("<red>WorldEdit or FastAsyncWorldEdit not found. Disabling...")));
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // Adventure Audiences
        this.audiences = BukkitAudiences.create(this);

        // Classes
        configLoaderUtil = new ConfigLoaderUtil(this);
        messagesManager = new MessagesManager(this, configLoaderUtil);
        settingsManager = new SettingsManager(this, configLoaderUtil);
        skyNodeManager = new SkyNodeManager(this, configLoaderUtil, messagesManager);
        skyTaskManager = new SkyTaskManager(this, configLoaderUtil, messagesManager, skyNodeManager);
        schematicManager = new SchematicManager(this, messagesManager, settingsManager);
        SkyNodeCommand skyNodeCommand = new SkyNodeCommand(this, messagesManager, schematicManager, skyTaskManager);

        // Set skynodes command executor and tabcompleter.
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setExecutor(skyNodeCommand);
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setTabCompleter(skyNodeCommand);
        // Register blockBreakListener.
        Bukkit.getPluginManager().registerEvents(new SkyNodeBlockBreakListener(this, messagesManager, settingsManager, skyNodeManager), this);

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
        configLoaderUtil.reloadConfig();
        settingsManager.reloadSettings();
        settings = settingsManager.getSettings();
        messagesManager.reloadMessages();
        messages = messagesManager.getMessages();
        skyTaskManager.createSkyTasks();
        startTasks();
    }

    private void startTasks() {
        Logger logger = this.getLogger();
        // Cancel previous BukkitTask if it exists.
        if(!bukkitTasks.isEmpty()) {
            for (BukkitTask currentTask : bukkitTasks) {
                if (currentTask != null) {
                    currentTask.cancel();
                }
            }
            bukkitTasks = new ArrayList<>();
        }

        for(SkyTask skyTask : skyTaskManager.getSkyTasksList()) {
            String taskId = skyTask.taskId();
            int delaySeconds = skyTask.delaySeconds();
            List<SkyNode> skyNodes = skyTask.skyNodes();

            BukkitTask bukkitTask = new BukkitRunnable() {
                @Override
                public void run() {
                    SkyNode randSkyNode = skyNodes.get(new Random().nextInt(skyNodes.size()));
                    try {
                        schematicManager.paste(taskId, randSkyNode.nodeId(), randSkyNode.nodeWorld(), randSkyNode.vector3List(), randSkyNode.nodeSchems(), randSkyNode.region(), randSkyNode.safeLocation(), null);
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
