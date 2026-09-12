/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.SkillLevelCondition
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

import com.sucy.skill.api.player.PlayerSkill;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "skill level",
        name = "Skill Level",
        nameZh = "检查技能等级",
        description = "Applies child components when the skill level is with the range. This checks the skill level of the caster, not the targets.",
        descriptionZh = "检查施法者（不是目标）指定技能的等级是否落在 [min-level, max-level] 闭区间内。该节点只判一次，通过后把原目标列表整体交给子节点。找不到填写的技能名时，退回用当前触发的这个技能的等级来判断。",
        container = true)
public class SkillLevelCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Skill",
            labelZh = "技能",
            tooltip = "[skill] The name of the skill to check the level of. If you want to check the current skill, enter the current skill's name anyway",
            tooltipZh = "要查等级的技能名。填当前技能名即检查自身等级；填的名字在该玩家身上找不到时，会退回使用当前触发技能的等级。")
    private static final String SKILL     = "skill";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Min Level",
            labelZh = "最低等级",
            tooltip = "[min-level] The minimum level of the skill needed",
            tooltipZh = "技能等级下限，闭区间。不填按 1 计算。",
            defaultValue = "2")
    private static final String MIN_LEVEL = "min-level";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Max Level",
            labelZh = "最高等级",
            tooltip = "[max-level] The maximum level of the skill needed",
            tooltipZh = "技能等级上限，闭区间。不填按 99 计算。",
            defaultValue = "99")
    private static final String MAX_LEVEL = "max-level";

    @Override
    public String getKey() {
        return "skill level";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        return test(caster, level, null) && executeChildren(caster, level, targets);
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final int min = settings.getInt(MIN_LEVEL, 1);
        final int max = settings.getInt(MAX_LEVEL, 99);

        final String skill = settings.getString(SKILL, "");
        final PlayerSkill triggeredSkill = getSkillData(caster);
        if (triggeredSkill == null) { return false; }
        PlayerSkill data = triggeredSkill.getPlayerData().getSkill(skill);
        if (data == null) { data = triggeredSkill; }

        return data.getLevel() >= min && data.getLevel() <= max;
    }
}
