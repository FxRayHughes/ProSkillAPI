package com.sucy.skill.nms.v1_16;

import com.sucy.skill.nms.NmsBridge;

/**
 * Serves 1.16, the first release where Paper's Adventure text API is available.
 */
public class V1_16BridgeFactory extends ModernBridgeFactory {
    public V1_16BridgeFactory() {
        super(1, 16, 1, 17);
    }

    @Override
    public NmsBridge create() {
        return new V1_16Bridge();
    }
}
