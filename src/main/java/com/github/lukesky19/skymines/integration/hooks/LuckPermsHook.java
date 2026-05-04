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
package com.github.lukesky19.skymines.integration.hooks;

import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.github.lukesky19.skymines.settings.data.Context;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * This class manages interfacing with the luckperms api.
 */
public class LuckPermsHook implements Hook {
    private final @NonNull SkyPlugin plugin;
    private @Nullable LuckPerms luckPermsAPI;
    private @Nullable UserManager userManager;
    private @Nullable PlayerAdapter<Player> playerAdapter;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public LuckPermsHook(@NonNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link LuckPerms} api from LuckPerms.
     */
    @Override
    public void initialize() {
        if(plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<LuckPerms> rsp = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
            if(rsp != null) {
                this.luckPermsAPI = rsp.getProvider();
                this.userManager = luckPermsAPI.getUserManager();
                this.playerAdapter = luckPermsAPI.getPlayerAdapter(Player.class);
            }
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return luckPermsAPI != null && userManager != null;
    }

    /**
     * Get the {@link User} for the {@link Player} provided.
     * @param player The {@link Player}.
     * @return The {@link User} or null if LuckPerms isn't hooked into.
     */
    public @Nullable User getUser(@NonNull Player player) {
        if(luckPermsAPI == null || userManager == null || playerAdapter == null) return null;
        return playerAdapter.getUser(player);
    }

    /**
     * Checks if the user has an inheritance node for the group and contexts provided.
     * @param user The {@link User}.
     * @param group The group name.
     * @param contexts The {@link List} of {@link Context}s.
     * @return true if the player has the inheritance node, false if not.
     */
    public boolean hasInheritanceNode(@NonNull User user, @NonNull String group, @NonNull List<Context> contexts) {
        return user.getDistinctNodes().stream()
                .filter(node -> node instanceof InheritanceNode)
                .map(node -> (InheritanceNode) node)
                .filter(node -> node.getGroupName().equals(group))
                .anyMatch(node -> {
                    if(!contexts.isEmpty()) {
                        ImmutableContextSet contextSet = node.getContexts();
                        return contexts.stream()
                                .filter(e -> e.type() != null && e.value() != null)
                                .allMatch(e -> contextSet.contains(e.type(), e.value()));
                    }

                    return true;
                });
    }
}