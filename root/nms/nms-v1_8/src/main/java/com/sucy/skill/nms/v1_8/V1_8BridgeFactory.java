package com.sucy.skill.nms.v1_8;

import com.sucy.skill.nms.NmsBridge;

/**
 * Serves 1.8, the oldest core SkillAPI supports.
 */
public class V1_8BridgeFactory extends LegacyBridgeFactory {
    public V1_8BridgeFactory() {
        super(8);
    }

    @Override
    public NmsBridge create() {
        return new V1_8Bridge();
    }
}
