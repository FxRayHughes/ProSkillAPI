/**
 * SkillAPI
 * com.sucy.skill.serialization.ItemSerializationService
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.serialization;

import org.bukkit.inventory.ItemStack;

/**
 * Stable item serialization boundary. Implementations own their format prefix
 * and must return null for unsupported input so multiple modules can coexist
 * during long server upgrade paths.
 */
public interface ItemSerializationService {
    /**
     * @param items inventory contents to persist
     * @return serialized data, or null when this module cannot write the data
     */
    String serialize(ItemStack[] items);

    /**
     * @param data persisted inventory data
     * @return restored contents, or null when the format belongs to another module
     */
    ItemStack[] deserialize(String data);
}
