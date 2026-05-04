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
import com.github.lukesky19.skymines.gui.GUIConfigManager;
import com.github.lukesky19.skymines.gui.config.WorldMinePreviewConfig;
import com.github.lukesky19.skymines.gui.guis.FreePreviewGUI;
import com.github.lukesky19.skymines.locale.Locale;
import com.github.lukesky19.skymines.locale.LocaleManager;
import com.github.lukesky19.skymines.mine.MineConfigManager;
import com.github.lukesky19.skymines.mine.MineDataManager;
import com.github.lukesky19.skymines.mine.config.WorldMineConfig;
import com.github.lukesky19.skymines.mine.interfaces.Mine;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * This class is used to create the preview command argument.
 */
public class PreviewCommand {
    private final @NonNull SkyMines skyMines;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull GUIConfigManager guiConfigManager;
    private final @NonNull MineConfigManager mineConfigManager;
    private final @NonNull MineDataManager mineDataManager;
    private final @NonNull UUIDGUIManager guiManager;

    /**
     * Default Constructor.
     * You should use {@link #PreviewCommand(SkyMines, LocaleManager, GUIConfigManager, MineConfigManager, MineDataManager, UUIDGUIManager)} instead.
     * @deprecated You should use {@link #PreviewCommand(SkyMines, LocaleManager, GUIConfigManager, MineConfigManager, MineDataManager, UUIDGUIManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public PreviewCommand() {
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
     */
    public PreviewCommand(
            @NonNull SkyMines skyMines,
            @NonNull LocaleManager localeManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull MineConfigManager mineConfigManager,
            @NonNull MineDataManager mineDataManager,
            @NonNull UUIDGUIManager guiManager) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.mineConfigManager = mineConfigManager;
        this.mineDataManager = mineDataManager;
        this.guiManager = guiManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the preview command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the preview command argument.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("preview")
            .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.preview") && ctx.getSender() instanceof Player)
            .executes(ctx -> {
                ComponentLogger logger = skyMines.getComponentLogger();
                Player player = (Player) ctx.getSource().getSender();
                Locale locale = localeManager.getConfiguration();
                WorldMinePreviewConfig guiConfig = guiConfigManager.getMinePreviewConfig();

                if(guiConfig == null) {
                    logger.warn(AdventureUtility.plain("The gui config for the world mine preview gui is invalid."));
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

                FreePreviewGUI freePreviewGUI = new FreePreviewGUI(skyMines, guiManager, player, mineId, mineConfig, guiConfig);

                boolean creationResult = freePreviewGUI.create();
                if(!creationResult) {
                    logger.error(AdventureUtility.deserialize("Unable to create the InventoryView for the preview GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                boolean updateResult = freePreviewGUI.update();
                if(!updateResult) {
                    logger.error(AdventureUtility.deserialize("Unable to decorate the preview GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                boolean openResult = freePreviewGUI.open();
                if(!openResult) {
                    logger.error(AdventureUtility.deserialize("Unable to open the preview GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                return 1;
            });

        return builder.build();
    }
}
