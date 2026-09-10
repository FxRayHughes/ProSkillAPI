package com.sucy.skill.nms.v1_17;

import com.sucy.skill.nms.v1_16.V1_16Bridge;

/**
 * Bridge for the 1.17 - 1.19 generation.
 *
 * <p>Delta from 1.16: 1.17 moved the server internals out of the versioned
 * {@code net.minecraft.server.vX_Y_RZ} package. That is what permanently ended
 * the reflective NMS approach, but because this chain has been API-only since
 * 1.13 there is nothing to adjust for it. The module exists to mark that
 * boundary explicitly and to own the 1.17 - 1.19 range.</p>
 */
public class V1_17Bridge extends V1_16Bridge {
    @Override
    public String id() {
        return "v1_17";
    }
}
