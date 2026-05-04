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

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.registry.RegistryUtil;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.locale.Locale;
import com.github.lukesky19.skymines.locale.LocaleManager;
import com.github.lukesky19.skymines.mine.MineConfigManager;
import com.github.lukesky19.skymines.mine.MineDataManager;
import com.github.lukesky19.skymines.mine.config.WorldMineConfig;
import com.github.lukesky19.skymines.mine.interfaces.Mine;
import com.github.lukesky19.skymines.player.MineBlockManager;
import com.github.lukesky19.skymines.util.enums.BlockUnlockResult;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.block.BlockType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is used to create the blocks command argument.
 */
public class BlocksCommand {
    private final @NonNull SkyMines skyMines;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull MineConfigManager mineConfigManager;
    private final @NonNull MineDataManager mineDataManager;
    private final @NonNull MineBlockManager blocksManager;

    /**
     * Default Constructor.
     * You should use {@link #BlocksCommand(SkyMines, LocaleManager, MineConfigManager, MineDataManager, MineBlockManager)} instead.
     * @deprecated You should use {@link #BlocksCommand(SkyMines, LocaleManager, MineConfigManager, MineDataManager, MineBlockManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public BlocksCommand() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     *
     * @param skyMines A {@link SkyMines} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param mineConfigManager A {@link MineConfigManager} instance.
     * @param mineDataManager A {@link MineDataManager} instance.
     * @param blocksManager A {@link MineBlockManager} instance.
     */
    public BlocksCommand(
            @NonNull SkyMines skyMines,
            @NonNull LocaleManager localeManager,
            @NonNull MineConfigManager mineConfigManager,
            @NonNull MineDataManager mineDataManager,
            @NonNull MineBlockManager blocksManager) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.mineConfigManager = mineConfigManager;
        this.mineDataManager = mineDataManager;
        this.blocksManager = blocksManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the blocks command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the blocks command argument.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("blocks")
                .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.blocks"));

