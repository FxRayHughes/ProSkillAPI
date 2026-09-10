/**
 * SkillAPI
 * com.sucy.skill.serialization.gson.GsonItemSerializationService
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.serialization.gson;

import com.google.gson.annotations.SerializedName;
import com.sucy.skill.serialization.ItemSerializationService;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Current item format backed by Gson. The JSON payload is Base64 wrapped to
 * retain the historical method/storage contract while avoiding delimiter
 * collisions in configuration values.
 */
public final class GsonItemSerializationService implements ItemSerializationService {
    public static final String GSON_PREFIX = "gson-v1:";
    private static final String BUKKIT_STREAM_PREFIX = "bukkit:";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    public String serialize(ItemStack[] items) {
        if (items == null) {
            return null;
        }
        try {
            InventoryPayload payload = new InventoryPayload();
            payload.size = items.length;
            payload.slots = new ArrayList<SlotPayload>();
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    SlotPayload slot = new SlotPayload();
                    slot.index = i;
                    slot.item = items[i];
                    payload.slots.add(slot);
                }
            }
            byte[] json = GsonUtils.toJson(payload).getBytes(UTF_8);
            return GSON_PREFIX + Base64.getEncoder().encodeToString(json);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public ItemStack[] deserialize(String data) {
        if (data == null) {
            return null;
        }
        if (data.startsWith(GSON_PREFIX)) {
            return readGson(data.substring(GSON_PREFIX.length()));
        }
        if (data.startsWith(BUKKIT_STREAM_PREFIX)) {
            return readLegacyBukkitStream(data.substring(BUKKIT_STREAM_PREFIX.length()));
        }
        return null;
    }

    private ItemStack[] readGson(String encoded) {
        try {
            byte[] json = Base64.getDecoder().decode(encoded);
            InventoryPayload payload = GsonUtils.fromJson(new String(json, UTF_8), InventoryPayload.class);
            if (payload == null || payload.size < 0) {
                return null;
            }
            ItemStack[] items = new ItemStack[payload.size];
            if (payload.slots != null) {
                for (SlotPayload slot : payload.slots) {
                    if (slot != null && slot.index >= 0 && slot.index < items.length) {
                        items[slot.index] = slot.item;
                    }
                }
            }
            return items;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Reads the temporary Bukkit object-stream format from the previous
     * compatibility refactor so upgrading servers do not lose combat snapshots.
     */
    private ItemStack[] readLegacyBukkitStream(String encoded) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            BukkitObjectInputStream input = new BukkitObjectInputStream(new ByteArrayInputStream(bytes));
            ItemStack[] items = new ItemStack[input.readInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) input.readObject();
            }
            input.close();
            return items;
        } catch (Exception ex) {
            return null;
        }
    }

    public static final class InventoryPayload {
        private int size;
        private List<SlotPayload> slots;
    }

    public static final class SlotPayload {
        private int index;
        @SerializedName("item")
        private ItemStack item;
    }
}
