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
import com.github.lukesky19.skynodes.records.Settings;
import org.spongepowered.configurate.CommentedConfigurationNode;

public class SettingsManager {
    // Constructor(s)
    public SettingsManager(SkyNodes plugin) {
        this.plugin = plugin;
        cfgMgr = plugin.getCfgMgr();
    }

    // Variable(s)
    final SkyNodes plugin;
    final ConfigManager cfgMgr;
    Settings settings;

    // Getter(s)
    public Settings getSettings() {
        return settings;
    }

    // Methods
    // Loads the settings from the nodes.yml file.
    private void loadSettings(CommentedConfigurationNode comConfNode) {
        boolean debug;

        if(!comConfNode.node("debug").isNull()) {
            debug = comConfNode.node("debug").getBoolean();
        } else {
            debug = false;
        }

        settings = new Settings(debug);
    }

    // (Re-)loads the settings
    public void reloadSettings() {
        loadSettings(cfgMgr.getNodesConfig());
    }
}
