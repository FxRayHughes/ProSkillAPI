/**
 * SkillAPI
 * com.sucy.skill.dynamic.target.OffsetTarget
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
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

import com.google.common.collect.ImmutableList;
import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.dynamic.TempEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies child effects to a location offset from the current targets
 */
@SkillNode(
        key = "offset",
        name = "Offset",
        nameZh = "偏移选取目标",
        description = "Targets a location that is the given offset away from each target.",
        descriptionZh = "不做任何搜索，直接把每个当前目标的坐标按其自身朝向平移，得到一个新的位置目标（临时位置实体）。朝向只取水平分量，所以俯仰角不影响前/右方向，常用于在目标身前、身侧或头顶生成效果。",
        container = true)
public class OffsetTarget extends TargetComponent {
    private static final Vector UP = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Forward",
            labelZh = "前方",
            tooltip = "[forward] The offset from the target in the direction they are facing. Negative numbers go backwards.",
            tooltipZh = "沿目标水平朝向的前后位移，单位方块；负数表示往身后偏移。数值随技能等级/属性变化。")
    private static final String FORWARD    = "forward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Upward",
            labelZh = "上方",
            tooltip = "[upward] The offset from the target upwards. Negative numbers go below them.",
            tooltipZh = "垂直位移，单位方块，以目标脚底坐标为基准；负数表示落到脚下。注意该值是直接设定偏移向量的 Y 分量，不与前/右方向叠加倾斜。数值随技能等级/属性变化。")
    private static final String UPWARD     = "upward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Right",
            labelZh = "右方",
            tooltip = "[right] The offset from the target to their right. Negative numbers go to the left.",
            tooltipZh = "沿目标右手方向的左右位移，单位方块；负数表示往左偏移。数值随技能等级/属性变化。")
    private static final String RIGHT      = "right";
    private static final String HORIZONTAL = "horizontal";

    /** {@inheritDoc} */
    @Override
    public void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) {
        makeCircleIndicator(list, getTargetLoc(caster, level, target), 0.5);
    }

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        return determineTargets(caster, level, targets, t -> ImmutableList.of(getTargetLoc(caster, level, t)));
    }

    private TempEntity getTargetLoc(LivingEntity caster, int level, LivingEntity t) {
        final boolean horizontal = settings.getBool(HORIZONTAL, false);
        final double forward = parseValues(caster, FORWARD, level, 0);
        final double upward = parseValues(caster, UPWARD, level, 0);
        final double right = parseValues(caster, RIGHT, level, 0);

        final Vector dir = t.getLocation().getDirection().setY(0).normalize();
        if (horizontal) { dir.setY(0).normalize(); }

        final Vector nor = dir.clone().crossProduct(UP);
        dir.multiply(forward);
        dir.add(nor.multiply(right)).setY(upward);

        return TempEntity.create(t.getLocation().add(dir));
    }

    @Override
    public String getKey() {
        return "offset";
    }
}
