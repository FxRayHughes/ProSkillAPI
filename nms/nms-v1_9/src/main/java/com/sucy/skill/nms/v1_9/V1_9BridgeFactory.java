package com.sucy.skill.nms.v1_9;

import com.sucy.skill.nms.NmsBridge;
import com.sucy.skill.nms.v1_8.LegacyBridgeFactory;

/**
 * Serves 1.9.
 */
public class V1_9BridgeFactory extends LegacyBridgeFactory {
    public V1_9BridgeFactory() {
        super(9);
    }

    @Override
    public NmsBridge create() {
        return new V1_9Bridge();
    }
}
