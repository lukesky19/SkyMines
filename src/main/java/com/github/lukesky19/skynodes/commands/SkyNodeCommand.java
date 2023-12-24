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
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getDefaultGameMode;
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
        BukkitAudiences audiences = plugin.getAudiences();
        Logger logger = plugin.getLogger();
        switch (args.length) {
            case 1 -> {
                switch (args[0]) {
                    case "reload" -> {
                        if (sender instanceof Player) {
                            if (sender.hasPermission("skynodes.commands.reload")) {
                                try {
                                    plugin.reload();
                                    audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.reload()));
                                    return true;
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.noPermission()));
                                return false;
                            }
                        } else {
                            try {
                                plugin.reload();
                                logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(messages.reload()));
                                return true;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    case "help" -> {
                        if(sender instanceof Player) {
                            if (sender.hasPermission("skynodes.commands.help")) {
                                List<Component> helpMessage = messages.help();
                                for (Component msg : helpMessage) {
                                    audiences.player((Player) sender).sendMessage(msg);
                                }
                                return true;
                            } else {
                                audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.noPermission()));
                                return false;
                            }
                        } else {
                            List<Component> helpMessage = messages.help();
                            for (Component msg : helpMessage) {
                                logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(msg));
                            }
                            return true;
                        }
                    }
                    case "paste" -> {
                        if(sender instanceof Player) {
                            if (sender.hasPermission("skynodes.commands.paste")) {
                                audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.missingArgumentTaskId()));
                                return true;
                            } else {
                                audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.noPermission()));
                                return false;
                            }
                        } else {
                            logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(messages.inGameOnly()));
                            return false;
                        }
                    }
                    case "undo" -> {
                        if(sender instanceof Player) {
                            if (sender.hasPermission("skynodes.commands.undo")) {
                                schemMgr.undo((Player) sender);
                                return true;
                            } else {
                                audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.noPermission()));
                                return false;
                            }
                        } else {
                            logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(messages.inGameOnly()));
                            return false;
                        }
                    }
                    case "redo" -> {
                        if(sender instanceof Player) {
                            if (sender.hasPermission("skynodes.commands.redo")) {
                                schemMgr.redo((Player) sender);
                                return true;
                            } else {
                                audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.noPermission()));
                                return false;
                            }
                        } else {
                            logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(messages.inGameOnly()));
                            return false;
                        }
                    }
                    default -> {
                        if(sender instanceof Player) {
                            audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.unknownArgument()));
                            return false;
                        } else {
                            logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(messages.unknownArgument()));
                            return false;
                        }
                    }
                }
            }
            case 2 -> {
                if (args[0].equals("paste")) {
                    if(sender instanceof Player) {
                        if (sender.hasPermission("skynodes.commands.paste")) {
                            audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.missingArgumentNodeId()));
                            return true;
                        } else {
                            audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.noPermission()));
                            return false;
                        }
                    } else {
                        logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(messages.inGameOnly()));
                        return false;
                    }
                } else {
                    audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.unknownArgument()));
                    return false;
                }
            }
            case 3 -> {
                if (args[0].equals("paste")) {
                    if (sender instanceof Player) {
                        if (sender.hasPermission("skynodes.commands.paste")) {
                            for (SkyTask skyTask : taskMgr.getSkyTasksList()) {
                                if (args[1].equals(skyTask.taskId())) {
                                    for (SkyNode skyNode : skyTask.skyNodes()) {
                                        if (args[2].equals(skyNode.nodeId())) {
                                            try {
                                                schemMgr.paste(skyTask.taskId(), skyNode.nodeId(), skyNode.nodeWorld(), skyNode.blockVector3(), skyNode.nodeSchems(), skyNode.region(), skyNode.safeLocation(), (Player) sender);
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            } finally {
                                                audiences.player((Player) sender).sendMessage(messages.prefix().append(
                                                        mm.deserialize(messages.nodePasteSuccess(),
                                                                Placeholder.parsed("taskid", args[1]),
                                                                Placeholder.parsed("nodeid", args[2]))));
                                                return true;
                                            }
                                        }
                                    }
                                    audiences.player((Player) sender).sendMessage(messages.prefix().append(
                                            mm.deserialize(messages.invalidNodeId(),
                                                    Placeholder.parsed("nodeid", args[2]))));
                                    return false;
                                }
                            }
                            audiences.player((Player) sender).sendMessage(messages.prefix().append(
                                    mm.deserialize(messages.invalidTaskId(),
                                            Placeholder.parsed("taskid", args[1]))));
                            return false;
                        } else {
                            audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.noPermission()));
                            return false;
                        }
                    } else {
                        logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(messages.inGameOnly()));
                        return false;
                    }
                } else {
                    audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.unknownArgument()));
                    return false;
                }
            }
            default -> {
                if(sender instanceof Player) {
                    audiences.player((Player) sender).sendMessage(messages.prefix().append(messages.unknownArgument()));
                    return false;
                } else {
                    logger.log(Level.INFO, ANSIComponentSerializer.ansi().serialize(messages.unknownArgument()));
                    return false;
                }
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        BukkitAudiences audiences = plugin.getAudiences();
        Messages messages = msgsMgr.getMessages();
        switch(args.length) {
            case 1 -> {
                ArrayList<String> subCmds = new ArrayList<>();
                if (sender instanceof Player) {
                    if (sender.hasPermission("skynodes.commands.help")) {
                        subCmds.add("help");
                    }
                    if (sender.hasPermission("skynodes.commands.paste")) {
                        subCmds.add("paste");
                    }
                    if (sender.hasPermission("skynodes.commands.reload")) {
                        subCmds.add("reload");
                    }
                    if (sender.hasPermission("skynodes.commands.undo")) {
                        subCmds.add("undo");
                    }
                    if (sender.hasPermission("skynodes.commands.redo")) {
                        subCmds.add("redo");
                    }
                } else {
                    subCmds.add("help");
                    subCmds.add("reload");
                }
                return subCmds;
            }
            case 2 -> {
                if(args[0].equalsIgnoreCase("paste")) {
                    List<String> taskIds = new ArrayList<>();
                    if(sender instanceof Player) {
                        if(sender.hasPermission("skynodes.commands.paste")) {
                            for(SkyTask skyTask : taskMgr.getSkyTasksList()) {
                                taskIds.add(skyTask.taskId());
                            }
                        }
                    }
                    return taskIds;
                }
                return new ArrayList<>();
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("paste")) {
                    List<String> nodeIds = new ArrayList<>();
                    if(sender instanceof Player) {
                        if(sender.hasPermission("skynodes.commands.paste")) {
                            for(SkyTask skyTask : taskMgr.getSkyTasksList()) {
                                if(Objects.equals(skyTask.taskId(), args[1])) {
                                    List<SkyNode> nodesList = skyTask.skyNodes();
                                    for(SkyNode skyNode : nodesList) {
                                        nodeIds.add(skyNode.nodeId());
                                    }
                                }
                            }
                        }
                    }
                    return nodeIds;
                }
                return new ArrayList<>();
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }
}
