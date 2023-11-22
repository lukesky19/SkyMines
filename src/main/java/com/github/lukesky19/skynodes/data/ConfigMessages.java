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
package com.github.lukesky19.skynodes.data;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public record ConfigMessages(
        String prefixMessage,
        String reloadMessage,
        List<String> helpMessage,
        String noNodesFoundMessage,
        String consoleNodePasteSuccessMessage,
        String consoleNodePasteFailureMessage,
        String operationFailureMessage,
        String clipboardLoadFailureMessage,
        String noPermissionMessage,
        String unknownArgumentMessage,
        String playerNodePasteSuccessMessage,
        String playerNodePasteFailureMessage,
        String playerSchematicNotFoundMessage,
        String missingWorldEditorFastAsyncWorldEditMessage,
        String worldNotFoundMessage,
        String schematicsListErrorMessage,
        String schematicNotFoundMessage,
        String invalidLocationMessage,
        String invalidSafeLocationMessage,
        String invalidRegionMessage,
        String blocksAllowedListErrorMessage,
        String invalidBlockMaterialMessage,
        String bypassedSafeTeleportMessage,
        String bypassedBlockBreakCheckMessage,
        String canMineMessage,
        String canNotMineMessage) {

    public static ConfigMessages loadPluginMessages(CommentedConfigurationNode configurationNode) {
        try {
            return new ConfigMessages(
                    configurationNode.node("Prefix").getString(),
                    configurationNode.node("Reload").getString(),
                    configurationNode.node("Help").getList(String.class),
                    configurationNode.node("NoNodesFound").getString(),
                    configurationNode.node("ConsoleNodePasteSuccess").getString(),
                    configurationNode.node("ConsoleNodePasteFailure").getString(),
                    configurationNode.node("OperationFailure").getString(),
                    configurationNode.node("ClipboardLoadFailure").getString(),
                    configurationNode.node("NoPermission").getString(),
                    configurationNode.node("UnknownArgument").getString(),
                    configurationNode.node("PlayerNodePasteSuccess").getString(),
                    configurationNode.node("PlayerNodePasteFailure").getString(),
                    configurationNode.node("PlayerSchematicNotFound").getString(),
                    configurationNode.node("MissingWorldEditOrFastAsyncWorldEdit").getString(),
                    configurationNode.node("WorldNotFound").getString(),
                    configurationNode.node("SchematicsListError").getString(),
                    configurationNode.node("SchematicNotFound").getString(),
                    configurationNode.node("InvalidLocation").getString(),
                    configurationNode.node("InvalidSafeLocation").getString(),
                    configurationNode.node("InvalidRegion").getString(),
                    configurationNode.node("BlocksAllowedListError").getString(),
                    configurationNode.node("InvalidBlockMaterial").getString(),
                    configurationNode.node("BypassedSafeTeleport").getString(),
                    configurationNode.node("BypassedBlockBreakCheck").getString(),
                    configurationNode.node("CanMine").getString(),
                    configurationNode.node("CanNotMine").getString());
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
