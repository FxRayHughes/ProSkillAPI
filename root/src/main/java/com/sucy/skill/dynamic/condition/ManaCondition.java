/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ManaCondition
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
package com.sucy.skill.dynamic.condition;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.player.PlayerSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "mana",
        name = "Mana",
        nameZh = "检查法力",
        description = "Applies child components when the target's mana matches the settings.",
        descriptionZh = "要求目标是玩家，并按法力值过滤：可比较绝对值、占自身最大法力的百分比，或与技能所属玩家的差值/百分比差值，结果落在 [最小值, 最大值] 闭区间内才通过。差值类型取的是技能数据所属玩家的法力，施法者不是玩家时会因取不到技能数据而报错。",
        container = true)
public class ManaCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of measurement to use for the mana. Mana is their flat mana left. Percent is the percentage of mana they have left. Difference is the difference between the target's flat mana and the caster's. Difference percent is the difference between the target's percentage mana left and the casters",
            tooltipZh = "Mana=法力绝对值；Percent=占自身最大法力的百分比；Difference=目标减技能所属玩家的法力；Difference Percent=两者百分比之差。比较忽略大小写，未匹配到时按 Mana 处理。",
            options = {"Mana", "Percent", "Difference", "Difference Percent"},
            optionsZh = {"法力", "可选值2", "可选值3", "可选值4"},
            defaultValue = "Mana")
    private static final String TYPE = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Min Value",
            labelZh = "最小值",
            tooltip = "[min-value] The minimum amount of mana needed",
            tooltipZh = "下限，闭区间。不填按 0 计算。")
    private static final String MIN  = "min-value";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Max Value",
            labelZh = "最大值",
            tooltip = "[max-value] The maximum amount of mana needed",
            tooltipZh = "上限，闭区间。不填按 99 计算，法力上限较高时务必调大。")
    private static final String MAX  = "max-value";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        if (!(target instanceof Player)) {
            return false;
        }

        final String type = settings.getString(TYPE, "Mana").toLowerCase();
        final double min = parseValues(caster, MIN, level, 0);
        final double max = parseValues(caster, MAX, level, 99);
        final PlayerData data = SkillAPI.getPlayerData((Player) target);
        final PlayerSkill skill = getSkillData(caster);
        final double mana = data.getMana();

        double value;
        switch (type) {
            case "difference percent":
                value = (mana - skill.getPlayerData().getMana()) * 100 / skill.getPlayerData().getMana();
                break;
            case "difference":
                value = mana - skill.getPlayerData().getMana();
                break;
            case "percent":
                value = mana * 100 / data.getMaxMana();
                break;
            default:
                value = mana;
                break;
        }
        return value >= min && value <= max;
    }

    @Override
    public String getKey() {
        return "mana";
    }
}
