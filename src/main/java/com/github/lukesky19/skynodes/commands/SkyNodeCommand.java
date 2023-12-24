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

import com.github.lukesky19.skynodes.managers.MessagesManager;
import com.github.lukesky19.skynodes.managers.SkyTaskManager;
import com.github.lukesky19.skynodes.records.Messages;
import com.github.lukesky19.skynodes.managers.SchematicManager;
import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.records.SkyNode;
import com.github.lukesky19.skynodes.records.SkyTask;
import com.sk89q.worldedit.command.WorldEditCommands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
    final SkyNodes plugin;
    final MessagesManager msgsMgr;
    final SchematicManager schemMgr;
    final SkyTaskManager taskMgr;
    final MiniMessage mm = MiniMessage.miniMessage();
    public SkyNodeCommand(SkyNodes plugin) {
        this.plugin = plugin;
        msgsMgr = plugin.getMsgsMgr();
        schemMgr = plugin.getSchemMgr();
        taskMgr = plugin.getTaskMgr();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Messages messages = msgsMgr.getMessages();
        switch (args.length) {
            case 1 -> {
                switch (args[0]) {
                    case "reload" -> {
                        if (sender.hasPermission("skynodes.commands.reload")) {
                            try {
                                plugin.reload();
                                sender.sendMessage(messages.prefix().append(messages.reload()));
                                return true;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            sender.sendMessage(messages.prefix().append(messages.noPermission()));
                            return false;
                        }
                    }
                    case "help" -> {
                        if (sender.hasPermission("skynodes.commands.help")) {
                            List<Component> helpMessage = messages.help();
                            for (Component msg : helpMessage) {
                                sender.sendMessage(msg);
                            }
                            return true;
                        } else {
                            sender.sendMessage(messages.prefix().append(messages.noPermission()));
                            return false;
                        }
                    }
                    case "paste" -> {
                        if (sender.hasPermission("skynodes.commands.paste")) {
                            // Missing argument. Where is taskId?
                            return true;
                        }
                        return false;
                    }
                    case "undo" -> {
                        if (sender.hasPermission("skynodes.commands.undo")) {
                            schemMgr.undo((Player) sender);
                        }
                        return false;
                    }
                    case "redo" -> {
                        if (sender.hasPermission("skynodes.commands.redo")) {
                            schemMgr.redo((Player) sender);
                        }
                        return false;
                    }
                    default -> {
                        sender.sendMessage(messages.prefix().append(messages.unknownArgument()));
                        sender.sendMessage(mm.deserialize("Length: " + args.length + " argument: " + args[0]));
                        return false;
                    }
                }
            }
            case 2 -> {
                if (args[0].equals("paste")) {
                    if (sender.hasPermission("skynodes.commands.paste")) {
                        // Where is nodeId?
                        return true;
                    }
                    return false;
                } else {
                    sender.sendMessage(messages.prefix().append(messages.unknownArgument()));
                    sender.sendMessage(mm.deserialize("Length: " + args.length + " argument: " + args[0]));
                    return false;
                }
            }
            case 3 -> {
                if (args[0].equals("paste")) {
                    if (sender.hasPermission("skynodes.commands.paste")) {
                        List<SkyTask> tasksList = taskMgr.getSkyTasksList();
                        for (SkyTask skyTask : tasksList) {
                            if (args[1].equals(skyTask.taskId())) {
                                plugin.getComponentLogger().info(mm.deserialize("Tasks Match"));
                                List<SkyNode> nodesList = skyTask.skyNodes();
                                for (SkyNode skyNode : nodesList) {
                                    if (args[2].equals(skyNode.nodeId())) {
                                        plugin.getComponentLogger().info(mm.deserialize("Nodes Match"));
                                        try {
                                            schemMgr.paste(skyTask.taskId(), skyNode.nodeId(), skyNode.nodeWorld(), skyNode.blockVector3(), skyNode.nodeSchems(), skyNode.region(), skyNode.safeLocation(), (Player) sender);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        } finally {
                                            sender.sendMessage(messages.prefix().append(
                                                    mm.deserialize(messages.nodePasteSuccess(),
                                                            Placeholder.parsed("taskid", args[1]),
                                                            Placeholder.parsed("nodeid", args[2]))));
                                            return true;
                                        }
                                    }
                                }
                                sender.sendMessage(mm.deserialize(messages.invalidNodeId(),
                                        Placeholder.parsed("nodeid", args[2])));
                                return false;
                            }
                        }
                        sender.sendMessage(mm.deserialize(messages.invalidTaskId(),
                                Placeholder.parsed("taskid", args[1])));
                        return false;
                    } else {
                        sender.sendMessage(messages.prefix().append(messages.noPermission()));
                        return false;
                    }
                } else {
                    sender.sendMessage(messages.prefix().append(messages.unknownArgument()));
                    sender.sendMessage(mm.deserialize("Length: " + args.length + " argument: " + args[0]));
                    return false;
                }
            }
            default -> {
                sender.sendMessage(messages.prefix().append(messages.unknownArgument()));
                sender.sendMessage(mm.deserialize("Length: " + args.length + " argument: " + args[0]));
                return false;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = (Player) sender;
        SkyTask task;
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
                if(p.hasPermission("skynodes.commands.undo")) {
                    subCmds.add("undo");
                }
                if(p.hasPermission("skynodes.commands.redo")) {
                    subCmds.add("redo");
                }
                return subCmds;
            case 2:
                if (args[0].equalsIgnoreCase("paste")) {
                    if (p.hasPermission("skynodes.commands.paste")) {
                        List<String> taskIds = new ArrayList<>();
                        for (SkyTask skyTask : taskMgr.getSkyTasksList()) {
                            taskIds.add(skyTask.taskId());
                        }
                        return taskIds;
                    }
                }
            case 3:
                if (args[0].equalsIgnoreCase("paste")) {
                    if (p.hasPermission("skynodes.commands.paste")) {
                        List<String> nodeIds = new ArrayList<>();
                        for (SkyTask skyTask : taskMgr.getSkyTasksList()) {
                            if (Objects.equals(skyTask.taskId(), args[1])) {
                                List<SkyNode> nodesList = skyTask.skyNodes();
                                for (SkyNode skyNode : nodesList) {
                                    nodeIds.add(skyNode.nodeId());
                                }
                            }
                        }
                        return nodeIds;
                    }
                }
            default:
                return Collections.emptyList();
        }
    }
}
