package com.sucy.skill.dynamic.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述一个动态技能节点，供编辑器生成节点面板。
 * <p>
 * 标注在 {@code dynamic/} 下的 trigger / target / condition / mechanic 类上。
 * 构建期的 {@code exportNodes} 任务反射读取本注解生成编辑器数据，
 * 运行期插件自身也可读取（RUNTIME 保留）。
 * <p>
 * 节点的分类由类所属的 {@code ComponentType} 决定，无需在注解里重复声明。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SkillNode {

    /**
     * 节点在技能配置里的键，必须与 {@code getKey()} 的返回值一致。
     * 导出任务会校验两者，不一致直接让构建失败。
     */
    String key();

    /** 编辑器中显示的英文名。 */
    String name();

    /** 编辑器中显示的中文名；留空则回退到英文名。 */
    String nameZh() default "";

    /** 英文说明。 */
    String description() default "";

    /** 中文说明；留空则回退到英文说明。 */
    String descriptionZh() default "";

    /**
     * 能否挂子节点，即编辑器里是否绘制输出端点。
     * <p>
     * 默认 false 适用于 mechanic；trigger / target / condition 通常为 true。
     */
    boolean container() default false;

    /**
     * 需要哪些外部插件才能生效。
     * <p>
     * 填插件名（如 {@code MythicMobs}、{@code Vault}）。缺少时该节点通常静默失效，
     * 编辑器据此在节点上给出提示，避免服主配了技能却不知道为何没反应。
     */
    String[] requiresPlugins() default {};

    /**
     * 需要哪些前置节点先执行。
     * <p>
     * 填前置节点的 key（如 {@code remember targets}）。用于表达通过 cast data
     * 传值的隐式依赖：读取方单独存在时拿不到数据，编辑器可提示补上写入方。
     */
    String[] requiresNodes() default {};

    /**
     * 依赖的运行时能力，填 {@link Capability} 的常量名。
     * <p>
     * 用于表达既非插件也非节点的前提，例如脚本机制需要 JVM 带 JavaScript 引擎
     * （Java 15 起 Nashorn 已移除，需自行提供实现）。
     */
    Capability[] requiresCapabilities() default {};

    /** 运行时能力，用于表达非插件、非节点的前提条件。 */
    enum Capability {
        /** 需要可用的 JavaScript 脚本引擎；Java 15 起需自行提供 Nashorn 等实现。 */
        SCRIPT_ENGINE
    }
}
