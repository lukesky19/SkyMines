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

import com.github.lukesky19.skynodes.data.ConfigMessages;
import com.github.lukesky19.skynodes.managers.ConfigManager;
import com.github.lukesky19.skynodes.managers.SchematicManager;
import com.github.lukesky19.skynodes.SkyNodes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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
    static final MiniMessage mm = MiniMessage.miniMessage();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ConfigMessages configMessages = ConfigManager.getConfigMessages();
        if(args.length == 1) {
            switch(args[0]) {
                case "reload":
                    if(sender.hasPermission("skynodes.commands.reload")) {
                        try {
                            SkyNodes.reloadConfigFiles();
                            SkyNodes.reloadTasks();
                            sender.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.reloadMessage()));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        sender.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.noPermissionMessage()));
                    }
                    break;
                case "help":
                    if(sender.hasPermission("skynodes.commands.help")) {
                        List<String> helpMessage = configMessages.helpMessage();
                        for(String msg : helpMessage) {
                            sender.sendMessage(mm.deserialize(msg));
                        }
                    } else {
                        sender.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.noPermissionMessage()));
                    }
                    break;
                default:
                    sender.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.unknownArgumentMessage()));
                    break;
            }
        } else if(args.length == 6) {
            if(sender.hasPermission("skynodes.commands.paste")) {
                if (Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                    File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + args[1]);
                    if (file.exists()) {
                        try {
                            SchematicManager.pasteFromCommand(Bukkit.getServer().getWorld(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), file);
                            sender.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.playerNodePasteSuccessMessage()));
                            return true;
                        } catch (Exception e) {
                            sender.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.playerNodePasteFailureMessage()));
                            return false;
                        }
                    } else {
                        sender.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.playerSchematicNotFoundMessage()));
                        return false;
                    }
                }

                File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + args[1]);
                if(file.exists()) {
                    try {
                        SchematicManager.pasteFromCommand(Bukkit.getServer().getWorld(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), file);
                        sender.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.playerNodePasteSuccessMessage()));
                        return true;
                    } catch (Exception e) {
                        sender.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.playerNodePasteFailureMessage()));
                        return false;
                    }
                }
            } else {
                sender.sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.noPermissionMessage()));
                return false;
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = (Player) sender;
        switch (args.length) {
            case 1:
                ArrayList<String> subCmds = new ArrayList<>();
                if(p.hasPermission("skynodes.commands.help")) {
                    subCmds.add("help");
                }
                if(p.hasPermission("skynodes.commands.paste")) {
                    subCmds.add("paste");
                }
                if(p.hasPermission("skynodes.commands.reload")) {
                    subCmds.add("reload");
                }
                return subCmds;
            case 2:
                if (args[0].equalsIgnoreCase("paste")) {
                    if (p.hasPermission("skynodes.commands.paste")) {
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
                }
            case 3:
                if (args[0].equalsIgnoreCase("paste")) {
                    if (p.hasPermission("skynodes.commands.paste")) {
                        List<World> worlds = SkyNodes.getInstance().getServer().getWorlds();
                        List<String> worldNames = new ArrayList<>();
                        for (World w : worlds) {
                            worldNames.add(w.getName());
                        }
                        return worldNames;
                    } else {
                        return Collections.emptyList();
                    }
                }
            case 4:
                if (args[0].equalsIgnoreCase("paste")) {
                    if (p.hasPermission("skynodes.commands.paste")) {
                        return List.of(String.valueOf((int) p.getLocation().getX()));
                    } else {
                        return Collections.emptyList();
                    }
                }
            case 5:
                if (args[0].equalsIgnoreCase("paste")) {
                    if (p.hasPermission("skynodes.commands.paste")) {
                        return List.of(String.valueOf((int) p.getLocation().getY()));
                    } else {
                        return Collections.emptyList();
                    }
                }
            case 6:
                if (args[0].equalsIgnoreCase("paste")) {
                    if (p.hasPermission("skynodes.commands.paste")) {
                        return List.of(String.valueOf((int) p.getLocation().getZ()));
                    } else {
                        return Collections.emptyList();
                    }
                }
            default:
                return Collections.emptyList();
        }
    }
}
