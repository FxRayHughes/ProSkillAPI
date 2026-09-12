package com.sucy.skill.nms.v1_20;

import com.sucy.skill.nms.NmsBridge;

/**
 * Serves 1.20, the release that introduced item data components.
 */
public class V1_20BridgeFactory extends ModernBridgeFactory {
    public V1_20BridgeFactory() {
        super(1, 20, 1, 21);
    }

    @Override
    public NmsBridge create() {
        return new V1_20Bridge();
    }
}
