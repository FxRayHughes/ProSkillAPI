package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.util.Buff;
import com.sucy.skill.api.util.BuffManager;
import com.sucy.skill.api.util.BuffType;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2017
 * com.sucy.skill.dynamic.mechanic.BuffMechanic
 */
@SkillNode(
        key = "buff",
        name = "Buff",
        nameZh = "增益",
        description = "Buffs combat stats of the target",
        descriptionZh = "给目标加一个限时的战斗数值增益/减益，可作用于普攻伤害、普攻防御、技能伤害、技能防御或治疗。若开启「立即执行」则完全走另一条路：不给任何目标挂 buff，而是只修改当前这一次伤害触发的数值，然后立刻返回。目标列表为空返回 false。注意「类型」填了非法值会抛异常（BuffType.valueOf 无兜底）。")
public class BuffMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Modifier",
            labelZh = "修饰方式",
            tooltip = "[modifier] The sort of scaling for the buff. Flat will increase/reduce incoming damage by a fixed amount where Multiplier does it by a percentage of the damage. Multipliers above 1 will increase damage taken while multipliers below 1 reduce damage taken.",
            tooltipZh = "数值的计算方式。Flat 为固定值加减，Multiplier 为按比例缩放（大于 1 增伤、小于 1 减伤）。",
            options = {"Flat", "Multiplier"},
            optionsZh = {"可选值1", "倍率"},
            defaultValue = "Flat")
    private static final String MODIFIER = "modifier";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Category",
            labelZh = "类别",
            tooltip = "[category] What kind of skill damage to affect. If left empty, this will affect all skill damage.",
            tooltipZh = "限定影响哪一类技能伤害。留空表示影响全部技能伤害。")
    private static final String CATEGORY = "category";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] What type of buff to apply. DAMAGE/DEFENSE is for regular attacks, SKILL_DAMAGE/SKILL_DEFENSE are for damage from abilities, and HEALING is for healing from abilities",
            tooltipZh = "增益作用的对象：DAMAGE/DEFENSE 对普通攻击，SKILL_DAMAGE/SKILL_DEFENSE 对技能伤害，HEALING 对技能治疗。填非枚举值会直接抛异常。",
            options = {"DAMAGE", "DEFENSE", "SKILL_DAMAGE", "SKILL_DEFENSE", "HEALING"},
            optionsZh = {"可选值1", "可选值2", "可选值3", "可选值4", "可选值5"},
            defaultValue = "DAMAGE")
    private static final String TYPE     = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Value",
            labelZh = "数值",
            tooltip = "[value] The amount to increase/decrease incoming damage by",
            tooltipZh = "增益数值，默认 1.0，随技能等级缩放。含义取决于「修饰方式」是固定值还是倍率。")
    private static final String VALUE    = "value";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Seconds",
            labelZh = "秒数",
            tooltip = "[seconds] The duration of the buff in seconds",
            tooltipZh = "增益持续秒数，默认 3.0，随技能等级缩放。内部乘 20 换算成 tick。开启「立即执行」时该项被忽略。")
    private static final String SECONDS  = "seconds";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Immediate",
            labelZh = "立即执行",
            tooltip = "[immediate] Whether or not to apply the buff to the current damage trigger.",
            tooltipZh = "为 true 时不挂持续 buff，只对当前正在处理的这次伤害触发生效一次，且忽略「类型」「秒数」「类别」。只在伤害类触发器下面用才有意义。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String IMMEDIATE = "immediate";

    @Override
    public String getKey() {
        return "buff";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0) return false;

        boolean immediate = settings.getString(IMMEDIATE, "false").equalsIgnoreCase("true");
        double value = parseValues(caster, VALUE, level, 1.0);
        boolean percent = settings.getString(MODIFIER, "flat").equalsIgnoreCase("multiplier");

        if (immediate) {
            skill.setImmediateBuff(value, !percent);
            return true;
        }

        final BuffType buffType;
        try {
            buffType = BuffType.valueOf(settings.getString(TYPE, "DAMAGE").toUpperCase());
        } catch (IllegalArgumentException ex) {
            // A malformed config entry should fail this mechanic only, rather
            // than aborting the entire dynamic skill registration.
            return false;
        }
        double seconds = parseValues(caster, SECONDS, level, 3.0);
        String category = settings.getString(CATEGORY, null);
        int ticks = (int) (seconds * 20);
        for (LivingEntity target : targets) {
            BuffManager.getBuffData(target).addBuff(
                    buffType,
                    category,
                    new Buff(this.skill.getName() + "-" + caster.getName(), value, percent),
                    ticks);
        }
        return targets.size() > 0;
    }
}
