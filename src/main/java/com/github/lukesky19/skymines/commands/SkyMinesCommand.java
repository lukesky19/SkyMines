/*
    SkyMines tracks blocks broken in specific regions, replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2025  lukeskywlker19

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
package com.github.lukesky19.skymines.commands;

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.configuration.loader.LocaleManager;
import com.github.lukesky19.skymines.configuration.record.Locale;
import com.github.lukesky19.skymines.manager.MineManager;
import com.github.lukesky19.skymines.mine.Mine;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * This class handles the creation of the SkyMines command.
 */
public final class SkyMinesCommand {
    private final SkyMines skyMines;
    private final LocaleManager localeManager;
    private final MineManager mineManager;

    public SkyMinesCommand(
            SkyMines skyMines,
            LocaleManager localeManager, MineManager mineManager) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.mineManager = mineManager;
    }

    /**
     * Creates a command to be passed into the LifeCycleAPI.
     * @return A LiteralCommandNode of a CommandSourceStack.
     */
    public LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skymines")
                .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines"));
        
        builder.then(Commands.literal("time")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.time") && ctx.getSender() instanceof Player)
            .then(Commands.argument("mine_id", StringArgumentType.string())
                .suggests((commandContext, suggestionsBuilder) -> {
                    for(String mineId : mineManager.getMineIds()) {
                        suggestionsBuilder.suggest(mineId);
                    }

                    return suggestionsBuilder.buildFuture();
                })
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();
                    String mineId = ctx.getArgument("mine_id", String.class);

                    Mine mine = mineManager.getMineById(mineId);
                    if(mine != null) {
                        Integer time = mine.getPlayerTime(uuid);
                        if(time != null) {
                            List<TagResolver.Single> placeholders = List.of(
                                    Placeholder.parsed("mine_id", mineId),
                                    Placeholder.parsed("time", localeManager.getTimeMessage(time)));

                            player.sendMessage(FormatUtil.format(locale.prefix() + locale.mineTime(), placeholders));

                            return 1;
                        } else {
                            player.sendMessage(FormatUtil.format(locale.prefix() + "You do not have any time for this mine."));
                            return 0;
                        }
                    } else {
                        player.sendMessage(FormatUtil.format("No mine for mine id " + mineId));
                        return 0;
                    }
                })
            )
        );

        builder.then(Commands.literal("add")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.add"))
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("mine_id", StringArgumentType.string())
                    .suggests((commandContext, suggestionsBuilder) -> {
                        for(String mineId : mineManager.getMineIds()) {
                            suggestionsBuilder.suggest(mineId);
                        }
                        return suggestionsBuilder.buildFuture();
                    })
                    .then(Commands.argument("time", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                            Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                            UUID targetUUID = targetPlayer.getUniqueId();
                            String mineId = ctx.getArgument("mine_id", String.class);
                            int time = ctx.getArgument("time", int.class);

                            Mine mine = mineManager.getMineById(mineId);
                            if(mine != null) {
                                if(time >= 1) {
                                    int result = mine.addPlayerTime(targetPlayer, targetUUID, time);

                                    List<TagResolver.Single> placeholders = List.of(
                                            Placeholder.parsed("player_name", targetPlayer.getName()),
                                            Placeholder.parsed("mine_id", mineId),
                                            Placeholder.parsed("time", localeManager.getTimeMessage(result)));

                                    targetPlayer.sendMessage(FormatUtil.format(locale.prefix() + locale.mineTimeGiven(), placeholders));
                                    sender.sendMessage(FormatUtil.format(locale.prefix() + locale.mineTimeGivenTo(), placeholders));

                                    return 1;
                                } else {
                                    sender.sendMessage(FormatUtil.format("<red>Time must be greater than or equal to 1!</red>"));
                                    return 0;
                                }
                            } else {
                                sender.sendMessage(FormatUtil.format("<red>No mine for mine id <yellow>" + mineId + "</yellow>.</red>"));
                                return 0;
                            }
                        })
                    )
                )
            )
        );
        
        builder.then(Commands.literal("remove")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.remove"))
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("mine_id", StringArgumentType.string())
                    .suggests((commandContext, suggestionsBuilder) -> {
                        for(String mineId : mineManager.getMineIds()) {
                            suggestionsBuilder.suggest(mineId);
                        }

                        return suggestionsBuilder.buildFuture();
                    })
                    .then(Commands.argument("time", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                            Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                            UUID targetUUID = targetPlayer.getUniqueId();
                            String mineId = ctx.getArgument("mine_id", String.class);
                            int time = ctx.getArgument("time", int.class);

                            Mine mine = mineManager.getMineById(mineId);
                            if(mine != null) {
                                if(time >= 1) {
                                    int result = mine.removePlayerTime(targetPlayer, targetUUID, time);

                                    List<TagResolver.Single> placeholders = List.of(
                                            Placeholder.parsed("player_name", targetPlayer.getName()),
                                            Placeholder.parsed("mine_id", mineId),
                                            Placeholder.parsed("time", localeManager.getTimeMessage(result)));

                                    targetPlayer.sendMessage(FormatUtil.format(locale.prefix() + locale.mineTimeGiven(), placeholders));
                                    sender.sendMessage(FormatUtil.format(locale.prefix() + locale.mineTimeGivenTo(), placeholders));

                                    return 1;
                                } else {
                                    sender.sendMessage(FormatUtil.format("<red>Time must be greater than or equal to 1!</red>"));
                                    return 0;
                                }
                            } else {
                                sender.sendMessage(FormatUtil.format("<red>No mine for mine id <yellow>" + mineId + "</yellow>.</red>"));
                                return 0;
                            }
                        })
                    )
                )
            )
        );
        
        builder.then(Commands.literal("set")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.set"))
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("mine_id", StringArgumentType.string())
                    .suggests((commandContext, suggestionsBuilder) -> {
                        for(String mineId : mineManager.getMineIds()) {
                            suggestionsBuilder.suggest(mineId);
                        }

                        return suggestionsBuilder.buildFuture();
                    })
                    .then(Commands.argument("time", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                            Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                            UUID targetUUID = targetPlayer.getUniqueId();
                            String mineId = ctx.getArgument("mine_id", String.class);
                            int time = ctx.getArgument("time", int.class);

                            Mine mine = mineManager.getMineById(mineId);
                            if(mine != null) {
                                if(time >= 0) {
                                    int result = mine.setPlayerTime(targetPlayer, targetUUID, time);

                                    List<TagResolver.Single> placeholders = List.of(
                                            Placeholder.parsed("player_name", targetPlayer.getName()),
                                            Placeholder.parsed("mine_id", mineId),
                                            Placeholder.parsed("time", localeManager.getTimeMessage(result)));

                                    targetPlayer.sendMessage(FormatUtil.format(locale.prefix() + locale.mineTimeGiven(), placeholders));
                                    sender.sendMessage(FormatUtil.format(locale.prefix() + locale.mineTimeGivenTo(), placeholders));

                                    return 1;
                                } else {
                                    sender.sendMessage(FormatUtil.format("<red>Time must be greater than or equal to 0!</red>"));
                                    return 0;
                                }
                            } else {
                                sender.sendMessage(FormatUtil.format("<red>No mine for mine id <yellow>" + mineId + "</yellow>.</red>"));
                                return 0;
                            }
                        })
                    )
                )
            )
        );

        builder.then(Commands.literal("help")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.help"))
            .executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                Locale locale = localeManager.getLocale();

                for (String msg : locale.help()) {
                    sender.sendMessage(FormatUtil.format(msg));
                }

                return 1;
            })
        );

        builder.then(Commands.literal("reload")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.reload"))
            .executes(ctx -> {
                Locale locale = localeManager.getLocale();

                skyMines.reload();

                ctx.getSource().getSender().sendMessage(FormatUtil.format(locale.prefix() + locale.reload()));

                return 1;
            })
        );

        return builder.build();
    }
}
