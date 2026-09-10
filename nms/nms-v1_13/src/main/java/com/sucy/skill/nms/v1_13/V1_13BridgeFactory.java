package com.sucy.skill.nms.v1_13;

import com.sucy.skill.nms.NmsBridge;

/**
 * Serves 1.13 through 1.15, the releases that have the flattened API but not
 * yet Adventure.
 */
public class V1_13BridgeFactory extends ModernBridgeFactory {
    public V1_13BridgeFactory() {
        super(1, 13, 1, 16);
    }

    @Override
    public NmsBridge create() {
        return new V1_13Bridge();
    }
}
