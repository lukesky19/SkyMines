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
package com.github.lukesky19.skynodes.configuration.validator;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.configuration.record.Settings;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * This class validates the plugin's settings.yml file.
 */
public class SettingsValidator {
    final SkyNodes skyNodes;

    /**
     * Constructor
     * @param skyNodes The plugin's instance.
     */
    public SettingsValidator(SkyNodes skyNodes) {
        this.skyNodes = skyNodes;
    }

    /**
     * Validates a Settings object and soft-disables the plugin if invalid.
     * @param settings The Settings object to be validated.
     */
    public void validateSettings(Settings settings) {
        ComponentLogger logger = skyNodes.getComponentLogger();
        MiniMessage mm = MiniMessage.miniMessage();

        if(settings == null) {
            logger.warn(mm.deserialize("<red>Failed to load <yellow>settings.yml</yellow> configuration.</red>"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(settings.configVersion() == null) {
            logger.warn(mm.deserialize("<red>The <yellow>config-version</yellow> setting in <yellow>settings.yml</yellow> is invalid.</red>"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(settings.locale() == null) {
            logger.warn(mm.deserialize("<red>The <yellow>locale</yellow> setting in <yellow>settings.yml</yellow> is invalid.</red>"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
        }
    }

}
