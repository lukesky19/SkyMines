/*
    SkyMines offers different types mines to get resources from.
    Copyright (C) 2023 lukeskywlker19

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
package com.github.lukesky19.skymines.manager.config;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.api.time.Time;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.Locale;
import com.github.lukesky19.skymines.data.config.Settings;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class loads the plugin's locale configuration.
 */
public class LocaleManager extends SimpleConfigManager<Locale> {
    private final @NonNull SettingsManager settingsManager;
    private @NonNull Locale DEFAULT_LOCALE;

    /**
     * Constructor
     * @param skyMines The SkyMines' Plugin
     * @param settingsManager A SettingsLoader instance.
     */
    public LocaleManager(@NonNull SkyMines skyMines, @NonNull SettingsManager settingsManager)  {
        super(skyMines, Locale.class);
        this.settingsManager = settingsManager;

        createDefaultLocale();
    }

    @Override
    public @NonNull Locale getConfiguration() {
        if(configuration == null) return DEFAULT_LOCALE;
        return configuration;
    }

    @Override
    public void loadConfiguration() {
        @Nullable Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.error(AdventureUtil.deserialize("Failed to load plugin's locale due to plugin settings being null."));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtil.deserialize("Failed to load plugin's locale to use in settings.yml is null."));
            return;
        }

        String localeString = settings.locale();
        configurationPath = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));

        YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(configurationPath);
        try {
            ConfigurationNode root = yamlConfigurationLoader.load();

            migrateVersion(root);

            configuration = root.get(configClass);
            if(configuration == null) {
                logger.warn(AdventureUtil.deserialize("Failed to load configuration. Class name: " + this.getClass().getName()));
                return;
            }
            Locale preMigrationConfiguration = configuration;

            // Migrate configuration
            configuration = migrateConfiguration(configuration);
            // If migration failed, return
            if(configuration == null) {
                logger.warn(AdventureUtil.deserialize("Migrated configuration is invalid. Class name: " + this.getClass().getName()));
                return;
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(configuration)) {
                logger.warn(AdventureUtil.deserialize("Configuration validation failed. Class name: " + this.getClass().getName()));
                configuration = null;
                return;
            }

            // Save the migrated configuration if different
            if(configuration != preMigrationConfiguration) {
                saveConfiguration(configuration);
            }
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load configuration. Error: " + configurateException.getMessage()));
        }
    }

    @Override
    protected void saveBundledConfig() {
        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            plugin.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    @Override
    public @Nullable Locale migrateConfiguration(@NonNull Locale locale) {
        switch(locale.version()) {
            case 6 -> {
                // Latest version, do nothing
                return locale;
            }

            case 5 -> {
                return new Locale(
                        6,
                        locale.prefix(),
                        locale.help(),
                        locale.reload(),
                        locale.noMineWithId(),
                        locale.guiOpenError(),
                        locale.packetMineMessages(),
                        locale.worldMineMessages(),
                        locale.timeMessage());
            }

            case 4 -> {
                Locale.WorldMineMessages oldWorldMineMessages = locale.worldMineMessages();
                Locale.WorldMineMessages newWorldMineMessages = new Locale.WorldMineMessages(
                        oldWorldMineMessages.invalidBlockType(),
                        oldWorldMineMessages.blockAlreadyUnlocked(),
                        oldWorldMineMessages.blockAlreadyLocked(),
                        oldWorldMineMessages.blockUnlocked(),
                        oldWorldMineMessages.blockLocked(),
                        "<aqua>Your unlocked blocks for mine <yellow><mine_id></yellow> have been reset.</aqua>",
                        oldWorldMineMessages.playerBlockUnlocked(),
                        oldWorldMineMessages.playerBlockLocked(),
                        "<aqua>Player <yellow><player></yellow> unlocked blocks for mine <yellow><mine_id></yellow> have been removed.</aqua>",
                        oldWorldMineMessages.blockBreakNotUnlocked(),
                        oldWorldMineMessages.blockBreakNotAllowed(),
                        oldWorldMineMessages.blockBreakNotPlayerPlaced(),
                        oldWorldMineMessages.blockBreakNotPlayerWaterLogged(),
                        oldWorldMineMessages.blockPlaceNotUnlocked(),
                        oldWorldMineMessages.blockPlaceNotAllowed(),
                        oldWorldMineMessages.blockInteractionNotUnlocked(),
                        oldWorldMineMessages.blockInteractionNotAllowed(),
                        "<red>You do not have enough <currency> to unlock this block.</red>",
                        "<red>The block <yellow><block_type></yellow> is not purchasable with currency <yellow><currency></yellow>.</red>",
                        oldWorldMineMessages.guiErrorNotInMine(),
                        "money",
                        "player points");

                return new Locale(
                        6,
                        locale.prefix(),
                        locale.help(),
                        locale.reload(),
                        locale.noMineWithId(),
                        locale.guiOpenError(),
                        locale.packetMineMessages(),
                        newWorldMineMessages,
                        locale.timeMessage());
            }

            case 3 -> {
                logger.warn(AdventureUtil.deserialize("Version 3 of the locale configuration requires manual migration!"));
                logger.warn(AdventureUtil.deserialize("You should take a backup of your existing locale configuration and regenerate your en_US.yml locale configuration."));
                logger.warn(AdventureUtil.deserialize("You can then make any changes from there. The default locale configuration will be used in the meantime."));
                return null;
            }

            default -> {
                logger.warn(AdventureUtil.deserialize("Failed to migrate your locale configuration. Please update to the newest version or regenerate your locale file. The default locale will be used."));
                return null;
            }
        }
    }

    @Override
    public boolean validateConfiguration(@Nullable Locale configuration) {
        if(configuration == null) {
            logger.warn(AdventureUtil.deserialize("Unable to validate locale as the locale configuration failed to load. The default locale will be used."));
            return false;
        }

        if(configuration.prefix() == null
                || configuration.help() == null
                || configuration.reload() == null
                || configuration.noMineWithId() == null
                || configuration.guiOpenError() == null) {
            logger.warn(AdventureUtil.deserialize("One of the plugin's locale messages is null. Double-check your configuration. The default locale will be used."));
            this.configuration = null;
            return false;
        }

        Locale.PacketMineMessages packetMessages = configuration.packetMineMessages();
        if(packetMessages.mineTimeChanged() == null
                || packetMessages.mineTimeChangedTo() == null
                || packetMessages.mineTime() == null
                || packetMessages.noMineTime() == null
                || packetMessages.playerMineTime() == null
                || packetMessages.playerNoMineTime() == null
                || packetMessages.mineAccessNoTime() == null
                || packetMessages.canNotBreakBlock() == null
                || packetMessages.canNotPlaceBlock() == null
                || packetMessages.cooldown() == null
                || packetMessages.timeInvalidLessThenOne() == null
                || packetMessages.timeInvalidLessThenZero() == null) {
            logger.warn(AdventureUtil.deserialize("One of the plugin's packet mine locale messages is null. Double-check your configuration. The default locale will be used."));
            this.configuration = null;
            return false;
        }

        Locale.WorldMineMessages worldMineMessages = configuration.worldMineMessages();
        if(worldMineMessages.invalidBlockType() == null
                || worldMineMessages.blockAlreadyUnlocked() == null
                || worldMineMessages.blockAlreadyLocked() == null
                || worldMineMessages.blockUnlocked() == null
                || worldMineMessages.blockLocked() == null
                || worldMineMessages.playerBlockUnlocked() == null
                || worldMineMessages.playerBlockLocked() == null
                || worldMineMessages.blockBreakNotUnlocked() == null
                || worldMineMessages.blockBreakNotAllowed() == null
                || worldMineMessages.blockPlaceNotUnlocked() == null
                || worldMineMessages.blockInteractionNotUnlocked() == null
                || worldMineMessages.blockInteractionNotAllowed() == null
                || worldMineMessages.notEnoughCurrency() == null
                || worldMineMessages.guiErrorNotInMine() == null
                || worldMineMessages.moneyCurrencyName() == null
                || worldMineMessages.playerPointsCurrencyName() == null) {
            logger.warn(AdventureUtil.deserialize("One of the plugin's world mine locale messages is null. Double-check your configuration. The default locale will be used."));
            this.configuration = null;
            return false;
        }

        Locale.TimeMessage timeMessage= configuration.timeMessage();
        if(timeMessage.prefix() == null
                || timeMessage.years() == null
                || timeMessage.months() == null
                || timeMessage.weeks() == null
                || timeMessage.days() == null
                || timeMessage.hours() == null
                || timeMessage.minutes() == null
                || timeMessage.seconds() == null
                || timeMessage.suffix() == null) {
            logger.warn(AdventureUtil.deserialize("One of the plugin's time message locale messages is null. Double-check your configuration. The default locale will be used."));
            this.configuration = null;
            return false;
        }

        return true;
    }

    /**
     * Gets the time message to display in the boss bar.
     * @param timeSeconds The time in seconds.
     * @return A String containing the time message.
     */
    @NonNull
    public String getTimeMessage(long timeSeconds) {
        Locale locale = this.getConfiguration();
        Time timeRecord = TimeUtil.millisToTime(timeSeconds * 1000L);

        List<TagResolver.Single> placeholders = List.of(
                Placeholder.parsed("years", String.valueOf(timeRecord.years())),
                Placeholder.parsed("months", String.valueOf(timeRecord.months())),
                Placeholder.parsed("weeks", String.valueOf(timeRecord.weeks())),
                Placeholder.parsed("days", String.valueOf(timeRecord.days())),
                Placeholder.parsed("hours", String.valueOf(timeRecord.hours())),
                Placeholder.parsed("minutes", String.valueOf(timeRecord.minutes())),
                Placeholder.parsed("seconds", String.valueOf(timeRecord.seconds())));

        StringBuilder stringBuilder = getStringBuilder(locale, timeRecord);

        return AdventureUtil.serialize(AdventureUtil.deserialize(stringBuilder.toString(), placeholders));
    }

    /**
     * Builds the string by populating any non-zero individual time units.
     * @param locale The plugin's locale
     * @param timeRecord The record containing the individual time units to display.
     * @return A populated StringBuilder. May be empty if all time units were 0 and no suffix was configured.
     */
    private @NonNull StringBuilder getStringBuilder(@NonNull Locale locale, @NonNull Time timeRecord) {
        Locale.TimeMessage timeMessage = locale.timeMessage();
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(timeMessage.prefix());

        boolean isFirstUnit = true;

        if(timeRecord.years() > 0) {
            stringBuilder.append(timeMessage.years());
            isFirstUnit = false;
        }

        if (timeRecord.months() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.months());
            isFirstUnit = false;
        }

        if (timeRecord.weeks() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.weeks());
            isFirstUnit = false;
        }

        if (timeRecord.days() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.days());
            isFirstUnit = false;
        }

        if (timeRecord.hours() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.hours());
            isFirstUnit = false;
        }

        if (timeRecord.minutes() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.minutes());
            isFirstUnit = false;
        }

        if (timeRecord.seconds() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.seconds());
            isFirstUnit = false;
        }

        if(isFirstUnit) {
            stringBuilder.append(timeMessage.seconds());
        }

        stringBuilder.append(timeMessage.suffix());
        return stringBuilder;
    }

    /**
     * Creates the default locale configuration to use if the locale configuration is invalid.
     */
    private void createDefaultLocale() {
        DEFAULT_LOCALE = new Locale(
                5,
                "<yellow><bold>SkyMines</bold></yellow><gray> ▪ </gray>",
                List.of(
                        "<aqua>SkyMines is developed by <white><bold>lukeskywlker19</bold></white>.</aqua>",
                        "<aqua>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click>",
                        " ",
                        "<aqua><bold>List of Commands:</bold></aqua>",
                        "<white>/</white><aqua>skymines</aqua> <yellow>help</yellow>",
                        "<white>/</white><aqua>skymines</aqua> <yellow>reload</yellow>",
                        "<white>/</white><aqua>skymines</aqua> <yellow>time</yellow> <yellow><mine_id></yellow>",
                        "<white>/</white><aqua>skymines</aqua> <yellow>time</yellow> <yellow><mine_id></yellow> <yellow><player></yellow>",
                        "<white>/</white><aqua>skymines</aqua> <yellow>time</yellow> <yellow>add</yellow> <yellow><player></yellow> <yellow><mine_id></yellow> <yellow><time in seconds></yellow>",
                        "<white>/</white><aqua>skymines</aqua> <yellow>time</yellow> <yellow>remove</yellow> <yellow><player></yellow> <yellow><mine_id></yellow> <yellow><time in seconds></yellow>",
                        "<white>/</white><aqua>skymines</aqua> <yellow>time</yellow> <yellow>set</yellow> <yellow><player></yellow> <yellow><mine_id></yellow> <yellow><time in seconds></yellow>",
                        "<white>/</white><aqua>skymines</aqua> <yellow>blocks</yellow> <yellow>unlock</yellow> <yellow><player></yellow> <yellow><mine_id></yellow> <yellow><block></yellow>",
                        "<white>/</white><aqua>skymines</aqua> <yellow>blocks</yellow> <yellow>lock</yellow> <yellow><player></yellow> <yellow><mine_id></yellow> <yellow><block></yellow>"),
                "<aqua>The plugin has reloaded successfully.</aqua>",
                "<red>There is no mine with that name.",
                "<red>Unable to open this GUI because of a configuration error.</red>",
                new Locale.PacketMineMessages(
                        "<aqua>You now have access to <yellow><mine_id></yellow> for <time>.</aqua>",
                        "<aqua>Player <yellow><player_name></yellow> now has access to <yellow><mine_id></yellow> for <time>.</aqua>",
                        "<aqua>You have <time> left for <yellow><mine_id></yellow>.</aqua>",
                        "<aqua>You have no time for <yellow><mine_id></yellow>.</aqua>",
                        "<aqua>Player <yellow><player></yellow> has <time> left for <yellow><mine_id></yellow>.</aqua>",
                        "<aqua>Player <yellow><player></yellow> has no time for <yellow><mine_id></yellow>.</aqua>",
                        "<red>You do not have any time to access the mine. Purchase some on <yellow>/shop</yellow>.</red>",
                        "<red>This block cannot be mined.</red>",
                        "<red>You cannot place blocks inside this mine.</red>",
                        "<red>This block is currently on cooldown. Try mining elsewhere.</red>",
                        "<red>Time must be greater than or equal to 1!</red>",
                        "<red>Time must be greater than or equal to 0!</red>"),
                new Locale.WorldMineMessages(
                        "<red>Invalid block type provided.</red>",
                        "<red>The block <yellow><block_type></yellow> is already unlocked for player <yellow><player></yellow> and mine <yellow><mine_id></yellow>.</red>",
                        "<red>The block <yellow><block_type></yellow> is already locked for player <yellow><player></yellow> and mine <yellow><mine_id></yellow>.</red>",
                        "<aqua>You can now mine <yellow><block_type></yellow> in mine <yellow><mine_id></yellow>.</aqua>",
                        "<aqua>You can no longer mine <yellow><block_type></yellow> in mine <yellow><mine_id></yellow>.</aqua>",
                        "<aqua>Your unlocked blocks for mine <yellow><mine_id></yellow> have been reset.</aqua>",
                        "<aqua>Player <yellow><player></yellow> can now mine <yellow><block_type></yellow> in mine <yellow><mine_id></yellow>.</aqua>",
                        "<aqua>Player <yellow><player></yellow> can no longer mine <yellow><block_type></yellow> in mine <yellow><mine_id></yellow>.</aqua>",
                        "<aqua>Player <yellow><player></yellow> unlocked blocks for mine <yellow><mine_id></yellow> have been removed.</aqua>",
                        "<red>You cannot mine this block because it has not been unlocked. Unlock blocks in <yellow>/skymines shop</yellow>.</red>",
                        "<red>This block cannot be mined.</red>",
                        "<red>This block is not a player-placed block and cannot be mined.</red>",
                        "<red>This block is not player water-logged and cannot be unwater-logged.<red>",
                        "<red>You cannot place this block because it has not been unlocked. Unlock blocks in <yellow>/skymines shop</yellow>.</red>",
                        "<red>This block cannot be placed.<red>",
                        "<red>You cannot interact with this block because it has not been unlocked. Unlock blocks in <yellow>/skymines shop</yellow>.</red>",
                        "<red>This block cannot be interacted with.<red>",
                        "<red>You do not have enough <currency> to unlock this block.</red>",
                        "<red>The block <yellow><block_type></yellow> is not purchasable with currency <yellow><currency></yellow>.</red>",
                        "<red>You must be inside a mine to open <yellow>/skymines shop</yellow>.</red>",
                        "money",
                        "player points"),
                new Locale.TimeMessage(
                        "",
                        "<yellow><years></yellow> year(s)",
                        "<yellow><months></yellow> month(s)",
                        "<yellow><weeks></yellow> week(s)",
                        "<yellow><days></yellow> day(s)",
                        "<yellow><hours></yellow> hour(s)",
                        "<yellow><minutes></yellow> minute(s)",
                        "<yellow><seconds></yellow> second(s)",
                        "."));
    }

    /**
     * Migrate the config version format.
     * @param root The root {@link ConfigurationNode}.
     */
    private void migrateVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        if(version == 0) {
            ConfigurationNode legacyVersionNode = root.node("config-version");
            @Nullable String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
            try {
                switch (legacyVersion) {
                    case "3.2.0.0" -> versionNode.set(5);

                    case "3.1.0.0" -> versionNode.set(4);

                    case "3.0.0.0" -> versionNode.set(3);

                    case null, default -> logger.warn(AdventureUtil.deserialize("Failed to convert String-based version to numeric version"));
                }
            } catch (SerializationException e) {
                logger.warn(AdventureUtil.deserialize("Failed to convert String-based version to numeric version"));
            }
        }
    }
}