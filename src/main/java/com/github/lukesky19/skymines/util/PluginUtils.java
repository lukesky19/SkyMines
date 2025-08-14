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
package com.github.lukesky19.skymines.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * This class contains methods used throughout the plugin.
 */
public class PluginUtils {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public PluginUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Create a new {@link Location} that removes the yaw and pitch.
     * @param location The {@link Location} to clean.
     * @return A {@link Location} without the yaw and pitch.
     */
    public static @NotNull Location getCleanLocation(@NotNull Location location) {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
