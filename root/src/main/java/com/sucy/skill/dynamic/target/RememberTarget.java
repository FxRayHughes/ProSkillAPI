/**
 * SkillAPI
 * com.sucy.skill.dynamic.target.RememberTarget
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
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a flag to each target
 */
@SkillNode(
        key = "remember",
        name = "Remember",
        nameZh = "记忆选取目标",
        description = "Targets entities stored using the \"Remember Targets\" mechanic for the matching key. If it was never set, this will fail.",
        descriptionZh = "取出之前由「记忆目标」机制以相同键名存下的那批目标，完全忽略当前输入目标。键名从未被写入过时返回空列表，该分支不执行。本节点不经过阵营、穿墙、最多目标数等通用过滤，存进去的是什么就原样取出。",
        container = true,
        requiresNodes = {"remember targets"})
public class RememberTarget extends TargetComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key for the target group that should match that used by the \"Remember Targets\" skill",
            tooltipZh = "读取用的键名，必须和「记忆目标」机制里写入时用的键名完全一致，否则取不到任何目标。同一次施法的数据互相独立。",
            defaultValue = "target")
    private static final String KEY = "key";

    /** {@inheritDoc} */
    @Override
    public void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) {
        final List<LivingEntity> targets = getTargets(caster, level, null);
        if (!targets.isEmpty()) { makeCircleIndicator(list, targets.get(0), 0.5); }
    }

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        return remember(caster, settings.getString(KEY, ""));
    }

    public static List<LivingEntity> remember(final LivingEntity caster, final String key) {
        final Object data = DynamicSkill.getCastData(caster).get(key);
        //noinspection unchecked - proper skill setup should cause this to work
        return data == null ? ImmutableList.of() : (List<LivingEntity>) data;
    }

    @Override
    public String getKey() {
        return "remember";
    }
}
