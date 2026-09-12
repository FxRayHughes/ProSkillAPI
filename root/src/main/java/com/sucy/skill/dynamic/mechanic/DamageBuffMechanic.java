/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.DamageBuffMechanic
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
 * Applies a flag to each target
 */
@SkillNode(
        key = "damage buff",
        name = "Damage Buff",
        nameZh = "伤害增益",
        description = "Modifies the physical damage dealt by each target by a multiplier or a flat amount for a limited duration. Negative flat amounts or multipliers less than one will reduce damage dealt while the opposite will increase damage dealt. (e.g. a 5% damage buff would be a multiplier or 1.05)",
        descriptionZh = "限时提升或削弱目标造成的伤害，可选作用于普攻伤害或技能伤害。固定值为负、或倍率小于 1 都是减伤，反之增伤（例如增伤 5% 用倍率 1.05）。buff 以当前技能名为标识写入 BuffManager，到时自动失效。目标列表为空返回 false。")
public class DamageBuffMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of buff to apply. Flat increases damage by a fixed amount while multiplier increases it by a percentage.",
            tooltipZh = "数值计算方式。Flat 为固定值增减，Multiplier 为按比例缩放。",
            options = {"Flat", "Multiplier"},
            optionsZh = {"可选值1", "倍率"},
            defaultValue = "Flat")
    private static final String TYPE    = "type";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Skill Damage",
            labelZh = "技能伤害",
            tooltip = "[skill] Whether or not to buff skill damage. If false, it will affect physical damage.",
            tooltipZh = "对应配置键 skill。为 true 时影响目标造成的技能伤害（SKILL_DAMAGE），为 false 时影响普通物理攻击伤害（DAMAGE）。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String SKILL   = "skill";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Value",
            labelZh = "数值",
            tooltip = "[value] The amount to increase/decrease the damage by. A negative amoutn with the \"Flat\" type will decrease damage, similar to a number less than 1 for the multiplier.",
            tooltipZh = "增伤数值，默认 1.0，随技能等级缩放。固定值模式下填负数为减伤，倍率模式下填小于 1 为减伤。")
    private static final String VALUE   = "value";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Seconds",
            labelZh = "秒数",
            tooltip = "[seconds] The duration of the buff in seconds",
            tooltipZh = "buff 持续秒数，默认 3.0，随技能等级缩放。内部乘 20 换算成 tick。")
    private static final String SECONDS = "seconds";

    @Override
    public String getKey() {
        return "damage buff";
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
        if (targets.size() == 0) { return false; }

        boolean skill = settings.getString(SKILL, "false").equalsIgnoreCase("true");
        boolean percent = settings.getString(TYPE, "flat").toLowerCase().equals("multiplier");
        double value = parseValues(caster, VALUE, level, 1.0);
        double seconds = parseValues(caster, SECONDS, level, 3.0);
        int ticks = (int) (seconds * 20);
        for (LivingEntity target : targets) {
            BuffManager.addBuff(
                    target,
                    skill ? BuffType.SKILL_DAMAGE : BuffType.DAMAGE,
                    new Buff(this.skill.getName(), value, percent),
                    ticks);
        }
        return targets.size() > 0;
    }
}
