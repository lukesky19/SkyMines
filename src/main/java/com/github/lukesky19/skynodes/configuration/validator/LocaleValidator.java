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
import com.github.lukesky19.skynodes.configuration.record.Locale;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * This class manages the validation logic for locale configuration.
 */
public class LocaleValidator {
    final SkyNodes skyNodes;

    /**
     * Constructor
     * @param skyNodes The plugin's instance.
     */
    public LocaleValidator(SkyNodes skyNodes) {
        this.skyNodes = skyNodes;
    }

    /**
     * Validates a Locale object and soft-disables the plugin if invalid.
     * @param localeConfiguration The Locale object to be validated.
     * @param locale The name of the locale being validated. Used in error messages.
     */
    public void validateLocale(Locale localeConfiguration, String locale) {
        ComponentLogger logger = skyNodes.getComponentLogger();
        MiniMessage mm = MiniMessage.miniMessage();

        if(localeConfiguration == null) {
            logger.warn(mm.deserialize("<red>Failed to load the locale selected in settings.yml. Does <yellow>" + locale + "</yellow> exist?</red>"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.configVersion() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>config-version</yellow> setting in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.prefix() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>prefix</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        for(String msg : localeConfiguration.help()) {
            if(msg == null) {
                logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>help</yellow> message in <yellow>" + locale + "</yellow> is invalid. Is it a valid list of Strings?"));
                logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
                skyNodes.setPluginState(false);
                return;
            }
        }

        if(localeConfiguration.reload() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>reload</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.noPermission() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>no-permission</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.unknownArgument() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>unknown-argument</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.inGameOnly() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>in-game-only</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }
        
        if(localeConfiguration.configLoadError() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>config-load-error</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.invalidParentRegion() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>invalid-parent-region</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.invalidChildRegion() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>invalid-child-region</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.blocksAllowedListError() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>blocks-allowed-list-error</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.invalidBlockMaterial() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>invalid-block-material</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.invalidReplacementMaterial() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>invalid-replacement-material</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.invalidDelaySeconds() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>invalid-delay-seconds</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.cooldown() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>cooldown</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
            return;
        }

        if(localeConfiguration.canNotMine() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The <yellow>can-not-mine</yellow> message in <yellow>" + locale + "</yellow> is invalid."));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            skyNodes.setPluginState(false);
        }
    }
}
