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
import com.github.lukesky19.skynodes.data.ConfigSettings;
import com.github.lukesky19.skynodes.data.ConfigMessages;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;

public class ConfigManager {
    static final Path nodeConfigPath = Path.of(SkyNodes.getInstance().getDataFolder() + "/nodes.yml");
    static final Path messagesConfigPath = Path.of(SkyNodes.getInstance().getDataFolder() + "/messages.yml");

    static CommentedConfigurationNode nodeConfig;
    static CommentedConfigurationNode messagesConfig;
    static ConfigSettings configSettings;
    static ConfigMessages configMessages;
    public static CommentedConfigurationNode getNodeConfig() {
        return nodeConfig;
    }
    public static CommentedConfigurationNode getMessagesConfig() {
        return messagesConfig;
    }
    public static ConfigSettings getConfigSettings() {
        return configSettings;
    }
    public static ConfigMessages getConfigMessages() {
        return configMessages;
    }

    public static void loadConfig() {
        // Node Config
        YamlConfigurationLoader loader;
        loader = YamlConfigurationLoader.builder()
                .nodeStyle(NodeStyle.BLOCK)
                .path(nodeConfigPath)
                .build();
        try {
            nodeConfig = loader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        // Messages Config
        loader = YamlConfigurationLoader.builder()
                .nodeStyle(NodeStyle.BLOCK)
                .path(messagesConfigPath)
                .build();
        try {
            messagesConfig = loader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        // Load plugin settings.
        try {
            configSettings = ConfigSettings.loadConfigSettings(getNodeConfig());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Load plugin messages.
        try {
            configMessages = ConfigMessages.loadPluginMessages(getMessagesConfig());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyDefaultConfig() {
        if (!nodeConfigPath.toFile().exists())
            SkyNodes.getInstance().saveResource("nodes.yml", false);
        if (!messagesConfigPath.toFile().exists())
            SkyNodes.getInstance().saveResource("messages.yml", false);
    }
}
