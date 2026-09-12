package com.sucy.skill.nms.v1_10;

import com.sucy.skill.nms.NmsBridge;

/**
 * Serves 1.10.
 */
public class V1_10BridgeFactory extends LegacyBridgeFactory {
    public V1_10BridgeFactory() {
        super(10);
    }

    @Override
    public NmsBridge create() {
        return new V1_10Bridge();
    }
}
