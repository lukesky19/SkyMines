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
package com.github.lukesky19.skynodes.configuration.config;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.configuration.locale.FormattedLocale;
import com.github.lukesky19.skynodes.configuration.locale.LocaleManager;
import com.github.lukesky19.skynodes.configuration.settings.SettingsManager;
import com.github.lukesky19.skynodes.utils.ConfigurationUtility;
import com.github.lukesky19.skynodes.utils.PasteManager;
import com.github.lukesky19.skynodes.utils.SchedulerUtility;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class ConfigManager {
    final SkyNodes skyNodes;
    final LocaleManager localeManager;
    final SettingsManager settingsManager;
    final ConfigValidator configValidator;
    final PasteManager pasteManager;
    final SchedulerUtility schedulerUtility;
    final ConfigurationUtility configurationUtility;
    ParsedConfig parsedConfig;

    /**
     * Constructor
     * @param skyNodes The plugin's instance.
     * @param localeManager A LocaleManager instance.
     * @param settingsManager A SettingsManager instance.
     * @param configurationUtility A ConfigurationUtility instance.
     */
    public ConfigManager(
            SkyNodes skyNodes,
            LocaleManager localeManager,
            SettingsManager settingsManager,
            ConfigValidator configValidator,
            PasteManager pasteManager,
            SchedulerUtility schedulerUtility,
            ConfigurationUtility configurationUtility) {
        this.skyNodes = skyNodes;
        this.localeManager = localeManager;
        this.settingsManager = settingsManager;
        this.configValidator = configValidator;
        this.pasteManager = pasteManager;
        this.schedulerUtility = schedulerUtility;
        this.configurationUtility = configurationUtility;
    }

    public ParsedConfig getConfiguration() {
        return parsedConfig;
    }

    /**
     * A method to reload the plugin's tasks/nodes config.
    */
    public void reload() {
        ComponentLogger logger = skyNodes.getComponentLogger();

        PlainConfig plainConfig;
        schedulerUtility.stopTasks();
        if(!skyNodes.isPluginEnabled()) return;
        FormattedLocale messages = localeManager.formattedLocale();

        skyNodes.saveResource("config.yml", false);

        Path path = Path.of(skyNodes.getDataFolder() + File.separator + "config.yml");
        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
        try {
            plainConfig = loader.load().get(PlainConfig.class);
        } catch (ConfigurateException exception) {
            skyNodes.setPluginState(false);
            return;
        }

        if(plainConfig == null) {
            logger.error(messages.configLoadError());
            logger.info(messages.softDisable());

            skyNodes.setPluginState(false);
            return;
        }

        if(!configValidator.areTasksConfigured(plainConfig)) {
            logger.info(messages.softDisable());
            skyNodes.setPluginState(false);
            return;
        }

        LinkedHashMap<String, ParsedConfig.SkyTask> skyTasks = new LinkedHashMap<>();
        for(Map.Entry<String, PlainConfig.SkyTask> taskEntry : plainConfig.tasks().entrySet()) {
            LinkedHashMap<String, ParsedConfig.SkyNode> skyNodeLinkedHashMap = new LinkedHashMap<>();

            if(!configValidator.areNodesConfigured(plainConfig)) {
                logger.info(messages.softDisable());
                skyNodes.setPluginState(false);
                return;
            }

            for(Map.Entry<String, PlainConfig.SkyNode> nodeEntry : taskEntry.getValue().nodes().entrySet()) {
                PlainConfig.SkyNode node = nodeEntry.getValue();
                String taskId = taskEntry.getKey();
                String nodeId = nodeEntry.getKey();

                World nodeWorld = configValidator.verifyWorld(taskId, nodeId, node.world());
                if(nodeWorld == null) {
                    logger.info(messages.softDisable());
                    skyNodes.setPluginState(false);
                    return;
                }
                List<BlockVector3> vector3List = configValidator.verifyLocations(taskId, nodeId, node.location());
                if(vector3List == null || vector3List.isEmpty()) {
                    logger.info(messages.softDisable());
                    skyNodes.setPluginState(false);
                    return;
                }
                List<File> schematicList = configValidator.verifySchematics(taskId, nodeId, node.schematics());
                if(schematicList == null || schematicList.isEmpty()) {
                    logger.info(messages.softDisable());
                    skyNodes.setPluginState(false);
                    return;
                }

                ProtectedRegion region = configValidator.verifyRegion(taskId, nodeId, node.region(), nodeWorld);
                if(region == null) {
                    logger.info(messages.softDisable());
                    skyNodes.setPluginState(false);
                    return;
                }

                Location safeLocation = configValidator.verifySafeLocation(taskId, nodeId, node.safeLocation(), nodeWorld);
                if(safeLocation == null) {
                    logger.info(messages.softDisable());
                    skyNodes.setPluginState(false);
                    return;
                }

                List<Material> materialList = configValidator.parseMaterials(taskId, nodeId, node.blocksAllowed());
                if(materialList == null) {
                    logger.info(messages.softDisable());
                    skyNodes.setPluginState(false);
                    return;
                }
                skyNodeLinkedHashMap.put(nodeEntry.getKey(), new ParsedConfig.SkyNode(node.world(), vector3List, node.schematics(), node.region(), safeLocation, materialList));
            }
            skyTasks.put(taskEntry.getKey(), new ParsedConfig.SkyTask(taskEntry.getValue().delaySeconds(), skyNodeLinkedHashMap));
        }
        parsedConfig = new ParsedConfig(skyTasks);
    }

    public void startTasks() {
        ComponentLogger logger = skyNodes.getComponentLogger();

        if(!skyNodes.isPluginEnabled()) {
            logger.warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyNodes</aqua> <gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
            return;
        }

        for(Map.Entry<String, ParsedConfig.SkyTask> task : parsedConfig.tasks().entrySet()) {
            String taskId = task.getKey();
            ParsedConfig.SkyTask skyTask = task.getValue();

            BukkitTask bukkitTask = new BukkitRunnable() {
                @Override
                public void run() {
                    List<Map.Entry<String, ParsedConfig.SkyNode>> nodeList = skyTask.skyNodes().entrySet().stream().toList();
                    Map.Entry<String, ParsedConfig.SkyNode> selectedNode = nodeList.get(new Random().nextInt(nodeList.size()));

                    try {
                        pasteManager.paste(taskId, selectedNode.getKey(), selectedNode.getValue(), null);
                    } finally {
                        if(settingsManager.getSettings().debug()) {
                            logger.info(
                                    MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().nodePasteSuccess(),
                                            Placeholder.parsed("taskid", taskId),
                                            Placeholder.parsed("nodeid", selectedNode.getKey())));
                        }
                    }
                }
            }.runTaskTimer(skyNodes,20L*60,20L* skyTask.delaySeconds());
            schedulerUtility.addTask(bukkitTask);
        }
        
        if(settingsManager.getSettings().debug()) {
            logger.info(localeManager.formattedLocale().startTasksSuccess());
        }
    }
}
