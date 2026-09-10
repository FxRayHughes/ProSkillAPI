package com.sucy.skill.nms.v1_12;

import com.sucy.skill.nms.v1_11.V1_11Bridge;
import com.sucy.skill.nms.v1_8.ActionBarAccess;

/**
 * Bridge for the 1.12 NMS generation, the last before the flattening.
 *
 * <p>Delta from 1.11: {@code PacketPlayOutChat} replaced its raw {@code byte}
 * slot argument with the {@code ChatMessageType} enum, so the action bar
 * constructor lookup differs.</p>
 */
public class V1_12Bridge extends V1_11Bridge {
    @Override
    public String id() {
        return "v1_12";
    }

    @Override
    protected ActionBarAccess newActionBarAccess() {
        return new ChatTypeActionBarAccess();
    }
}
