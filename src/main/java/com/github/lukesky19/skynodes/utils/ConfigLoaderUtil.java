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
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public final class ConfigLoaderUtil {
    // Constructor
    public ConfigLoaderUtil(SkyNodes plugin) {
        this.plugin = plugin;
    }

    // Variables
    final SkyNodes plugin;
    CommentedConfigurationNode nodesConfig;
    CommentedConfigurationNode messagesConfig;
    CommentedConfigurationNode settingsConfig;

    public CommentedConfigurationNode getNodesConfig() {
        return nodesConfig;
    }
    public CommentedConfigurationNode getMessagesConfig() {
        return messagesConfig;
    }
    public CommentedConfigurationNode getSettingsConfig() {
        return settingsConfig;
    }

    /**
     * Saves default config files if the files do not exist already.
     * (Re-)loads all necessary config files from disk or sets them to null.
     */
    public void reloadConfig() {
        Path settingsConfigPath =  Path.of(plugin.getDataFolder() + "/settings.yml");
        Path nodeConfigPath = Path.of(plugin.getDataFolder() + "/nodes.yml");
        Path messagesConfigPath = Path.of(plugin.getDataFolder() + "/messages.yml");

        saveDefaultConfig(settingsConfigPath, nodeConfigPath, messagesConfigPath);

        loadSettingsConfig(settingsConfigPath);
        loadNodesConfig(nodeConfigPath);
        loadMessagesConfig(messagesConfigPath);
    }

    private void saveDefaultConfig(Path settingsConfigPath, Path nodeConfigPath, Path messagesConfigPath) {
        if(!settingsConfigPath.toFile().exists()) plugin.saveResource("settings.yml", false);
        if(!nodeConfigPath.toFile().exists()) plugin.saveResource("nodes.yml", false);
        if(!messagesConfigPath.toFile().exists()) plugin.saveResource("messages.yml", false);
    }

    private void loadSettingsConfig(Path settingsConfigPath) {
        YamlConfigurationLoader loader = getYamlConfigurationLoader(settingsConfigPath);
        try {
            settingsConfig = loader.load();
        } catch (ConfigurateException e) {
            settingsConfig = null;
            throw new RuntimeException(e);
        }
    }

    private void loadNodesConfig(Path nodeConfigPath) {
        // Load Nodes Config
        YamlConfigurationLoader loader = getYamlConfigurationLoader(nodeConfigPath);
        try {
            nodesConfig = loader.load();
        } catch (ConfigurateException e) {
            nodesConfig = null;
            throw new RuntimeException(e);
        }
    }

    private void loadMessagesConfig(Path messagesConfigPath) {
        // Load Messages Config
        YamlConfigurationLoader loader = getYamlConfigurationLoader(messagesConfigPath);
        try {
            messagesConfig = loader.load();
        } catch (ConfigurateException e) {
            messagesConfig = null;
            throw new RuntimeException(e);
        }
    }

    private YamlConfigurationLoader getYamlConfigurationLoader(final Path path) {
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
