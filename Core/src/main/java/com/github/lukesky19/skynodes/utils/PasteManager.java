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
package com.github.lukesky19.skynodes.utils;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.configuration.config.ConfigValidator;
import com.github.lukesky19.skynodes.configuration.config.ParsedConfig;
import com.github.lukesky19.skynodes.configuration.locale.LocaleManager;
import com.github.lukesky19.skynodes.configuration.settings.SettingsManager;
import com.github.lukesky19.skynodes.configuration.locale.FormattedLocale;
import com.github.lukesky19.skynodes.configuration.settings.SettingsConfiguration;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class PasteManager {
    final SkyNodes skyNodes;
    final LocaleManager localeManager;
    final SettingsManager settingsManager;
    final ConfigValidator configValidator;
    final SchedulerUtility schedulerUtility;
    final MiniMessage mm = MiniMessage.miniMessage();

    public PasteManager(
            SkyNodes skyNodes,
            LocaleManager localeManager,
            SettingsManager settingsManager,
            ConfigValidator configValidator,
            SchedulerUtility schedulerUtility) {
        this.skyNodes = skyNodes;
        this.localeManager = localeManager;
        this.settingsManager = settingsManager;
        this.configValidator = configValidator;
        this.schedulerUtility = schedulerUtility;
    }

    /**
     * Checks if the player is within a region, pastes a schematic, and saves the EditSession to a player's LocalSession.
     *
     * @param taskId The identifier for a task.
     * @param nodeId The identifier for a node.
     * @param node The config for the node being pasted.
     * @param player The player to teleport to safety if in the pasting area, or null of not checking.
     */
    public void paste(String taskId, String nodeId, ParsedConfig.SkyNode node, Player player) {
        ComponentLogger logger = skyNodes.getComponentLogger();
        FormattedLocale messages = localeManager.formattedLocale();

        World nodeWorld = configValidator.verifyWorld(taskId, nodeId, node.nodeWorld());
        if (nodeWorld == null) {
            logger.error(
                    MiniMessage.miniMessage().deserialize(messages.nodePasteFailure(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId)));
            logger.info(messages.softDisable());
            schedulerUtility.stopTasks();
            return;
        }

        BlockVector3 vector3 = node.vector3List().get(new Random().nextInt(node.vector3List().size()));

        List<File> schematicsList = configValidator.verifySchematics(taskId, nodeId, node.nodeSchems());
        if (schematicsList == null) {
            logger.error(
                    MiniMessage.miniMessage().deserialize(messages.nodePasteFailure(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId)));
            logger.info(messages.softDisable());
            schedulerUtility.stopTasks();
            return;
        }
        File schematic = schematicsList.get(new Random().nextInt(schematicsList.size()));

        ProtectedRegion region = configValidator.verifyRegion(taskId, nodeId, node.region(), nodeWorld);
        if (region == null) {
            logger.error(
                    MiniMessage.miniMessage().deserialize(messages.nodePasteFailure(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId)));
            logger.info(messages.softDisable());
            schedulerUtility.stopTasks();
            return;
        }

        Clipboard clipboard = loadClipboard(schematic, taskId, nodeId);
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(nodeWorld);
        completeOperation(clipboard, weWorld, vector3, region, node.safeLocation(), player);
    }

    /**
     * Undos the last WorldEdit change.
     * @param player A Bukkit Player
     */
    public void undo(Player player) {
        FormattedLocale messages = localeManager.formattedLocale();

        if (player != null) {
            com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
            SessionManager manager = WorldEdit.getInstance().getSessionManager();
            LocalSession localSession = manager.get(actor);
            if (localSession != null) {
                BlockBag blockBag = localSession.getBlockBag(actor);
                Bukkit.getScheduler().runTaskAsynchronously(skyNodes, () -> {
                    EditSession undoSession = localSession.undo(blockBag, actor);
                    if (undoSession != null) {
                        localSession.remember(undoSession);
                        player.sendMessage(messages.prefix().append(messages.undo()));
                    } else {
                        player.sendMessage(messages.prefix().append(messages.noUndo()));
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
        FormattedLocale messages = localeManager.formattedLocale();

        Bukkit.getScheduler().runTaskAsynchronously(skyNodes, () -> {
            if (player != null) {
                try {
                    // Get the player's last EditSession to redo.
                    com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
                    SessionManager manager = WorldEdit.getInstance().getSessionManager();
                    LocalSession localSession = manager.get(actor);
                    if (localSession != null) {
                        BlockBag blockBag = localSession.getBlockBag(actor);
                        EditSession redoSession = localSession.redo(blockBag, actor);
                        if (redoSession != null) {
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

    private Clipboard loadClipboard(File schematic, String taskId, String nodeId) {
        ComponentLogger logger = skyNodes.getComponentLogger();
        FormattedLocale messages = localeManager.formattedLocale();

        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        try (ClipboardReader reader = Objects.requireNonNull(format).getReader(new FileInputStream(schematic))) {
            clipboard = reader.read();
        } catch (IOException e) {
            logger.error(
                    mm.deserialize(messages.clipboardLoadFailure(),
                            Placeholder.parsed("taskid", taskId),
                            Placeholder.parsed("nodeid", nodeId)));
            throw new RuntimeException(e);
        }
        return clipboard;
    }

    private void completeOperation(Clipboard clipboard, com.sk89q.worldedit.world.World world, BlockVector3 blockVector3, ProtectedRegion region, org.bukkit.Location safeLocation, Player player) {
        ComponentLogger logger = skyNodes.getComponentLogger();

        if (player != null) {
            com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
            Bukkit.getScheduler().runTaskAsynchronously(skyNodes, () -> {
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
                    logger.info(localeManager.formattedLocale().operationFailure());
                    throw new RuntimeException(e);
                }
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(skyNodes, () -> {
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
                    logger.error(localeManager.formattedLocale().operationFailure());
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
        FormattedLocale messages = localeManager.formattedLocale();
        SettingsConfiguration settingsConfiguration = settingsManager.getSettings();

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
                        if (settingsConfiguration.debug() && p.hasPermission("skynodes.debug")) {
                            p.sendMessage(messages.prefix().append(messages.bypassedSafeTeleport()));
                        }
                    }
                }
            }
        }
    }
}
