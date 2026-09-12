/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ElseCondition
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
package com.sucy.skill.dynamic.condition;

import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * A simple "else" condition, applying if the previous component failed
 */
@SkillNode(
        key = "else",
        name = "Else",
        nameZh = "检查否则",
        description = "Applies child elements if the previous component failed to execute. This not only applies for conditions not passing, but mechanics failing due to no target or other cases.",
        descriptionZh = "只在同级的上一个节点执行失败时才运行子节点。上一个节点不限于条件不通过，机制因为没有目标或其他原因没生效也算失败。该节点不过滤目标，把收到的目标列表原样传给子节点。",
        container = true)
public class ElseCondition extends ConditionComponent {

    @Override
    public String getKey() {
        return "else";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        return test(caster, level, null) && executeChildren(caster, level, targets);
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        return !lastPassed();
    }
}
