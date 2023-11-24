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
import com.github.lukesky19.skynodes.records.Node;
import com.github.lukesky19.skynodes.records.Task;
import com.github.lukesky19.skynodes.utils.ConfigurateUtil;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.ArrayList;
import java.util.Objects;

public class TaskManager {
    // Constructor
    public TaskManager(SkyNodes plugin) {
        this.plugin = plugin;
        cfgMgr = plugin.getCfgMgr();
        nodeMgr = plugin.getNodeMgr();
        confUtil = plugin.getConfUtil();
    }
    // Variables
    final SkyNodes plugin;
    final ConfigManager cfgMgr;
    final NodeManager nodeMgr;
    final ConfigurateUtil confUtil;
    final ArrayList<Task> tasksList = new ArrayList<>();
    final ArrayList<Node> nodesList = new ArrayList<>();
    // Getter(s)
    public ArrayList<Task> getTasksList() {
        return tasksList;
    }

    private void loadTasks() {
        ArrayList<CommentedConfigurationNode> comConfNode_Tasks = confUtil.getConfigSection(cfgMgr.getNodesConfig(), "tasks");
        for(CommentedConfigurationNode task : comConfNode_Tasks) {
            ArrayList<CommentedConfigurationNode> comConfNode_Nodes = confUtil.getConfigSection(task, "nodes");
            for(CommentedConfigurationNode node : comConfNode_Nodes) {
                nodesList.add(nodeMgr.createNode(node));
            }
            tasksList.add(new Task(Objects.requireNonNull(task.key()).toString(), nodesList));
        }
    }

    public void reloadTasks() {
        loadTasks();
    }
}
