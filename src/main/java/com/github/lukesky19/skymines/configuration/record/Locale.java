/*
    SkyMines tracks blocks broken in specific regions, replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2025  lukeskywlker19

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
package com.github.lukesky19.skymines.configuration.record;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.List;

/**
 * The plugin's locale configuration
 * @param configVersion The config version of the file.
 * @param prefix The plugin's prefix.
 * @param help The plugin's help message
 * @param reload The plugin's reload message.
 * @param mineTimeGiven The message sent to the player when they are given time.
 * @param mineTimeGivenTo The message sent to the sender who ran the command to give players time.
 * @param mineTime The message sent to the player who ran the command to view their time for a mine.
 * @param mineNoAccess The message sent to the player when they don't have access to a mine.
 * @param mineNoPlace The message sent to the player tries to place blocks in a mine.
 * @param cooldown The message sent to the player when they try to mine a block they have already mined and is that block is on cooldown.
 * @param canNotMine The message sent to the player when the block mined is not allowed to be mined.
 * @param timeMessage The message used in the boss bar and when a player views their mine time using a command.
 */
@ConfigSerializable
public record Locale(
        String configVersion,
        String prefix,
        List<String> help,
        String reload,
        String mineTimeGiven,
        String mineTimeGivenTo,
        String mineTime,
        String mineNoAccess,
        String mineNoPlace,
        String cooldown,
        String canNotMine,
        TimeMessage timeMessage) {
    /**
     *
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
