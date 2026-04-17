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

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration to create the shop GUI and free blocks GUI.
 * @param version The config version.
 * @param guiType The {@link GUIType} to display.
 * @param guiName The name of the GUI to show in the Inventory.
 * @param itemsPerPage The items per page to display.
 * @param filler The filler buttons configuration.
 * @param nextPage The next page button configuration.
 * @param prevPage The previous page button configuration.
 * @param exit The exit button configuration.
 * @param currency The currency toggle button.
 * @param dummyButtons A {@link List} of {@link ButtonConfig} to display in the GUI. These buttons are like filler, but can have a configured slot.
 * @param slots The slots to display items in.
 */
@ConfigSerializable
public record WorldMineShopConfig(
        int version,
        @Nullable GUIType guiType,
        @Nullable String guiName,
        @Nullable Integer itemsPerPage,
        WorldMineShopConfig.ButtonConfig filler,
        WorldMineShopConfig.ButtonConfig nextPage,
        WorldMineShopConfig.ButtonConfig prevPage,
        WorldMineShopConfig.ButtonConfig exit,
        WorldMineShopConfig.ButtonConfig currency,
        @NonNull List<ButtonConfig> dummyButtons,
        @NonNull List<Integer> slots) {
    /**
     * This record contains the config to display buttons in the GUI.
     * @param slot The slot to display the button at.
     * @param displayItem The {@link ItemStackConfig} for the button.
     */
    @ConfigSerializable
    public record ButtonConfig(
            @Nullable Integer slot,
            @NonNull ItemStackConfig displayItem) {}
}
