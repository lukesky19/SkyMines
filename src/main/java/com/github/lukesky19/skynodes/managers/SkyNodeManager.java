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
package com.github.lukesky19.skynodes.managers;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.records.Messages;
import com.github.lukesky19.skynodes.records.SkyNode;
import com.github.lukesky19.skynodes.utils.ConfigLoaderUtil;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SkyNodeManager {
    // Constructor
    public SkyNodeManager(SkyNodes plugin, ConfigLoaderUtil configLoaderUtil, MessagesManager messagesManager) {
        this.plugin = plugin;
        this.configLoaderUtil = configLoaderUtil;
        this.messagesManager = messagesManager;
    }

    // Variables
    final SkyNodes plugin;
    final ConfigLoaderUtil configLoaderUtil;
    final MessagesManager messagesManager;
    final MiniMessage mm = MiniMessage.miniMessage();

    List<SkyNode> allSkyNodes;

    // Getter(s)
    public List<SkyNode> getAllSkyNodes() {
        return allSkyNodes;
    }

    /**
     * Loads, and stores all configured SkyNodes into memory.
     * Will not store any null SkyNodes (invalid configuration).
     */

    public List<SkyNode> loadSkyNodes(@NotNull CommentedConfigurationNode task) {
        allSkyNodes = new ArrayList<>();
        List<CommentedConfigurationNode> nodesList = configLoaderUtil.getConfigSection(task, "nodes");
        List<SkyNode> skyNodesList = new ArrayList<>();
            for(CommentedConfigurationNode skyNodeComConfNode : nodesList) {
                SkyNode skyNode = createSkyNode(Objects.requireNonNull(task.key()).toString(), skyNodeComConfNode);
                if (skyNode != null) {
                    skyNodesList.add(skyNode);
                    allSkyNodes.add(skyNode);
                }
            }
        return skyNodesList;
    }

    /**
     * Loads all necessary data for a specific SkyNode from the config file nodes.yml.
     * @param nodeConfig The CommentedConfigurationNode associated with a specific SkyNode.
     * @return A new SkyNode object or null.
     */
    private SkyNode createSkyNode(String taskId, CommentedConfigurationNode nodeConfig) {
        Logger logger = plugin.getLogger();
        // Plugin Messages
        Messages messages = messagesManager.getMessages();

        // Variables for data necessary for a new Node object.
        String nodeId = Objects.requireNonNull(nodeConfig.key()).toString();
        World nodeWorld;
        List<BlockVector3> vectors = new ArrayList<>();
        List<File> nodeSchematics = new ArrayList<>();
        ProtectedRegion nodeRegion;
        Location safeLocation;
        List<Material> materialsList = new ArrayList<>();

        // Get the configured world for the node
        MultiverseCore core = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        try {
            nodeWorld = core.getMVWorldManager().getMVWorld(nodeConfig.node("world").getString()).getCBWorld();
        } catch (Exception e) {
            logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                    mm.deserialize(messages.worldNotFound(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId))));
            return null;
        }

        // Parse the locations configured for the node
        List<String> coordsList = new ArrayList<>();
        try {
            coordsList = nodeConfig.node("location").getList(String.class);
        } catch (SerializationException e) {
            logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                    mm.deserialize(messages.invalidLocation(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId))));
            return null;
        }

        if(!coordsList.isEmpty()) {
            for(String set : coordsList) {
                String[] coords = set.split(" ");
                BlockVector3 vector = BlockVector3.at(
                        Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2]));
                vectors.add(vector);
            }
        } else {
            logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                    mm.deserialize(messages.invalidLocation(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId))));
            return null;
        }

        // Get the list of schematics for the node.
        List<String> schemNames;
        try {
            schemNames = nodeConfig.node("schematics").getList(String.class);
        } catch (SerializationException e) {
            logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                    mm.deserialize(messages.schematicsListError(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId))));
            return null;
        }

        // Looks for the schematic on disk, either in the WorldEdit/schematics or FastAsyncWorldEdit/schematics folder.
        if(schemNames != null) {
            for (String s : schemNames) {
                File file;
                if (plugin.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                    file = new File(Objects.requireNonNull(plugin.getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + s);
                } else if (plugin.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
                    file = new File(Objects.requireNonNull(plugin.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + s);
                } else {
                    logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                            mm.deserialize(messages.schematicNotFound(),
                                    Placeholder.parsed("taskid", taskId),
                                    Placeholder.parsed("nodeid", nodeId))));
                    return null;
                }
                // If the schematic exists on disk, add it to the list of schematic files.
                nodeSchematics.add(file);
            }
        }

        // Get the region for the node.
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Objects.requireNonNull(nodeWorld)));
        if(Objects.requireNonNull(regions).hasRegion(nodeConfig.node("region").getString())) {
            nodeRegion = regions.getRegion(Objects.requireNonNull(nodeConfig.node("region").getString()));
        } else {
            logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                    mm.deserialize(messages.invalidRegion(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId))));
            return null;
        }

        // Get the safe location for the node.
        try {
            String[] safeLocationXYZ = Objects.requireNonNull(
                    nodeConfig.node("safe-location").getString()).split(" ");
            safeLocation = new Location(
                    nodeWorld,
                    Integer.parseInt(safeLocationXYZ[0]),
                    Integer.parseInt(safeLocationXYZ[1]),
                    Integer.parseInt(safeLocationXYZ[2]));
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                    mm.deserialize(messages.invalidSafeLocation(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId))));
            return null;
        }

        // Get the list of materials names that will be allowed to be broken in the region.
        List<String> materialIds;
        try {
            materialIds = nodeConfig.node("blocks-allowed").getList(String.class);
        } catch (SerializationException e) {
            logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                    mm.deserialize(messages.blocksAllowedListError(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId))));
            return null;
        }

        // If material name is a valid material, add it to the material list.
        for(String id : Objects.requireNonNull(materialIds)) {
            try {
                materialsList.add(Material.matchMaterial(id));
            } catch (Exception e) {
                logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                        mm.deserialize(messages.invalidBlockMaterial(),
                                Placeholder.parsed("taskid", taskId),
                                Placeholder.parsed("nodeid", nodeId))));
                return null;
            }
        }

        return new SkyNode(nodeId, nodeWorld, vectors, nodeSchematics, nodeRegion, safeLocation, materialsList);
    }

}