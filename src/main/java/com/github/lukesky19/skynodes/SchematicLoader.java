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
package com.github.lukesky19.skynodes;

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
import com.sk89q.worldedit.world.World;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.github.lukesky19.skynodes.SkyNodes.getSkyNodesLogger;

public class SchematicLoader {

    public static void paste(Location location, File file) {
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(file);
        Clipboard clipboard;
        BlockVector3 blockVector3 = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (clipboardFormat != null) {
            try (ClipboardReader clipboardReader = clipboardFormat.getReader(new FileInputStream(file))) {
                World world = BukkitAdapter.adapt(location.getWorld());
                EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).build();
                clipboard = clipboardReader.read();

                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(blockVector3)
                        .ignoreAirBlocks(true)
                        .build();
                try {
                    Operations.complete(operation);
                    editSession.close();
                } catch (WorldEditException e) {
                    getSkyNodesLogger().error(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>The operation failed to complete."));
                    getSkyNodesLogger().error(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] " + e.getCause()));
                }
            } catch (IOException e) {
                getSkyNodesLogger().error(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>Unable to load to clipboard."));
                getSkyNodesLogger().error(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] " + e.getCause()));
            }
        }
    }
}
