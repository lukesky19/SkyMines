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

import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.integration.Hook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * This class manages interfacing with Vault.
 */
public class EconomyHook implements Hook {
    private final @NonNull SkyMines skyMines;
    private @Nullable Economy economy;

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     */
    public EconomyHook(@NonNull SkyMines skyMines) {
        this.skyMines = skyMines;
    }

    /**
     * Attempt to get the {@link Economy} from Vault.
     */
    @Override
    public void initialize() {
        if(skyMines.getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = skyMines.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
            }
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return economy != null;
    }

    /**
     * Remove the amount provided from the player's balance.
     * This method will prevent balances from going into the negative.
     * @apiNote If the economy was not hooked into, this method will do nothing. Can be checked with {@link #isHooked()}.
     * @param player The {@link Player}.
     * @param amount The amount to remove.
     */
    public void removeFromBalance(@NonNull Player player, double amount) {
        if(economy == null) return;

        double balance = economy.getBalance(player);
        if(balance - amount < 0) {
            economy.withdrawPlayer(player, balance);
        } else {
            economy.withdrawPlayer(player, amount);
        }
    }

    /**
     * Get the player's balance.
     * @apiNote Will always return 0 if the economy was not hooked into. Can be checked with {@link #isHooked()}.
     * @param player The {@link Player} to get the economy for.
     * @return The player's balance or 0 if not hooked.
     */
    public double getBalance(@NonNull Player player) {
        if(economy == null) return 0;

        return economy.getBalance(player);
    }
}
