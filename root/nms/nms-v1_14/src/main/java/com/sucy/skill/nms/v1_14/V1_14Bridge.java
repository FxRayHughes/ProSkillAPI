package com.sucy.skill.nms.v1_14;

import com.sucy.skill.nms.v1_13.V1_13Bridge;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Bridge for the 1.14 and 1.15 Bukkit API generations.
 *
 * <p>These releases retain the public 1.13 behavior but add
 * {@code ItemMeta#setCustomModelData(Integer)}. Keeping the two direct calls
 * in this module prevents the main plugin and the 1.12/1.13 class paths from
 * resolving methods that do not exist on their server APIs.</p>
 */
public class V1_14Bridge extends V1_13Bridge {
    @Override
    public String id() {
        return "v1_14";
    }

    /**
     * Writes the integer model identifier using the API introduced in 1.14.
     * Bukkit stores this value in ItemMeta, so the caller must still attach
     * the modified metadata to its ItemStack after all other edits.
     *
     * @param meta metadata to modify
     * @param data custom-model identifier
     * @return true when metadata was available and updated
     */
    @Override
    public boolean setCustomModelData(ItemMeta meta, int data) {
        if (meta == null) {
            return false;
        }
        meta.setCustomModelData(data);
        return true;
    }

    /**
     * Reads the model identifier only when the metadata explicitly contains
     * one; zero is a valid configured value and therefore must not be used as
     * the absence marker.
     *
     * @param meta metadata to inspect
     * @return configured identifier, or {@code null} when unset
     */
    @Override
    public Integer getCustomModelData(ItemMeta meta) {
        return meta != null && meta.hasCustomModelData() ? meta.getCustomModelData() : null;
    }
}
