package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Map;

/**
 * Shared typed numeric source and finite-result validation for value operations.
 * Subclasses only select a formula; the v1 field protocol stays identical.
 */
public abstract class AbstractValueOperation extends MechanicComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Output Key", labelZh = "结果键", defaultValue = "value")
    private static final String OUTPUT = "key";
    @SkillField(kind = FieldKind.ListValue, label = "A Source", labelZh = "输入 A 来源",
            options = {"literal", "attribute", "cast-data", "signal"},
            optionsZh = {"字面值", "属性", "施法数据", "信号参数"}, defaultValue = "literal")
    private static final String A_SOURCE = "a-source";
    @SkillField(kind = FieldKind.StringValue, label = "A Key", labelZh = "输入 A 键")
    private static final String A_KEY = "a-key";
    @SkillField(kind = FieldKind.DoubleValue, label = "A Literal", labelZh = "输入 A 数值", defaultValue = "0")
    private static final String A_VALUE = "a-value";
    @SkillField(kind = FieldKind.ListValue, label = "B Source", labelZh = "输入 B 来源",
            options = {"literal", "attribute", "cast-data", "signal"},
            optionsZh = {"字面值", "属性", "施法数据", "信号参数"}, defaultValue = "literal")
    private static final String B_SOURCE = "b-source";
    @SkillField(kind = FieldKind.StringValue, label = "B Key", labelZh = "输入 B 键")
    private static final String B_KEY = "b-key";
    @SkillField(kind = FieldKind.DoubleValue, label = "B Literal", labelZh = "输入 B 数值", defaultValue = "0")
    private static final String B_VALUE = "b-value";
    @SkillField(kind = FieldKind.ListValue, label = "C Source", labelZh = "输入 C 来源",
            options = {"literal", "attribute", "cast-data", "signal"},
            optionsZh = {"字面值", "属性", "施法数据", "信号参数"}, defaultValue = "literal")
    private static final String C_SOURCE = "c-source";
    @SkillField(kind = FieldKind.StringValue, label = "C Key", labelZh = "输入 C 键")
    private static final String C_KEY = "c-key";
    @SkillField(kind = FieldKind.DoubleValue, label = "C Literal", labelZh = "输入 C 数值", defaultValue = "0")
    private static final String C_VALUE = "c-value";
    @SkillField(kind = FieldKind.ListValue, label = "Mode", labelZh = "运算模式",
            options = {"minimum", "maximum", "nearest", "floor", "ceiling"},
            optionsZh = {"最小值", "最大值", "四舍五入", "向下取整", "向上取整"}, defaultValue = "nearest")
    private static final String MODE = "mode";

    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) return false;
        Double a = read(caster, "a");
        if (a == null) return false;
        String operation = getKey().substring("value ".length());
        Double b = operation.equals("round") ? null : read(caster, "b");
        if (!operation.equals("round") && b == null) return false;
        Double c = operation.equals("clamp") || operation.equals("interpolate") ? read(caster, "c") : null;
        if ((operation.equals("clamp") || operation.equals("interpolate")) && c == null) return false;
        double result;
        switch (operation) {
            case "subtract": result = a - b; break;
            case "divide": if (b == 0) return false; result = a / b; break;
            case "remainder": if (b == 0) return false; result = a % b; break;
            case "extremum": result = "maximum".equals(settings.getString(MODE, "minimum"))
                    ? Math.max(a, b) : Math.min(a, b); break;
            case "clamp": if (b > c) return false; result = Math.max(b, Math.min(c, a)); break;
            case "round":
                Double precision = read(caster, "b");
                if (precision == null || precision < 0 || precision > 9 || precision % 1 != 0) return false;
                double factor = Math.pow(10, precision);
                String mode = settings.getString(MODE, "nearest");
                result = ("floor".equals(mode) ? Math.floor(a * factor)
                        : "ceiling".equals(mode) ? Math.ceil(a * factor) : Math.round(a * factor)) / factor;
                break;
            case "interpolate": if (c < 0 || c > 1) return false; result = a + (b - a) * c; break;
            case "angle": result = ((b - a + 540) % 360 + 360) % 360 - 180; break;
            default: return false;
        }
        if (!Double.isFinite(result)) return false;
        String output = settings.getString(OUTPUT, "value");
        if (output.isEmpty()) return false;
        DynamicSkill.getCastData(caster).put(output, result);
        return true;
    }

    /** Null represents a missing or malformed source and is never coerced to zero. */
    private Double read(LivingEntity caster, String operand) {
        String source = settings.getString(operand + "-source", "literal");
        String key = settings.getString(operand + "-key", "");
        Object raw;
        switch (source) {
            case "literal": raw = settings.getObj(operand + "-value", 1); break;
            case "cast-data": raw = key.isEmpty() ? null : DynamicSkill.getCastData(caster).get(key); break;
            case "signal": raw = key.isEmpty() ? null : DynamicSkill.getCastData(caster).get("signal-" + key); break;
            case "attribute":
                if (key.isEmpty() || SkillAPI.getAttributeManager() == null
                        || !SkillAPI.getAttributeManager().getAttributes().containsKey(key.toLowerCase())) return null;
                raw = AttributeAPI.getAttribute(caster, key);
                break;
            default: return null;
        }
        if (raw == null || raw instanceof Map || raw instanceof List) return null;
        try {
            double value = Double.parseDouble(raw.toString());
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException exception) { return null; }
    }
}
