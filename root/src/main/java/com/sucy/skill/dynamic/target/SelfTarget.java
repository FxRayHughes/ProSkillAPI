/**
 * SkillAPI
 * com.sucy.skill.dynamic.target.SelfTarget
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

import com.google.common.collect.ImmutableList;
import com.sucy.skill.cast.IIndicator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * Applies child components to the caster
 */
@SkillNode(
        key = "self",
        name = "Self",
        nameZh = "自身选取目标",
        description = "Returns the current target back to the caster.",
        descriptionZh = "丢弃当前所有目标，把目标重新指回施法者本人，结果固定只有一个。常用于在一段针对敌人的逻辑中途把效果切回自己身上（例如吸血、自我增益）。不受阵营、穿墙、最多目标数等设置影响。",
        container = true)
public class SelfTarget extends TargetComponent {

    /** {@inheritDoc} */
    @Override
    public void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) {
        makeCircleIndicator(list, caster, 0.5);
    }

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        return ImmutableList.of(caster);
    }

    @Override
    public String getKey() {
        return "self";
    }
}
