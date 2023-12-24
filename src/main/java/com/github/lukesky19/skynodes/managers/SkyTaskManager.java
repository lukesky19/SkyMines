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
import com.github.lukesky19.skynodes.utils.ConfigurateUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkyTaskManager {

    final SkyNodes plugin;
    final Logger logger;
    final ConfigManager cfgMgr;
    final MessagesManager msgsMgr;
    final NodeManager nodeMgr;
    final ConfigurateUtil confUtil;
    List<SkyTask> tasksList;
    public List<SkyTask> getSkyTasksList() {
        return tasksList;
    }

    public SkyTaskManager(SkyNodes plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.cfgMgr = plugin.getCfgMgr();
        this.msgsMgr = plugin.getMsgsMgr();
        this.nodeMgr = plugin.getNodeMgr();
        this.confUtil = plugin.getConfUtil();
    }

    /**
     * Create a SkyTask for each task in nodes.yml.
     */
    public void createSkyTasks() {
        Messages messages = msgsMgr.getMessages();
        tasksList = new ArrayList<>();
        List<CommentedConfigurationNode> comConfNodeTasksList = confUtil.getConfigSection(cfgMgr.getNodesConfig(), "tasks");
        for(CommentedConfigurationNode taskComConfNode : comConfNodeTasksList) {
            String taskId = Objects.requireNonNull(taskComConfNode.key()).toString();
            int delaySeconds = taskComConfNode.node("delay").getInt();
            List<SkyNode> skyNodesList = nodeMgr.loadSkyNodes(taskComConfNode);
            if(!skyNodesList.isEmpty()) {
                tasksList.add(new SkyTask(taskId, delaySeconds, skyNodesList));
            } else {
                logger.log(Level.WARNING, ANSIComponentSerializer.ansi().serialize(
                        messages.prefix().append(
                                MiniMessage.miniMessage().deserialize(messages.noNodesFound(),
                                        Placeholder.parsed("taskid", taskId)))));
            }
        }
    }
}
