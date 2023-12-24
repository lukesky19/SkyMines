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
import com.github.lukesky19.skynodes.records.Settings;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class SchematicManager {
    public SchematicManager(SkyNodes plugin) {
        this.plugin = plugin;
        msgsMgr = plugin.getMsgsMgr();
        settingsMgr = plugin.getSettingsMgr();
        logger = plugin.getComponentLogger();
    }
    final SkyNodes plugin;
    final MessagesManager msgsMgr;
    final SettingsManager settingsMgr;
    final ComponentLogger logger;
    final MiniMessage mm = MiniMessage.miniMessage();

    /**
     * Checks if the player is within a region, pastes a schematic, and saves the EditSession to a player's LocalSession.
     * @param taskId The id of the task which contains the node being pasted.
     * @param nodeId The id of the node being pasted.
     * @param world The World to paste the schematic in.
     * @param blockVector3 Location to paste the schematic.
     * @param schemList A list of schematics to choose from.
     * @param region The region the schematic is pasted in.
     * @param safeLocation The safe location to teleport any players in the region.
     * @param player The player to save the paste's EditSession to.
     * @return true if successful, false if it fails.
     */
    public void paste(String taskId, String nodeId, World world, BlockVector3 blockVector3, List<File> schemList, ProtectedRegion region, org.bukkit.Location safeLocation, Player player) {
        Messages configMessages = msgsMgr.getMessages();
        File file = schemList.get(new Random().nextInt(schemList.size()));
        // Prepare the clipboard.
        ClipboardReader reader = prepareClipboardReader(taskId, nodeId, file);
        // Prepare the EditSession
        EditSession session = prepareEditSession(world);
        // Prepare the Operation.
        Operation operation = prepareOperation(taskId, nodeId, reader, session, blockVector3);

        // Check for player in region before pasting.
        playerCheck(region, safeLocation);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Operations.complete(operation);
            } catch (WorldEditException e) {
                logger.error(configMessages.operationFailure());
                throw new RuntimeException(e);
            } finally {
                session.close();
                if (player != null) {
                    // Save the EditSession to the player's LocalSession
                    com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
                    SessionManager manager = WorldEdit.getInstance().getSessionManager();
                    LocalSession localSession = manager.get(actor);
                    localSession.remember(session);
                }
            }
        });
    }

    /**
     * Undos the last WorldEdit change.
     * @param player A Bukkit Player
     */
    public void undo(Player player) {
        Messages messages = msgsMgr.getMessages();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if(player != null) {
                // Get the player's last EditSession to undo.
                com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
                SessionManager manager = WorldEdit.getInstance().getSessionManager();
                LocalSession localSession = manager.get(actor);
                if (localSession != null) {
                    BlockBag blockBag = localSession.getBlockBag(actor);
                    EditSession undoSession = localSession.undo(blockBag, actor);
                    if(undoSession != null) {
                        localSession.remember(undoSession);
                        player.sendMessage(messages.prefix().append(messages.undo()));
                    } else {
                        player.sendMessage(messages.prefix().append(messages.noUndo()));
                    }
                }
            }
        });
    }

    /**
     * Redos the last WorldEdit change.
     * @param player A Bukkit Player
     */
    public void redo(Player player) {
        Messages messages = msgsMgr.getMessages();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (player != null) {
                try {
                    // Get the player's last EditSession to redo.
                    com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
                    SessionManager manager = WorldEdit.getInstance().getSessionManager();
                    LocalSession localSession = manager.get(actor);
                    if (localSession != null) {
                        BlockBag blockBag = localSession.getBlockBag(actor);
                        EditSession redoSession = localSession.redo(blockBag, actor);
                        if(redoSession != null) {
                            localSession.remember(redoSession);
                            player.sendMessage(messages.prefix().append(messages.redo()));
                        } else {
                            player.sendMessage(messages.prefix().append(messages.noRedo()));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private ClipboardReader prepareClipboardReader(String taskId, String nodeId, File file) {
        Messages messages = msgsMgr.getMessages();
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(file);
        ClipboardReader clipboardReader = null;
        try {
            clipboardReader = Objects.requireNonNull(clipboardFormat).getReader(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            logger.error(mm.deserialize(messages.schematicNotFound(),
                    Placeholder.parsed("taskid", taskId),
                    Placeholder.parsed("nodeid", nodeId)));
            logger.error(mm.deserialize(e.getMessage()));
        } catch (IOException e) {
            logger.error(mm.deserialize(messages.clipboardLoadFailure(),
                    Placeholder.parsed("taskid", taskId),
                    Placeholder.parsed("nodeid", nodeId)));
            logger.error(mm.deserialize(e.getMessage()));
        }
        return clipboardReader;
    }

    private EditSession prepareEditSession(org.bukkit.World world) {
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
        return WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build();
    }

    private Operation prepareOperation(String taskId, String nodeId, ClipboardReader clipboardReader, EditSession editSession, BlockVector3 blockVector3) {
        Messages messages = msgsMgr.getMessages();
        Operation operation;
        Clipboard clipboard = null;
        try {
            clipboard = clipboardReader.read();
        } catch (IOException e) {
            logger.error(mm.deserialize(messages.clipboardLoadFailure(),
                    Placeholder.parsed("taskid", taskId),
                    Placeholder.parsed("nodeid", nodeId)));
            logger.error(mm.deserialize(e.getMessage()));
        }
        operation = new ClipboardHolder(Objects.requireNonNull(clipboard))
                        .createPaste(editSession)
                        .to(blockVector3)
                        .ignoreAirBlocks(true)
                        .build();
        return operation;
    }

    private void playerCheck(ProtectedRegion skyNodeRegion, org.bukkit.Location safeLocation) {
        Messages messages = msgsMgr.getMessages();
        Settings settings = settingsMgr.getSettings();

        // Get a list of all online players.
        Collection<? extends Player> playerList = Bukkit.getOnlinePlayers();
        for (Player p : playerList) {
            // Get all regions the player is in
            Location loc = BukkitAdapter.adapt(p.getLocation());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(loc);

            // Check if player is in the region for the provided SkyNode.
            for (ProtectedRegion region : set) {
                if (Objects.equals(region, skyNodeRegion)) {
                    if (!p.hasPermission("skynodes.bypass.safeteleport")) {
                        p.teleport(safeLocation);
                    } else {
                        if (settings.debug() && p.hasPermission("skynodes.debug")) {
                            p.sendMessage(messages.prefix().append(messages.bypassedSafeTeleport()));
                        }
                    }
                }
            }
        }
    }
}
