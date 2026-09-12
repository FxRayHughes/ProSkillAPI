package com.sucy.skill.nms.v26_2;

import com.sucy.skill.nms.MinecraftVersion;
import com.sucy.skill.nms.NmsBridge;
import com.sucy.skill.nms.NmsBridgeFactory;

/**
 * 服务 26.2 及之后的版本，同时充当版本探测失败时的兜底。
 *
 * <p>桥接实现完全基于公开 API 且带运行时探测，因此在无法识别核心时
 * 提供本实现远好过拒绝启动。</p>
 */
public class V26_2BridgeFactory implements NmsBridgeFactory {

    @Override
    public String id() {
        return "v26_2";
    }

    @Override
    public boolean supports(MinecraftVersion version) {
        if (version.isUnknown()) {
            return true;
        }
        // 26.2 起，以及任何更高的大版本
        return version.getMajor() > 26
                || (version.getMajor() == 26 && version.getMinor() >= 2);
    }

    @Override
    public NmsBridge create() {
        return new V26_2Bridge();
    }
}
