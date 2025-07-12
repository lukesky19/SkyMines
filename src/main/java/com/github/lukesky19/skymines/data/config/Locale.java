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
package com.github.lukesky19.skymines.data.config;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skymines.mine.PacketMine;
import com.github.lukesky19.skymines.mine.WorldMine;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The plugin's locale configuration
 * @param configVersion The config version of the file.
 * @param prefix The plugin's prefix.
 * @param help The plugin's help message
 * @param reload The plugin's reload message.
 * @param noMineWithId The message sent when a mine was not found for a particular mine id.
 * @param packetMineMessages The messages specific to {@link PacketMine}s.
 * @param worldMineMessages The messages specific to {@link WorldMine}s
 * @param timeMessage The message used in the boss bar and when a player views their mine time using a command.
 */
@ConfigSerializable
public record Locale(
        String configVersion,
        String prefix,
        List<String> help,
        String reload,
        String noMineWithId,
        String guiOpenError,
        @NotNull PacketMineMessages packetMineMessages,
        @NotNull WorldMineMessages worldMineMessages,
        TimeMessage timeMessage) {

    /**
     * This record contains messages related to packet mines.
     * @param mineTimeChanged The message sent to the player when they are given time.
     * @param mineTimeChangedTo The message sent to the sender who ran the command to give players time.
     * @param mineTime The message sent to the player who ran the command to view their time for a mine.
     * @param noMineTime The message sent to the player when they don't have time to use the mine.
     * @param playerMineTime The message sent to the player who is viewing another player's mine time.
     * @param playerNoMineTime The message sent to the player who is viewing another player's mine time, and they have none.
     * @param mineAccessNoTime The message sent the player attempting to use a mine and doesn't have time for the mine.
     * @param canNotBreakBlock The message sent to the player when the block mined is not allowed to be mined.
     * @param canNotPlaceBlock The message sent to the player tries to place blocks in a mine.
     * @param cooldown The message sent to the player when the block mined is on cooldown.
     * @param timeInvalidLessThenOne The message sent to the player modifying a player's mine time and the time is less than 1.
     * @param timeInvalidLessThenZero The message sent to the player modifying a player's mine time and the time is less than 0.
     */
    @ConfigSerializable
    public record PacketMineMessages(
            String mineTimeChanged,
            String mineTimeChangedTo,
            String mineTime,
            String noMineTime,
            String playerMineTime,
            String playerNoMineTime,
            String mineAccessNoTime,
            String canNotBreakBlock,
            String canNotPlaceBlock,
            String cooldown,
            String timeInvalidLessThenOne,
            String timeInvalidLessThenZero
    ) {}

    /**
     * This record contains messages related to world mines.
     * @param invalidBlockType The message sent to the player unlocking or locking a block for another player and the block type is invalid.
     * @param blockAlreadyUnlocked The message sent to the player unlocking a block for another player, but is already unlocked.
     * @param blockAlreadyLocked The message sent to the player locking a block for another player, but is already locked.
     * @param blockUnlocked The message sent to the player when a block is unlocked.
     * @param blockLocked The message sent to the player when a block is locked.
     * @param playerBlockUnlocked The message sent to the player who unlocked a block for another player on success.
     * @param playerBlockLocked The message sent to the player who locked a block for another player on success.
     * @param blockBreakNotUnlocked The message sent to the player trying to break a block, but the breaking was cancelled because the block was not unlocked.
     * @param blockBreakNotAllowed The message sent to the player trying to break a block, but the breaking was cancelled.
     * @param blockBreakNotPlayerPlaced The message sent to the player trying to break a block, but the breaking was cancelled because it was not a player-placed block.
     * @param blockBreakNotPlayerWaterLogged The message sent to the player trying to break a block, but the breaking was cancelled because it was not water logged by a player.
     * @param blockPlaceNotUnlocked The message sent to the player trying to place a block, but the placing was cancelled because the block was not unlocked.
     * @param blockPlaceNotAllowed The message sent to the player trying to place a block, but the placement was cancelled.
     * @param blockInteractionNotUnlocked The message sent to the player when attempting a block interaction, but the block is not unlocked.
     * @param blockInteractionNotAllowed The message sent to the player when attempting a block interaction, but is not allowed for said block.
     * @param notEnoughMoney The message sent to the player when trying to unlock a block through purchase, but lacks the funds to do so.
     * @param guiErrorNotInMine The message sent to the player attempting to open a shop GUI while not inside a mine.
     */
    @ConfigSerializable
    public record WorldMineMessages(
            String invalidBlockType,
            String blockAlreadyUnlocked,
            String blockAlreadyLocked,
            String blockUnlocked,
            String blockLocked,
            String playerBlockUnlocked,
            String playerBlockLocked,
            String blockBreakNotUnlocked,
            String blockBreakNotAllowed,
            String blockBreakNotPlayerPlaced,
            String blockBreakNotPlayerWaterLogged,
            String blockPlaceNotUnlocked,
            String blockPlaceNotAllowed,
            String blockInteractionNotUnlocked,
            String blockInteractionNotAllowed,
            String notEnoughMoney,
            String guiErrorNotInMine
    ) {}

    /**
     * @param prefix The text to display before the first time unit.
     * @param years The text to display when the player's time enters years.
     * @param months The text to display when the player's time enters months.
     * @param weeks The text to display when the player's time enters weeks.
     * @param days The text to display when the player's time enters days.
     * @param hours The text to display when the player's time enters hours.
     * @param minutes The text to display when the player's time enters minutes.
     * @param seconds The text to display when the player's time enters seconds.
     * @param suffix The text to display after the last time unit.
     */
    @ConfigSerializable
    public record TimeMessage(
            String prefix,
            String years,
            String months,
            String weeks,
            String days,
            String hours,
            String minutes,
            String seconds,
            String suffix) {}
}
