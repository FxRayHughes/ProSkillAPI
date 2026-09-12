package com.sucy.skill.hook.mythic;

/**
 * 适配模块与主工程之间的桥接注册表。
 * <p>
 * 主工程在 onEnable 早期调用 {@link #register}，v4/v5 模块通过 {@link #skills()}
 * 取回主工程能力。这样适配模块只需依赖本接口层。
 */
public final class MythicBridges {

    private static volatile SkillBridge skills;

    private MythicBridges() {
    }

    public static void register(final SkillBridge bridge) {
        skills = bridge;
    }

    /**
     * @return 主工程注入的桥接实现；未注册时为 null，调用方需自行短路。
     */
    public static SkillBridge skills() {
        return skills;
    }
}
