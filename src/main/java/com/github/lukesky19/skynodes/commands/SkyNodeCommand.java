/*
    SkyNodes tracks blocks broken in specific regions (nodes), replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2024  lukeskywlker19

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

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.configuration.loader.LocaleLoader;
import com.github.lukesky19.skynodes.configuration.record.Locale;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SkyNodeCommand implements CommandExecutor, TabCompleter {
    final SkyNodes skyNodes;
    final LocaleLoader localeLoader;
    final MiniMessage mm = MiniMessage.miniMessage();
    public SkyNodeCommand(
            SkyNodes skyNodes,
            LocaleLoader localeLoader) {
        this.skyNodes = skyNodes;
        this.localeLoader = localeLoader;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Locale locale  = localeLoader.getLocale();
        ComponentLogger logger = skyNodes.getComponentLogger();

        if(!skyNodes.isPluginEnabled()) {
            if(args[0].equalsIgnoreCase("reload")) {
                if(sender instanceof Player player) {
                    if(player.hasPermission("skynodes.commands." + args[0])) {
                        skyNodes.reload();
                        if(!skyNodes.isPluginEnabled()) {
                            sender.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>The plugin was reloaded, but is still soft-disabled due to a configuration error.</red>"));
                            return false;
                        } else {
                            locale = localeLoader.getLocale();
                            sender.sendMessage(mm.deserialize(locale.prefix() + locale.reload()));
                            return true;
                        }
                    } else {
                        if(!skyNodes.isPluginEnabled()) {
                            sender.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>You do not have permission for this command.</red>"));
                            sender.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                            return false;
                        } else {
                            locale = localeLoader.getLocale();
                            sender.sendMessage(mm.deserialize(locale.prefix() + locale.noPermission()));
                            return true;
                        }
                    }
                } else {
                    skyNodes.reload();
                    if (!skyNodes.isPluginEnabled()) {
                        logger.warn(mm.deserialize("<red>The plugin was reloaded, but is still soft-disabled due to a configuration error.</red>"));
                        return false;
                    } else {
                        logger.info(mm.deserialize(locale.reload()));
                        return true;
                    }
                }
            } else {
                if(sender instanceof Player) {
                    if(!skyNodes.isPluginEnabled()) {
                        sender.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                        sender.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>Unknown argument. Double-check your command.</red>"));

                    } else {
                        sender.sendMessage(mm.deserialize(locale.prefix() + locale.unknownArgument()));
                    }
                } else {
                    if(!skyNodes.isPluginEnabled()) {
                        logger.warn(mm.deserialize("<red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                        logger.warn(mm.deserialize("<red>Unknown argument. Double-check your command.</red>"));
                    } else {
                        logger.info(mm.deserialize(locale.unknownArgument()));
                    }
                }
                return false;
            }
        }

        if(args.length == 1) {
            switch(args[0]) {
                case "help" -> {
                    if(sender instanceof Player player) {
                        if(sender.hasPermission("skynodes.commands." + args[0])) {
                            for (String msg : locale.help()) {
                                player.sendMessage(mm.deserialize(msg));
                            }
                            return true;
                        } else {
                            player.sendMessage(mm.deserialize(locale.prefix() + locale.noPermission()));
                            return false;
                        }
                    } else {
                        for (String msg : locale.help()) {
                            logger.info(mm.deserialize(msg));
                        }
                        return true;
                    }
                }

                case "reload" -> {
                    if(sender instanceof Player player) {
                        if(sender.hasPermission("skynodes.commands." + args[0])) {
                            skyNodes.reload();
                            sender.sendMessage(mm.deserialize(locale.prefix() + locale.reload()));
                            return true;
                        } else {
                            player.sendMessage(mm.deserialize(locale.prefix() + locale.noPermission()));
                            return false;
                        }
                    } else {
                        skyNodes.reload();
                        logger.info(mm.deserialize(locale.reload()));
                        return true;
                    }
                }

                default -> {
                    if(sender instanceof Player player) {
                        player.sendMessage(mm.deserialize(locale.prefix() + locale.unknownArgument()));
                    } else {
                        logger.info(mm.deserialize(locale.unknownArgument()));
                    }
                    return false;
                }
            }
        } else {
            if(sender instanceof Player player) {
                player.sendMessage(mm.deserialize(locale.prefix() + locale.unknownArgument()));
            } else {
                logger.info(mm.deserialize(locale.unknownArgument()));
            }
            return false;
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1) {
            ArrayList<String> subCmds = new ArrayList<>();
            if(sender instanceof Player) {
                if (sender.hasPermission("skynodes.commands.help")) {
                    subCmds.add("help");
                }
                if (sender.hasPermission("skynodes.commands.reload")) {
                    subCmds.add("reload");
                }
            } else {
                subCmds.add("help");
                subCmds.add("reload");
            }
            return subCmds;
        }

        return Collections.emptyList();
    }
}
