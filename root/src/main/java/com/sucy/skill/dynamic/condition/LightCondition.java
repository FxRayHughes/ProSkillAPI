/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.LightCondition
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
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the lighting at the target's location to be within a range
 */
@SkillNode(
        key = "light",
        name = "Light",
        nameZh = "检查亮度",
        description = "Applies child components when the light level at the target's location matches the settings.",
        descriptionZh = "检查目标所在方块的光照等级，落在 [最低亮度, 最高亮度] 闭区间内才通过。注意两项都没有可用的默认值：最高亮度不填时按 0 处理，会导致只有全黑（光照 0）才通过，务必显式填写上限。",
        container = true)
public class LightCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Min Light",
            labelZh = "最低亮度",
            tooltip = "[min-light] The minimum light level needed. 16 is full brightness while 0 is complete darkness",
            tooltipZh = "光照下限，闭区间。0 为全黑、15/16 为最亮。不填按 0 计算。")
    private static final String MIN = "min-light";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Max Light",
            labelZh = "最高亮度",
            tooltip = "[max-light] The maximum light level needed. 16 is full brightness while 0 is complete darkness",
            tooltipZh = "光照上限，闭区间。不填按 0 计算（不是最大亮度），此时只有光照为 0 的位置才通过，必须显式填写。")
    private static final String MAX = "max-light";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final double min = parseValues(caster, MIN, level, 0);
        final double max = parseValues(caster, MAX, level, 0);
        final double light = target.getLocation().getBlock().getLightLevel();
        return light >= min && light <= max;
    }

    @Override
    public String getKey() {
        return "light";
    }
}
