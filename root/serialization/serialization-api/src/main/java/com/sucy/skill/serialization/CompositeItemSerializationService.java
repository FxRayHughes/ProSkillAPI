/**
 * SkillAPI
 * com.sucy.skill.serialization.CompositeItemSerializationService
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.serialization;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Tries item serializers in priority order. New writers should be listed first,
 * while legacy readers stay later so old persisted inventories remain readable
 * without allowing deprecated formats to be written again.
 */
public final class CompositeItemSerializationService implements ItemSerializationService {
    private final List<ItemSerializationService> services;

    public CompositeItemSerializationService(List<ItemSerializationService> services) {
        this.services = new ArrayList<ItemSerializationService>(services);
    }

    @Override
    public String serialize(ItemStack[] items) {
        for (ItemSerializationService service : services) {
            String data = service.serialize(items);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    @Override
    public ItemStack[] deserialize(String data) {
        for (ItemSerializationService service : services) {
            ItemStack[] items = service.deserialize(data);
            if (items != null) {
                return items;
            }
        }
        return null;
    }
}
