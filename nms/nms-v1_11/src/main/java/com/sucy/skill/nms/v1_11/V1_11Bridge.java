package com.sucy.skill.nms.v1_11;

import com.sucy.skill.nms.v1_10.V1_10Bridge;
import com.sucy.skill.nms.v1_8.DamageLoreAccess;
import com.sucy.skill.nms.v1_8.TitleAccess;

/**
 * Bridge for the 1.11 NMS generation.
 *
 * <p>Two public APIs landed in 1.11 that replace NMS work done since 1.8:
 * {@code ItemMeta#setUnbreakable} and {@code Player#sendTitle} with timings.
 * Both are preferred here because they survive future remappings, while the
 * attribute-modifier clearing still has no public equivalent and stays on
 * NBT.</p>
 */
public class V1_11Bridge extends V1_10Bridge {
    @Override
    public String id() {
        return "v1_11";
    }

    @Override
    protected TitleAccess newTitleAccess() {
        return new BukkitTitleAccess();
    }

    @Override
    protected DamageLoreAccess newDamageLoreAccess() {
        return new MetaUnbreakableDamageLoreAccess();
    }
}
