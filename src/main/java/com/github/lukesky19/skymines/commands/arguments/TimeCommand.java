/*
    SkyMines offers different types mines to get resources from.
    Copyright (C) 2023 lukeskywlker19

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
package com.github.lukesky19.skymines.commands.arguments;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import com.github.lukesky19.skymines.data.config.Locale;
import com.github.lukesky19.skymines.manager.config.LocaleManager;
import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.manager.mine.packet.MineTimeManager;
import com.github.lukesky19.skymines.mine.AbstractMine;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * This class is used to create the time command argument.
 */
public class TimeCommand {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull MineDataManager mineDataManager;
    private final @NotNull MineTimeManager mineTimeManager;

    /**
     * Default Constructor.
     * You should use {@link #TimeCommand(LocaleManager, MineDataManager, MineTimeManager)} instead.
     * @deprecated You should use {@link #TimeCommand(LocaleManager, MineDataManager, MineTimeManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public TimeCommand() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance
     * @param mineDataManager A {@link MineDataManager} instance.
     * @param mineTimeManager A {@link MineTimeManager} instance.
     */
    public TimeCommand(
            @NotNull LocaleManager localeManager,
            @NotNull MineDataManager mineDataManager,
            @NotNull MineTimeManager mineTimeManager) {
        this.localeManager = localeManager;
        this.mineDataManager = mineDataManager;
        this.mineTimeManager = mineTimeManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the time command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the time command argument.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("time")
            .requires(ctx ->
                ctx.getSender().hasPermission("skymines.commands.skymines.time")
                    && ctx.getSender() instanceof Player);

        builder.then(Commands.argument("mine_id", StringArgumentType.string())
            .suggests((commandContext, suggestionsBuilder) -> {
                for(String mineId : mineDataManager.getMineIdsWithTime()) {
                    suggestionsBuilder.suggest(mineId);
                }

                return suggestionsBuilder.buildFuture();
            })

            .then(Commands.argument("player", ArgumentTypes.player())
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    String mineId = ctx.getArgument("mine_id", String.class);
                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                    Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                    UUID targetPlayerId = targetPlayer.getUniqueId();
                    Locale locale = localeManager.getLocale();

                    AbstractMine mine = mineDataManager.getMineById(mineId);
                    if(mine == null) {
                        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

                        sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.noMineWithId(), placeholders));
                        return 0;
                    }

                    long time = mineTimeManager.getMineTime(targetPlayerId, mineId);
                    List<TagResolver.Single> placeholders = List.of(
                            Placeholder.parsed("mine_id", mineId),
                            Placeholder.parsed("time", localeManager.getTimeMessage(time)));

                    if(time <= 0) {
                        sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().noMineTime(), placeholders));
                    } else {
                        sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineTime(), placeholders));
                    }

                    return 1;
                }))

                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();
                    String mineId = ctx.getArgument("mine_id", String.class);

                    AbstractMine mine = mineDataManager.getMineById(mineId);
                    if(mine == null) {
                        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.noMineWithId(), placeholders));
                        return 0;
                    }

                    long time = mineTimeManager.getMineTime(uuid, mineId);
                    List<TagResolver.Single> placeholders = List.of(
                            Placeholder.parsed("mine_id", mineId),
                            Placeholder.parsed("time", localeManager.getTimeMessage(time)));

                    if(time <= 0) {
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().noMineTime(), placeholders));
                    } else {
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineTime(), placeholders));
                    }

                    return 1;
                }));

        builder.then(Commands.literal("add")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.time.add"))
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("mine_id", StringArgumentType.string())
                    .suggests((commandContext, suggestionsBuilder) -> {
                        for(String mineId : mineDataManager.getMineIdsWithTime()) {
                            suggestionsBuilder.suggest(mineId);
                        }
                        return suggestionsBuilder.buildFuture();
                    })
                    .then(Commands.argument("time", StringArgumentType.string())
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                            Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                            UUID targetUUID = targetPlayer.getUniqueId();
                            String mineId = ctx.getArgument("mine_id", String.class);
                            String timeString = ctx.getArgument("time", String.class);
                            long timeSeconds = TimeUtil.stringToMillis(timeString) / 1000;

                            AbstractMine mine = mineDataManager.getMineById(mineId);
                            if(mine == null) {
                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.noMineWithId(), placeholders));
                                return 0;
                            }

                            if(timeSeconds >= 1) {
                                mineTimeManager.incrementMineTime(targetUUID, mineId, timeSeconds);

                                long mineTimeSeconds = mineTimeManager.getMineTime(targetUUID, mineId);

                                List<TagResolver.Single> placeholders = List.of(
                                        Placeholder.parsed("player_name", targetPlayer.getName()),
                                        Placeholder.parsed("mine_id", mineId),
                                        Placeholder.parsed("time", localeManager.getTimeMessage(mineTimeSeconds)));

                                targetPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineTimeChanged(), placeholders));
                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineTimeChangedTo(), placeholders));

                                return 1;
                            } else {
                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().timeInvalidLessThenOne()));
                                return 0;
                            }
                        })
                    )
                )
            )
        );

        builder.then(Commands.literal("remove")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.time.remove"))
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("mine_id", StringArgumentType.string())
                    .suggests((commandContext, suggestionsBuilder) -> {
                        for(String mineId : mineDataManager.getMineIds()) {
                            suggestionsBuilder.suggest(mineId);
                        }

                        return suggestionsBuilder.buildFuture();
                    })
                    .then(Commands.argument("time", StringArgumentType.string())
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                            Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                            UUID targetUUID = targetPlayer.getUniqueId();
                            String mineId = ctx.getArgument("mine_id", String.class);
                            String timeString = ctx.getArgument("time", String.class);
                            long timeSeconds = TimeUtil.stringToMillis(timeString) / 1000;

                            AbstractMine mine = mineDataManager.getMineById(mineId);
                            if(mine == null) {
                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.noMineWithId(), placeholders));
                                return 0;
                            }

                            if(timeSeconds >= 1) {
                                mineTimeManager.decrementMineTime(targetUUID, mineId, timeSeconds);

                                long mineTimeSeconds = mineTimeManager.getMineTime(targetUUID, mineId);

                                List<TagResolver.Single> placeholders = List.of(
                                        Placeholder.parsed("player_name", targetPlayer.getName()),
                                        Placeholder.parsed("mine_id", mineId),
                                        Placeholder.parsed("time", localeManager.getTimeMessage(mineTimeSeconds)));

                                targetPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineTimeChanged(), placeholders));
                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineTimeChangedTo(), placeholders));

                                return 1;
                            } else {
                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().timeInvalidLessThenOne()));
                                return 0;
                            }
                        })
                    )
                )
            )
        );

        builder.then(Commands.literal("set")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.time.set"))
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("mine_id", StringArgumentType.string())
                    .suggests((commandContext, suggestionsBuilder) -> {
                        for(String mineId : mineDataManager.getMineIds()) {
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
                            String timeString = ctx.getArgument("time", String.class);
                            long timeSeconds = TimeUtil.stringToMillis(timeString) / 1000;

                            AbstractMine mine = mineDataManager.getMineById(mineId);
                            if(mine == null) {
                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.noMineWithId(), placeholders));
                                return 0;
                            }

                            if(timeSeconds < 0) {
                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().timeInvalidLessThenZero()));
                                return 0;
                            }

                            mineTimeManager.setMineTime(targetUUID, mineId, timeSeconds);

                            long mineTimeSeconds = mineTimeManager.getMineTime(targetUUID, mineId);

                            List<TagResolver.Single> placeholders = List.of(
                                    Placeholder.parsed("player_name", targetPlayer.getName()),
                                    Placeholder.parsed("mine_id", mineId),
                                    Placeholder.parsed("time", localeManager.getTimeMessage(mineTimeSeconds)));

                            targetPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineTimeChanged(), placeholders));
                            sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineTimeChangedTo(), placeholders));

                            return 1;
                        })
                    )
                )
            )
        );

        return builder.build();
    }
}
