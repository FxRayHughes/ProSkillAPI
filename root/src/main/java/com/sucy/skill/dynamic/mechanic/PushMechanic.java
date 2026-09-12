/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.PushMechanic
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

import com.sucy.skill.dynamic.target.RememberTarget;
import org.bukkit.Location;
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
        key = "push",
        name = "Push",
        nameZh = "推动",
        description = "Pushes the target relative to the caster. This will do nothing if used with the caster as the target. Positive numbers apply knockback while negative numbers pull them in.",
        descriptionZh = "以某个来源点为中心，把每个目标沿「来源指向目标」的方向推开（速度为负值则拉近）。来源默认是施法者位置，也可通过 source 指定之前用「记忆目标」存下的那批目标中的第一个。垂直分量会被单独处理为原 Y 分量的五分之一再加 0.5，所以总会带一点上抛。目标与来源重合（向量长度为 0）时跳过该目标；若所有目标都被跳过则返回 false。另有一个未在编辑器中暴露的 type 键控制衰减方式：默认 scaled 表示速度除以距离平方（离得越远推力衰减越快），fixed 表示除以距离（各距离下推力恒定），inverse 表示直接乘距离（越远推得越猛）。")
public class PushMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Speed",
            labelZh = "速度",
            tooltip = "[speed] How fast to push the target away. Use a negative value to pull them closer.",
            tooltipZh = "推动强度，随技能等级缩放，默认 3.0。正值击退，负值拉近；实际速度还要经 type 指定的距离衰减换算。")
    private static final String SPEED  = "speed";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Source",
            labelZh = "来源",
            tooltip = "[source] The source to push/pull from. This should be a key used in a Remember Targets mechanic. If no targets are remembered, this will default to the caster.",
            tooltipZh = "推力来源的引用键，需与「记忆目标」节点存入的键一致。留空或找不到记忆目标时退回以施法者位置为中心。",
            defaultValue = "none")
    private static final String SOURCE = "source";

    @Override
    public String getKey() {
        return "push";
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

        final double speed = parseValues(caster, SPEED, level, 3.0);
        final String type = settings.getString("type", "scaled").toLowerCase();

        final List<LivingEntity> sources = RememberTarget.remember(caster, settings.getString(SOURCE, "_none"));
        final Location center = sources.isEmpty() ? caster.getLocation() : sources.get(0).getLocation();

        boolean worked = false;
        for (LivingEntity target : targets) {
            final Vector vel = target.getLocation().subtract(center).toVector();
            if (vel.lengthSquared() == 0) {
                continue;
            } else if (type.equals("inverse")) { vel.multiply(speed); } else if (type.equals("fixed")) {
                vel.multiply(speed / vel.length());
            } else { // "scaled"
                vel.multiply(speed / vel.lengthSquared());
            }
            vel.setY(vel.getY() / 5 + 0.5);
            target.setVelocity(vel);
            worked = true;
        }
        return worked;
    }
}
