/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ClassLevelCondition
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

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "class level",
        name = "Class Level",
        nameZh = "检查职业等级",
        description = "Applies child components when the level of the class with this skill is within the range. This only checks the level of the caster, not the targets.",
        descriptionZh = "检查目标玩家主职业的等级是否落在 [min-level, max-level] 闭区间内。非玩家目标、或玩家没有主职业时不通过。注意实际判定的是每个目标，而不是施法者。",
        container = true)
public class ClassLevelCondition extends ConditionComponent
{
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Min Level",
            labelZh = "最低等级",
            tooltip = "[min-level] The minimum class level the player should be. If the player has multiple classes, this will be of their main class",
            tooltipZh = "主职业等级下限，闭区间。该值在加载时读取且不随技能等级缩放；配置里缺省时按 0 处理（不是标注的 2）。",
            defaultValue = "2")
    private static final String MIN_LEVEL = "min-level";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Max Level",
            labelZh = "最高等级",
            tooltip = "[max-level] The maximum class level the player should be. If the player has multiple classes, this will be of their main class",
            tooltipZh = "主职业等级上限，闭区间。加载时读取；配置里缺省时按 0 处理，会导致除 0 级外全部不通过，务必显式填写。",
            defaultValue = "99")
    private static final String MAX_LEVEL = "max-level";

    private int min, max;

    @Override
    public String getKey() {
        return "class level";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        min = settings.getInt(MIN_LEVEL);
        max = settings.getInt(MAX_LEVEL);
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        if (!(target instanceof Player)) return false;

        final PlayerClass playerClass = SkillAPI.getPlayerData((Player) target).getMainClass();
        return playerClass != null && playerClass.getLevel() >= min && playerClass.getLevel() <= max;
    }
}
