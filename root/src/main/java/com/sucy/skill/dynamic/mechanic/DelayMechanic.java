/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.DelayMechanic
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

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Executes child components after a delay
 */
@SkillNode(
        key = "delay",
        name = "Delay",
        nameZh = "延迟",
        description = "Applies child components after a delay.",
        descriptionZh = "等待一段时间后再执行子节点，把当前的目标列表原样传下去。与「引导」不同，本节点没有任何打断机制，也不限制施法者行动，定时任务一旦排下就一定会执行。目标列表为空返回 false，否则排完任务立刻返回 true——返回 true 不代表子节点已经跑过。",
        container = true)
public class DelayMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Delay",
            labelZh = "延迟",
            tooltip = "[delay] The amount of time to wait before applying child components in seconds",
            tooltipZh = "等待时长，单位秒，默认 2.0，随技能等级缩放。内部乘 20 换算成 tick。")
    private static final String SECONDS = "delay";

    @Override
    public String getKey() {
        return "delay";
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
    public boolean execute(final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        if (targets.size() == 0) {
            return false;
        }
        double seconds = parseValues(caster, SECONDS, level, 2.0);
        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("SkillAPI"),
                () -> executeChildren(caster, level, targets),
                (long) (seconds * 20)
        );
        return true;
    }
}
