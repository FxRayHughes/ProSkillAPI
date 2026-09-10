package com.sucy.skill.nms.v26;

import com.sucy.skill.nms.v1_21.V1_21Bridge;

/**
 * Bridge for the 26.x generation, the current Paper line.
 *
 * <p>Delta from 1.21: the version scheme itself changed from {@code 1.MINOR} to
 * {@code MAJOR.MINOR}. Every text and item call this bridge makes is Adventure
 * or component based by now and is inherited unchanged; owning the range
 * explicitly is what keeps a future 26.x-only fix from having to widen the
 * 1.21 module's window.</p>
 */
public class V26Bridge extends V1_21Bridge {
    @Override
    public String id() {
        return "v26";
    }
}
