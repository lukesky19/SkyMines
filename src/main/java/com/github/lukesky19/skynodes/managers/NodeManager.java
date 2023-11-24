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
import com.github.lukesky19.skynodes.records.Node;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.*;

public class NodeManager {
    final SkyNodes plugin;
    final ComponentLogger logger;
    final ConfigManager cfgMgr;
    final MessagesManager msgsMgr;
    final MiniMessage mm = MiniMessage.miniMessage();

    public NodeManager(SkyNodes plugin) {
        this.plugin = plugin;
        logger = this.plugin.getComponentLogger();
        cfgMgr = plugin.getCfgMgr();
        msgsMgr = plugin.getMsgsMgr();
    }

    public Node createNode(CommentedConfigurationNode configNode) {
        Messages messages = msgsMgr.getMessages();

        String nodeId;
        World nodeWorld;
        List<File> nodeSchemFiles = new ArrayList<>();
        int nodeX, nodeY, nodeZ;
        ProtectedRegion nodeRegion;
        Location safeLocation;
        List<Material> materialsList = new ArrayList<>();

        // Get the key for the CommentedConfigurationNode. Aka, the ID.
        nodeId = Objects.requireNonNull(configNode.key()).toString();

        // Get the world the node is in.
        String worldName = configNode.node("world").getString();
        File worldFile = new File(plugin.getServer().getWorldContainer() + File.separator + worldName);
        if(worldFile.isDirectory() && worldFile.exists()) {
            nodeWorld = new WorldCreator(Objects.requireNonNull(worldName)).createWorld();
        } else {
            logger.error(mm.deserialize(messages.worldNotFoundMessage(),
                    Placeholder.parsed("nodeid", nodeId)));
            return null;
        }

        // Get the location where the node will be pasted.
        // X, Y, and Z coordinates.
        try {
            String[] locationXYZ = Objects.requireNonNull(configNode.node("location").getString()).split(" ");
            nodeX = Integer.parseInt(locationXYZ[0]);
            nodeY = Integer.parseInt(locationXYZ[1]);
            nodeZ = Integer.parseInt(locationXYZ[2]);
        } catch (NumberFormatException e) {
            logger.error(mm.deserialize(messages.invalidLocationMessage(),
                    Placeholder.parsed("nodeid", nodeId)));
            return null;
        }

        // Get the safe location for the node.
        // This is where the player is teleported if they are in the region when a schematic is pasted.
        try {
            String[] safeLocationXYZ = Objects.requireNonNull(
                    configNode.node("safe-location").getString()).split(" ");
            safeLocation = new Location(
                    nodeWorld,
                    Integer.parseInt(safeLocationXYZ[0]),
                    Integer.parseInt(safeLocationXYZ[1]),
                    Integer.parseInt(safeLocationXYZ[2]));
        } catch (NumberFormatException e) {
            logger.error(mm.deserialize(messages.invalidSafeLocationMessage(),
                    Placeholder.parsed("nodeid", nodeId)));
            return null;
        }

        // Get the list of schematics for the node.
        List<String> schemNames;
        try {
            schemNames = configNode.node("schematics").getList(String.class);
        } catch (SerializationException e) {
            logger.error(mm.deserialize(messages.schematicsListErrorMessage(),
                    Placeholder.parsed("nodeid", nodeId)));
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
                    logger.error(mm.deserialize(messages.schematicNotFoundMessage(),
                            Placeholder.parsed("nodeid", nodeId)));
                    return null;
                }
                // If the schematic exists on disk, add it to the list of schematic files.
                nodeSchemFiles.add(file);
            }
        }

        // Get the region for the node.
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Objects.requireNonNull(nodeWorld)));
        if(Objects.requireNonNull(regions).hasRegion(configNode.node("region").getString())) {
            nodeRegion = regions.getRegion(Objects.requireNonNull(configNode.node("region").getString()));
        } else {
            logger.error(mm.deserialize(messages.invalidRegionMessage(),
                    Placeholder.parsed("nodeid", nodeId)));
            return null;
        }

        // Get the list of materials names that will be allowed to be broken in the region.
        List<String> materialIds;
        try {
            materialIds = configNode.node("blocks-allowed").getList(String.class);
        } catch (SerializationException e) {
            logger.error(mm.deserialize(messages.blocksAllowedListErrorMessage(),
                    Placeholder.parsed("nodeid", nodeId)));
            return null;
        }

        // If material name is a valid material, add it to the material list.
        for(String id : Objects.requireNonNull(materialIds)) {
            try {
                materialsList.add(Material.matchMaterial(id));
            } catch (Exception e) {
                logger.error(mm.deserialize(messages.invalidBlockMaterialMessage(),
                        Placeholder.parsed("nodeid", nodeId)));
                return null;
            }
        }
        return new Node(nodeId, nodeWorld, nodeX, nodeY, nodeZ, nodeSchemFiles, nodeRegion, safeLocation, materialsList);
    }
}