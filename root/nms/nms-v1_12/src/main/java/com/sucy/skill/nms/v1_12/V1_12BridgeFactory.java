package com.sucy.skill.nms.v1_12;

import com.sucy.skill.nms.NmsBridge;

/**
 * Serves 1.12.
 */
public class V1_12BridgeFactory extends LegacyBridgeFactory {
    public V1_12BridgeFactory() {
        super(12);
    }

    @Override
    public NmsBridge create() {
        return new V1_12Bridge();
    }
}
