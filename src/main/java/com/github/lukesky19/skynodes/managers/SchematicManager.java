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

import com.github.lukesky19.skynodes.data.ConfigMessages;
import com.github.lukesky19.skynodes.data.ConfigSettings;
import com.github.lukesky19.skynodes.data.Node;
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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

import static com.github.lukesky19.skynodes.SkyNodes.getInstance;

public class SchematicManager {

    static final MiniMessage mm = MiniMessage.miniMessage();

    public static void pasteFromConfig(Node node) {
        ConfigMessages configMessages = ConfigManager.getConfigMessages();
        ConfigSettings configSettings = ConfigManager.getConfigSettings();
        File file = node.nodeSchems().get(new Random().nextInt(node.nodeSchems().size()));
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(file);
        BlockVector3 blockVector3 = BlockVector3.at(node.nodeX(), node.nodeY(), node.nodeZ());

        if (clipboardFormat != null) {
            try {
                ClipboardReader clipboardReader = clipboardFormat.getReader(new FileInputStream(file));
                com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(node.nodeWorld());
                EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build();
                Clipboard clipboard = clipboardReader.read();

                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(blockVector3)
                        .ignoreAirBlocks(true)
                        .build();

                // Check for online players in node region before pasting.
                Collection<? extends Player> playerList = Bukkit.getOnlinePlayers();
                for (Player p : playerList) {
                    Location loc = BukkitAdapter.adapt(p.getLocation());
                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionQuery query = container.createQuery();
                    ApplicableRegionSet set = query.getApplicableRegions(loc);

                    for (ProtectedRegion region : set) {
                        getInstance().getComponentLogger().info(mm.deserialize(region.getId()));
                        if (Objects.equals(region, node.region())) {
                            if (!p.hasPermission("skynodes.bypass.safeteleport")) {
                                p.teleport(node.safeLocation());
                            } else {
                                if (configSettings.debug()) {
                                        p.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.bypassedSafeTeleportMessage()));
                                }
                            }
                        }
                    }
                }

                // Paste the schematic
                try {
                    Operations.complete(operation);
                    editSession.close();
                } catch (WorldEditException e) {
                    getInstance().getComponentLogger().error(mm.deserialize(configMessages.operationFailureMessage()));
                    getInstance().getComponentLogger().error(mm.deserialize(e.getMessage()));
                }
            } catch (IOException e) {
                getInstance().getComponentLogger().error(mm.deserialize(configMessages.clipboardLoadFailureMessage()));
                getInstance().getComponentLogger().error(mm.deserialize(e.getMessage()));
            }
        }
    }

    public static void pasteFromCommand(org.bukkit.World world, int x, int y, int z, File file) {
        ConfigMessages configMessages = ConfigManager.getConfigMessages();
        ConfigSettings configSettings = ConfigManager.getConfigSettings();
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(file);
        BlockVector3 blockVector3 = BlockVector3.at(x, y, z);
        if (clipboardFormat != null) {
            try {
                ClipboardReader clipboardReader = clipboardFormat.getReader(new FileInputStream(file));
                com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
                EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build();
                Clipboard clipboard = clipboardReader.read();

                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(blockVector3)
                        .ignoreAirBlocks(true)
                        .build();

                // Paste the schematic
                try {
                    Operations.complete(operation);
                    editSession.close();
                } catch (WorldEditException e) {
                    getInstance().getComponentLogger().error(mm.deserialize(configMessages.operationFailureMessage()));
                    getInstance().getComponentLogger().error(mm.deserialize(e.getMessage()));
                }
            } catch (IOException e) {
                getInstance().getComponentLogger().error(mm.deserialize(configMessages.clipboardLoadFailureMessage()));
                getInstance().getComponentLogger().error(mm.deserialize(e.getMessage()));
            }
        }
    }
}
