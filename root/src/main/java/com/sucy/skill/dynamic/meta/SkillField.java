package com.sucy.skill.dynamic.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述节点的一个可配置字段。
 * <p>
 * 标注在保存字段键的 {@code static final String} 常量上，例如：
 * <pre>
 * &#64;SkillField(kind = FieldKind.NUMBER, label = "Radius", labelZh = "半径",
 *             tooltip = "Radius of the ring in blocks", defaultValue = "2")
 * private static final String RADIUS = "ring-radius";
 * </pre>
 * 常量的值即配置文件里的键名，因此无需在注解里再写一遍。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SkillField {

    /** 编辑器的呈现形式。 */
    FieldKind kind();

    /** 英文标签。 */
    String label();

    /** 中文标签；留空则回退到英文标签。 */
    String labelZh() default "";

    /** 英文提示。 */
    String tooltip() default "";

    /** 中文提示；留空则回退到英文提示。 */
    String tooltipZh() default "";

    /** 可选值，仅 DROPDOWN 与 LIST 使用。 */
    String[] options() default {};

    /**
     * 可选值的中文翻译，顺序须与 {@link #options()} 一一对应。
     * 长度不匹配时导出任务会报错，避免错位。
     */
    String[] optionsZh() default {};

    /** 默认值；NUMBER 类型填基础值。 */
    String defaultValue() default "";

    /** NUMBER 类型每级的增量，默认 0 表示不随等级变化。 */
    String scale() default "0";
}
