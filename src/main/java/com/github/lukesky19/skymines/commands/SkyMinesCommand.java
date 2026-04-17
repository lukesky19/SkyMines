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
package com.github.lukesky19.skymines.commands;

import com.github.lukesky19.skylib.paper.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.commands.arguments.*;
import com.github.lukesky19.skymines.manager.config.GUIConfigManager;
import com.github.lukesky19.skymines.manager.config.LocaleManager;
import com.github.lukesky19.skymines.manager.config.MineConfigManager;
import com.github.lukesky19.skymines.manager.hook.HookManager;
import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.manager.mine.packet.MineTimeManager;
import com.github.lukesky19.skymines.manager.mine.world.BlocksManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

/**
 * This class handles the creation of the SkyMines command.
 */
public final class SkyMinesCommand {
    private final @NonNull SkyMines skyMines;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull GUIConfigManager guiConfigManager;
    private final @NonNull MineConfigManager mineConfigManager;
    private final @NonNull UUIDGUIManager guiManager;
    private final @NonNull MineDataManager mineDataManager;
    private final @NonNull MineTimeManager mineTimeManager;
    private final @NonNull BlocksManager blocksManager;
    private final @NonNull HookManager hookManager;

    /**
     * Default Constructor.
     * You should use {@link #SkyMinesCommand(SkyMines, LocaleManager, GUIConfigManager, MineConfigManager, UUIDGUIManager, MineDataManager, MineTimeManager, BlocksManager, HookManager)} instead.
     * @deprecated You should use {@link #SkyMinesCommand(SkyMines, LocaleManager, GUIConfigManager, MineConfigManager, UUIDGUIManager, MineDataManager, MineTimeManager, BlocksManager, HookManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public SkyMinesCommand() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param mineConfigManager A {@link MineConfigManager} instance.
     * @param guiManager A {@link UUIDGUIManager} instance.
     * @param mineDataManager A {@link MineDataManager} instance.
     * @param mineTimeManager A {@link MineTimeManager} instance.
     * @param blocksManager A {@link BlocksManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public SkyMinesCommand(
            @NonNull SkyMines skyMines,
            @NonNull LocaleManager localeManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull MineConfigManager mineConfigManager,
            @NonNull UUIDGUIManager guiManager,
            @NonNull MineDataManager mineDataManager,
            @NonNull MineTimeManager mineTimeManager,
            @NonNull BlocksManager blocksManager,
            @NonNull HookManager hookManager) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.mineConfigManager = mineConfigManager;
        this.guiManager = guiManager;
        this.mineDataManager = mineDataManager;
        this.mineTimeManager = mineTimeManager;
        this.blocksManager = blocksManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the skymines command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the skymines command.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skymines")
                .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines"));

        TimeCommand timeCommand = new TimeCommand(localeManager, mineDataManager, mineTimeManager);
        BlocksCommand blocksCommand = new BlocksCommand(skyMines, localeManager, mineConfigManager, mineDataManager, blocksManager);
        HelpCommand helpCommand = new HelpCommand(localeManager);
        ReloadCommand reloadCommand = new ReloadCommand(skyMines, localeManager);
        ShopCommand shopCommand = new ShopCommand(skyMines, localeManager, guiConfigManager,mineConfigManager, mineDataManager, guiManager, blocksManager, hookManager);
        PreviewCommand previewCommand = new PreviewCommand(skyMines, localeManager, guiConfigManager, mineConfigManager, mineDataManager, guiManager);

        builder.then(timeCommand.createCommand());
        builder.then(blocksCommand.createCommand());
        builder.then(helpCommand.createCommand());
        builder.then(reloadCommand.createCommand());
        builder.then(shopCommand.createCommand());
        builder.then(previewCommand.createCommand());

        return builder.build();
    }
}
