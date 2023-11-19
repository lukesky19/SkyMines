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
import com.github.lukesky19.skynodes.util.ConfigRecord;
import com.github.lukesky19.skynodes.util.ConfigUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
 
public final class SkyNodes extends JavaPlugin {
    static List<BukkitTask> list = new ArrayList<>();
    private static SkyNodes instance;

    public static SkyNodes getInstance() {
        return instance;
    }

    public void onEnable() {
        SkyNodes.instance = this;
        reload();
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setExecutor(new SkyNodeCommand());
    }

    public void onDisable() {
        for (BukkitTask t : list) {
            t.cancel();
        }
    }

    public static void reload() {
        for (BukkitTask t : list) {
            t.cancel();
        }
        list = new ArrayList<>();

        ConfigUtil.copyDefaultConfig();
        CommentedConfigurationNode nodeConfig = ConfigRecord.getConfig().nodeConfig();
        int i = 0;
        while (!nodeConfig.node( "nodes", i).virtual()) {
            final Location location = getRandomLocation(parseLocations(i));
            final File finalFile = getRandomSchematic(parseSchematics(i));

            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (finalFile != null) {
                        SchematicLoader.paste(location, finalFile);
                    }
                }
            }.runTaskTimer(instance, 20L * nodeConfig.node("delay_between_nodes").getLong(), 20L * nodeConfig.node("nodes", i, "timer").getLong());
            list.add(task);
            i++;
        }
    }

    @NotNull
    public static List<Location> parseLocations(int i) {
        List<String> stringCoordsList;
        CommentedConfigurationNode nodeConfig = ConfigRecord.getConfig().nodeConfig();
        List<Double> XYZ = new ArrayList<>();
        List<Location> locations = new ArrayList<>();

        try {
            stringCoordsList = nodeConfig.node( "nodes", i, "locations").getList(String.class);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        if (stringCoordsList != null) {
            for (String str : stringCoordsList) {
                String[] split = str.split(" ");

                for (String s : split) {
                 XYZ.add(Double.valueOf(s));
                }

                if (XYZ.size() == 3) {
                    Location loc = new Location(
                            Bukkit.getWorld(Objects.requireNonNull(nodeConfig.node("nodes", i, "world").getString())),
                            XYZ.get(0),
                            XYZ.get(1),
                            XYZ.get(2));
                    locations.add(loc);
                }
            }
        }
        return locations;
    }

    public static Location getRandomLocation(@NotNull List<Location> locations) {
        Random random = new Random();
        return locations.get(random.nextInt(locations.size()));
    }
    @NotNull
    public static List<File> parseSchematics(int i) {
        CommentedConfigurationNode nodeConfig = ConfigRecord.getConfig().nodeConfig();
        List<File> schemFiles = new ArrayList<>();

        if (!nodeConfig.node( "nodes", i).virtual()) {
            List<String> schemNames;
            File file;
            try {
                schemNames = nodeConfig.node("nodes", i, "schematics").getList(String.class);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }

            if (schemNames != null) {
                for (String s : schemNames) {
                    if (instance.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                        file = new File(Objects.requireNonNull(instance.getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder() + File.separator + Objects.requireNonNull(instance.getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder() + "schematics" + File.separator + s);
                    } else if (instance.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
                        file = new File(Objects.requireNonNull(instance.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit")).getDataFolder() + File.separator + Objects.requireNonNull(instance.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit")).getDataFolder() + "schematics" + File.separator + s);
                    } else {
                        instance.getComponentLogger().error(MiniMessage.miniMessage().deserialize("<red>Worldedit or FastAsyncWorldEdit is not installed OR the schematic name configured at node " + i + " is invalid."));
                        file = null;
                    }

                    if (file != null) {
                        schemFiles.add(file);
                    }
                }
            }
        }
        return schemFiles;
    }
   
   public static File getRandomSchematic(@NotNull List<File> schematics) {
        Random random = new Random();
        return schematics.get(random.nextInt(schematics.size()));
    }
}
