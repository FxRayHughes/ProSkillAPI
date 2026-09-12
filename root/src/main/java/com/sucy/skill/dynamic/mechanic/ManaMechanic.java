/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ManaMechanic
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

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ManaCost;
import com.sucy.skill.api.enums.ManaSource;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Gives mana to each target
 */
@SkillNode(
        key = "mana",
        name = "Mana",
        nameZh = "法力",
        description = "Restores or deducts mana from the target.",
        descriptionZh = "为玩家目标回复或扣除法力，非玩家目标会被跳过（法力是玩家专属资源）。可选固定点数或按目标最大法力的百分比。计算结果为正走回复流程（来源记为技能），为负走消耗流程（消耗类型记为技能效果），因此会分别受各自的事件与上限规则约束。只有至少处理了一个玩家目标才返回成功。")
public class ManaMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The unit to use for the amount of mana to restore/drain. Mana does a flat amount while Percent does a percentage of their max mana",
            tooltipZh = "数值单位：「法力」按固定点数增减，「百分比」按目标最大法力的百分之几增减。默认按固定法力值。",
            options = {"Mana", "Percent"},
            optionsZh = {"法力", "可选值2"},
            defaultValue = "Mana")
    private static final String TYPE  = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Value",
            labelZh = "数值",
            tooltip = "[value] The amount of mana to restore/drain",
            tooltipZh = "法力变化量，配合类型解释为点数或百分比；正数回复，负数扣除。数值随技能等级/属性变化，默认 1。")
    private static final String VALUE = "value";

    @Override
    public String getKey() {
        return "mana";
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
        boolean percent = settings.getString(TYPE, "mana").toLowerCase().equals("percent");
        double value = parseValues(caster, VALUE, level, 1.0);

        boolean worked = false;
        for (LivingEntity target : targets) {
            if (!(target instanceof Player)) {
                continue;
            }

            worked = true;

            PlayerData data = SkillAPI.getPlayerData((Player) target);
            double amount;
            if (percent) {
                amount = data.getMaxMana() * value / 100;
            } else {
                amount = value;
            }

            if (amount > 0) {
                data.giveMana(amount, ManaSource.SKILL);
            } else {
                data.useMana(-amount, ManaCost.SKILL_EFFECT);
            }
        }
        return worked;
    }
}
