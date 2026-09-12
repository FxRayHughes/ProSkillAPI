package com.sucy.skill.nms.v1_17;

import com.sucy.skill.nms.NmsBridge;

/**
 * Serves 1.17 through 1.19.
 */
public class V1_17BridgeFactory extends ModernBridgeFactory {
    public V1_17BridgeFactory() {
        super(1, 17, 1, 20);
    }

    @Override
    public NmsBridge create() {
        return new V1_17Bridge();
    }
}
