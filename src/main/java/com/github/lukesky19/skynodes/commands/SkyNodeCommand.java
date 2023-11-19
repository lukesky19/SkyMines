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
package com.github.lukesky19.skynodes.commands;

import com.github.lukesky19.skynodes.SchematicLoader;
import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.util.ConfigRecord;
import java.io.File;
import java.util.Objects;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;


public class SkyNodeCommand implements CommandExecutor {
    final CommentedConfigurationNode nodeConfig = ConfigRecord.getConfig().nodeConfig();
   
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
       if (label.equals("skynodes")) {
           if (args.length == 1) {
               if (args[0].equals("reload")) {
                   SkyNodes.reload();
               }
           } else if (args.length == 6 && args[0].equals("paste")) {
               int i = 0;
               while (!nodeConfig.node("nodes", i).virtual()) {
                   if (Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                       File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder() + File.separator + Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder() + "schematics" + File.separator + args[1] + ".schem");
                       if (file.exists()) {
                           Location location = new Location(Bukkit.getWorld(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
                           SchematicLoader.paste(location, file);
                           return true;
                       } else {
                           sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>The schematic name provided is invalid or doesn't exist."));
                           return false;
                       }
                   } else if (Bukkit.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
                       File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit")).getDataFolder() + File.separator + Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit")).getDataFolder() + "schematics" + File.separator + args[1] + ".schem");
                       if (file.exists()) {
                           Location location = new Location(Bukkit.getWorld(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
                           SchematicLoader.paste(location, file);
                           return true;
                       }
                       sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>The schematic name provided is invalid or doesn't exist."));
                       return false;
                   }
                   i++;
               }
           }
       }
       return false;
   }
}