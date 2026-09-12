package com.sucy.skill.dynamic.meta;

/**
 * 节点字段在编辑器中的呈现形式。
 * <p>
 * 取值与 new-editor 既有的 {@code kind} 契约一一对应，导出时原样写出，
 * 编辑器因此无需改动读取逻辑。
 */
public enum FieldKind {

    /** 单行文本。 */
    StringValue,

    /** 整数输入。 */
    IntValue,

    /** 小数输入。 */
    DoubleValue,

    /** 随技能等级缩放的数值，导出为 base/scale 两个输入。 */
    AttributeValue,

    /** 单选下拉，取值限定在 options 内。 */
    ListValue,

    /** 多选列表，取值限定在 options 内。 */
    MultiListValue,

    /** 字符串列表，可自由增删行。 */
    StringListValue
}
