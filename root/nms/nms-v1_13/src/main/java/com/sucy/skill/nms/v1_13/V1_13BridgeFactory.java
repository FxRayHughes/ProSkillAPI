package com.sucy.skill.nms.v1_13;

import com.sucy.skill.nms.NmsBridge;

/**
 * Serves 1.13, the first flattened release. 1.14 and later have their own
 * bridge because CustomModelData was added to ItemMeta in that release.
 */
public class V1_13BridgeFactory extends ModernBridgeFactory {
    public V1_13BridgeFactory() {
        super(1, 13, 1, 14);
    }

    @Override
    public NmsBridge create() {
        return new V1_13Bridge();
    }
}
