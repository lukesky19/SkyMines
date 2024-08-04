/*
    SkyNodes places a random configured schematic after a set period of time.
    Copyright (C) 2023  lukeskywlker19

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
package com.github.lukesky19.skynodes.configuration.locale;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public record LocaleConfiguration(
        String prefix,
        List<String> help,
        String reload,
        String configLoadError,
        String startTasksSuccess,
        String noTasksFound,
        String noNodesFound,
        String operationFailure,
        String clipboardLoadFailure,
        String noPermission,
        String unknownArgument,
        String missingArgumentTaskId,
        String missingArgumentNodeId,
        String nodePasteSuccess,
        String nodePasteFailure,
        String worldNotFound,
        String schematicListError,
        String schematicNotFound,
        String invalidLocation,
        String invalidSafeLocation,
        String invalidRegion,
        String blocksAllowedListError,
        String invalidBlockMaterial,
        String undo,
        String redo,
        String noUndo,
        String noRedo,
        String invalidTaskId,
        String invalidNodeId,
        String inGameOnly,
        String softDisable,
        String bypassedSafeTeleport,
        String bypassedBlockBreakCheck,
        String canMine,
        String canNotMine) {
}
