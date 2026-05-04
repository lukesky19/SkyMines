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
package com.github.lukesky19.skymines.mine;

import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.block.BlockDataManager;
import com.github.lukesky19.skymines.bossbar.BossBarManager;
import com.github.lukesky19.skymines.locale.LocaleManager;
import com.github.lukesky19.skymines.mine.config.PacketMineConfig;
import com.github.lukesky19.skymines.mine.config.WorldMineConfig;
import com.github.lukesky19.skymines.mine.impl.PacketMine;
import com.github.lukesky19.skymines.mine.impl.WorldMine;
import com.github.lukesky19.skymines.mine.interfaces.Mine;
import com.github.lukesky19.skymines.player.CooldownManager;
import com.github.lukesky19.skymines.player.MineBlockManager;
import com.github.lukesky19.skymines.player.MineTimeManager;
import com.github.lukesky19.skymines.player.PlayerDataManager;
import com.github.lukesky19.skymines.settings.SettingsManager;
import org.jspecify.annotations.NonNull;

/**
 * This class manages the creation of mines from their config files from {@link MineConfigManager}.
 * The actual {@link Mine}s are stored in {@link MineDataManager}.
 */
public class MineManager {
    private final @NonNull SkyMines skyMines;
    private final SettingsManager settingsManager;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull MineConfigManager mineConfigManager;
    private final @NonNull MineDataManager mineDataManager;
    private final @NonNull CooldownManager cooldownManager;
    private final @NonNull MineTimeManager mineTimeManager;
    private final @NonNull PlayerDataManager playerDataManager;
    private final @NonNull BossBarManager bossBarManager;
    private final @NonNull MineBlockManager blocksManager;
    private final @NonNull BlockDataManager pdcManager;

    /**
     * Default Constructor.
     * You should use {@link #MineManager(SkyMines, SettingsManager, LocaleManager, MineConfigManager, MineDataManager, CooldownManager, MineTimeManager, PlayerDataManager, BossBarManager, MineBlockManager)} instead.
     * @deprecated You should use {@link #MineManager(SkyMines, SettingsManager, LocaleManager, MineConfigManager, MineDataManager, CooldownManager, MineTimeManager, PlayerDataManager, BossBarManager, MineBlockManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public MineManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param mineConfigManager A {@link MineConfigManager} instance.
     * @param mineDataManager A {@link MineDataManager} instance.
     * @param cooldownManager A {@link CooldownManager} instance.
     * @param mineTimeManager A {@link MineTimeManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param bossBarManager A {@link BossBarManager} instance.
     * @param blocksManager A {@link MineBlockManager} instance.
     */
    public MineManager(
            @NonNull SkyMines skyMines, SettingsManager settingsManager,
            @NonNull LocaleManager localeManager,
            @NonNull MineConfigManager mineConfigManager,
            @NonNull MineDataManager mineDataManager,
            @NonNull CooldownManager cooldownManager,
            @NonNull MineTimeManager mineTimeManager,
            @NonNull PlayerDataManager playerDataManager,
            @NonNull BossBarManager bossBarManager,
            @NonNull MineBlockManager blocksManager) {
        this.skyMines = skyMines;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.mineConfigManager = mineConfigManager;
        this.mineDataManager = mineDataManager;
        this.cooldownManager = cooldownManager;
        this.mineTimeManager = mineTimeManager;
        this.playerDataManager = playerDataManager;
        this.bossBarManager = bossBarManager;
        this.blocksManager = blocksManager;
        this.pdcManager = new BlockDataManager();
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
    public void createPacketMine(@NonNull String mineId, @NonNull PacketMineConfig mineConfig) {
        Mine mine = new PacketMine(skyMines, settingsManager, localeManager, playerDataManager, cooldownManager, mineTimeManager, bossBarManager, mineConfig);

        if(mine.isSetup()) mineDataManager.addMine(mineId, mine);
    }

    /**
     * Creates a new {@link WorldMine} and adds it the list of mines if it was created successfully.
     * @param mineId The id of the mine being created.
     * @param mineConfig The {@link WorldMineConfig} for the mine being created.
     */
    public void createWorldMine(@NonNull String mineId, @NonNull WorldMineConfig mineConfig) {
        Mine mine = new WorldMine(skyMines, settingsManager, localeManager, playerDataManager, blocksManager, bossBarManager, pdcManager, mineConfig);

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
