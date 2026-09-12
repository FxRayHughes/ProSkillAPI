/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.LaunchMechanic
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
package com.sucy.skill.dynamic.mechanic;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Launches the target in a given direction relative to their forward direction
 */
@SkillNode(
        key = "launch",
        name = "Launch",
        nameZh = "发射",
        description = "Launches the target relative to their forward direction. Use negative values to go in the opposite direction (e.g. negative forward makes the target go backwards)",
        descriptionZh = "直接设置每个目标的速度矢量，把目标朝指定方向抛出。前后与左右分量在水平面上计算（参考方向的竖直分量被清零），竖直分量由「上方速度」独立指定。这是覆盖式赋值而非叠加，会清掉目标原有的运动惯性。参考方向可选目标自身朝向、施法者朝向，或从施法者指向目标的方向（后者适合做击退）。目标列表为空时不执行。")
public class LaunchMechanic extends MechanicComponent {
    private Vector up = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Forward Speed",
            labelZh = "前方速度",
            tooltip = "[forward] The speed to give the target in the direction they are facing",
            tooltipZh = "沿参考方向前方的速度，负数为向后。数值随技能等级/属性变化，默认 0。")
    private static final String FORWARD = "forward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Upward Speed",
            labelZh = "上方速度",
            tooltip = "[upward] The speed to give the target upwards",
            tooltipZh = "竖直方向速度，直接作为速度的 Y 分量写入（不是叠加），负数为向下。数值随技能等级/属性变化，默认 0。")
    private static final String UPWARD  = "upward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Right Speed",
            labelZh = "右方速度",
            tooltip = "[right] The speed to give the target to their right",
            tooltipZh = "沿参考方向右侧的速度，负数为向左。数值随技能等级/属性变化，默认 0。")
    private static final String RIGHT   = "right";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "[PREM] Relative",
            labelZh = "相对方向",
            tooltip = "[relative] Determines what is considered \"forward\". Target uses the direction the target is facing, Caster uses the direction the caster is facing, and Between uses the direction from the caster to the target.",
            tooltipZh = "决定何为「前方」：「目标」用目标自身朝向，「施法者」用施法者朝向，「之间」用从施法者指向目标的水平方向（常用于击退）。默认目标。",
            options = {"Target", "Caster", "Between"},
            optionsZh = {"可选值1", "可选值2", "可选值3"},
            defaultValue = "Target")
    private static final String RELATIVE = "relative";

    @Override
    public String getKey() {
        return "launch";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0) {
            return false;
        }

        double forward = parseValues(caster, FORWARD, level, 0);
        double upward = parseValues(caster, UPWARD, level, 0);
        double right = parseValues(caster, RIGHT, level, 0);
        String relative = settings.getString(RELATIVE, "target").toLowerCase();
        for (LivingEntity target : targets) {
            final Vector dir;
            if (relative.equals("caster")) {
                dir = caster.getLocation().getDirection().setY(0).normalize();
            } else if (relative.equals("between")) {
                dir = target.getLocation().toVector().subtract(caster.getLocation().toVector()).setY(0).normalize();
            } else {
                dir = target.getLocation().getDirection().setY(0).normalize();
            }

            final Vector nor = dir.clone().crossProduct(up);
            dir.multiply(forward);
            dir.add(nor.multiply(right)).setY(upward);

            target.setVelocity(dir);
        }
        return targets.size() > 0;
    }
}
