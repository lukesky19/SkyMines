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
package com.github.lukesky19.skymines.util.enums;

/**
 * The results from unlocking a block.
 */
public enum BlockUnlockResult {
    /**
     * Unlocked failed due to invalid plugin settings.
     */
    INVALID_SETTINGS,
    /**
     * Unlock failed due to an invalid LuckPerms' user.
     */
    INVALID_USER,
    /**
     * Unlock failed due to the player being excluded.
     */
    PLAYER_EXCLUDED,
    /**
     * The unlock was successful.
     */
    SUCCESS
}