package com.sucy.skill.nms.v1_21;

import com.sucy.skill.nms.NmsBridge;

/**
 * Serves 1.21 up to the version-scheme change. The upper bound is major 2 so
 * any future 1.22+ release still lands here rather than falling through.
 */
public class V1_21BridgeFactory extends ModernBridgeFactory {
    public V1_21BridgeFactory() {
        super(1, 21, 2, 0);
    }

    @Override
    public NmsBridge create() {
        return new V1_21Bridge();
    }
}
