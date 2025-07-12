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
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.configuration.GUIConfigManager;
import com.github.lukesky19.skymines.configuration.LocaleManager;
import com.github.lukesky19.skymines.configuration.MineConfigManager;
import com.github.lukesky19.skymines.data.config.Locale;
import com.github.lukesky19.skymines.data.config.world.WorldMineConfig;
import com.github.lukesky19.skymines.data.config.world.WorldMineGUIConfig;
import com.github.lukesky19.skymines.gui.UnlocksShopGUI;
import com.github.lukesky19.skymines.manager.gui.GUIManager;
import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.manager.mine.world.BlocksManager;
import com.github.lukesky19.skymines.mine.AbstractMine;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is used to create the shop command argument.
 */
public class ShopCommand {
    private final @NotNull SkyMines skyMines;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull MineConfigManager mineConfigManager;
    private final @NotNull MineDataManager mineDataManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull BlocksManager blocksManager;

    /**
     * Default Constructor.
     * You should use {@link #ShopCommand(SkyMines, LocaleManager, GUIConfigManager, MineConfigManager, MineDataManager, GUIManager, BlocksManager)} instead.
     * @deprecated You should use {@link #ShopCommand(SkyMines, LocaleManager, GUIConfigManager, MineConfigManager, MineDataManager, GUIManager, BlocksManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public ShopCommand() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param mineConfigManager A {@link MineConfigManager} instance.
     * @param mineDataManager A {@link MineDataManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param blocksManager A {@link BlocksManager} instance.
     */
    public ShopCommand(
            @NotNull SkyMines skyMines,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull MineConfigManager mineConfigManager,
            @NotNull MineDataManager mineDataManager,
            @NotNull GUIManager guiManager,
            @NotNull BlocksManager blocksManager) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.mineConfigManager = mineConfigManager;
        this.mineDataManager = mineDataManager;
        this.guiManager = guiManager;
        this.blocksManager = blocksManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the shop command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the shop command argument.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("shop")
                .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.shop") && ctx.getSender() instanceof Player)
                .executes(ctx -> {
                    ComponentLogger logger = skyMines.getComponentLogger();
                    Player player = (Player) ctx.getSource().getSender();
                    Locale locale = localeManager.getLocale();
                    @Nullable WorldMineGUIConfig guiConfig = guiConfigManager.getWorldMineShopConfig();

                    if(guiConfig == null) {
                        logger.warn(AdventureUtil.serialize("The gui config for the world mine shop is invalid."));
                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    AbstractMine mine = mineDataManager.getMineByLocation(player.getLocation());
                    if(mine == null) {
                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.worldMineMessages().guiErrorNotInMine()));
                        return 0;
                    }

                    @Nullable String mineId = mine.getMineId();
                    if(mineId == null) {
                        logger.warn(AdventureUtil.serialize("The mine id for a mine is invalid."));
                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    @Nullable WorldMineConfig mineConfig = mineConfigManager.getWorldMineConfig(mineId);
                    if(mineConfig == null) {
                        logger.warn(AdventureUtil.serialize("The mine config for mine id " + mineId + " is invalid."));
                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    UnlocksShopGUI unlocksShopGUI = new UnlocksShopGUI(skyMines, guiManager, player, localeManager, blocksManager, mineId, mineConfig, guiConfig);

                    boolean creationResult = unlocksShopGUI.create();
                    if(!creationResult) {
                        logger.error(AdventureUtil.serialize("Unable to create the InventoryView for the unlocks shop GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    // This method is completed sync, the api returns a CompletableFuture for supporting plugins with async requirements.
                    boolean updateResult = unlocksShopGUI.update();
                    if(!updateResult) {
                        logger.error(AdventureUtil.serialize("Unable to decorate the unlocks shop GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean openResult = unlocksShopGUI.open();
                    if(!openResult) {
                        logger.error(AdventureUtil.serialize("Unable to open the unlocks shop GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    return 1;
                });

        return builder.build();
    }
}
