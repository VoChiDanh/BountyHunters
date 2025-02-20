package net.Indyuce.bountyhunters.api.player;

import net.Indyuce.bountyhunters.BountyHunters;
import org.bukkit.OfflinePlayer;

/**
 * Used to update player data when a player is offline.
 * <p>
 * This is an interface implemented by both SQL and YAML data providers.
 */
public interface OfflinePlayerData {

    static OfflinePlayerData get(OfflinePlayer player) {
        return BountyHunters.getInstance().getPlayerDataManager().getOfflineData(player);
    }

    void addSuccessfulBounties(int value);

    /**
     * Used to give a player head to an online player or
     * save it in the head GUI which they can open later.
     */
    void givePlayerHead(OfflinePlayer owner);
}
