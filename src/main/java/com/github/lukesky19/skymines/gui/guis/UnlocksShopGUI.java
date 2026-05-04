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
package com.github.lukesky19.skymines.gui.guis;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.adventure.PaperAdventureUtility;
import com.github.lukesky19.skylib.paper.api.format.FormatUtil;
import com.github.lukesky19.skylib.paper.api.gui.GUIButton;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skylib.paper.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.gui.config.WorldMineShopConfig;
import com.github.lukesky19.skymines.integration.HookManager;
import com.github.lukesky19.skymines.integration.hooks.EconomyHook;
import com.github.lukesky19.skymines.integration.hooks.PlayerPointsHook;
import com.github.lukesky19.skymines.locale.Locale;
import com.github.lukesky19.skymines.locale.LocaleManager;
import com.github.lukesky19.skymines.mine.config.WorldMineConfig;
import com.github.lukesky19.skymines.player.MineBlockManager;
import com.github.lukesky19.skymines.util.enums.BlockUnlockResult;
import com.github.lukesky19.skymines.util.enums.Currency;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This GUI allows players to unlock blocks for a world mine by purchasing them.
 */
public class UnlocksShopGUI extends ChestGUI<UUID> {
    // Plugin Data
    private final @NonNull LocaleManager localeManager;
    private final @NonNull MineBlockManager blocksManager;
    private final @NonNull HookManager hookManager;
    // Player data
    private final @NonNull UUID uuid;
    // Config Data
    private final @NonNull String mineId;
    private final @NonNull WorldMineConfig mineConfig;
    private final @NonNull WorldMineShopConfig guiConfig;
    // GUI data
    private int pageNum = 0;
    private int currentUnlockKey = 0;
    private int numOfUnlocksAdded = 0;
    private int numOfUnlocksErrored = 0;
    private final @NonNull Map<Integer, Integer> unlocksAddedPerPage = new HashMap<>();
    private final @NonNull Map<Integer, Integer> unlocksErroredPerPage = new HashMap<>();
    private @NonNull Currency currency = Currency.MONEY;

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param guiManager A {@link UUIDGUIManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param player The {@link Player} this GUI is being created for.
     * @param blocksManager A {@link MineBlockManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param mineId The mine id the gui is for.
     * @param mineConfig The {@link WorldMineConfig} for the mine.
     * @param guiConfig The {@link WorldMineShopConfig}.
     */
    public UnlocksShopGUI(
            @NonNull SkyMines skyMines,
            @NonNull UUIDGUIManager guiManager,
            @NonNull Player player,
            @NonNull LocaleManager localeManager,
            @NonNull MineBlockManager blocksManager,
            @NonNull HookManager hookManager,
            @NonNull String mineId,
            @NonNull WorldMineConfig mineConfig,
            @NonNull WorldMineShopConfig guiConfig) {
        super(skyMines, guiManager, player.getUniqueId(), player);
        this.localeManager = localeManager;
        this.uuid = player.getUniqueId();
        this.blocksManager = blocksManager;
        this.hookManager = hookManager;
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
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the block unlocks shop due to an invalid GUIType"));
            return false;
        }

        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("mine_id", mineId));

        String guiName = Objects.requireNonNullElse(guiConfig.guiName(), "");

        return create(guiType, guiName, placeholders);
    }

