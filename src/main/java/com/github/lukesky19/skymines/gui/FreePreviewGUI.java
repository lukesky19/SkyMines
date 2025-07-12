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
package com.github.lukesky19.skymines.gui;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.world.WorldMineConfig;
import com.github.lukesky19.skymines.data.config.world.WorldMineGUIConfig;
import com.github.lukesky19.skymines.manager.gui.GUIManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This GUI allows players to view free blocks for a world mine.
 */
public class FreePreviewGUI extends ChestGUI {
    // Config
    private final @NotNull String mineId;
    private final @NotNull WorldMineConfig mineConfig;
    private final @NotNull WorldMineGUIConfig guiConfig;

    // Player
    private final @NotNull UUID uuid;

    // GUI data
    private int pageNum = 0;
    private int currentPreviewKey = 0;
    private int numOfPreviewsAdded = 0;
    private int numOfPreviewsErrored = 0;
    private final @NotNull Map<Integer, Integer> previewsAddedPerPage = new HashMap<>();
    private final @NotNull Map<Integer, Integer> previewsErroredPerPage = new HashMap<>();

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param player The {@link Player} this GUI is being created for.
     * @param mineId The mine id the gui is for.
     * @param mineConfig The {@link WorldMineConfig} for the mine.
     * @param guiConfig The {@link WorldMineGUIConfig}.
     */
    public FreePreviewGUI(
            @NotNull SkyMines skyMines,
            @NotNull GUIManager guiManager,
            @NotNull Player player,
            @NotNull String mineId,
            @NotNull WorldMineConfig mineConfig,
            @NotNull WorldMineGUIConfig guiConfig) {
        super(skyMines, guiManager, player);
        this.uuid = player.getUniqueId();
        this.mineId = mineId;
        this.mineConfig = mineConfig;
        this.guiConfig = guiConfig;
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the block previews shop due to an invalid GUIType"));
            return false;
        }

        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

        String guiName = Objects.requireNonNullElse(guiConfig.guiName(), "");

