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
package com.github.lukesky19.skynodes.configuration.locale;

import com.github.lukesky19.skynodes.SkyNodes;
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
     * Checks if the locale config is valid.
     * @param localeConfiguration The LocaleConfiguration to be validated.
     * @param locale The name of the locale being validated. Used in error messages.
     * @return true if valid, false if not.
     */
    public boolean isLocaleValid(LocaleConfiguration localeConfiguration, String locale) {
        ComponentLogger logger = skyNodes.getComponentLogger();
        MiniMessage mm = MiniMessage.miniMessage();

        if(localeConfiguration == null) {
            logger.warn(mm.deserialize("<red>Failed to load the locale selected in settings.yml. Does <yellow>" + locale + "</yellow> exist?</red>"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.prefix() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The prefix message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        for(String msg : localeConfiguration.help()) {
            if(msg == null) {
                logger.warn(MiniMessage.miniMessage().deserialize("<red>The help message in <yellow>" + locale + "</yellow> is invalid. Is it a valid list of Strings?"));
                logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
                return false;
            }
        }

        if(localeConfiguration.reload() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The reload message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.configLoadError() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The config-load-error message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.startTasksSuccess() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The start-tasks-success message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.noTasksFound() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The no-tasks-found message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.noNodesFound() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The no-nodes-found message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.operationFailure() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The operation-failure message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.clipboardLoadFailure() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The clipboard-load-failure message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.noPermission() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The no-permission message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.unknownArgument() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The unknown-argument message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.missingArgumentTaskId() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The missing-argument-task-id message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.missingArgumentNodeId() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The missing-argument-node-id message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.nodePasteSuccess() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The node-paste-success message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.nodePasteFailure() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The node-paste-failure message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.warn(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.worldNotFound() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The world-not-found message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.schematicListError() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The schematic-list-error message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.schematicNotFound() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The schematic-not-found message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.invalidLocation() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The invalid-location message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.invalidSafeLocation() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The invalid-safe-location message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.invalidRegion() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The invalid-region message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.blocksAllowedListError() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The blocks-allowed-list-error message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.invalidBlockMaterial() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The invalid-block-material message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.undo() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The undo message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.redo() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The redo message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.noUndo() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The no-undo message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.noRedo() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The no-redo message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.invalidTaskId() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The invalid-task-id message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.invalidNodeId() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The invalid-node-id message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.inGameOnly() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The in-game-only message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.softDisable() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The soft-disable message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.bypassedSafeTeleport() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The bypassed-safe-teleport message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.bypassedBlockBreakCheck() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The bypassed-block-break-check message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.canMine() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The can-mine message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        if(localeConfiguration.canNotMine() == null) {
            logger.warn(MiniMessage.miniMessage().deserialize("<red>The can-not-mine message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            logger.info(mm.deserialize("<red>Soft-disabling the plugin. The plugin will not work until you resolve the above problem and reload the plugin.</red>"));
            return false;
        }

        return true;
    }
}
