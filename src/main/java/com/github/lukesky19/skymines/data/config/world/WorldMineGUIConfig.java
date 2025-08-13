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
package com.github.lukesky19.skymines.data.config.world;

import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration to create the shop GUI and free blocks GUI.
 * @param configVersion The config version of the file.
 * @param guiType The {@link GUIType} to display.
 * @param guiName The name of the GUI to show in the Inventory.
 * @param itemsPerPage The items per page to display.
 * @param filler The filler buttons configuration.
 * @param nextPage The next page button configuration.
 * @param prevPage The previous page button configuration.
 * @param exit The exit button configuration.
 * @param dummyButtons A {@link List} of {@link ButtonConfig} to display in the GUI. These buttons are like filler, but can have a configured slot.
 * @param slots The slots to display items in.
 */
@ConfigSerializable
public record WorldMineGUIConfig(
        @Nullable String configVersion,
        @Nullable GUIType guiType,
        @Nullable String guiName,
        @Nullable Integer itemsPerPage,
        @NotNull WorldMineGUIConfig.ButtonConfig filler,
        @NotNull WorldMineGUIConfig.ButtonConfig nextPage,
        @NotNull WorldMineGUIConfig.ButtonConfig prevPage,
        @NotNull WorldMineGUIConfig.ButtonConfig exit,
        @NotNull List<ButtonConfig> dummyButtons,
        @NotNull List<Integer> slots) {
    /**
     * This record contains the config to display buttons in the GUI.
     * @param slot The slot to display the button at.
     * @param displayItem The {@link ItemStackConfig} for the button.
     */
    @ConfigSerializable
    public record ButtonConfig(
            @Nullable Integer slot,
            @NotNull ItemStackConfig displayItem) {}
}
