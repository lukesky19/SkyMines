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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class SkyNodeCommand implements CommandExecutor, TabCompleter {

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // args[0] = paste
        // args[1] = schematic
        // args[2] = world
        // args[3,4,5] = coords
        if (label.equalsIgnoreCase("skynodes")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    SkyNodes.reload();
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>The plugin has been reloaded."));
                } else if(args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>Skynodes is developed by <white><bold>lukeskywlker19<reset><red>."));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><white><underlined><bold>https://github.com/lukesky19</click><reset><red>."));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] "));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>List of Commands:"));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <white>/<aqua>skynodes <red>paste <white><<yellow>schematic name<white>> <white><<yellow>world<white>> <white><<yellow>X<white>> <white><<yellow>Y<white>> <white><<yellow>Z<white>>"));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <white>/<aqua>skynodes <red>reload"));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <white>/<aqua>skynodes <red>help"));
                }
            } else if (args.length == 6) {
                if (args[0].equalsIgnoreCase("paste")) {
                    Location location = null;
                    if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                        File file = new File(Objects.requireNonNull(getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + args[1]);
                        if (file.exists()) {
                            World world = SkyNodes.getInstance().getServer().getWorld(args[2]);
                            if(world != null) {
                                location = new Location(
                                        world,
                                        Double.parseDouble(args[3]),
                                        Double.parseDouble(args[4]),
                                        Double.parseDouble(args[5]));
                            }
                            try {
                                SchematicLoader.paste(location, file);
                                sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>Node pasted successfully."));
                            } catch (Exception e) {
                                sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red><bold>AN ERROR HAS OCCURED:."));
                                throw new RuntimeException(e);
                            }
                            return true;
                        } else {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>The schematic name provided is invalid or doesn't exist."));
                            return false;
                        }
                    } else if (getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
                        File file = new File(Objects.requireNonNull(getServer().getPluginManager().getPlugin("FastAsyncWorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + args[1] + ".schem");
                        if (file.exists()) {
                            World world = SkyNodes.getInstance().getServer().getWorld(args[2]);
                            if(world != null) {
                                location = new Location(
                                        world,
                                        Double.parseDouble(args[3]),
                                        Double.parseDouble(args[4]),
                                        Double.parseDouble(args[5]));
                            }
                            try {
                                SchematicLoader.paste(location, file);
                                sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>Node pasted successfully."));
                            } catch (Exception e) {
                                sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red><bold>AN ERROR HAS OCCURED:."));
                                throw new RuntimeException(e);
                            }
                            return true;
                        } else {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>The schematic name provided is invalid or doesn't exist."));
                            return false;
                        }
                    } else {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>Worldedit or FastAsyncWorldEdit is not installed. Please install one."));
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // args[0] = paste
        // args[1] = schematic
        // args[2] = world
        // args[3,4,5] = coords
        if (label.equals("skynodes")) {
            Player p = (Player) sender;
            switch(args.length) {
                case 1:
                    return Arrays.asList("paste", "help", "reload");
                case 2:
                    if(args[0].equalsIgnoreCase("paste")) {
                        File directory = null;
                        List<String> schematicNames = new ArrayList<>();
                        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                            directory = new File(Objects.requireNonNull(getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder() + File.separator + "schematics");
                        } else if (getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
                            directory = new File(Objects.requireNonNull(getServer().getPluginManager().getPlugin("FastAsyncWorldEdit")).getDataFolder() + File.separator + "schematics");
                        }
                        for (final File fileEntry : Objects.requireNonNull(Objects.requireNonNull(directory).listFiles())) {
                            if (fileEntry.isFile()) {
                                schematicNames.add(fileEntry.getName());
                            }
                        }
                        return schematicNames;
                    } else {
                        return Collections.emptyList();
                    }
                case 3:
                    if(args[0].equalsIgnoreCase("paste")) {
                        List<World> worlds = SkyNodes.getInstance().getServer().getWorlds();
                        List<String> worldNames = new ArrayList<>();
                        for (World w : worlds) {
                            worldNames.add(w.getName());
                        }
                        return worldNames;
                    } else {
                        return Collections.emptyList();
                    }
                case 4:
                    if(args[0].equalsIgnoreCase("paste")) {
                        return List.of(String.valueOf(p.getLocation().getX()));
                    } else {
                        return Collections.emptyList();
                    }
                case 5:
                    if(args[0].equalsIgnoreCase("paste")) {
                        return List.of(String.valueOf(p.getLocation().getY()));
                    } else {
                        return Collections.emptyList();
                    }
                case 6:
                    if(args[0].equalsIgnoreCase("paste")) {
                        return List.of(String.valueOf(p.getLocation().getZ()));
                    } else {
                        return Collections.emptyList();
                    }
                default:
                    return Collections.emptyList();
            }
        }
        return null;
    }

}
