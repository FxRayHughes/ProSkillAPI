package com.sucy.skill.nms.v1_11;

import com.sucy.skill.nms.NmsBridge;

/**
 * Serves 1.11.
 */
public class V1_11BridgeFactory extends LegacyBridgeFactory {
    public V1_11BridgeFactory() {
        super(11);
    }

    @Override
    public NmsBridge create() {
        return new V1_11Bridge();
    }
}
