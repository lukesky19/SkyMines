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
package com.github.lukesky19.skynodes.listeners;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.configuration.config.ConfigValidator;
import com.github.lukesky19.skynodes.configuration.config.ParsedConfig;
import com.github.lukesky19.skynodes.configuration.config.ConfigManager;
import com.github.lukesky19.skynodes.configuration.locale.LocaleManager;
import com.github.lukesky19.skynodes.configuration.settings.SettingsManager;
import com.github.lukesky19.skynodes.configuration.locale.FormattedLocale;
import com.github.lukesky19.skynodes.configuration.settings.SettingsConfiguration;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.*;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SkyNodeBlockBreakListener implements Listener {
    public SkyNodeBlockBreakListener(
            SkyNodes plugin,
            LocaleManager localeManager,
            SettingsManager settingsManager,
            ConfigManager configManager,
            ConfigValidator configValidator) {
        this.plugin = plugin;
        this.localeManager = localeManager;
        this.settingsManager = settingsManager;
        this.configManager = configManager;
        this.configValidator = configValidator;
    }
    final SkyNodes plugin;
    final ConfigManager configManager;
    final LocaleManager localeManager;
    final SettingsManager settingsManager;
    final ConfigValidator configValidator;

    @EventHandler
    public void onNodeBreak(BlockBreakEvent e) {
        if(!plugin.isPluginEnabled()) return;

        // Check if the block broken is:
        // a. within the world for a SkyNode
        // b. within the region for a SkyNode.
        // c. on the allowed-blocks list for a SkyNode.
        FormattedLocale formattedLocale = localeManager.formattedLocale();
        SettingsConfiguration settingsConfiguration = settingsManager.getSettings();
        BlockVector3 blockVector3 = BlockVector3.at(e.getBlock().getLocation().getX(), e.getBlock().getLocation().getY(), e.getBlock().getLocation().getZ());

        for(Map.Entry<String, ParsedConfig.SkyTask> skyTaskEntry : configManager.getConfiguration().tasks().entrySet()) {
            for(Map.Entry<String, ParsedConfig.SkyNode> skyNodeEntry : skyTaskEntry.getValue().skyNodes().entrySet()) {
                ParsedConfig.SkyNode skyNode = skyNodeEntry.getValue();

                MultiverseCore mVCore = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
                World nodeWorld;
                nodeWorld = Objects.requireNonNull(mVCore).getMVWorldManager().getMVWorld(skyNode.nodeWorld()).getCBWorld();

                ProtectedRegion region = configValidator.verifyRegion(skyTaskEntry.getKey(), skyNodeEntry.getKey(), skyNode.region(), nodeWorld);

                List<Material> materialsList = skyNode.materials();

                if(Objects.equals(e.getBlock().getWorld(), nodeWorld)) {
                    if(region.contains(blockVector3)) {
                        if(e.getPlayer().hasPermission("skynodes.bypass.blockbreakcheck")) {
                            if(settingsConfiguration.debug() && e.getPlayer().hasPermission("skynodes.debug")) {
                                e.getPlayer().sendMessage(formattedLocale.prefix().append(formattedLocale.bypassedBlockBreakCheck()));
                            }
                            return;
                        }

                        for(Material mat : materialsList) {
                            if(Objects.equals(e.getBlock().getType(), mat)) {
                                if(settingsConfiguration.debug() && e.getPlayer().hasPermission("skynodes.debug")) {
                                    e.getPlayer().sendMessage(formattedLocale.prefix().append(formattedLocale.canMine()));
                                }
                            } else {
                                if(settingsConfiguration.debug() && e.getPlayer().hasPermission("skynodes.debug")) {
                                    e.getPlayer().sendMessage(formattedLocale.prefix().append(formattedLocale.canNotMine()));
                                }
                                e.setCancelled(true);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
}
