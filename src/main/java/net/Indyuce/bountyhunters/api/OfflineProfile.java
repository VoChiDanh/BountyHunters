package net.Indyuce.bountyhunters.api;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Not used yet but here if BH needs to stop using OfflinePlayers
 * and migrate to Name/UUID duos to store player data
 *
 * @author cympe
 * @deprecated Not implemented yet
 */
@Deprecated
public class OfflineProfile {
    private final UUID uuid;
    private final String name;
    private final OfflinePlayer offline;

    /**
     * Solves the problem of trying to retrieve OfflinePlayers when loading
     * bounties. If the offline player is found then we can access its name,
     * otherwise we can't.
     * <p>
     * Displaying player skulls require the OfflinePlayer instance.
     * <p>
     * Added in 2.3.15
     *
     * @param player Offline player
     */
    public OfflineProfile(OfflinePlayer player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.offline = player;
    }

    /**
     * @param obj Either a string of a configuration section to support both new
     *            and old bounty data formats (2.3.15 or earlier)
     */
    public OfflineProfile(Object obj) {

        // Backwards compatibility
        if (obj instanceof String) {
            uuid = UUID.fromString((String) obj);
            offline = Bukkit.getOfflinePlayer(uuid);
            name = getOfflinePlayer().getName();
            return;
        }

        if (obj instanceof ConfigurationSection config) {
            uuid = UUID.fromString(config.getString("uuid"));
            name = config.getString("name");
            offline = Bukkit.getOfflinePlayer(uuid);
            return;
        }

        throw new IllegalArgumentException("Provide either a string or a config section");
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @NotNull
    public OfflinePlayer getOfflinePlayer() {
        return offline;
    }

    public ItemStack displaySkull() {
        Validate.notNull(offline, "Skull owner not found");

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(offline);
        skull.setItemMeta(skullMeta);
        return skull;
    }
}
