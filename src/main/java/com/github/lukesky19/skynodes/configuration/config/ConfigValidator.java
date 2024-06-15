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
import com.github.lukesky19.skynodes.configuration.locale.LocaleManager;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigValidator {
    final SkyNodes skyNodes;
    final LocaleManager localeManager;

    /**
     * Constructor
     * @param skyNodes The plugin's instance.
     * @param localeManager A LocaleManager instance.
     */
    public ConfigValidator(
            SkyNodes skyNodes,
            LocaleManager localeManager) {
        this.skyNodes = skyNodes;
        this.localeManager = localeManager;
    }

    public Boolean areTasksConfigured(PlainConfig plainConfig) {
        ComponentLogger logger = skyNodes.getComponentLogger();

        if(plainConfig.tasks().entrySet().isEmpty()) {
            logger.error(localeManager.formattedLocale().noTasksFound());
            return false;
        }
        return true;
    }

    public Boolean areNodesConfigured(PlainConfig plainConfig) {
        ComponentLogger logger = skyNodes.getComponentLogger();

        for(Map.Entry<String, PlainConfig.SkyTask> skyTask : plainConfig.tasks().entrySet()) {
            if(skyTask.getValue().nodes().isEmpty()) {
                logger.error(
                        MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().noNodesFound(),
                                Placeholder.parsed("taskid", skyTask.getKey())));
                return false;
            }
        }
        return true;
    }

    /**
     * Ran on startup/reload and when a node is pasted. A world could be loaded/unloaded at will without the plugin knowing.
     * @param taskId The identifier for a task.
     * @param nodeId The identifier for a node.
     * @param worldName The name of the world to paste a node in as a String.
     * @return A Bukkit World or null if parsing fails.
     */
    public World verifyWorld(String taskId, String nodeId, String worldName) {
        ComponentLogger logger = skyNodes.getComponentLogger();

        MultiverseCore mVCore = (MultiverseCore) skyNodes.getServer().getPluginManager().getPlugin("Multiverse-Core");
        World nodeWorld;
        nodeWorld = Objects.requireNonNull(mVCore).getMVWorldManager().getMVWorld(worldName).getCBWorld();
        if(nodeWorld == null) {
            logger.error(
                    MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().worldNotFound(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId)));
        }
        return nodeWorld;
    }

    /**
     * Only ran on startup/reload to verify locations. Locations can only update through a reload.
     * @param taskId The identifier for a task.
     * @param nodeId The identifier for a node.
     * @param locations The list of strings (representing X Y Z coordinates) to parse.
     * @return A list of BlockVector3s for pasting a node in or null if parsing fails.
     */
    public List<BlockVector3> verifyLocations(String taskId, String nodeId, List<String> locations) {
        ComponentLogger logger = skyNodes.getComponentLogger();

        List<BlockVector3> vector3List = new ArrayList<>();
        if(!locations.isEmpty()) {
            for(String set : locations) {
                try {
                    String[] coords = set.split(" ");
                    BlockVector3 vector = BlockVector3.at(
                            Integer.parseInt(coords[0]),
                            Integer.parseInt(coords[1]),
                            Integer.parseInt(coords[2]));
                    vector3List.add(vector);
                } catch (NumberFormatException e) {
                    logger.error(
                            MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().invalidLocation(),
                                    Placeholder.parsed("taskid", taskId),
                                    Placeholder.parsed("nodeid", nodeId)));
                    return null;
                }
            }
            return vector3List;
        } else {
            logger.error(
                    MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().invalidLocation(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId)));
            return null;
        }
    }

    /**
     * Ran on startup/reload and when a node is pasted. Schematics can be created or deleted without the plugin knowing.
     * @param taskId The identifier for a task.
     * @param nodeId The identifier for a node.
     * @param schematics A list of schematic names.
     * @return A list of files associated with the schematics or null if parsing fails.
     */
    public List<File> verifySchematics(String taskId, String nodeId, List<String> schematics) {
        ComponentLogger logger = skyNodes.getComponentLogger();

        List<File> schemFiles = new ArrayList<>();
        if(!schematics.isEmpty()) {
            for(String s : schematics) {
                File file;
                Plugin worldEdit = skyNodes.getServer().getPluginManager().getPlugin("WorldEdit");
                Plugin fAWE = skyNodes.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit");
                if(worldEdit != null && worldEdit.isEnabled()) {
                    try {
                        file = new File(worldEdit.getDataFolder() + File.separator + "schematics" + File.separator + s);
                    } catch (Exception e) {
                        logger.error(
                                MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().schematicNotFound(),
                                        Placeholder.parsed("taskid", taskId),
                                        Placeholder.parsed("nodeid", nodeId)));
                        return null;
                    }
                    schemFiles.add(file);
                } else if(fAWE != null && fAWE.isEnabled()) {
                    try {
                        file = new File(fAWE.getDataFolder() + File.separator + "schematics" + File.separator + s);
                    } catch (Exception e) {
                        logger.error(
                                MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().schematicNotFound(),
                                        Placeholder.parsed("taskid", taskId),
                                        Placeholder.parsed("nodeid", nodeId)));
                        return null;
                    }
                    schemFiles.add(file);
                } else {
                    logger.error(
                            MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().schematicNotFound(),
                                    Placeholder.parsed("taskid", taskId),
                                    Placeholder.parsed("nodeid", nodeId)));
                    return null;
                }
            }
        } else {
            logger.error(
                    MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().schematicsListError(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId)));
            return null;
        }
        return schemFiles;
    }

    /**
     * Ran on startup/reload and when a node is pasted. Regions can be created or deleted without the plugin knowing.
     * @param taskId The identifier for a task.
     * @param nodeId The identifier for a node.
     * @param region The region name as a String that the node is in.
     * @param nodeWorld The Bukkit World the node is in.
     * @return A WorldGuard region or null if parsing fails.
     */
    public ProtectedRegion verifyRegion(String taskId, String nodeId, String region, World nodeWorld) {
        ComponentLogger logger = skyNodes.getComponentLogger();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Objects.requireNonNull(nodeWorld)));
        if(Objects.requireNonNull(regions).hasRegion(region)) {
            return regions.getRegion(region);
        } else {
            logger.error(
                    MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().invalidRegion(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId)));
            return null;
        }
    }

    /**
     * Ran on startup/reload.
     * A world could technically be unloaded, but that is checked before this, so safe location can only be changed with a reload.
     * @param taskId The identifier for a task.
     * @param nodeId The identifier for a node.
     * @param location A set of X Y Z coordinates as a String.
     * @param nodeWorld A bukkit world.
     * @return A bukkit location or null if parsing failed.
     */
    public Location verifySafeLocation(String taskId, String nodeId, String location, World nodeWorld) {
        ComponentLogger logger = skyNodes.getComponentLogger();

        try {
            String[] safeLocationXYZ = location.split(" ");
            return new Location(
                    nodeWorld,
                    Integer.parseInt(safeLocationXYZ[0]),
                    Integer.parseInt(safeLocationXYZ[1]),
                    Integer.parseInt(safeLocationXYZ[2]));
        } catch (NumberFormatException e) {
            logger.error(
                    MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().invalidSafeLocation(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId)));
            return null;
        }
    }

    /**
     * Ran on startup/reload. Materials list can only change on reload.
     * @param taskId The identifier for a task.
     * @param nodeId The identifier for a node.
     * @param materialIds A list of blocksAllowed (as Strings) to parse.
     * @return A parsed list of Bukkit Materials or null if parsing failed.
     */
    public List<Material> parseMaterials(String taskId, String nodeId, List<String> materialIds) {
        ComponentLogger logger = skyNodes.getComponentLogger();

        List<Material> materialsList = new ArrayList<>();
        if(!materialIds.isEmpty()) {
            for (String id : Objects.requireNonNull(materialIds)) {
                try {
                    materialsList.add(Material.matchMaterial(id));
                } catch (Exception e) {
                    logger.error(
                            MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().invalidBlockMaterial(),
                                    Placeholder.parsed("taskid", taskId),
                                    Placeholder.parsed("nodeid", nodeId)));
                    return null;
                }
            }
        } else {
            logger.error(
                    MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().blocksAllowedListError(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId)));
            return null;
        }
        return materialsList;
    }
}