        return create(guiType, guiName, placeholders);
    }

    /**
     * A method to create all the buttons in the inventory GUI.
     * @return A {@link CompletableFuture} containing a {@link Boolean} where true is successful, otherwise false.
     */
    @Override
    public boolean update() {
        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to add buttons to the GUI as the InventoryView was not created."));
            return false;
        }

        // If the items per page was not configured log a warning and return false.
        if(guiConfig.itemsPerPage() == null) {
            logger.warn(AdventureUtil.serialize("Unable to add buttons to the GUI as the items per page is not configured."));
            return false;
        }
        int itemsPerPage = guiConfig.itemsPerPage();

        // Get the GUI size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        createFillerButtons(guiSize);
        createPreviewButtons(itemsPerPage);

        previewsAddedPerPage.put(pageNum, numOfPreviewsAdded);
        previewsErroredPerPage.put(pageNum, numOfPreviewsErrored);

        if(numOfPreviewsAdded >= itemsPerPage && currentPreviewKey <= (mineConfig.freeBreakable().size() - 1)) {
            createNextPageButton();
        }

        if(pageNum > 0) {
            createPreviousPageButton();
        }

        createExitButton();

        return super.update();
    }

    /**
     * Refreshes the current buttons displayed.
     * @return @return A {@link CompletableFuture} containing a {@link Boolean} where true is successful, otherwise false.
     */
    @Override
    public boolean refresh() {
        int previewsErroredCurrentPage = previewsErroredPerPage.get(pageNum);
        int previewsAddedCurrentPage = previewsAddedPerPage.get(pageNum);

        previewsErroredPerPage.remove(pageNum);
        previewsAddedPerPage.remove(pageNum);

        currentPreviewKey = currentPreviewKey - ((previewsErroredCurrentPage + previewsAddedCurrentPage));

        numOfPreviewsAdded = 0;
        numOfPreviewsErrored = 0;

        return super.refresh();
    }

    /**
     * Handles when the GUI is closed by the player.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);
    }

    /**
     * This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Create and add the filler buttons.
     * @param guiSize The size of the Inventory/GUI.
     */
    private void createFillerButtons(int guiSize) {
        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiConfig.filler().displayItem();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, null, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);

            GUIButton fillerButton = guiButtonBuilder.build();

            for (int i = 0; i <= (guiSize - 1); i++) {
                setButton(i, fillerButton);
            }
        });
    }

    /**
     * Create the items displayed for the blocks that can be freely broken.
     * @param itemsPerPage The number of items to display per page.
     */
    private void createPreviewButtons(int itemsPerPage) {
        List<Integer> slots = new ArrayList<>(guiConfig.slots());

        while(numOfPreviewsAdded < itemsPerPage) {
            if(currentPreviewKey >= mineConfig.freeBreakable().size() || slots.isEmpty()) return;

            WorldMineConfig.FreeBlockData freeBlockData = mineConfig.freeBreakable().get(currentPreviewKey);
            if(freeBlockData.blockType() == null) {
                logger.warn(AdventureUtil.serialize("For mine " + mineId + " a block type is null for preview key: " + currentPreviewKey));
                handlePreviewError();
                continue;
            }

            Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(logger, freeBlockData.blockType());
            if(optionalBlockType.isEmpty()) {
                logger.warn(AdventureUtil.serialize("For mine " + mineId + " a block type of name " + freeBlockData.blockType() + " is invalid for preview key: " + currentPreviewKey));
                handlePreviewError();
                continue;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(freeBlockData.displayItem(), player, null, List.of());

            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresentOrElse(itemStack -> {
                GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                guiButtonBuilder.setItemStack(itemStack);

                int slot = slots.removeFirst();
                setButton(slot, guiButtonBuilder.build());

                currentPreviewKey++;
                numOfPreviewsAdded++;
            }, this::handlePreviewError);
        }
    }

    /**
     * Create the button to go to the previous page.
     */
    private void createPreviousPageButton() {
        // Check if the slot is not configured and send a warning.
        if(guiConfig.prevPage().slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to add a previous page button due to a slot not being configured."));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiConfig.prevPage().displayItem();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, null, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(event -> {
                int previewsErroredCurrentPage = previewsErroredPerPage.get(pageNum);
                int previewsAddedCurrentPage = previewsAddedPerPage.get(pageNum);
                int previewsErroredPrevPage = previewsErroredPerPage.get(pageNum - 1);
                int previewsAddedPrevPage = previewsAddedPerPage.get(pageNum - 1);

                currentPreviewKey = currentPreviewKey - ((previewsErroredCurrentPage + previewsAddedCurrentPage) + (previewsErroredPrevPage + previewsAddedPrevPage));

                numOfPreviewsAdded = 0;
                numOfPreviewsErrored = 0;
                pageNum--;

                this.update();
            });

            setButton(guiConfig.prevPage().slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create the button to go to the next page.
     */
    private void createNextPageButton() {
        // Check if the slot is not configured and send a warning.
        if(guiConfig.nextPage().slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to add a next page button due to a slot not being configured."));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiConfig.nextPage().displayItem();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, null, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(event -> {
                numOfPreviewsAdded = 0;
                numOfPreviewsErrored = 0;
                pageNum++;

                this.update();
            });

            setButton(guiConfig.nextPage().slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create the button to exit the GUI.
     */
    private void createExitButton() {
        // Check if the slot is not configured and send a warning.
        if(guiConfig.exit().slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to add a previous page button due to a slot not being configured."));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiConfig.exit().displayItem();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, null, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(event -> {
                close();
            });

            setButton(guiConfig.exit().slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Handle when an preview button cannot be shown due to an error.
     */
    private void handlePreviewError() {
        currentPreviewKey++;
        numOfPreviewsErrored++;
    }
}
