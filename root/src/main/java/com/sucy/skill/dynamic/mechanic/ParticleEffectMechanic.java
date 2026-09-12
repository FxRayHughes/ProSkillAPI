/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ParticleEffectMechanic
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Steven Sucy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.particle.EffectPlayer;
import com.sucy.skill.api.particle.target.EntityTarget;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "particle effect",
        name = "Particle Effect",
        nameZh = "粒子效果",
        description = "Plays a particle effect that follows the current target, using formulas to determine shape, size, and motion",
        descriptionZh = "为每个目标启动一个持续跟随该目标移动的粒子特效（EffectPlayer + EntityTarget），特效的形状、大小、运动由公式配置决定，与 particle 节点的一次性撒点不同。同一个 effect-key 同时只能有一个特效实例存活，重复施放会顶掉旧的。目标列表为空时返回 false。")
public class ParticleEffectMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Duration",
            labelZh = "持续时间",
            tooltip = "[duration] The time to play the effect for in seconds",
            tooltipZh = "特效持续秒数，随技能等级缩放；内部乘 20 转成 tick，默认 5 秒。")
    private static final String DURATION = "duration";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Effect Key",
            labelZh = "特效引用键",
            tooltip = "[effect-key] The key to refer to the effect by. Only one effect of each key can be active at a time.",
            tooltipZh = "特效的引用键，默认取技能名。相同键的特效同时只能存在一个，可用于避免叠加或用于后续手动停止。",
            defaultValue = "default")
    private static final String KEY      = "effect-key";

    @Override
    public String getKey() {
        return "particle effect";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets)
    {
        String key = settings.getString(KEY, skill.getName());
        int duration = (int) (20 * parseValues(caster, DURATION, level, 5));

        EffectPlayer player = new EffectPlayer(settings);
        for (LivingEntity target : targets)
            player.start(new EntityTarget(target), key, duration, level, true);

        return targets.size() > 0;
    }
}
