/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.StatusCondition
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

import com.sucy.skill.api.util.FlagManager;
import com.sucy.skill.api.util.StatusFlag;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target to have a status condition
 */
@SkillNode(
        key = "status",
        name = "Status",
        nameZh = "检查状态",
        description = "Applies child components when the target has the status condition.",
        descriptionZh = "检查目标身上的状态（由状态类机制施加的眩晕、定身等标记）。选“Any”时只要任意一个内置状态生效即算“有状态”；选具体状态时检查对应标记。注意取反判定区分大小写：只有配置值恰好是小写的“not active”才会要求状态不存在。",
        container = true)
public class StatusCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] Whether or not the status should be active",
            tooltipZh = "只有值恰好是小写“not active”才要求状态不存在（此处比较区分大小写，写成“Not Active”不生效），其余任何值都要求状态存在。",
            options = {"Active", "Not Active"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "Active")
    private static final String TYPE   = "type";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Status",
            labelZh = "状态",
            tooltip = "[status] The status to look for",
            tooltipZh = "要检查的状态名，转小写后作为标记键查询。“Any”表示 stun/root/invincible/absorb/disarm/silence/channeling 中任一生效即算有状态；列表里的“Curse”没有对应的内置状态标记。",
            options = {"Any", "Absorb", "Curse", "Disarm", "Invincible", "Root", "Silence", "Stun"},
            optionsZh = {"任意", "状态效果2", "状态效果3", "状态效果4", "状态效果5", "状态效果6", "状态效果7", "状态效果8"},
            defaultValue = "Any")
    private static final String STATUS = "status";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final boolean active = !settings.getString(TYPE, "active").equals("not active");
        final String status = settings.getString(STATUS, "Any").toLowerCase();

        if (status.equals("any")) {
            return active == Arrays.stream(StatusFlag.ALL).anyMatch(flag -> FlagManager.hasFlag(target, flag));
        } else {
            return FlagManager.hasFlag(target, status) == active;
        }
    }

    @Override
    public String getKey() {
        return "status";
    }
}
