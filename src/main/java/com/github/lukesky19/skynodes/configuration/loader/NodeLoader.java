/*
    SkyNodes tracks blocks broken in specific regions (nodes), replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2024  lukeskywlker19

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
package com.github.lukesky19.skynodes.configuration.loader;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.configuration.record.Node;
import com.github.lukesky19.skynodes.utils.ConfigurationUtility;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

public class NodeLoader {
    final SkyNodes skyNodes;
    final ConfigurationUtility configurationUtility;
    Node node;

    public NodeLoader(
            SkyNodes skyNodes,
            ConfigurationUtility configurationUtility) {
        this.skyNodes = skyNodes;
        this.configurationUtility = configurationUtility;
    }

    public Node getNodes() {
        return node;
    }

    public void reload() {
        node = null;
        ComponentLogger logger = skyNodes.getComponentLogger();

        if(!skyNodes.isPluginEnabled()) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The nodes config cannot be loaded due to a previous plugin error.</red>"));
            logger.error(MiniMessage.miniMessage().deserialize("<red>Please check your server's console.</red>"));
            return;
        }

        Path path = Path.of(skyNodes.getDataFolder() + File.separator + "nodes.yml");
        if(!path.toFile().exists()) {
            skyNodes.saveResource("nodes.yml", false);
        }

        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
        try {
            node = loader.load().get(Node.class);
        } catch (ConfigurateException ignored) {}
    }
}
