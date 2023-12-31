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
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
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
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SchematicManager {
    public SchematicManager(SkyNodes plugin, MessagesManager messagesManager, SettingsManager settingsManager) {
        this.plugin = plugin;
        this.messagesManager = messagesManager;
        this.settingsManager = settingsManager;
    }

    final SkyNodes plugin;
    final MessagesManager messagesManager;
    final SettingsManager settingsManager;
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
        Messages configMessages = messagesManager.getMessages();
        Logger logger = plugin.getLogger();
        File file = schemList.get(new Random().nextInt(schemList.size()));

        Clipboard clipboard = loadClipboard(file, taskId, nodeId);
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
        completeOperation(clipboard, weWorld, blockVector3, region, safeLocation, player);
    }

    /**
     * Undos the last WorldEdit change.
     * @param player A Bukkit Player
     */
    public void undo(Player player) {
        Messages messages = messagesManager.getMessages();
        BukkitAudiences audiences = plugin.getAudiences();

        if(player != null) {
            com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
            SessionManager manager = WorldEdit.getInstance().getSessionManager();
            LocalSession localSession = manager.get(actor);
            if (localSession != null) {
                BlockBag blockBag = localSession.getBlockBag(actor);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    EditSession undoSession = localSession.undo(blockBag, actor);
                    if(undoSession != null) {
                        localSession.remember(undoSession);
                        audiences.player(player).sendMessage(messages.prefix().append(messages.undo()));
                    } else {
                        audiences.player(player).sendMessage(messages.prefix().append(messages.noUndo()));
                    }
                });
            }
        }
    }

    /**
     * Redos the last WorldEdit change.
     * @param player A Bukkit Player
     */
    public void redo(Player player) {
        Messages messages = messagesManager.getMessages();
        BukkitAudiences audiences = plugin.getAudiences();

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
                            audiences.player(player).sendMessage(messages.prefix().append(messages.redo()));
                        } else {
                            audiences.player(player).sendMessage(messages.prefix().append(messages.noRedo()));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private Clipboard loadClipboard(File schematic, String taskId, String nodeId) {
        Messages messages = messagesManager.getMessages();

        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        try(ClipboardReader reader = format.getReader(new FileInputStream(schematic))) {
            clipboard = reader.read();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                    mm.deserialize(messages.clipboardLoadFailure(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId))));
            throw new RuntimeException(e);
        }
        return clipboard;
    }

    private void completeOperation(Clipboard clipboard, com.sk89q.worldedit.world.World world, BlockVector3 blockVector3, ProtectedRegion region, org.bukkit.Location safeLocation, Player player) {
        Messages messages = messagesManager.getMessages();
        if(player != null) {
            com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).actor(actor).build()) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(blockVector3)
                            .ignoreAirBlocks(true)
                            .build();

                    // Check for player in region before pasting.
                    playerCheck(region, safeLocation);

                    Operations.complete(operation);

                    saveEditSession(editSession, player);
                } catch (WorldEditException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(blockVector3)
                            .ignoreAirBlocks(true)
                            .build();

                    // Check for player in region before pasting.
                    playerCheck(region, safeLocation);

                    Operations.complete(operation);
                } catch (WorldEditException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void saveEditSession(EditSession editSession, Player player) {
        if (player != null) {
            // Save the EditSession to the player's LocalSession
            com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
            SessionManager manager = WorldEdit.getInstance().getSessionManager();
            LocalSession localSession = manager.get(actor);
            localSession.remember(editSession);
        }
    }

    private void playerCheck(ProtectedRegion skyNodeRegion, org.bukkit.Location safeLocation) {
        Messages messages = messagesManager.getMessages();
        Settings settings = settingsManager.getSettings();
        BukkitAudiences audiences = plugin.getAudiences();

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
                            audiences.player(p).sendMessage(messages.prefix().append(messages.bypassedSafeTeleport()));
                        }
                    }
                }
            }
        }
    }
}
