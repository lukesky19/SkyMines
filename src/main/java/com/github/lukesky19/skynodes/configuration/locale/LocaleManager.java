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
import com.github.lukesky19.skynodes.configuration.settings.SettingsManager;
import com.github.lukesky19.skynodes.utils.ConfigurationUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LocaleManager {
    final SkyNodes skyNodes;
    final SettingsManager settingsManager;
    final LocaleValidator localeValidator;
    final ConfigurationUtility configurationUtility;

    FormattedLocale formattedLocale;

    public LocaleManager(
            SkyNodes skyNodes,
            SettingsManager settingsManager,
            LocaleValidator localeValidator,
            ConfigurationUtility configurationUtility)  {
        this.skyNodes = skyNodes;
        this.settingsManager = settingsManager;
        this.localeValidator = localeValidator;
        this.configurationUtility = configurationUtility;
    }
    public FormattedLocale formattedLocale() {
        return formattedLocale;
    }

    public void reload() {
        formattedLocale = null;
        if(!skyNodes.isPluginEnabled()) {
            return;
        }

        copyDefaultLocales();

        String locale = settingsManager.getSettings().locale() + ".yml";
        Path path = Path.of(skyNodes.getDataFolder() + File.separator + "locale" + File.separator + locale);

        LocaleConfiguration localeConfiguration = null;
        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
        try {
            localeConfiguration = loader.load().get(LocaleConfiguration.class);
        } catch (ConfigurateException ignored) {}

        if(!localeValidator.isLocaleValid(localeConfiguration, locale)) {
            skyNodes.setPluginState(false);
        } else {
            assert localeConfiguration != null;
            formattedLocale = decorateLocale(localeConfiguration);
        }
    }

    /**
     * Creates a FormattedLocale object that represents all the configurable plugin messages.
     * @param localeConfiguration An unformatted locale configuration.
     * @return A FormattedLocale object.
     */
    private FormattedLocale decorateLocale(LocaleConfiguration localeConfiguration) {
        MiniMessage mm = MiniMessage.miniMessage();
        List<Component> help = new ArrayList<>();
        for(String msg : localeConfiguration.help()) {
            help.add(mm.deserialize(msg));
        }

        return new FormattedLocale(
                mm.deserialize(localeConfiguration.prefix()),
                help,
                mm.deserialize(localeConfiguration.reload()),
                mm.deserialize(localeConfiguration.configLoadError()),
                mm.deserialize(localeConfiguration.startTasksSuccess()),
                mm.deserialize(localeConfiguration.noTasksFound()),
                localeConfiguration.noNodesFound(),
                mm.deserialize(localeConfiguration.operationFailure()),
                localeConfiguration.clipboardLoadFailure(),
                mm.deserialize(localeConfiguration.noPermission()),
                mm.deserialize(localeConfiguration.unknownArgument()),
                mm.deserialize(localeConfiguration.missingArgumentTaskId()),
                mm.deserialize(localeConfiguration.missingArgumentNodeId()),
                localeConfiguration.nodePasteSuccess(),
                localeConfiguration.nodePasteFailure(),
                localeConfiguration.worldNotFound(),
                localeConfiguration.schematicListError(),
                localeConfiguration.schematicNotFound(),
                localeConfiguration.invalidLocation(),
                localeConfiguration.invalidSafeLocation(),
                localeConfiguration.invalidRegion(),
                localeConfiguration.blocksAllowedListError(),
                localeConfiguration.invalidBlockMaterial(),
                mm.deserialize(localeConfiguration.undo()),
                mm.deserialize(localeConfiguration.redo()),
                mm.deserialize(localeConfiguration.noUndo()),
                mm.deserialize(localeConfiguration.noRedo()),
                localeConfiguration.invalidTaskId(),
                localeConfiguration.invalidNodeId(),
                mm.deserialize(localeConfiguration.inGameOnly()),
                mm.deserialize(localeConfiguration.softDisable()),
                mm.deserialize(localeConfiguration.bypassedSafeTeleport()),
                mm.deserialize(localeConfiguration.bypassedBlockBreakCheck()),
                mm.deserialize(localeConfiguration.canMine()),
                mm.deserialize(localeConfiguration.canNotMine()));
    }

    /**
     * Copies the default locale files that come bundled with the plugin, if they do not exist at least.
    */
    private void copyDefaultLocales() {
        skyNodes.saveResource("locale/en_US.yml", false);
    }
}
