/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ParticleMechanic
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

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.util.ParticleHelper;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Plays a particle effect
 */
@SkillNode(
        key = "particle",
        name = "Particle",
        nameZh = "粒子",
        description = "Plays a particle effect about the target.",
        descriptionZh = "在每个目标当前位置播放一次粒子。播放点先沿目标水平朝向做前/后、上/下、左/右三轴偏移（朝向已被压平到水平面，抬头低头不影响偏移方向）。若配置了 arrangement，则按 circle/sphere/hemisphere 在 radius 范围内随机撒点（采用拒绝采样，是实心填充而非描边），否则只在该点播放一个点。particles（数量）与 radius（半径）两个键会随技能等级缩放。目标列表为空时返回 false。")
public class ParticleMechanic extends MechanicComponent
{
    private static final Vector UP = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Forward Offset",
            labelZh = "前方偏移",
            tooltip = "[forward] How far forward in front of the target in blocks to play the particles. A negative value will go behind.",
            tooltipZh = "沿目标水平朝向向前偏移的格数，负值向后。不随等级缩放（读取固定数值）。",
            defaultValue = "0")
    private static final String FORWARD = "forward";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Upward Offset",
            labelZh = "上方偏移",
            tooltip = "[upward] How far above the target in blocks to play the particles. A negative value will go below.",
            tooltipZh = "沿 Y 轴向上偏移的格数，负值向下。不随等级缩放。",
            defaultValue = "0")
    private static final String UPWARD  = "upward";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Right Offset",
            labelZh = "右方偏移",
            tooltip = "[right] How far to the right of the target to play the particles. A negative value will go to the left.",
            tooltipZh = "沿目标右手方向偏移的格数，负值向左。不随等级缩放。",
            defaultValue = "0")
    private static final String RIGHT   = "right";

    @Override
    public String getKey() {
        return "particle";
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
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets)
    {
        if (targets.size() == 0)
        {
            return false;
        }

        double forward = settings.getDouble(FORWARD, 0);
        double upward = settings.getDouble(UPWARD, 0);
        double right = settings.getDouble(RIGHT, 0);

        final Settings copy = new Settings(settings);
        copy.set(ParticleHelper.PARTICLES_KEY, parseValues(caster, ParticleHelper.PARTICLES_KEY, level, 1), 0);
        copy.set(ParticleHelper.RADIUS_KEY, parseValues(caster, ParticleHelper.RADIUS_KEY, level, 0), 0);
        copy.set("level", level);

        for (LivingEntity target : targets)
        {
            Location loc = target.getLocation();
            Vector dir = loc.getDirection().setY(0).normalize();
            Vector side = dir.clone().crossProduct(UP);
            loc.add(dir.multiply(forward)).add(0, upward, 0).add(side.multiply(right));

            ParticleHelper.play(loc, copy);
        }

        return targets.size() > 0;
    }
}
