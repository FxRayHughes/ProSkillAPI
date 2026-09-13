/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.TimeCondition
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

import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the game time to match the settings
 */
@SkillNode(
        key = "time",
        name = "Time",
        nameZh = "检查时间",
        description = "Applies child components when the server time matches the settings.",
        descriptionZh = "检查施法者所在世界的时间是白天还是夜晚，夜晚定义为世界时间落在 [12300, 23850] 闭区间内。该节点不逐目标判断，只判一次，通过后把原目标列表整体交给子节点。",
        container = true)
public class TimeCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Time",
            labelZh = "时间",
            tooltip = "[time] The time to check for in the current world",
            tooltipZh = "只有值等于“night”（忽略大小写）才要求处于夜晚区间，其余任何值（含 Day）都要求时间在该区间之外。",
            options = {"Day", "Night"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "Day")
    private static final String TIME = "time";

    @Override
    public String getKey() {
        return "time";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        return test(caster, level, null) && executeChildren(caster, level, targets);
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final boolean night = settings.getString(TIME, "Day").toLowerCase().equals("night");
        return night == (caster.getWorld().getTime() >= 12300 && caster.getWorld().getTime() <= 23850);
    }
}
