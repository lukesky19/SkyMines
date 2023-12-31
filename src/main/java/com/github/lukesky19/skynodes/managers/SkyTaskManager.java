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
import com.github.lukesky19.skynodes.records.SkyNode;
import com.github.lukesky19.skynodes.records.SkyTask;
import com.github.lukesky19.skynodes.utils.ConfigLoaderUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class SkyTaskManager {
    // Constructor
    public SkyTaskManager(SkyNodes plugin, ConfigLoaderUtil configLoaderUtil, MessagesManager messagesManager, SkyNodeManager skyNodeManager) {
        this.plugin = plugin;
        this.configLoaderUtil = configLoaderUtil;
        this.messagesManager = messagesManager;
        this.skyNodeManager = skyNodeManager;
    }

    // Variables
    final SkyNodes plugin;
    final ConfigLoaderUtil configLoaderUtil;
    final MessagesManager messagesManager;
    final SkyNodeManager skyNodeManager;
    List<SkyTask> tasksList;

    // Getter(s)
    public List<SkyTask> getSkyTasksList() {
        return tasksList;
    }

    /**
     * Create a SkyTask for each task in nodes.yml.
     */
    public void createSkyTasks() {
        Messages messages = messagesManager.getMessages();
        tasksList = new ArrayList<>();
        List<CommentedConfigurationNode> comConfNodeTasksList = configLoaderUtil.getConfigSection(configLoaderUtil.getNodesConfig(), "tasks");
        for(CommentedConfigurationNode taskComConfNode : comConfNodeTasksList) {
            String taskId = Objects.requireNonNull(taskComConfNode.key()).toString();
            int delaySeconds = taskComConfNode.node("delay").getInt();
            List<SkyNode> skyNodesList = skyNodeManager.loadSkyNodes(taskComConfNode);
            if(!skyNodesList.isEmpty()) {
                tasksList.add(new SkyTask(taskId, delaySeconds, skyNodesList));
            } else {
                plugin.getLogger().log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                        messages.prefix().append(
                                MiniMessage.miniMessage().deserialize(messages.noNodesFound(),
                                        Placeholder.parsed("taskid", taskId)))));
            }
        }
    }
}