    /**
     * A method to create all the buttons in the inventory GUI.
     * @return true is successful, otherwise false.
     */
    @Override
    public boolean update() {
        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtility.plain("Unable to add buttons to the GUI as the InventoryView was not created."));
            return false;
        }

        // If the items per page was not configured log a warning and return false.
        if(guiConfig.itemsPerPage() == null) {
            logger.warn(AdventureUtility.plain("Unable to add buttons to the GUI as the items per page is not configured."));
            return false;
        }
        int itemsPerPage = guiConfig.itemsPerPage();

        // Get the GUI size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        createFillerButtons(guiSize);

        createDummyButtons();

        createUnlockButtons(itemsPerPage);

        unlocksAddedPerPage.put(pageNum, numOfUnlocksAdded);
        unlocksErroredPerPage.put(pageNum, numOfUnlocksErrored);

        if(numOfUnlocksAdded >= itemsPerPage && currentUnlockKey <= (mineConfig.unlockableBreakable().size() - 1)) {
            createNextPageButton();
        }

        if(pageNum > 0) {
            createPreviousPageButton();
        }

        createCurrencyButton();

        createExitButton();

        return super.update();
    }

    /**
     * Refreshes the current buttons displayed.
     * @return @return A {@link CompletableFuture} containing a {@link Boolean} where true is successful, otherwise false.
     */
    @Override
    public boolean refresh() {
        int unlocksErroredCurrentPage = unlocksErroredPerPage.get(pageNum);
        int unlocksAddedCurrentPage = unlocksAddedPerPage.get(pageNum);

        unlocksErroredPerPage.remove(pageNum);
        unlocksAddedPerPage.remove(pageNum);

        currentUnlockKey = currentUnlockKey - ((unlocksErroredCurrentPage + unlocksAddedCurrentPage));

        numOfUnlocksAdded = 0;
        numOfUnlocksErrored = 0;

        return super.refresh();
    }

    /**
     * Handles when the GUI is closed by the player.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NonNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);
    }

    /**
     * This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleBottomDrag(@NonNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleGlobalDrag(@NonNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NonNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleGlobalClick(@NonNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Create and add the filler buttons.
     * @param guiSize The size of the Inventory/GUI.
     */
    private void createFillerButtons(int guiSize) {
        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiConfig.filler().displayItem();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, List.of());

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
     * Create the items displayed for the blocks that can be unlocked.
     * @param itemsPerPage The number of items to display per page.
     */
    private void createUnlockButtons(int itemsPerPage) {
        Locale locale = localeManager.getConfiguration();
        List<Integer> slots = new ArrayList<>(guiConfig.slots());

        while(numOfUnlocksAdded < itemsPerPage) {
            if(currentUnlockKey >= mineConfig.unlockableBreakable().size() || slots.isEmpty()) return;

            WorldMineConfig.UnlockBlockData unlockBlockData = mineConfig.unlockableBreakable().get(currentUnlockKey);
            if(unlockBlockData.blockType() == null) {
                logger.warn(AdventureUtility.plain("For mine " + mineId + " a block type is null for unlock key: " + currentUnlockKey));
                handleUnlockError();
                continue;
            }

            Double money = Objects.requireNonNullElse(unlockBlockData.priceData().money(), -1.0);
            Integer points = Objects.requireNonNullElse(unlockBlockData.priceData().playerPoints(), -1);
            if(money <= -1 && points <= -1) {
                logger.warn(AdventureUtility.plain("For mine " + mineId + " a there is no configured buy price for unlock key: " + currentUnlockKey));
                handleUnlockError();
                continue;
            }

            List<TagResolver.Single> lorePlaceholders = new ArrayList<>();
            if(currency == Currency.MONEY) {
                String price = money >= 0 ? String.valueOf(money) : "-1";
                lorePlaceholders.add(Placeholder.parsed("price", price));
                lorePlaceholders.add(Placeholder.parsed("currency", locale.worldMineMessages().moneyCurrencyName()));
            } else {
                String price = points >= 0 ? String.valueOf(points) : "-1";
                lorePlaceholders.add(Placeholder.parsed("price", price));
                lorePlaceholders.add(Placeholder.parsed("currency", locale.worldMineMessages().playerPointsCurrencyName()));
            }

            BlockType blockType = unlockBlockData.blockType();
            ItemStackConfig itemStackConfig = blocksManager.isBlockTypeUnlocked(player, mineId, blockType)
                    ? unlockBlockData.displayItemUnlocked()
                    : unlockBlockData.displayItemLocked();

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(itemStackConfig, player, lorePlaceholders);

            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresentOrElse(itemStack -> {
                GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                guiButtonBuilder.setItemStack(itemStack);
                guiButtonBuilder.setAction(_ -> {
                    if(blocksManager.isBlockTypeUnlocked(player, mineId, blockType)) return;

                    EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
                    PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);
                    if(!isCurrencyValid(locale, economyHook, playerPointsHook, blockType, money, points)) return;

                    List<TagResolver.Single> unlockMessagePlaceholders = List.of(
                            Placeholder.parsed("block_type", FormatUtil.formatBlockTypeName(blockType)),
                            Placeholder.parsed("mine_id", mineId));

                    BlockUnlockResult result = blocksManager.addUnlockedBlock(player, mineId, blockType);
                    switch(result) {
                        case INVALID_SETTINGS -> player.sendMessage(PaperAdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockUnlockSettingsError(), unlockMessagePlaceholders));
                        case INVALID_USER -> player.sendMessage(PaperAdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockUnlockUserError(), unlockMessagePlaceholders));
                        case PLAYER_EXCLUDED -> player.sendMessage(PaperAdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockUnlockExcluded(), unlockMessagePlaceholders));
                        case SUCCESS -> {
                            player.sendMessage(PaperAdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockUnlocked(), unlockMessagePlaceholders));

                            removeCurrency(economyHook, playerPointsHook, money, points);
                        }
                    }

                    refresh();
                });

                int slot = slots.removeFirst();
                setButton(slot, guiButtonBuilder.build());

                currentUnlockKey++;
                numOfUnlocksAdded++;
            }, this::handleUnlockError);
        }
    }

    /**
     * Create the button to go to the previous page.
     */
    private void createPreviousPageButton() {
        // Check if the slot is not configured and send a warning.
        if(guiConfig.prevPage().slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add a previous page button due to a slot not being configured."));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiConfig.prevPage().displayItem();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(_ -> {
                int unlocksErroredCurrentPage = unlocksErroredPerPage.get(pageNum);
                int unlocksAddedCurrentPage = unlocksAddedPerPage.get(pageNum);
                int unlocksErroredPrevPage = unlocksErroredPerPage.get(pageNum - 1);
                int unlocksAddedPrevPage = unlocksAddedPerPage.get(pageNum - 1);

                currentUnlockKey = currentUnlockKey - ((unlocksErroredCurrentPage + unlocksAddedCurrentPage) + (unlocksErroredPrevPage + unlocksAddedPrevPage));

                numOfUnlocksAdded = 0;
                numOfUnlocksErrored = 0;
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
            logger.warn(AdventureUtility.plain("Unable to add a next page button due to a slot not being configured."));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiConfig.nextPage().displayItem();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(_ -> {
                numOfUnlocksAdded = 0;
                numOfUnlocksErrored = 0;
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
            logger.warn(AdventureUtility.plain("Unable to add a exit button due to a slot not being configured."));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiConfig.exit().displayItem();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(_ -> {
                close();
            });

            setButton(guiConfig.exit().slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create the button to change the currency used.
     */
    private void createCurrencyButton() {
        // Check if the slot is not configured and send a warning.
        if(guiConfig.currency().slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add the currency button due to a slot not being configured."));
            return;
        }

        Locale locale = localeManager.getConfiguration();

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiConfig.currency().displayItem();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(
                itemConfig,
                player,
                List.of(Placeholder.parsed("currency", currency == Currency.MONEY ?
                        locale.worldMineMessages().moneyCurrencyName() :
                        locale.worldMineMessages().playerPointsCurrencyName())));

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(_ -> {
                if(currency == Currency.MONEY) {
                    currency = Currency.PLAYER_POINTS;
                } else {
                    currency = Currency.MONEY;
                }

                refresh();
            });

            setButton(guiConfig.currency().slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        guiConfig.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtility.plain("Unable to add a dummy button to the unlocks shop GUI due to an invalid slot."));
                return;
            }

            ItemStackConfig itemStackConfig = buttonConfig.displayItem();
            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());
            Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresent(itemStack -> {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(itemStack);

                setButton(buttonConfig.slot(), builder.build());
            });
        });
    }

    /**
     * Handle when an unlockable button cannot be shown due to an error.
     */
    private void handleUnlockError() {
        currentUnlockKey++;
        numOfUnlocksErrored++;
    }

    /**
     * Checks if the player has the currency required currency to purchase the unlock.
     * @param locale The {@link Locale}.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param blockType The {@link BlockType} being unlocked.
     * @param money The money for the unlock or -1.0
     * @param points The points for the unlock or -1.
     * @return true if valid, false if not.
     */
    private boolean isCurrencyValid(
            @NonNull Locale locale,
            @NonNull EconomyHook economyHook,
            @NonNull PlayerPointsHook playerPointsHook,
            @NonNull BlockType blockType,
            double money,
            int points) {
        if(currency == Currency.MONEY) {
            List<TagResolver.Single> errorMessagePlaceholders = List.of(
                    Placeholder.parsed("block_type", FormatUtil.formatBlockTypeName(blockType)),
                    Placeholder.parsed("currency", locale.worldMineMessages().moneyCurrencyName()));

            if(!economyHook.isHooked()) {
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockNotPurchasable(), errorMessagePlaceholders));
                return false;
            }

            if(money <= -1) {
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockNotPurchasable(), errorMessagePlaceholders));
                return false;
            }

            if(economyHook.getBalance(player) < money) {
                player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.worldMineMessages().notEnoughCurrency(), errorMessagePlaceholders));
                close();
                return false;
            }
        } else {
            List<TagResolver.Single> errorMessagePlaceholders = List.of(
                    Placeholder.parsed("block_type", FormatUtil.formatBlockTypeName(blockType)),
                    Placeholder.parsed("currency", locale.worldMineMessages().playerPointsCurrencyName()));

            if(!playerPointsHook.isHooked()) {
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockNotPurchasable(), errorMessagePlaceholders));
                return false;
            }

            if(points <= -1) {
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.worldMineMessages().blockNotPurchasable(), errorMessagePlaceholders));
                return false;
            }

            if(playerPointsHook.getBalance(player) < points) {
                player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.worldMineMessages().notEnoughCurrency(), errorMessagePlaceholders));
                close();
                return false;
            }
        }

        return true;
    }

    /**
     * Remove the currency from the player's balance.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param money The money or -1.0.
     * @param points The points or -1.
     */
    private void removeCurrency(
            @NonNull EconomyHook economyHook,
            @NonNull PlayerPointsHook playerPointsHook,
            double money,
            int points) {
        if(currency == Currency.MONEY) {
            if(!economyHook.isHooked()) return;
            if(money <= -1) return;
            if(economyHook.getBalance(player) < money) return;

            economyHook.removeFromBalance(player, money);
        } else {
            if(!playerPointsHook.isHooked()) return;
            if(points <= -1) return;
            if(playerPointsHook.getBalance(player) < points) return;

            playerPointsHook.removeFromBalance(player, points);
        }
    }
}