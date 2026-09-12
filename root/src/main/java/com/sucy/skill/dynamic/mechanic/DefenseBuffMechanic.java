/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.DefenseBuffMechanic
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
        key = "defense buff",
        name = "Defense Buff",
        nameZh = "防御增益",
        description = "Modifies the physical damage taken by each target by a multiplier or a flat amount for a limited duration. Negative flag amounts or multipliers less than one will reduce damage taken while the opposite will increase damage taken. (e.g. a 5% defense buff would be a multiplier or 0.95, since you would be taking 95% damage)",
        descriptionZh = "限时提升或削弱目标承受的伤害，可选作用于物理防御或技能防御。固定值为负、或倍率小于 1 都表示少受伤（例如减伤 5% 用倍率 0.95），反之则多受伤。buff 以当前技能名为标识写入 BuffManager，到时自动失效。目标列表为空返回 false。")
public class DefenseBuffMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of buff to apply. Flat will increase/reduce incoming damage by a fixed amount where Multiplier does it by a percentage of the damage. Multipliers above 1 will increase damage taken while multipliers below 1 reduce damage taken.",
            tooltipZh = "数值计算方式。Flat 为固定值增减受到的伤害，Multiplier 为按比例缩放（大于 1 多受伤，小于 1 少受伤）。",
            options = {"Flat", "Multiplier"},
            optionsZh = {"可选值1", "倍率"},
            defaultValue = "Flat")
    private static final String TYPE    = "type";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Skill Defense",
            labelZh = "技能防御",
            tooltip = "[skill] Whether or not to buff skill defense. If false, it will affect physical defense.",
            tooltipZh = "对应配置键 skill。为 true 时影响目标承受的技能伤害（SKILL_DEFENSE），为 false 时影响物理伤害（DEFENSE）。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String SKILL   = "skill";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Value",
            labelZh = "数值",
            tooltip = "[value] The amount to increase/decrease incoming damage by",
            tooltipZh = "防御数值，默认 1.0，随技能等级缩放。含义取决于「类型」是固定值还是倍率。")
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
        return "defense buff";
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
                    skill ? BuffType.SKILL_DEFENSE : BuffType.DEFENSE,
                    new Buff(this.skill.getName(), value, percent),
                    ticks);
        }
        return targets.size() > 0;
    }
}
