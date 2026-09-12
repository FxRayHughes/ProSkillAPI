package com.sucy.skill.nms.v26_1;

import com.sucy.skill.nms.MinecraftVersion;
import com.sucy.skill.nms.NmsBridge;
import com.sucy.skill.nms.NmsBridgeFactory;

/**
 * 服务 26.1.x 这一段。
 *
 * <p>26.2 由独立模块接管，因此这里的窗口必须闭合在 minor == 1，
 * 否则会把 26.2 一并吞掉。</p>
 */
public class V26_1BridgeFactory implements NmsBridgeFactory {

    @Override
    public String id() {
        return "v26_1";
    }

    @Override
    public boolean supports(MinecraftVersion version) {
        return version.getMajor() == 26 && version.getMinor() == 1;
    }

    @Override
    public NmsBridge create() {
        return new V26_1Bridge();
    }
}
