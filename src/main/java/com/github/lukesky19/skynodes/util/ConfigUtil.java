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
package com.github.lukesky19.skynodes.util;
 
import com.github.lukesky19.skynodes.SkyNodes;
import java.nio.file.Path;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
    public class ConfigUtil {
        public static final Path nodeConfigPath = Path.of(SkyNodes.getInstance().getDataFolder() + "/nodes.yml");
        private static YamlConfigurationLoader buildYCL() {
        return (YamlConfigurationLoader.builder().nodeStyle(NodeStyle.BLOCK).path(nodeConfigPath)).build();
        }
        public static CommentedConfigurationNode loadConfig() {
            CommentedConfigurationNode commentedConfigurationNode;
            YamlConfigurationLoader loader = buildYCL();
 
            try {
                commentedConfigurationNode = loader.load();
            } catch (ConfigurateException e) {
                throw new RuntimeException(e);
            }
            return commentedConfigurationNode;
        }

        public static void copyDefaultConfig() {
            if (!nodeConfigPath.toFile().exists())
                SkyNodes.getInstance().saveResource("nodes.yml", false);
        }
    }