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
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.configuration.LocaleManager;
import com.github.lukesky19.skymines.configuration.MineConfigManager;
import com.github.lukesky19.skymines.data.config.Locale;
import com.github.lukesky19.skymines.data.config.world.WorldMineConfig;
import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.manager.mine.world.BlocksManager;
import com.github.lukesky19.skymines.mine.AbstractMine;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is used to create the blocks command argument.
 */
public class BlocksCommand {
    private final @NotNull SkyMines skyMines;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull MineConfigManager mineConfigManager;
    private final @NotNull MineDataManager mineDataManager;
    private final @NotNull BlocksManager blocksManager;

    /**
     * Default Constructor.
     * You should use {@link #BlocksCommand(SkyMines, LocaleManager, MineConfigManager, MineDataManager, BlocksManager)} instead.
     * @deprecated You should use {@link #BlocksCommand(SkyMines, LocaleManager, MineConfigManager, MineDataManager, BlocksManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public BlocksCommand() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param mineConfigManager A {@link MineConfigManager} instance.
     * @param mineDataManager A {@link MineDataManager} instance.
     * @param blocksManager A {@link BlocksManager} instance.
     */
    public BlocksCommand(
            @NotNull SkyMines skyMines,
            @NotNull LocaleManager localeManager,
            @NotNull MineConfigManager mineConfigManager,
            @NotNull MineDataManager mineDataManager,
            @NotNull BlocksManager blocksManager) {
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
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("blocks")
                .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.blocks"));

        builder.then(Commands.literal("unlock")
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("mine_id", StringArgumentType.string())
                                .suggests((commandContext, suggestionsBuilder) -> {
                                    for(String mineId : mineDataManager.getMineIdsWithBlockUnlocks()) {
                                        suggestionsBuilder.suggest(mineId);
                                    }

                                    return suggestionsBuilder.buildFuture();
                                })

                                .then(Commands.argument("block_type", StringArgumentType.string())
                                        .suggests((commandContext, suggestionsBuilder) -> {
                                                ComponentLogger logger = skyMines.getComponentLogger();
                                                String mineId = commandContext.getArgument("mine_id", String.class);
                                                WorldMineConfig mineConfig = mineConfigManager.getWorldMineConfig(mineId);
                                                if(mineConfig == null) return suggestionsBuilder.buildFuture();

                                                mineConfig.unlockableBreakable().forEach(blockData -> {
                                                    String blockTypeName = blockData.blockType();
                                                    if(blockTypeName != null) {
                                                        @NotNull Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(logger, blockTypeName);
                                                        optionalBlockType.ifPresent(blockType -> suggestionsBuilder.suggest(blockType.getKey().getKey()));
                                                    }
                                                });

                                                return suggestionsBuilder.buildFuture();
                                        })

                                        .executes(ctx -> {
                                            ComponentLogger logger = skyMines.getComponentLogger();
                                            Locale locale = localeManager.getLocale();

                                            CommandSender sender = ctx.getSource().getSender();

                                            // Mine id
                                            String mineId = ctx.getArgument("mine_id", String.class);
                                            // Target Player
                                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                            Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                                            UUID targetPlayerId = targetPlayer.getUniqueId();

                                            // Block Type
                                            String blockTypeName = ctx.getArgument("block_type", String.class);
                                            @NotNull Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(logger, blockTypeName);
                                            if(optionalBlockType.isEmpty()) {
                                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("block_type", blockTypeName));

                                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().invalidBlockType(), placeholders));
                                                return 0;
                                            }
                                            BlockType blockType = optionalBlockType.get();

                                            AbstractMine mine = mineDataManager.getMineById(mineId);
                                            if(mine == null) {
                                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

                                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.noMineWithId(), placeholders));
                                                return 0;
                                            }

                                            List<TagResolver.Single> placeholders = List.of(
                                                    Placeholder.parsed("block_type", blockTypeName),
                                                    Placeholder.parsed("mine_id", mineId),
                                                    Placeholder.parsed("player", targetPlayer.getName()));

                                            if(blocksManager.isBlockTypeUnlocked(targetPlayerId, mineId, blockType)) {
                                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockAlreadyUnlocked(), placeholders));
                                                return 1;
                                            }

                                            blocksManager.addUnlockedBlock(targetPlayerId, mineId, blockType);

                                            sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().playerBlockUnlocked(), placeholders));
                                            targetPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockUnlocked(), placeholders));
                                            return 1;
                                        })
                                )
                        )
                )
        );

        builder.then(Commands.literal("lock")
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("mine_id", StringArgumentType.string())
                                .suggests((commandContext, suggestionsBuilder) -> {
                                    for(String mineId : mineDataManager.getMineIdsWithBlockUnlocks()) {
                                        suggestionsBuilder.suggest(mineId);
                                    }

                                    return suggestionsBuilder.buildFuture();
                                })


                                .then(Commands.argument("block_type", StringArgumentType.string())
                                        .suggests((commandContext, suggestionsBuilder) -> {
                                            ComponentLogger logger = skyMines.getComponentLogger();
                                            String mineId = commandContext.getArgument("mine_id", String.class);
                                            WorldMineConfig mineConfig = mineConfigManager.getWorldMineConfig(mineId);
                                            if(mineConfig == null) return suggestionsBuilder.buildFuture();

                                            mineConfig.unlockableBreakable().forEach(blockData -> {
                                                String blockTypeName = blockData.blockType();
                                                if(blockTypeName != null) {
                                                    @NotNull Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(logger, blockTypeName);
                                                    optionalBlockType.ifPresent(blockType -> suggestionsBuilder.suggest(blockType.getKey().getKey()));
                                                }
                                            });

                                            return suggestionsBuilder.buildFuture();
                                        })

                                        .executes(ctx -> {
                                            ComponentLogger logger = skyMines.getComponentLogger();
                                            Locale locale = localeManager.getLocale();

                                            CommandSender sender = ctx.getSource().getSender();

                                            // Mine id
                                            String mineId = ctx.getArgument("mine_id", String.class);
                                            // Target Player
                                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                            Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                                            UUID targetPlayerId = targetPlayer.getUniqueId();

                                            // Block Type
                                            String blockTypeName = ctx.getArgument("block_type", String.class);
                                            @NotNull Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(logger, blockTypeName);
                                            if(optionalBlockType.isEmpty()) {
                                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("block_type", blockTypeName));

                                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().invalidBlockType(), placeholders));
                                                return 0;
                                            }
                                            BlockType blockType = optionalBlockType.get();

                                            AbstractMine mine = mineDataManager.getMineById(mineId);
                                            if(mine == null) {
                                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

                                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.noMineWithId(), placeholders));
                                                return 0;
                                            }

                                            List<TagResolver.Single> placeholders = List.of(
                                                    Placeholder.parsed("block_type", blockTypeName),
                                                    Placeholder.parsed("mine_id", mineId),
                                                    Placeholder.parsed("player", targetPlayer.getName()));

                                            if(!blocksManager.isBlockTypeUnlocked(targetPlayerId, mineId, blockType)) {
                                                sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockAlreadyLocked(), placeholders));
                                                return 1;
                                            }

                                            blocksManager.removeUnlockedBlock(targetPlayerId, mineId, blockType);

                                            sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().playerBlockLocked(), placeholders));
                                            targetPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockLocked(), placeholders));
                                            return 1;
                                        })
                                )
                        )
                )
        );

        return builder.build();
    }
}
