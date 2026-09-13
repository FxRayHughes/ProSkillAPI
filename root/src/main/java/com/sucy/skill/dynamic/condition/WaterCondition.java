/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.WaterCondition
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

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target to have a specified potion effect
 */
@SkillNode(
        key = "water",
        name = "Water",
        nameZh = "检查水中",
        description = "Applies child components when the target is in or out of water, depending on the settings.",
        descriptionZh = "按目标脚部所在方块的材质名是否含“WATER”判断是否在水中。只看脚部那一格，且靠名称包含判断，含水方块（waterlogged）不算。",
        container = true)
public class WaterCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "State",
            labelZh = "状态",
            tooltip = "[state] Whether or not the target needs to be in the water",
            tooltipZh = "只有值恰好等于“out of water”（忽略大小写）才要求不在水中，其余任何值都要求在水中。",
            options = {"In Water", "Out Of Water"},
            optionsZh = {"水", "水"},
            defaultValue = "In Water")
    private static final String STATE = "state";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final boolean out = settings.getString(STATE, "In Water").toLowerCase().equals("out of water");
        final Material block = target.getLocation().getBlock().getType();
        return out != (block.name().contains("WATER"));
    }

    @Override
    public String getKey() {
        return "water";
    }
}
