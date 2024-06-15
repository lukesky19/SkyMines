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

import com.github.lukesky19.skynodes.configuration.config.ParsedConfig;
import com.github.lukesky19.skynodes.configuration.config.ConfigManager;
import com.github.lukesky19.skynodes.configuration.locale.LocaleManager;
import com.github.lukesky19.skynodes.configuration.locale.FormattedLocale;
import com.github.lukesky19.skynodes.configuration.settings.SettingsManager;
import com.github.lukesky19.skynodes.utils.PasteManager;
import com.github.lukesky19.skynodes.SkyNodes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class SkyNodeCommand implements CommandExecutor, TabCompleter {
    final SkyNodes skyNodes;
    final SettingsManager settingsManager;
    final LocaleManager localeManager;
    final PasteManager pasteManager;
    final ConfigManager configManager;
    final MiniMessage mm = MiniMessage.miniMessage();
    public SkyNodeCommand(
            SkyNodes skyNodes,
            SettingsManager settingsManager,
            LocaleManager localeManager,
            PasteManager pasteManager,
            ConfigManager configManager) {
        this.skyNodes = skyNodes;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.pasteManager = pasteManager;
        this.configManager = configManager;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FormattedLocale messages = localeManager.formattedLocale();
        ComponentLogger logger = skyNodes.getComponentLogger();

        switch(args.length) {
            case 1 -> {
                switch(args[0]) {
                    case "help" -> {
                        if(sender instanceof Player player) {
                            if(sender.hasPermission("skynodes.commands.help")) {
                                if(!skyNodes.isPluginEnabled()) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }

                                for (Component msg : messages.help()) {
                                    player.sendMessage(msg);
                                }
                                return true;
                            } else {
                                if(!skyNodes.isPluginEnabled()) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }

                                player.sendMessage(messages.prefix().append(messages.noPermission()));
                                return false;
                            }
                        } else {
                            if(!skyNodes.isPluginEnabled()) {
                                logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                return false;
                            }

                            for (Component msg : messages.help()) {
                                logger.info(msg);
                            }
                            return true;
                        }
                    }

                    case "reload" -> {
                        if(sender instanceof Player player) {
                            if(sender.hasPermission("skynodes.commands.reload")) {
                                skyNodes.reload();
                                if(!skyNodes.isPluginEnabled()) {
                                    logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin has been soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }
                                player.sendMessage(messages.prefix().append(messages.reload()));
                                return true;
                            } else {
                                if(!skyNodes.isPluginEnabled()) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }

                                player.sendMessage(messages.prefix().append(messages.noPermission()));
                                return false;
                            }
                        } else {
                            skyNodes.reload();
                            if(!skyNodes.isPluginEnabled()) {
                                logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin has been soft-disabled due to a configuration error.</red>"));
                                return false;
                            }

                            logger.warn(messages.reload());
                            return true;
                        }
                    }

                    case "paste" -> {
                        if(sender instanceof Player player) {
                            if(sender.hasPermission("skynodes.commands.paste")) {
                                if(!skyNodes.isPluginEnabled()) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }

                                player.sendMessage(messages.prefix().append(messages.missingArgumentTaskId()));
                                return true;
                            } else {
                                if(!skyNodes.isPluginEnabled()) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }

                                player.sendMessage(messages.prefix().append(messages.noPermission()));
                                return false;
                            }
                        } else {
                            if(!skyNodes.isPluginEnabled()) {
                                logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                return false;
                            }

                            logger.info(messages.inGameOnly());
                            return false;
                        }
                    }

                    case "undo" -> {
                        if(sender instanceof Player player) {
                            if (sender.hasPermission("skynodes.commands.undo")) {
                                if(!skyNodes.isPluginEnabled()) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }

                                pasteManager.undo((Player) sender);
                                return true;
                            } else {
                                if(!skyNodes.isPluginEnabled()) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }

                                player.sendMessage(messages.prefix().append(messages.noPermission()));
                                return false;
                            }
                        } else {
                            if(!skyNodes.isPluginEnabled()) {
                                logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                return false;
                            }

                            logger.info(messages.inGameOnly());
                            return false;
                        }
                    }

                    case "redo" -> {
                        if(sender instanceof Player player) {
                            if (sender.hasPermission("skynodes.commands.redo")) {
                                if(!skyNodes.isPluginEnabled()) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }

                                pasteManager.redo((Player) sender);
                                return true;
                            } else {
                                if(!skyNodes.isPluginEnabled()) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }

                                player.sendMessage(messages.prefix().append(messages.noPermission()));
                                return false;
                            }
                        } else {
                            if(!skyNodes.isPluginEnabled()) {
                                logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                return false;
                            }

                            logger.info(messages.inGameOnly());
                            return false;
                        }
                    }

                    default -> {
                        if(sender instanceof Player player) {
                            if(!skyNodes.isPluginEnabled()) {
                                logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return false;
                            }

                            player.sendMessage(messages.prefix().append(messages.unknownArgument()));
                        } else {
                            if(!skyNodes.isPluginEnabled()) {
                                logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                return false;
                            }

                            logger.info(messages.unknownArgument());
                        }
                        return false;
                    }
                }
            }

            case 2 -> {
                if(args[0].equals("paste")) {
                    if(sender instanceof Player player) {
                        if (sender.hasPermission("skynodes.commands.paste")) {
                            if(!skyNodes.isPluginEnabled()) {
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return false;
                            }

                            player.sendMessage(messages.prefix().append(messages.missingArgumentNodeId()));
                            return true;
                        } else {
                            if(!skyNodes.isPluginEnabled()) {
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return false;
                            }

                            player.sendMessage(messages.prefix().append(messages.noPermission()));
                            return false;
                        }
                    } else {
                        if(!skyNodes.isPluginEnabled()) {
                            logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                            return false;
                        }

                        logger.info(messages.inGameOnly());
                        return false;
                    }
                } else {
                    if(sender instanceof Player player) {
                        if(!skyNodes.isPluginEnabled()) {
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                            return false;
                        }

                        player.sendMessage(messages.prefix().append(messages.unknownArgument()));
                    } else {
                        if(!skyNodes.isPluginEnabled()) {
                            logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                            return false;
                        }

                        logger.info(messages.unknownArgument());
                    }
                    return false;
                }
            }

            case 3 -> {
                if(args[0].equals("paste")) {
                    if(sender instanceof Player player) {
                        if(sender.hasPermission("skynodes.commands.paste")) {
                            if(!skyNodes.isPluginEnabled()) {
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return false;
                            }

                            for(Map.Entry<String, ParsedConfig.SkyTask> skyTaskEntry : configManager.getConfiguration().tasks().entrySet()) {
                                String taskId = skyTaskEntry.getKey();

                                if(args[1].equals(taskId)) {
                                    for(Map.Entry<String, ParsedConfig.SkyNode> skyNodeEntry : skyTaskEntry.getValue().skyNodes().entrySet()) {
                                        ParsedConfig.SkyNode skyNode = skyNodeEntry.getValue();
                                        String nodeId = skyNodeEntry.getKey();

                                        if(args[2].equals(nodeId)) {
                                            try {
                                                pasteManager.paste(taskId, nodeId, skyNode, player);
                                            } finally {
                                                if (settingsManager.getSettings().debug()) {
                                                    logger.info(
                                                            MiniMessage.miniMessage().deserialize(localeManager.formattedLocale().nodePasteSuccess(),
                                                                    Placeholder.parsed("taskid", taskId),
                                                                    Placeholder.parsed("nodeid", nodeId)));
                                                }
                                            }
                                            return true;
                                        }
                                    }

                                    player.sendMessage(messages.prefix().append(
                                            mm.deserialize(messages.invalidNodeId(),
                                                    Placeholder.parsed("nodeid", args[2]))));
                                    return false;
                                }
                            }

                            player.sendMessage(messages.prefix().append(
                                    mm.deserialize(messages.invalidTaskId(),
                                            Placeholder.parsed("taskid", args[1]))));
                            return false;
                        } else {
                            if(!skyNodes.isPluginEnabled()) {
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return false;
                            }

                            player.sendMessage(messages.prefix().append(messages.noPermission()));
                            return false;
                        }
                    } else {
                        if(!skyNodes.isPluginEnabled()) {
                            logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                            return false;
                        }

                        logger.info(messages.inGameOnly());
                        return false;
                    }
                } else {
                    if(sender instanceof Player player) {
                        if(!skyNodes.isPluginEnabled()) {
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                            return false;
                        }

                        player.sendMessage(messages.prefix().append(messages.unknownArgument()));
                        return false;
                    }  else {
                        if(!skyNodes.isPluginEnabled()) {
                            logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                            return false;
                        }

                        logger.info(messages.unknownArgument());
                    }
                }
            }

            default -> {
                if(sender instanceof Player player) {
                    if(!skyNodes.isPluginEnabled()) {
                        logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                        return false;
                    }

                    player.sendMessage(messages.prefix().append(messages.unknownArgument()));
                } else {
                    if(!skyNodes.isPluginEnabled()) {
                        logger.warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                        return false;
                    }

                    logger.info(messages.unknownArgument());
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
                    if(sender instanceof Player player) {
                        if(sender.hasPermission("skynodes.commands.paste")) {
                            if(!skyNodes.isPluginEnabled()) {
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return new ArrayList<>();
                            }

                            for(Map.Entry<String, ParsedConfig.SkyTask> skyTask : configManager.getConfiguration().tasks().entrySet()) {
                                taskIds.add(skyTask.getKey());
                            }
                        }
                    } else {
                        if(!skyNodes.isPluginEnabled()) {
                            skyNodes.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                            return new ArrayList<>();
                        }

                        for(Map.Entry<String, ParsedConfig.SkyTask> skyTask : configManager.getConfiguration().tasks().entrySet()) {
                            taskIds.add(skyTask.getKey());
                        }
                    }
                    return taskIds;
                }
                return new ArrayList<>();
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("paste")) {
                    List<String> nodeIds = new ArrayList<>();
                    if(sender instanceof Player player) {
                        if(sender.hasPermission("skynodes.commands.paste")) {
                            if(!skyNodes.isPluginEnabled()) {
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray><aqua>SkyShop</aqua><gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return new ArrayList<>();
                            }

                            for(Map.Entry<String, ParsedConfig.SkyTask> skyTask : configManager.getConfiguration().tasks().entrySet()) {
                                if(Objects.equals(args[1], skyTask.getKey())) {
                                    for(Map.Entry<String, ParsedConfig.SkyNode> skyNode : skyTask.getValue().skyNodes().entrySet()) {
                                        nodeIds.add(skyNode.getKey());
                                    }
                                }
                            }
                        }
                    } else {
                        if(!skyNodes.isPluginEnabled()) {
                            skyNodes.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                            return new ArrayList<>();
                        }

                        for(Map.Entry<String, ParsedConfig.SkyTask> skyTask : configManager.getConfiguration().tasks().entrySet()) {
                            if(Objects.equals(args[1], skyTask.getKey())) {
                                for(Map.Entry<String, ParsedConfig.SkyNode> skyNode : skyTask.getValue().skyNodes().entrySet()) {
                                    nodeIds.add(skyNode.getKey());
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
