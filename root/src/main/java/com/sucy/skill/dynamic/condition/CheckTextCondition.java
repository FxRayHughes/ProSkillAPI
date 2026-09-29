package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.Locale;

/** Checks text without converting a missing cast value to an empty string. */
@SkillNode(key = "check text", name = "Check Text", nameZh = "检查文本",
        descriptionZh = "支持全等、包含、前缀和后缀；缺值不满足。", container = true)
public final class CheckTextCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Value Key", labelZh = "文本键", defaultValue = "text")
    private static final String KEY = "key";
    @SkillField(kind = FieldKind.StringValue, label = "Expected Text", labelZh = "期望文本", defaultValue = "")
    private static final String TEXT = "text";
    @SkillField(kind = FieldKind.ListValue, label = "Mode", labelZh = "匹配方式",
            options = {"equals", "contains", "prefix", "suffix"},
            optionsZh = {"全等", "包含", "前缀", "后缀"}, defaultValue = "equals")
    private static final String MODE = "mode";
    @SkillField(kind = FieldKind.BooleanValue, label = "Case Sensitive", labelZh = "区分大小写", defaultValue = "True")
    private static final String CASE = "case-sensitive";
    @Override public String getKey() { return "check text"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        Object raw = DynamicSkill.getCastData(caster).get(settings.getString(KEY, "text"));
        if (raw == null) return false;
        String actual = raw.toString();
        String expected = settings.getString(TEXT, "");
        if (!settings.getBool(CASE, true)) {
            actual = actual.toLowerCase(Locale.ROOT);
            expected = expected.toLowerCase(Locale.ROOT);
        }
        switch (settings.getString(MODE, "equals")) {
            case "equals": return actual.equals(expected);
            case "contains": return actual.contains(expected);
            case "prefix": return actual.startsWith(expected);
            case "suffix": return actual.endsWith(expected);
            default: return false;
        }
    }
}
