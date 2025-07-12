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
package com.github.lukesky19.skymines.configuration;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.api.time.Time;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.Locale;
import com.github.lukesky19.skymines.data.config.Settings;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class loads the plugin's locale configuration.
 */
public class LocaleManager {
    private final @NotNull SkyMines skyMines;
    private final @NotNull SettingsManager settingsManager;
    private @NotNull Locale DEFAULT_LOCALE;
    private @Nullable Locale locale;

    /**
     * Constructor
     * @param skyMines The SkyMines' Plugin
     * @param settingsManager A SettingsLoader instance.
     */
    public LocaleManager(@NotNull SkyMines skyMines, @NotNull SettingsManager settingsManager)  {
        this.skyMines = skyMines;
        this.settingsManager = settingsManager;

        createDefaultLocale();
    }

    /**
     * Gets the plugin's locale if not null or the default locale otherwise.
     * @return The plugin's locale if not null or the default locale otherwise.
     */
    @NotNull
    public Locale getLocale() {
        if(locale == null) return DEFAULT_LOCALE;
        return locale;
    }

    /**
     * Reloads the plugin's locale.
     */
    public void reload() {
        ComponentLogger logger = skyMines.getComponentLogger();
        locale = null;

        copyDefaultLocales();

        Settings settings = settingsManager.getSettings();
        if(settings == null) {
            logger.error(AdventureUtil.serialize("<red>Failed to load plugin's locale due to plugin settings being null.</red>"));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtil.serialize("<red>Failed to load plugin's locale to use in settings.yml is null.</red>"));
            return;
        }

        String localeString = settings.locale();
        Path path = Path.of(skyMines.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            locale = loader.load().get(Locale.class);
        } catch (ConfigurateException exception) {
            throw new RuntimeException(exception);
        }

        validateLocale();
    }

    /**
     * Copies the default locale files that come bundled with the plugin, if they do not exist at least.
    */
    private void copyDefaultLocales() {
        Path path = Path.of(skyMines.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            skyMines.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    /**
     * Checks if the locale configuration has any null-values.
     */
    private void validateLocale() {
        ComponentLogger logger = skyMines.getComponentLogger();
        if(locale == null) {
            logger.warn(AdventureUtil.serialize("Unable to validate locale as the locale configuration failed to load. The default locale will be used."));
            return;
        }

        switch(locale.configVersion()) {
            case "3.1.0.0" -> {
                // Validate
                if(locale.prefix() == null
                        || locale.help() == null
                        || locale.reload() == null
                        || locale.noMineWithId() == null
                        || locale.guiOpenError() == null) {
                    logger.warn(AdventureUtil.serialize("One of the plugin's locale messages is null. Double-check your configuration. The default locale will be used."));
                    locale = null;
                    return;
                }

                Locale.PacketMineMessages packetMessages = locale.packetMineMessages();
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
                    logger.warn(AdventureUtil.serialize("One of the plugin's packet mine locale messages is null. Double-check your configuration. The default locale will be used."));
                    locale = null;
                    return;
                }

                Locale.WorldMineMessages worldMineMessages = locale.worldMineMessages();
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
                        || worldMineMessages.notEnoughMoney() == null
                        || worldMineMessages.guiErrorNotInMine() == null) {
                    logger.warn(AdventureUtil.serialize("One of the plugin's world mine locale messages is null. Double-check your configuration. The default locale will be used."));
                    locale = null;
                    return;
                }

                Locale.TimeMessage timeMessage= locale.timeMessage();
                if(timeMessage.prefix() == null
                        || timeMessage.years() == null
                        || timeMessage.months() == null
                        || timeMessage.weeks() == null
                        || timeMessage.days() == null
                        || timeMessage.hours() == null
                        || timeMessage.minutes() == null
                        || timeMessage.seconds() == null
                        || timeMessage.suffix() == null) {
                    logger.warn(AdventureUtil.serialize("One of the plugin's time message locale messages is null. Double-check your configuration. The default locale will be used."));
                    locale = null;
                }
            }

            case "3.0.0.0" -> {
                logger.warn(AdventureUtil.serialize("You need to update your locale configuration to the newest version or regenerate your locale file. The default locale will be used."));
                locale = null;
            }

            case null -> {
                logger.warn(AdventureUtil.serialize("Unable to validate locale as the config version is invalid. The default locale will be used."));
                locale = null;
            }

            default -> {
                logger.warn(AdventureUtil.serialize("Unable to validate locale as the config version is unknown. The default locale will be used."));
                locale = null;
            }
        }
    }

    /**
     * Gets the time message to display in the boss bar.
     * @param timeSeconds The time in seconds.
     * @return A String containing the time message.
     */
    @NotNull
    public String getTimeMessage(long timeSeconds) {
        Locale locale = this.getLocale();
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

        return MiniMessage.miniMessage().serialize(AdventureUtil.serialize(stringBuilder.toString(), placeholders));
    }

    /**
     * Builds the string by populating any non-zero individual time units.
     * @param locale The plugin's locale
     * @param timeRecord The record containing the individual time units to display.
     * @return A populated StringBuilder. May be empty if all time units were 0 and no suffix was configured.
     */
    private @NotNull StringBuilder getStringBuilder(Locale locale, Time timeRecord) {
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
                "3.1.0.0",
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
                        "<aqua>Player <yellow><player></yellow> can now mine <yellow><block_type></yellow> in mine <yellow><mine_id></yellow>.</aqua>",
                        "<aqua>Player <yellow><player></yellow> can no longer mine <yellow><block_type></yellow> in mine <yellow><mine_id></yellow>.</aqua>",
                        "<red>You cannot mine this block because it has not been unlocked. Unlock blocks in <yellow>/skymines shop</yellow>.</red>",
                        "<red>This block cannot be mined.</red>",
                        "<red>This block is not a player-placed block and cannot be mined.</red>",
                        "<red>This block is not player water-logged and cannot be unwater-logged.<red>",
                        "<red>You cannot place this block because it has not been unlocked. Unlock blocks in <yellow>/skymines shop</yellow>.</red>",
                        "<red>This block cannot be placed.<red>",
                        "<red>You cannot interact with this block because it has not been unlocked. Unlock blocks in <yellow>/skymines shop</yellow>.</red>",
                        "<red>This block cannot be interacted with.<red>",
                        "<red>You do not have enough money to unlock this block.</red>",
                        "<red>You must be inside a mine to open <yellow>/skymines shop</yellow>.</red>"),
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
}
