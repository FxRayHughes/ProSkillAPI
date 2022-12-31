package com.sucy.skill.data.io;

import com.sucy.skill.api.player.PlayerAccounts;
import org.bukkit.OfflinePlayer;

public class SavePlayer extends PlayerAccounts {
    /**
     * Initializes a new container for player account data.
     * This shouldn't be used by other plugins as the API
     * provides one for each player already.
     *
     * @param player player to store data for
     */
    public SavePlayer(OfflinePlayer player) {
        super(player);
    }
}
