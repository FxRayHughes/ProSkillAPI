package com.sucy.skill.nms.v26;

import com.sucy.skill.nms.MinecraftVersion;
import com.sucy.skill.nms.NmsBridge;
import com.sucy.skill.nms.NmsBridgeFactory;

/**
 * Serves major version 2 and above, which covers the current 26.x line and any
 * later release under the new scheme.
 *
 * <p>This is also the fallback for an undetectable core: the bridge is entirely
 * public API with runtime probes, so serving it is strictly better than
 * refusing to start.</p>
 */
public class V26BridgeFactory implements NmsBridgeFactory {
    @Override
    public String id() {
        return "v26";
    }

    @Override
    public boolean supports(MinecraftVersion version) {
        return version.isUnknown() || version.getMajor() >= 2;
    }

    @Override
    public NmsBridge create() {
        return new V26Bridge();
    }
}
