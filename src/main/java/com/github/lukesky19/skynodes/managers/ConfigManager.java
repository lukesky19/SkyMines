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
import com.github.lukesky19.skynodes.utils.ConfigurateUtil;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;

public class ConfigManager {
    final SkyNodes plugin;
    final ConfigurateUtil confUtil;
    public ConfigManager(SkyNodes plugin) {
        this.plugin = plugin;
        confUtil = plugin.getConfUtil();
        nodeConfigPath = Path.of(plugin.getDataFolder() + "/nodes.yml");
        messagesConfigPath = Path.of(plugin.getDataFolder() + "/messages.yml");
    }
    final Path nodeConfigPath;
    final Path messagesConfigPath;
    CommentedConfigurationNode nodesConfig;
    CommentedConfigurationNode messagesConfig;

    public CommentedConfigurationNode getNodesConfig() {
        return nodesConfig;
    }
    public CommentedConfigurationNode getMessagesConfig() {
        return messagesConfig;
    }

    public void reloadConfig() {
        saveDefaultConfig();
        // Load Node Config
        YamlConfigurationLoader loader;
        loader = confUtil.getYamlConfigurationLoader(nodeConfigPath);
        try {
            nodesConfig = loader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        // Load Messages Config
        loader = confUtil.getYamlConfigurationLoader(messagesConfigPath);
        try {
            messagesConfig = loader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveDefaultConfig() {
        if (!nodeConfigPath.toFile().exists())
            plugin.saveResource("nodes.yml", false);
        if (!messagesConfigPath.toFile().exists())
            plugin.saveResource("messages.yml", false);
    }
}
