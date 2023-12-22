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
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
     * Checks if the player is within a region and pastes a schematic.
     * @param skyNode A SkyNode object.
     */
    public void pasteFromConfig(World world, BlockVector3 blockVector3, List<File> schemList, ProtectedRegion region, org.bukkit.Location safeLocation) {
        Messages configMessages = msgsMgr.getMessages();
        File file = schemList.get(new Random().nextInt(schemList.size()));
        // Prepare the clipboard.
        ClipboardReader reader = prepareClipboardReader(file);
        // Prepare the EditSession
        EditSession session = prepareEditSession(world);
        // Prepare the Operation.
        Operation operation = prepareOperation(reader, session, blockVector3);

        // Check for player in region before pasting.
        playerCheck(region, safeLocation);

        // Attempt to paste the schematic/node.
        try {
            Operations.complete(operation);
            session.close();
        } catch (WorldEditException e) {
            logger.error(configMessages.operationFailure());
            logger.error(mm.deserialize(e.getMessage()));
        }
    }

    /**
     * Pastes a schematic based on a World, X, Y, and Z coordinates, and a schematic File.
     * @param world A Bukkit World.
     * @param x A X coordinate.
     * @param y A Y coordinate.
     * @param z A Z coordinate.
     * @param file A schematic file.
     */
    public void pasteFromCommand(org.bukkit.World world, int x, int y, int z, File file) {
        Messages messages = msgsMgr.getMessages();
        BlockVector3 blockVector3 = BlockVector3.at(x, y, z);
        ClipboardReader reader = prepareClipboardReader(file);
        EditSession session = prepareEditSession(world);
        Operation operation = prepareOperation(reader, session, blockVector3);

        try {
            Operations.complete(operation);
            session.close();
        } catch (WorldEditException e) {
            logger.error(messages.operationFailure());
            logger.error(mm.deserialize(e.getMessage()));
        }
    }

    private ClipboardReader prepareClipboardReader(File file) {
        Messages messages = msgsMgr.getMessages();
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(file);
        ClipboardReader clipboardReader = null;
        try {
            clipboardReader = Objects.requireNonNull(clipboardFormat).getReader(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            logger.error(messages.consoleSchematicNotFound());
            logger.error(mm.deserialize(e.getMessage()));
        } catch (IOException e) {
            logger.error(messages.clipboardLoadFailure());
            logger.error(mm.deserialize(e.getMessage()));
        }
        return clipboardReader;
    }

    private EditSession prepareEditSession(org.bukkit.World world) {
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
        return WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build();
    }

    private Operation prepareOperation(ClipboardReader clipboardReader, EditSession editSession, BlockVector3 blockVector3) {
        Messages messages = msgsMgr.getMessages();
        Operation operation;
        Clipboard clipboard = null;
        try {
            clipboard = clipboardReader.read();
        } catch (IOException e) {
            logger.error(messages.clipboardLoadFailure());
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
                        if (settings.debug()) {
                            p.sendMessage(messages.prefix().append(messages.bypassedSafeTeleport()));
                        }
                    }
                }
            }
        }
    }
}
