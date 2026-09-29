package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

/** Compares a present finite cast value; absent data never falls back to zero. */
@SkillNode(key = "check number", name = "Check Number", nameZh = "精确检查数值",
        descriptionZh = "支持比较与开闭区间；缺值、非数字或非有限值均不满足。", container = true)
public final class CheckNumberCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Value Key", labelZh = "数值键", defaultValue = "value")
    private static final String KEY = "key";
    @SkillField(kind = FieldKind.ListValue, label = "Operator", labelZh = "比较方式",
            options = {"lt", "lte", "eq", "neq", "gte", "gt", "between"},
            optionsZh = {"小于", "小于等于", "等于", "不等于", "大于等于", "大于", "区间"}, defaultValue = "gte")
    private static final String OP = "operator";
    @SkillField(kind = FieldKind.AttributeValue, label = "Reference", labelZh = "比较值", defaultValue = "0")
    private static final String REF = "reference";
    @SkillField(kind = FieldKind.AttributeValue, label = "Minimum", labelZh = "区间下限", defaultValue = "0")
    private static final String MIN = "minimum";
    @SkillField(kind = FieldKind.AttributeValue, label = "Maximum", labelZh = "区间上限", defaultValue = "1")
    private static final String MAX = "maximum";
    @SkillField(kind = FieldKind.BooleanValue, label = "Include Minimum", labelZh = "包含下限", defaultValue = "True")
    private static final String INCLUDE_MIN = "include-minimum";
    @SkillField(kind = FieldKind.BooleanValue, label = "Include Maximum", labelZh = "包含上限", defaultValue = "True")
    private static final String INCLUDE_MAX = "include-maximum";

    @Override public String getKey() { return "check number"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        Object raw = DynamicSkill.getCastData(caster).get(settings.getString(KEY, "value"));
        if (raw == null) return false;
        double value;
        try { value = Double.parseDouble(raw.toString()); }
        catch (NumberFormatException ignored) { return false; }
        if (!Double.isFinite(value)) return false;
        String operator = settings.getString(OP, "gte");
        if ("between".equals(operator)) {
            double min = parseValues(caster, MIN, level, 0);
            double max = parseValues(caster, MAX, level, 1);
            return Double.isFinite(min) && Double.isFinite(max) && min <= max
                    && (settings.getBool(INCLUDE_MIN, true) ? value >= min : value > min)
                    && (settings.getBool(INCLUDE_MAX, true) ? value <= max : value < max);
        }
        double reference = parseValues(caster, REF, level, 0);
        if (!Double.isFinite(reference)) return false;
        switch (operator) {
            case "lt": return value < reference;
            case "lte": return value <= reference;
            case "eq": return value == reference;
            case "neq": return value != reference;
            case "gt": return value > reference;
            case "gte": return value >= reference;
            default: return false;
        }
    }
}
