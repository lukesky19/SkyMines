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
package com.github.lukesky19.skynodes;

import com.github.lukesky19.skynodes.commands.SkyNodeCommand;
import com.github.lukesky19.skynodes.configuration.config.ConfigValidator;
import com.github.lukesky19.skynodes.configuration.config.ConfigManager;
import com.github.lukesky19.skynodes.configuration.locale.LocaleManager;
import com.github.lukesky19.skynodes.configuration.locale.LocaleValidator;
import com.github.lukesky19.skynodes.configuration.settings.SettingsManager;
import com.github.lukesky19.skynodes.listeners.SkyNodeBlockBreakListener;
import com.github.lukesky19.skynodes.utils.ConfigurationUtility;
import com.github.lukesky19.skynodes.utils.PasteManager;
import com.github.lukesky19.skynodes.utils.SchedulerUtility;
import com.onarandombox.bstats.bukkit.Metrics;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SkyNodes extends JavaPlugin {
    SettingsManager settingsManager;
    LocaleManager localeManager;
    ConfigManager configManager;
    SchedulerUtility schedulerUtility;
    Boolean pluginState = true;

    public void setPluginState(Boolean pluginState) {
        this.pluginState = pluginState;
    }
    public Boolean isPluginEnabled() {
        return this.pluginState;
    }

    @Override
    public void onEnable() {
        int pluginId = 22278;
        new Metrics(this, pluginId);

        // Check if WorldEdit or FastAsyncWorldEdit is enabled.
        if(Bukkit.getPluginManager().getPlugin("WorldEdit") == null && Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") == null) {
            getComponentLogger().error(MiniMessage.miniMessage().deserialize("<red>WorldEdit or FastAsyncWorldEdit not found. Disabling..."));
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // Classes
        ConfigurationUtility configurationUtility = new ConfigurationUtility(this);
        settingsManager = new SettingsManager(this, configurationUtility);
        LocaleValidator localeValidator = new LocaleValidator(this);
        localeManager = new LocaleManager(this, settingsManager, localeValidator, configurationUtility);
        ConfigValidator configValidator = new ConfigValidator(this, localeManager);
        schedulerUtility = new SchedulerUtility();
        PasteManager pasteManager = new PasteManager(this, localeManager, settingsManager, configValidator, schedulerUtility);
        configManager = new ConfigManager(this, localeManager, settingsManager, configValidator, pasteManager, schedulerUtility, configurationUtility);

        SkyNodeCommand skyNodeCommand = new SkyNodeCommand(this, settingsManager, localeManager, pasteManager, configManager);

        // Set skynodes command executor and tabcompleter.
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setExecutor(skyNodeCommand);
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setTabCompleter(skyNodeCommand);
        // Register blockBreakListener.
        Bukkit.getPluginManager().registerEvents(new SkyNodeBlockBreakListener(this, localeManager, settingsManager, configManager, configValidator), this);

        reload();
    }

    @Override
    public void onDisable() {
        schedulerUtility.stopTasks();
    }

    public void reload() {
        pluginState = true;
        settingsManager.reload();
        localeManager.reload();
        configManager.reload();
        configManager.startTasks();
    }
}
