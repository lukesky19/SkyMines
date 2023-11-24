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
package com.github.lukesky19.skynodes.managers;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.records.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessagesManager {
    final SkyNodes plugin;
    final MiniMessage mm = MiniMessage.miniMessage();
    Messages messages;
    public MessagesManager(SkyNodes plugin)  {
        this.plugin = plugin;
    }
    public Messages getMessages() {
        return messages;
    }

    private void loadPluginMessages(CommentedConfigurationNode configurationNode) {
        try {
            List<Component> helpMessage = new ArrayList<>();
            for(String message : Objects.requireNonNull(configurationNode.node("Help").getList(String.class))) {
                helpMessage.add(mm.deserialize(message));
            }

            messages = new Messages(
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("Prefix").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("Reload").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("StartTasksSuccess").getString())),
                    helpMessage,
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("NoNodesFound").getString())),
                    configurationNode.node("ConsoleNodePasteSuccess").getString(),
                    configurationNode.node("ConsoleNodePasteFailure").getString(),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("OperationFailure").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("ClipboardLoadFailure").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("NoPermission").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("UnknownArgument").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("PlayerNodePasteSuccess").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("PlayerNodePasteFailure").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("PlayerSchematicNotFound").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("MissingWorldEditOrFastAsyncWorldEdit").getString())),
                    configurationNode.node("WorldNotFound").getString(),
                    configurationNode.node("SchematicsListError").getString(),
                    configurationNode.node("SchematicNotFound").getString(),
                    configurationNode.node("InvalidLocation").getString(),
                    configurationNode.node("InvalidSafeLocation").getString(),
                    configurationNode.node("InvalidRegion").getString(),
                    configurationNode.node("BlocksAllowedListError").getString(),
                    configurationNode.node("InvalidBlockMaterial").getString(),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("BypassedSafeTeleport").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("BypassedBlockBreakCheck").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("CanMine").getString())),
                    mm.deserialize(Objects.requireNonNull(configurationNode.node("CanNotMine").getString())));
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadMessages() {
        loadPluginMessages(plugin.getCfgMgr().getMessagesConfig());
    }
}
