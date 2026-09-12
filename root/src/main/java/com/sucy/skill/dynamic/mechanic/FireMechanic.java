/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.FireMechanic
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

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Executes child components after a delay
 */
@SkillNode(
        key = "fire",
        name = "Fire",
        nameZh = "火焰",
        description = "Sets the target on fire for a duration.",
        descriptionZh = "点燃每个目标，燃烧时间按秒配置并换算为 tick（秒×20）。只会延长燃烧：若目标当前剩余燃烧时间更长则保持原值，不会被缩短。秒数填 0 或负数时反而会立即熄灭目标（燃烧 tick 置 0），可当灭火用。目标列表为空时不执行。")
public class FireMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Seconds",
            labelZh = "秒数",
            tooltip = "[seconds] The duration of the fire in seconds",
            tooltipZh = "燃烧持续秒数，内部乘 20 转成 tick；只有大于目标当前剩余燃烧时间时才生效。填 0 或负数表示立即扑灭。数值随技能等级/属性变化，默认 3 秒。")
    private static final String SECONDS = "seconds";

    @Override
    public String getKey() {
        return "fire";
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
        double seconds = parseValues(caster, SECONDS, level, 3.0);
        int ticks = (int) (seconds * 20);
        for (LivingEntity target : targets) {
            int newTicks = ticks <= 0 ? 0 : Math.max(ticks, target.getFireTicks());
            target.setFireTicks(newTicks);
        }
        return targets.size() > 0;
    }
}
