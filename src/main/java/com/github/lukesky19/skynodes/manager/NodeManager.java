/*
    SkyNodes tracks blocks broken in specific regions (nodes), replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2024  lukeskywlker19

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
package com.github.lukesky19.skynodes.manager;

import com.github.lukesky19.skynodes.SkyNodes;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Handles the tracking of BlockStates of blocks broken in Nodes and sending block updates.
 */
public class NodeManager {
    final SkyNodes skyNodes;
    final HashMap<Player, HashMap<World, HashMap<Chunk, Collection<BlockState>>>> blocks;

    /**
     * Constructor
     * @param skyNodes Plugin Instance.
     */
    public NodeManager(SkyNodes skyNodes) {
        this.skyNodes = skyNodes;
        blocks = new HashMap<>();
    }

    /**
     * Checks if the plugin is tracking a BlockState.
     * @param player The Player.
     * @param world The World.
     * @param chunk The Chunk.
     * @param blockState The BlockState
     * @return true if tracked, false if not.
     */
    public boolean isBlockTracked(Player player, World world, Chunk chunk, BlockState blockState) {
        if(blocks.containsKey(player)) {
            HashMap<World, HashMap<Chunk, Collection<BlockState>>> worldMap = blocks.get(player);

            if(worldMap != null && worldMap.containsKey(world)) {
                HashMap<Chunk, Collection<BlockState>> chunkMap = worldMap.get(world);

                if(chunkMap != null && chunkMap.containsKey(chunk)) {
                    Collection<BlockState> blockStates = chunkMap.get(chunk);
                    return blockStates.contains(blockState);
                }
            }
        }

        return false;
    }

    /**
     * Will add a BlockState to track.
     * @param player The Player.
     * @param world The World.
     * @param chunk The Chunk.
     * @param blockState The BlockState
     */
    public void addTrackedBlock(Player player, World world, Chunk chunk, BlockState blockState) {
        HashMap<World, HashMap<Chunk, Collection<BlockState>>> worldMap = blocks.get(player);
        if(worldMap == null) worldMap = new HashMap<>();
        HashMap<Chunk, Collection<BlockState>> chunkMap = worldMap.get(world);
        if(chunkMap == null) chunkMap = new HashMap<>();
        Collection<BlockState> blockStates = chunkMap.get(chunk);
        if(blockStates == null) blockStates = new ArrayList<>();

        blockStates.add(blockState);
        chunkMap.put(chunk, blockStates);
        worldMap.put(world, chunkMap);
        blocks.put(player, worldMap);
    }

    /**
     * Will remove a BlockState from being tracked.
     * @param player The Player.
     * @param world The World.
     * @param chunk The Chunk.
     * @param blockState The BlockState
     */
    public void removeTrackedBlock(Player player, World world, Chunk chunk, BlockState blockState) {
        HashMap<World, HashMap<Chunk, Collection<BlockState>>> worldMap = blocks.get(player);
        HashMap<Chunk, Collection<BlockState>> chunkMap = worldMap.get(world);
        Collection<BlockState> blockStates = chunkMap.get(chunk);

        blockStates.remove(blockState);
        chunkMap.put(chunk, blockStates);
        worldMap.put(world, chunkMap);
        blocks.put(player, worldMap);
    }

    /**
     * For all tracked BlockStates for a specific player, in a specific world, and in a specific chunk, it will send an update for those tracked BlockStates.
     * @param player The Player
     * @param world The World.
     * @param chunk The Chunk.
     */
    public void scheduleBulkBlockChange(Player player, World world, Chunk chunk) {
        if(blocks.containsKey(player)) {
            HashMap<World, HashMap<Chunk, Collection<BlockState>>> worldMap = blocks.get(player);
            if(worldMap != null && worldMap.containsKey(world)) {
                HashMap<Chunk, Collection<BlockState>> chunkMap = worldMap.get(world);
                if(chunkMap != null && chunkMap.containsKey(chunk)) {
                    Collection<BlockState> blockStates = chunkMap.get(chunk);
                    Bukkit.getScheduler().runTaskLater(skyNodes, () -> player.sendBlockChanges(blockStates), 1L);
                }
            }
        }
    }

    /**
     * Will add a BlockState to track and set the client-side BlockState. Will not change the world's BlockState.
     * @param player The Player.
     * @param state The BlockState.
     */
    public void scheduleBlockChange(Player player, World world, Chunk chunk, BlockState state) {
        Bukkit.getScheduler().runTaskLater(skyNodes, () -> {
            if(player.isOnline()) {
                addTrackedBlock(player, world, chunk, state);
                player.sendBlockChange(state.getLocation(), state.getBlockData());
            }
        }, 1L);
    }

    /**
     * Will remove a BlockState from being tracked and set the client-side BlockState back to the world's BlockState.
     * @param player The Player.
     * @param world The World the block is in.
     * @param chunk The Chunk the block is in.
     * @param state The BlockState of the block in the world.
     * @param data The BlockData to revert the BlockState to.
     * @param delaySeconds The cooldown for the block (how long it is tracked for).
     */
    public void scheduleBlockRevert(Player player, World world, Chunk chunk, BlockState state, BlockData data, int delaySeconds) {
        Bukkit.getScheduler().runTaskLater(skyNodes, () -> {
            removeTrackedBlock(player, world, chunk, state);
            Bukkit.getScheduler().runTaskLater(skyNodes, () -> state.setBlockData(data), 1L);
            Bukkit.getScheduler().runTaskLater(skyNodes, () -> {
                if(player.isOnline() && chunk.isLoaded()) {
                    player.sendBlockChange(state.getLocation(), state.getBlockData());
                }
            }, 1L);
        }, 20L * delaySeconds);
    }
}
