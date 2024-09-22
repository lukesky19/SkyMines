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
import com.github.lukesky19.skynodes.configuration.record.Locale;
import com.github.lukesky19.skynodes.configuration.validator.LocaleValidator;
import com.github.lukesky19.skynodes.utils.ConfigurationUtility;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

public class LocaleLoader {
    final SkyNodes skyNodes;
    final SettingsLoader settingsLoader;
    final LocaleValidator localeValidator;
    final ConfigurationUtility configurationUtility;
    Locale locale;

    public LocaleLoader(
            SkyNodes skyNodes,
            SettingsLoader settingsLoader,
            LocaleValidator localeValidator,
            ConfigurationUtility configurationUtility)  {
        this.skyNodes = skyNodes;
        this.settingsLoader = settingsLoader;
        this.localeValidator = localeValidator;
        this.configurationUtility = configurationUtility;
    }
    public Locale getLocale() {
        return locale;
    }

    public void reload() {
        locale = null;
        ComponentLogger logger = skyNodes.getComponentLogger();

        if(!skyNodes.isPluginEnabled()) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The locale config cannot be loaded due to a previous plugin error.</red>"));
            logger.error(MiniMessage.miniMessage().deserialize("<red>Please check your server's console.</red>"));
            return;
        }

        copyDefaultLocales();

        String localeString = settingsLoader.getSettings().locale();
        Path path = Path.of(skyNodes.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));

        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
        try {
            locale = loader.load().get(Locale.class);
        } catch (ConfigurateException ignored) {}

        localeValidator.validateLocale(locale, localeString);
    }

    /**
     * Copies the default locale files that come bundled with the plugin, if they do not exist at least.
    */
    private void copyDefaultLocales() {
        skyNodes.saveResource("locale/en_US.yml", false);
    }
}
