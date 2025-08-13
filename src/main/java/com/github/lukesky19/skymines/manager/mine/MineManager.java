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
package com.github.lukesky19.skymines.manager.mine;

import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.packet.PacketMineConfig;
import com.github.lukesky19.skymines.data.config.world.WorldMineConfig;
import com.github.lukesky19.skymines.manager.bossbar.BossBarManager;
import com.github.lukesky19.skymines.manager.config.LocaleManager;
import com.github.lukesky19.skymines.manager.config.MineConfigManager;
import com.github.lukesky19.skymines.manager.mine.packet.CooldownManager;
import com.github.lukesky19.skymines.manager.mine.packet.MineTimeManager;
import com.github.lukesky19.skymines.manager.mine.world.BlocksManager;
import com.github.lukesky19.skymines.manager.mine.world.PDCManager;
import com.github.lukesky19.skymines.mine.AbstractMine;
import com.github.lukesky19.skymines.mine.PacketMine;
import com.github.lukesky19.skymines.mine.WorldMine;
import org.jetbrains.annotations.NotNull;

/**
 * This class manages the creation of mines from their config files from {@link MineConfigManager}.
 * The actual {@link AbstractMine}s are stored in {@link MineDataManager}.
 */
public class MineManager {
    private final @NotNull SkyMines skyMines;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull MineConfigManager mineConfigManager;
    private final @NotNull MineDataManager mineDataManager;
    private final @NotNull CooldownManager cooldownManager;
    private final @NotNull MineTimeManager mineTimeManager;
    private final @NotNull BossBarManager bossBarManager;
    private final @NotNull BlocksManager blocksManager;
    private final @NotNull PDCManager pdcManager;

    /**
     * Default Constructor.
     * You should use {@link #MineManager(SkyMines, LocaleManager, MineConfigManager, MineDataManager, CooldownManager, MineTimeManager, BossBarManager, BlocksManager)} instead.
     * @deprecated You should use {@link #MineManager(SkyMines, LocaleManager, MineConfigManager, MineDataManager, CooldownManager, MineTimeManager, BossBarManager, BlocksManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public MineManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param mineConfigManager A {@link MineConfigManager} instance.
     * @param mineDataManager A {@link MineDataManager} instance.
     * @param cooldownManager A {@link CooldownManager} instance.
     * @param mineTimeManager A {@link MineTimeManager} instance.
     * @param bossBarManager A {@link BossBarManager} instance.
     * @param blocksManager A {@link BlocksManager} instance.
     */
    public MineManager(
            @NotNull SkyMines skyMines,
            @NotNull LocaleManager localeManager,
            @NotNull MineConfigManager mineConfigManager,
            @NotNull MineDataManager mineDataManager,
            @NotNull CooldownManager cooldownManager,
            @NotNull MineTimeManager mineTimeManager,
            @NotNull BossBarManager bossBarManager,
            @NotNull BlocksManager blocksManager) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.mineConfigManager = mineConfigManager;
        this.mineDataManager = mineDataManager;
        this.cooldownManager = cooldownManager;
        this.mineTimeManager = mineTimeManager;
        this.bossBarManager = bossBarManager;
        this.blocksManager = blocksManager;
        this.pdcManager = new PDCManager();
    }

    /**
     * Re-create the mines from their configuration files.
     */
    public void reload() {
        clearMines(false);

        // Create mines
        mineConfigManager.getPacketMineConfigs().forEach(this::createPacketMine);
        mineConfigManager.getWorldMineConfigs().forEach(this::createWorldMine);
    }

    /**
     * Creates a new {@link PacketMine} and adds it the list of mines if it was created successfully.
     * @param mineId The id of the mine being created.
     * @param mineConfig The {@link PacketMineConfig} for the mine being created.
     */
    public void createPacketMine(@NotNull String mineId, @NotNull PacketMineConfig mineConfig) {
        AbstractMine mine = new PacketMine(skyMines, localeManager, cooldownManager, mineTimeManager, bossBarManager, mineConfig);

        if(mine.isSetup()) mineDataManager.addMine(mineId, mine);
    }

    /**
     * Creates a new {@link WorldMine} and adds it the list of mines if it was created successfully.
     * @param mineId The id of the mine being created.
     * @param mineConfig The {@link WorldMineConfig} for the mine being created.
     */
    public void createWorldMine(@NotNull String mineId, @NotNull WorldMineConfig mineConfig) {
        AbstractMine mine = new WorldMine(skyMines, localeManager, blocksManager, bossBarManager, pdcManager, mineConfig);

        if(mine.isSetup()) mineDataManager.addMine(mineId, mine);
    }

    /**
     * Calls all active Mine's cleanup function and clears the list of active mines.
     * @param onDisable Is the plugin being disabled?
     */
    public void clearMines(boolean onDisable) {
        mineDataManager.clearMines(onDisable);
    }
}
