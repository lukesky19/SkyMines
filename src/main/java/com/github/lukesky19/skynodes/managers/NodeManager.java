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
import com.github.lukesky19.skynodes.data.ConfigMessages;
import com.github.lukesky19.skynodes.data.Node;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.*;

public class NodeManager {
    static final MiniMessage mm = MiniMessage.miniMessage();
    private static ArrayList<Node> nodeDataList;
    public static ArrayList<Node> getNodeDataArrayList() {
        return nodeDataList;
    }

    public static void loadNodes() {
        CommentedConfigurationNode nodeConfig = ConfigManager.getNodeConfig();
        ConfigMessages configMessages = ConfigManager.getConfigMessages();
        // Get a Map of every CommentedConfigurationNode node.
        Map<Object, CommentedConfigurationNode> configNodeList = nodeConfig.node("nodes").childrenMap();
        nodeDataList = new ArrayList<>();

        for (Map.Entry<Object, CommentedConfigurationNode> nodeEntry : configNodeList.entrySet()) {
            CommentedConfigurationNode currentNode = nodeEntry.getValue();
            String nodeId = Objects.requireNonNull(currentNode.key()).toString();
            World nodeWorld;
            List<String> schemNames, materialIds;

            String worldName = currentNode.node("world").getString();
            File worldFile = new File(Bukkit.getServer().getWorldContainer() + File.separator + worldName);

            // Check if world of the node is actually a world.
            // If so, get that world by loading it using WorldCreator.
            if (worldFile.isDirectory() && worldFile.exists()) {
                nodeWorld = new WorldCreator(Objects.requireNonNull(worldName)).createWorld();
            } else {
                SkyNodes.getInstance().getComponentLogger().error(mm.deserialize(configMessages.worldNotFoundMessage(),
                        Placeholder.parsed("nodeid", nodeId)));
                return;
            }

            // Get the list of schematics that can be pasted for the node.
            List<File> schemFiles = new ArrayList<>();
            try {
                schemNames = currentNode.node("schematics").getList(String.class);
            } catch (SerializationException e) {
                SkyNodes.getInstance().getComponentLogger().error(mm.deserialize(configMessages.schematicsListErrorMessage(),
                        Placeholder.parsed("nodeid", nodeId)));
                return;
            }

            // Looks for the schematic on disk, either in the WorldEdit/schematics or FastAsyncWorldEdit/schematics folder.
            if(schemNames != null) {
                for (String s : schemNames) {
                    File file;
                    if (SkyNodes.getInstance().getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                        file = new File(Objects.requireNonNull(SkyNodes.getInstance().getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + s);
                    } else if (SkyNodes.getInstance().getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
                        file = new File(Objects.requireNonNull(SkyNodes.getInstance().getServer().getPluginManager().getPlugin("FastAsyncWorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + s);
                    } else {
                        SkyNodes.getInstance().getComponentLogger().error(mm.deserialize(configMessages.schematicNotFoundMessage(),
                                Placeholder.parsed("nodeid", nodeId)));
                        return;
                    }

                    // If the schematic exists on disk, add it to the list of schematic files.
                    schemFiles.add(file);
                }
            }

            // Get the node's X Y and Z coordinates where the paste the node.
            int nodeX;
            int nodeY;
            int nodeZ;
            try {
                String[] locationXYZ = Objects.requireNonNull(currentNode.node("location").getString()).split(" ");
                nodeX = Integer.parseInt(locationXYZ[0]);
                nodeY = Integer.parseInt(locationXYZ[1]);
                nodeZ = Integer.parseInt(locationXYZ[2]);
            } catch (NumberFormatException e) {
                SkyNodes.getInstance().getComponentLogger().error(mm.deserialize(configMessages.invalidLocationMessage(),
                        Placeholder.parsed("nodeid", nodeId)));
                return;
            }

            // Get the region for the node.
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(Objects.requireNonNull(nodeWorld)));
            ProtectedRegion region;
            if(Objects.requireNonNull(regions).hasRegion(currentNode.node("region").getString())) {
                region = regions.getRegion(Objects.requireNonNull(currentNode.node("region").getString()));
            } else {
                SkyNodes.getInstance().getComponentLogger().error(mm.deserialize(configMessages.invalidRegionMessage(),
                        Placeholder.parsed("nodeid", nodeId)));
                return;
            }

            // Get the safe location for the node.
            Location safeLocation;
            try {
                String[] safeLocationXYZ = Objects.requireNonNull(currentNode.node("safe-location").getString()).split(" ");
                safeLocation = new Location(nodeWorld, Integer.parseInt(safeLocationXYZ[0]), Integer.parseInt(safeLocationXYZ[1]), Integer.parseInt(safeLocationXYZ[2]));
            } catch (NumberFormatException e) {
                SkyNodes.getInstance().getComponentLogger().error(mm.deserialize(configMessages.invalidSafeLocationMessage(),
                        Placeholder.parsed("nodeid", nodeId)));
                return;
            }

            // Get the list of materials names that will be allowed to be broken in the region.
            List<Material> materialList = new ArrayList<>();
            try {
                materialIds = currentNode.node("blocks-allowed").getList(String.class);
            } catch (SerializationException e) {
                SkyNodes.getInstance().getComponentLogger().error(mm.deserialize(configMessages.blocksAllowedListErrorMessage(),
                        Placeholder.parsed("nodeid", nodeId)));
                return;
            }

            // If material name is a valid material, add it to the material list.
            for(String id : Objects.requireNonNull(materialIds)) {
                try {
                    materialList.add(Material.matchMaterial(id));
                } catch (Exception e) {
                    SkyNodes.getInstance().getComponentLogger().error(mm.deserialize(configMessages.invalidBlockMaterialMessage(),
                            Placeholder.parsed("nodeid", nodeId)));
                    SkyNodes.getInstance().getComponentLogger().error(MiniMessage.miniMessage().deserialize(e.getMessage()));
                    return;
                }
            }

            // Create Node using all the data from above.
            Node nodeData = Node.createNode(nodeId, nodeWorld, nodeX, nodeY, nodeZ, schemFiles, region, safeLocation, materialList);

            // Add node to the list of nodes.
            nodeDataList.add(nodeData);
        }
    }
}
