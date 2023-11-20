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
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class SkyNodes extends JavaPlugin {

    static BukkitTask task;
    private static SkyNodes instance;
    static ComponentLogger logger;
    public static SkyNodes getInstance() {
        return SkyNodes.instance;
    }
    public static ComponentLogger getSkyNodesLogger() {
        return logger;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        SkyNodes.instance = this;
        logger = instance.getComponentLogger();
        reload();
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setExecutor(new SkyNodeCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setTabCompleter(new SkyNodeCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(task != null) {
            task.cancel();
        }
    }

    public static void reload() {
        if(task != null) {
            task.cancel();
        }
        ConfigUtil.copyDefaultConfig();
        CommentedConfigurationNode nodeConfig = ConfigRecord.getConfig().nodeConfig();

        int s = 0;
        int count = 0;
        while(!nodeConfig.node("nodes", s).virtual()) {
            count++;
            s++;
        }

        int finalCount = count;
        final int[] r = new int[1];
        task = new BukkitRunnable() {
            @Override public void run() {
                r[0] = new Random().nextInt(finalCount);
                Location location = getRandomLocation(parseLocations(r[0]));
                File finalFile = getRandomSchematic(parseSchematics(r[0]));
                if(finalFile != null) {
                    try {
                        SchematicLoader.paste(location, finalFile);
                        logger.info(MiniMessage.miniMessage().deserialize("<red>Node " + (r[0] - 1) + " has pasted successfully."));
                    } catch (Exception e) {
                        logger.error(MiniMessage.miniMessage().deserialize("<red><bold>AN ERROR HAS OCCURED:"));
                        throw new RuntimeException(e);
                    }
                }
            }
        }.runTaskTimer(instance, 20L, 20L * nodeConfig.node("time-delay").getLong());
    }

    public static @NotNull List<Location> parseLocations(int i) {
        CommentedConfigurationNode nodeConfig = ConfigRecord.getConfig().nodeConfig();
        List<Double> XYZ = new ArrayList<>();
        List<Location> locations = new ArrayList<>();

        List<String> stringCoordsList;
        try {
            stringCoordsList = nodeConfig.node("nodes", i, "locations").getList(String.class);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        if(stringCoordsList != null) {
            for (String str : stringCoordsList) {
                String[] split = str.split(" ");

                for (String s : split) {
                    XYZ.add(Double.valueOf(s));
                }

                if (XYZ.size() == 3) {
                    String worldName = nodeConfig.node("nodes", i, "world").getString();
                    File worldFile = new File(Bukkit.getServer().getWorldContainer() + File.separator + worldName);
                    World world;
                    if(worldFile.isDirectory() && worldFile.exists()) {
                        world = new WorldCreator(Objects.requireNonNull(worldName)).createWorld();
                        Location loc = new Location(
                                world,
                                XYZ.get(0),
                                XYZ.get(1),
                                XYZ.get(2));
                        locations.add(loc);
                    } else {
                        logger.error(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>The world for node " + i + " is invalid."));
                    }
                } else {
                    logger.error(MiniMessage.miniMessage().deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>] <red>The coordinates for node " + i + " are invalid."));
                }
            }
        }
        return locations;
    }

    public static Location getRandomLocation(@NotNull List<Location> locations) {
        Random random = new Random();
        return locations.get(random.nextInt(locations.size()));
    }

    public static @NotNull List<File> parseSchematics(int i) {
        CommentedConfigurationNode nodeConfig = ConfigRecord.getConfig().nodeConfig();
        List<String> schemNames;
        List<File> schemFiles = new ArrayList<>();

        if(!nodeConfig.node("nodes", i).virtual()) {
            File file;
            try {
                schemNames = nodeConfig.node("nodes", i, "schematics").getList(String.class);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }

            if (schemNames != null) {
                for(String s : schemNames) {
                    if (instance.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                        file = new File(Objects.requireNonNull(instance.getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + s);
                    } else if (instance.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
                        file = new File(Objects.requireNonNull(instance.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + s);
                    } else {
                        logger.error(MiniMessage.miniMessage().deserialize("<red>Worldedit or FastAsyncWorldEdit is not installed OR the schematic name configured at node " + i + " is invalid."));
                        file = null;
                    }
                    if(file != null) {
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
