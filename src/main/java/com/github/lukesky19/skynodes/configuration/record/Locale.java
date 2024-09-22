/*
    SkyNodes tracks blocks broken in specific regions (nodes), replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2024  lukeskywlker19

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
package com.github.lukesky19.skynodes.configuration.record;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public record Locale(
        String configVersion,
        String prefix,
        List<String> help,
        String reload,
        String noPermission,
        String unknownArgument,
        String inGameOnly,
        String configLoadError,
        String invalidParentRegion,
        String invalidChildRegion,
        String blocksAllowedListError,
        String invalidBlockMaterial,
        String invalidReplacementMaterial,
        String invalidDelaySeconds,
        String cooldown,
        String canNotMine) {
}
