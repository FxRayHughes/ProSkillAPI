package com.sucy.skill.nms.v1_14;

import com.sucy.skill.nms.MinecraftVersion;
import com.sucy.skill.nms.NmsBridge;
import com.sucy.skill.nms.NmsBridgeFactory;

/**
 * Selects the bridge for 1.14 and 1.15, the window where CustomModelData is
 * available but the project still uses the pre-1.16 text implementation.
 */
public class V1_14BridgeFactory implements NmsBridgeFactory {
    @Override
    public String id() {
        return "v1_14";
    }

    @Override
    public boolean supports(MinecraftVersion version) {
        return !version.isUnknown()
                && version.isAtLeast(1, 14)
                && !version.isAtLeast(1, 16);
    }

    @Override
    public NmsBridge create() {
        return new V1_14Bridge();
    }
}
