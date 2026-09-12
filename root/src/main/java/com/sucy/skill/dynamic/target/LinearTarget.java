/**
 * SkillAPI
 * com.sucy.skill.dynamic.target.LinearTarget
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
 * Applies child components to the entities in a line in front of each of the
 * provided targets.
 */
@SkillNode(
        key = "linear",
        name = "Linear",
        nameZh = "直线选取目标",
        description = "Targets all units in a line in front of the current target (the casting player is the default target).",
        descriptionZh = "从每个当前目标的眼睛位置沿其视线方向发射一条射线，选中被射线穿过的所有生物，按距离由近到远排列。适合做穿透性的直线攻击。",
        container = true)
public class LinearTarget extends TargetComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Range",
            labelZh = "范围",
            tooltip = "[range] The max distance away any target can be in blocks",
            tooltipZh = "射线最大长度，单位方块；超出该距离的生物不参与判定。数值随技能等级/属性变化。")
    private static final String RANGE     = "range";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Tolerance",
            labelZh = "容差",
            tooltip = "[tolerance] How much to expand the potential entity's hitbox in all directions, in blocks. This makes it easier to aim",
            tooltipZh = "把候选生物的碰撞箱向各个方向额外扩大的方块数，值越大越容易瞄中（默认 4，相当于很宽松的瞄准）。数值随技能等级/属性变化。")
    private static final String TOLERANCE = "tolerance";

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        final double tolerance = parseValues(caster, TOLERANCE, level, 4.0);
        final double range = parseValues(caster, RANGE, level, 5.0);
        return determineTargets(caster, level, targets, t -> TargetHelper.getLivingTargets(t, range, tolerance));
    }

    /** {@inheritDoc} */
    @Override
    void makeIndicators(
            final List<IIndicator> list, final Player caster, final LivingEntity target, final int level) {
        // TODO - add indicators for linear targeting
    }

    @Override
    public String getKey() {
        return "linear";
    }
}
