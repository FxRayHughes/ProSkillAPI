package com.sucy.skill.nms.v1_10;

import com.sucy.skill.nms.v1_9.V1_9Bridge;

/**
 * Bridge for the 1.10 NMS generation.
 *
 * <p>Delta from 1.9: none of the internals SkillAPI touches changed. The module
 * exists so 1.10 has an explicit, named owner and so a future 1.10-only fix has
 * an obvious home instead of being wedged into a range check.</p>
 */
public class V1_10Bridge extends V1_9Bridge {
    @Override
    public String id() {
        return "v1_10";
    }
}
