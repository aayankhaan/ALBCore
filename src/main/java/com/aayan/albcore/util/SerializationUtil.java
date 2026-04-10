package com.aayan.albcore.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class SerializationUtil {

    private SerializationUtil() {}

    /**
     * Serialize an ItemStack into a Base64 string.
     * This preserves all NBT data, including custom names, lore, and rarities.
     *
     * @param item The ItemStack to serialize.
     * @return The Base64 string representing the item.
     */
    public static String toBase64(ItemStack item) {
        if (item == null) return "";
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataStream = new BukkitObjectOutputStream(outputStream)) {

            dataStream.writeObject(item);
            return Base64Coder.encodeLines(outputStream.toByteArray());

        } catch (IOException e) {
            java.util.logging.Logger.getLogger("ALBCore")
                    .warning("[ALBCore | SerializationUtil] Failed to serialize item: " + e.getMessage());
            return "";
        }
    }

    /**
     * Deserialize a Base64 string back into an ItemStack.
     *
     * @param base64 The Base64 string to deserialize.
     * @return The resulting ItemStack, or null if deserialization failed.
     */
    public static ItemStack fromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
             BukkitObjectInputStream dataStream = new BukkitObjectInputStream(inputStream)) {

            return (ItemStack) dataStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            java.util.logging.Logger.getLogger("ALBCore")
                    .warning("[ALBCore | SerializationUtil] Failed to deserialize item: " + e.getMessage());
            return null;
        }
    }
}
