package com.github.lukesky19.skymines.gui.config;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration to create the shop GUI.
 * @param version The config version.
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
public record WorldMinePreviewConfig(
        int version,
        @Nullable GUIType guiType,
        @Nullable String guiName,
        @Nullable Integer itemsPerPage,
        WorldMineShopConfig.ButtonConfig filler,
        WorldMineShopConfig.ButtonConfig nextPage,
        WorldMineShopConfig.ButtonConfig prevPage,
        WorldMineShopConfig.ButtonConfig exit,
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