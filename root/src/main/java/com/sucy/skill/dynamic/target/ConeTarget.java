/**
 * SkillAPI
 * com.sucy.skill.dynamic.target.ConeTarget
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
package com.sucy.skill.dynamic.target;

import com.sucy.skill.api.target.TargetHelper;
import com.sucy.skill.cast.IIndicator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies child components to the closest all nearby entities around
 * each of the current targets.
 */
@SkillNode(
        key = "cone",
        name = "Cone",
        nameZh = "扇形选取目标",
        description = "Targets all units in a line in front of the current target (the casting player is the default target). If you include the caster, that counts towards the max amount.",
        descriptionZh = "以每个当前目标的视线为轴心，选中其前方扇形范围内的生物。扇形只在水平面上判定（忽略俯仰角），命中者再经过阵营、穿墙和数量限制过滤。",
        container = true)
public class ConeTarget extends TargetComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Angle",
            labelZh = "角度",
            tooltip = "[angle] The angle of the cone arc in degrees",
            tooltipZh = "扇形的半张角，单位度：生物与目标水平朝向的夹角不超过该值才算命中，实际张开宽度是左右各 angle 度。填 0 或负数则一个都选不到，填 360 及以上等于选中范围内全部生物。数值随技能等级/属性变化。")
    private static final String ANGLE  = "angle";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Range",
            labelZh = "范围",
            tooltip = "[range] The max distance away any target can be in blocks",
            tooltipZh = "扇形的最大距离，单位方块；先以该值为边长在目标周围取立方体范围内的生物，再做角度判定。数值随技能等级/属性变化。")
    private static final String RANGE  = "range";

    /** {@inheritDoc} */
    @Override
    void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) {
        double range = parseValues(caster, RANGE, level, 3.0);
        double angle = parseValues(caster, ANGLE, level, 90.0);
        makeConeIndicator(list, target, range, angle);
    }

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(LivingEntity caster, int level, List<LivingEntity> targets) {
        double range = parseValues(caster, RANGE, level, 3.0);
        double angle = parseValues(caster, ANGLE, level, 90.0);
        return determineTargets(caster, level, targets, t -> TargetHelper.getConeTargets(t, angle, range));
    }

    @Override
    public String getKey() {
        return "cone";
    }
}