        builder.then(Commands.literal("unlock")
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("mine_id", StringArgumentType.word())
                                .suggests((_, suggestionsBuilder) -> {
                                    for(String mineId : mineDataManager.getMineIdsWithBlockUnlocks()) {
                                        suggestionsBuilder.suggest(mineId);
                                    }

                                    return suggestionsBuilder.buildFuture();
                                })

                                .then(Commands.argument("block_type", StringArgumentType.greedyString())
                                        .suggests((commandContext, suggestionsBuilder) -> {
                                                String mineId = commandContext.getArgument("mine_id", String.class);
                                                WorldMineConfig mineConfig = mineConfigManager.getWorldMineConfig(mineId);
                                                if(mineConfig == null) return suggestionsBuilder.buildFuture();

                                                mineConfig.unlockableBreakable().forEach(blockData -> {
                                                    if(blockData.blockType() != null) {
                                                        suggestionsBuilder.suggest(blockData.blockType().getKey().toString());
                                                    }
                                                });

                                                return suggestionsBuilder.buildFuture();
                                        })

                                        .executes(ctx -> {
                                            ComponentLogger logger = skyMines.getComponentLogger();
                                            Locale locale = localeManager.getConfiguration();

                                            CommandSender sender = ctx.getSource().getSender();

                                            // Mine id
                                            String mineId = ctx.getArgument("mine_id", String.class);
                                            // Target Player
                                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                            Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();

                                            // Block Type
                                            String blockTypeName = ctx.getArgument("block_type", String.class);
                                            Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(logger, blockTypeName);
                                            if(optionalBlockType.isEmpty()) {
                                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("block_type", blockTypeName));

                                                sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().invalidBlockType(), placeholders));
                                                return 0;
                                            }
                                            BlockType blockType = optionalBlockType.get();

                                            Mine mine = mineDataManager.getMineById(mineId);
                                            if(mine == null) {
                                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

                                                sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.noMineWithId(), placeholders));
                                                return 0;
                                            }

                                            List<TagResolver.Single> placeholders = List.of(
                                                    Placeholder.parsed("block_type", blockTypeName),
                                                    Placeholder.parsed("mine_id", mineId),
                                                    Placeholder.parsed("player", targetPlayer.getName()));

                                            if(blocksManager.isBlockTypeUnlocked(targetPlayer, mineId, blockType)) {
                                                sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockAlreadyUnlocked(), placeholders));
                                                return 1;
                                            }

                                            BlockUnlockResult result = blocksManager.addUnlockedBlock(targetPlayer, mineId, blockType);
                                            switch(result) {
                                                case INVALID_SETTINGS -> sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().playerBlockUnlockSettingsError(), placeholders));
                                                case INVALID_USER -> sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().playerBlockUnlockUserError(), placeholders));
                                                case PLAYER_EXCLUDED -> sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().playerBlockUnlockExcluded(), placeholders));
                                                case SUCCESS -> {
                                                    sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().playerBlockUnlocked(), placeholders));
                                                    targetPlayer.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockUnlocked(), placeholders));
                                                }
                                            }

                                            return 1;
                                        })
                                )
                        )
                )
        );

        builder.then(Commands.literal("lock")
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("mine_id", StringArgumentType.word())
                                .suggests((_, suggestionsBuilder) -> {
                                    for(String mineId : mineDataManager.getMineIdsWithBlockUnlocks()) {
                                        suggestionsBuilder.suggest(mineId);
                                    }

                                    return suggestionsBuilder.buildFuture();
                                })


                                .then(Commands.argument("block_type", StringArgumentType.greedyString())
                                        .suggests((commandContext, suggestionsBuilder) -> {
                                            String mineId = commandContext.getArgument("mine_id", String.class);
                                            WorldMineConfig mineConfig = mineConfigManager.getWorldMineConfig(mineId);
                                            if(mineConfig == null) return suggestionsBuilder.buildFuture();

                                            mineConfig.unlockableBreakable().forEach(blockData -> {
                                                if(blockData.blockType() != null) {
                                                    suggestionsBuilder.suggest(blockData.blockType().getKey().toString());
                                                }
                                            });

                                            return suggestionsBuilder.buildFuture();
                                        })

                                        .executes(ctx -> {
                                            ComponentLogger logger = skyMines.getComponentLogger();
                                            Locale locale = localeManager.getConfiguration();

                                            CommandSender sender = ctx.getSource().getSender();

                                            // Mine id
                                            String mineId = ctx.getArgument("mine_id", String.class);
                                            // Target Player
                                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                            Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                                            UUID targetPlayerId = targetPlayer.getUniqueId();

                                            // Block Type
                                            String blockTypeName = ctx.getArgument("block_type", String.class);
                                            Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(logger, blockTypeName);
                                            if(optionalBlockType.isEmpty()) {
                                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("block_type", blockTypeName));

                                                sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().invalidBlockType(), placeholders));
                                                return 0;
                                            }
                                            BlockType blockType = optionalBlockType.get();

                                            Mine mine = mineDataManager.getMineById(mineId);
                                            if(mine == null) {
                                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

                                                sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.noMineWithId(), placeholders));
                                                return 0;
                                            }

                                            List<TagResolver.Single> placeholders = List.of(
                                                    Placeholder.parsed("block_type", blockTypeName),
                                                    Placeholder.parsed("mine_id", mineId),
                                                    Placeholder.parsed("player", targetPlayer.getName()));

                                            if(!blocksManager.isBlockTypeUnlocked(targetPlayer, mineId, blockType)) {
                                                sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockAlreadyLocked(), placeholders));
                                                return 1;
                                            }

                                            blocksManager.removeUnlockedBlock(targetPlayerId, mineId, blockType);

                                            sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().playerBlockLocked(), placeholders));
                                            targetPlayer.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockLocked(), placeholders));
                                            return 1;
                                        })
                                )
                        )
                )
        );

        builder.then(Commands.literal("reset")
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("mine_id", StringArgumentType.word())
                                .suggests((_, suggestionsBuilder) -> {
                                    for(String mineId : mineDataManager.getMineIdsWithBlockUnlocks()) {
                                        suggestionsBuilder.suggest(mineId);
                                    }

                                    return suggestionsBuilder.buildFuture();
                                })

                                .executes(ctx -> {
                                    Locale locale = localeManager.getConfiguration();

                                    CommandSender sender = ctx.getSource().getSender();

                                    // Mine id
                                    String mineId = ctx.getArgument("mine_id", String.class);
                                    // Target Player
                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                    Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                                    UUID targetPlayerId = targetPlayer.getUniqueId();

                                    Mine mine = mineDataManager.getMineById(mineId);
                                    if(mine == null) {
                                        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

                                        sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.noMineWithId(), placeholders));
                                        return 0;
                                    }

                                    List<TagResolver.Single> placeholders = List.of(
                                            Placeholder.parsed("mine_id", mineId),
                                            Placeholder.parsed("player", targetPlayer.getName()));

                                    blocksManager.removeUnlockedBlocks(targetPlayerId, mineId);

                                    sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().playerBlocksLocked(), placeholders));
                                    targetPlayer.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blocksLocked(), placeholders));

                                    return 1;
                                })
                        )
                )
        );

        return builder.build();
    }
}
