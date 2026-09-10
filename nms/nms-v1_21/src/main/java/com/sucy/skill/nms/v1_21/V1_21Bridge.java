package com.sucy.skill.nms.v1_21;

import com.sucy.skill.nms.v1_20.V1_20Bridge;

/**
 * Bridge for the 1.21 generation.
 *
 * <p>Delta from 1.20: the data-component migration finished, and the
 * {@code Attribute} enum became a registry-backed type whose {@code valueOf}
 * is deprecated for removal. Nothing SkillAPI calls through this bridge is
 * affected — attribute lookups live in the bukkit-api compat module — so this
 * generation inherits behaviour and exists to own the 1.21 range.</p>
 */
public class V1_21Bridge extends V1_20Bridge {
    @Override
    public String id() {
        return "v1_21";
    }
}
