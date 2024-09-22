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
package com.github.lukesky19.skynodes;

import com.github.lukesky19.skynodes.commands.SkyNodeCommand;
import com.github.lukesky19.skynodes.configuration.loader.LocaleLoader;
import com.github.lukesky19.skynodes.configuration.loader.NodeLoader;
import com.github.lukesky19.skynodes.configuration.loader.SettingsLoader;
import com.github.lukesky19.skynodes.configuration.validator.LocaleValidator;
import com.github.lukesky19.skynodes.configuration.validator.SettingsValidator;
import com.github.lukesky19.skynodes.listeners.BlockBreakListener;
import com.github.lukesky19.skynodes.listeners.BlockClickListener;
import com.github.lukesky19.skynodes.listeners.BlockDropListener;
import com.github.lukesky19.skynodes.listeners.ChunkLoadListener;
import com.github.lukesky19.skynodes.manager.NodeManager;
import com.github.lukesky19.skynodes.utils.ConfigurationUtility;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class SkyNodes extends JavaPlugin {
    SettingsLoader settingsLoader;
    LocaleLoader localeLoader;
    NodeLoader nodeLoader;
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
        ConfigurationUtility configurationUtility = new ConfigurationUtility();
        SettingsValidator settingsValidator = new SettingsValidator(this);
        settingsLoader = new SettingsLoader(this, settingsValidator, configurationUtility);
        LocaleValidator localeValidator = new LocaleValidator(this);
        localeLoader = new LocaleLoader(this, settingsLoader, localeValidator, configurationUtility);
        nodeLoader = new NodeLoader(this, configurationUtility);
        NodeManager nodeManager = new NodeManager(this);
        SkyNodeCommand skyNodeCommand = new SkyNodeCommand(this, localeLoader);

        // Set skynodes command executor and tabcompleter.
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setExecutor(skyNodeCommand);
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setTabCompleter(skyNodeCommand);

        // Register Listeners.
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this, localeLoader, nodeLoader, nodeManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockClickListener(this, localeLoader, nodeLoader, nodeManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockDropListener(this, localeLoader, nodeLoader, nodeManager), this);
        Bukkit.getPluginManager().registerEvents(new ChunkLoadListener(nodeManager), this);

        reload();
    }

    public void reload() {
        pluginState = true;
        settingsLoader.reload();
        localeLoader.reload();
        nodeLoader.reload();
    }
}
