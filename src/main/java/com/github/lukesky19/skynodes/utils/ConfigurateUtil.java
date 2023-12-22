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
package com.github.lukesky19.skynodes.utils;

import com.github.lukesky19.skynodes.SkyNodes;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public class ConfigurateUtil {
    final SkyNodes plugin;
    public ConfigurateUtil(SkyNodes plugin) {
        this.plugin = plugin;
    }
    public YamlConfigurationLoader getYamlConfigurationLoader(final Path path) {
        return YamlConfigurationLoader.builder()
                .nodeStyle(NodeStyle.BLOCK)
                .path(path)
                .build();
    }
    public ArrayList<CommentedConfigurationNode> getConfigSection(CommentedConfigurationNode commentedConfigurationNode, String path) {
        ArrayList<CommentedConfigurationNode> commentedConfigurationNodes = new ArrayList<>();
        Map<Object, CommentedConfigurationNode> configNodeMap = commentedConfigurationNode.node(path).childrenMap();
        for (Map.Entry<Object, CommentedConfigurationNode> entry : configNodeMap.entrySet()) {
            commentedConfigurationNodes.add(entry.getValue());
        }
        return commentedConfigurationNodes;
    }
}
