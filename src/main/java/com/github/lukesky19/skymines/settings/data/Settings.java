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
package com.github.lukesky19.skymines.settings.data;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The plugin's settings configuration.
 * @param version The config version.
 * @param locale The locale file name (without .yml) to use.
 * @param messageCooldownDurationSeconds The duration in seconds message cooldowns should last for.
 * @param blockUnlockExcludedGroups The list of {@link GroupConfig} to exclude from unlocking blocks in world-based mines.
 */
@ConfigSerializable
public record Settings(
        int version,
        @Nullable String locale,
        long messageCooldownDurationSeconds,
        @NonNull List<GroupConfig> blockUnlockExcludedGroups) {}
