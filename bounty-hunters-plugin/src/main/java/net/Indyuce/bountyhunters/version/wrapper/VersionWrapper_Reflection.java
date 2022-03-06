package net.Indyuce.bountyhunters.version.wrapper;

import net.Indyuce.bountyhunters.BountyHunters;
import net.Indyuce.bountyhunters.version.wrapper.api.ItemTag;
import net.Indyuce.bountyhunters.version.wrapper.api.NBTItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VersionWrapper_Reflection implements VersionWrapper {

    @Override
    public boolean matchesMaterial(ItemStack item, ItemStack item1) {
        return item.getType() == item1.getType();
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int ticks, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, ticks, fadeOut);
    }

    @Override
    public void sendJson(Player player, String message) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            ServerGamePacketListenerImpl connection = (ServerGamePacketListenerImpl) handle.getClass().getField("connection").get(handle);
            connection.send(new ClientboundChatPacket(Component.Serializer.fromJson(message), ChatType.GAME_INFO, UUID.randomUUID()));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException exception) {
            throw new RuntimeException("Reflection issue: " + exception.getMessage());
        }
    }

    /**
     * @return Object required to send packets
     */
    private Class<?> obc(String str) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + BountyHunters.getInstance().getVersion().toString() + "." + str);
    }

    @Override
    public ItemStack getHead(OfflinePlayer player) {

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public void spawnParticle(Particle particle, Location loc, Player player, Color color) {
        player.spawnParticle(particle, loc, 0, new Particle.DustOptions(color, 1));
    }

    @Override
    public void setOwner(SkullMeta meta, OfflinePlayer player) {
        meta.setOwningPlayer(player);
    }

    @Override
    public NBTItem getNBTItem(ItemStack item) {
        return new NBTItem_Reflection(item);
    }

    public class NBTItem_Reflection extends NBTItem {
        private final net.minecraft.world.item.ItemStack nms;
        private final CompoundTag compound;

        public NBTItem_Reflection(ItemStack item) {
            super(item);

            try {
                nms = (net.minecraft.world.item.ItemStack) obc("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException
                    | ClassNotFoundException exception) {
                throw new RuntimeException("Reflection issue: " + exception.getMessage());
            }

            compound = nms.hasTag() ? nms.getTag() : new CompoundTag();
        }

        @Override
        public String getString(String path) {
            return compound.getString(path);
        }

        @Override
        public boolean hasTag(String path) {
            return compound.contains(path);
        }

        @Override
        public boolean getBoolean(String path) {
            return compound.getBoolean(path);
        }

        @Override
        public double getDouble(String path) {
            return compound.getDouble(path);
        }

        @Override
        public int getInteger(String path) {
            return compound.getInt(path);
        }

        @Override
        public NBTItem addTag(List<ItemTag> tags) {
            tags.forEach(tag -> {
                if (tag.getValue() instanceof Boolean)
                    compound.putBoolean(tag.getPath(), (boolean) tag.getValue());
                else if (tag.getValue() instanceof Double)
                    compound.putDouble(tag.getPath(), (double) tag.getValue());
                else if (tag.getValue() instanceof String)
                    compound.putString(tag.getPath(), (String) tag.getValue());
                else if (tag.getValue() instanceof Integer)
                    compound.putInt(tag.getPath(), (int) tag.getValue());
            });
            return this;
        }

        @Override
        public NBTItem removeTag(String... paths) {
            for (String path : paths)
                compound.remove(path);
            return this;
        }

        @Override
        public Set<String> getTags() {
            return compound.getAllKeys();
        }

        @Override
        public ItemStack toItem() {
            nms.setTag(compound);

            try {
                return (ItemStack) obc("inventory.CraftItemStack").getMethod("asBukkitCopy", nms.getClass()).invoke(null, nms);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException exception) {
                throw new RuntimeException("Reflection issue: " + exception.getMessage());
            }
        }
    }
}
