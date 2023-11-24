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
