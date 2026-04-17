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
import com.github.lukesky19.skylib.paper.api.adventure.PaperAdventureUtility;
import com.github.lukesky19.skylib.paper.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.Locale;
import com.github.lukesky19.skymines.data.config.world.WorldMineConfig;
import com.github.lukesky19.skymines.data.config.world.WorldMineShopConfig;
import com.github.lukesky19.skymines.gui.UnlocksShopGUI;
import com.github.lukesky19.skymines.manager.config.GUIConfigManager;
import com.github.lukesky19.skymines.manager.config.LocaleManager;
import com.github.lukesky19.skymines.manager.config.MineConfigManager;
import com.github.lukesky19.skymines.manager.hook.HookManager;
import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.manager.mine.world.BlocksManager;
import com.github.lukesky19.skymines.mine.Mine;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * This class is used to create the shop command argument.
 */
public class ShopCommand {
    private final @NonNull SkyMines skyMines;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull GUIConfigManager guiConfigManager;
    private final @NonNull MineConfigManager mineConfigManager;
    private final @NonNull MineDataManager mineDataManager;
    private final @NonNull UUIDGUIManager guiManager;
    private final @NonNull BlocksManager blocksManager;
    private final @NonNull HookManager hookManager;

    /**
     * Default Constructor.
     * You should use {@link #ShopCommand(SkyMines, LocaleManager, GUIConfigManager, MineConfigManager, MineDataManager, UUIDGUIManager, BlocksManager, HookManager)} instead.
     * @deprecated You should use {@link #ShopCommand(SkyMines, LocaleManager, GUIConfigManager, MineConfigManager, MineDataManager, UUIDGUIManager, BlocksManager, HookManager)} instead.
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
     * @param guiManager A {@link UUIDGUIManager} instance.
     * @param blocksManager A {@link BlocksManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public ShopCommand(
            @NonNull SkyMines skyMines,
            @NonNull LocaleManager localeManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull MineConfigManager mineConfigManager,
            @NonNull MineDataManager mineDataManager,
            @NonNull UUIDGUIManager guiManager,
            @NonNull BlocksManager blocksManager,
            @NonNull HookManager hookManager) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.mineConfigManager = mineConfigManager;
        this.mineDataManager = mineDataManager;
        this.guiManager = guiManager;
        this.blocksManager = blocksManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the shop command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the shop command argument.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("shop")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.shop") && ctx.getSender() instanceof Player)
            .executes(ctx -> {
                ComponentLogger logger = skyMines.getComponentLogger();
                Player player = (Player) ctx.getSource().getSender();
                Locale locale = localeManager.getConfiguration();
                WorldMineShopConfig guiConfig = guiConfigManager.getWorldMineShopConfig();

                if(guiConfig == null) {
                    logger.warn(AdventureUtility.plain("The gui config for the world mine shop is invalid."));
                    player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                Mine mine = mineDataManager.getMineByLocation(player.getLocation());
                if(mine == null) {
                    player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.worldMineMessages().guiErrorNotInMine()));
                    return 0;
                }

                String mineId = mine.getMineId();
                if(mineId == null) {
                    logger.warn(AdventureUtility.plain("The mine id for a mine is invalid."));
                    player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                WorldMineConfig mineConfig = mineConfigManager.getWorldMineConfig(mineId);
                if(mineConfig == null) {
                    logger.warn(AdventureUtility.plain("The mine config for mine id " + mineId + " is invalid."));
                    player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                UnlocksShopGUI unlocksShopGUI = new UnlocksShopGUI(skyMines, guiManager, player, localeManager, blocksManager, hookManager, mineId, mineConfig, guiConfig);

                boolean creationResult = unlocksShopGUI.create();
                if(!creationResult) {
                    logger.error(AdventureUtility.deserialize("Unable to create the InventoryView for the unlocks shop GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                // This method is completed sync, the api returns a CompletableFuture for supporting plugins with async requirements.
                boolean updateResult = unlocksShopGUI.update();
                if(!updateResult) {
                    logger.error(AdventureUtility.deserialize("Unable to decorate the unlocks shop GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                boolean openResult = unlocksShopGUI.open();
                if(!openResult) {
                    logger.error(AdventureUtility.deserialize("Unable to open the unlocks shop GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                return 1;
            });

        return builder.build();
    }
}
