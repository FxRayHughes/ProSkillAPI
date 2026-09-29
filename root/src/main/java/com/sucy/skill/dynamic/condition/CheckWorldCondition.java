package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

/** Uses loaded world metadata only; dimension names are Bukkit environment keys. */
@SkillNode(key = "check world", name = "Check World", nameZh = "检查世界与维度",
        descriptionZh = "同时按世界名与 Bukkit 维度筛选目标；留空的字段不限。", container = true)
public final class CheckWorldCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.StringValue, label = "World Name", labelZh = "世界名")
    private static final String NAME = "world";
    @SkillField(kind = FieldKind.ListValue, label = "Dimension", labelZh = "维度",
            options = {"any", "NORMAL", "NETHER", "THE_END"},
            optionsZh = {"不限", "主世界", "下界", "末地"}, defaultValue = "any")
    private static final String DIMENSION = "dimension";
    @Override public String getKey() { return "check world"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (target == null) return false;
        World world = target.getWorld();
        String name = settings.getString(NAME, "");
        String dimension = settings.getString(DIMENSION, "any");
        return (name.isEmpty() || world.getName().equals(name))
                && ("any".equals(dimension) || world.getEnvironment().name().equals(dimension));
    }
}
