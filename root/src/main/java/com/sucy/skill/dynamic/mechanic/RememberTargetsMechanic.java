/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.RememberTargetsMechanic
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

import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a flag to each target
 */
@SkillNode(
        key = "remember targets",
        name = "Remember Targets",
        nameZh = "记忆目标",
        description = "Stores the current targets for later use under a specified key",
        descriptionZh = "把当前这批目标以指定键存入施法者的施法数据中，供后续节点通过「记忆」目标选择器或「推动」节点的 source 取回复用。仅做记录，不对目标施加任何效果。目标列表为空或未配置 key 时返回 false。同一个键会被后写入的覆盖。")
public class RememberTargetsMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the targets under. The \"Remember\" target will use this key to apply effects to the targets later on.",
            tooltipZh = "存储目标列表用的唯一键，默认 target。后续用「记忆」目标选择器或 push 的 source 填同一个键即可取回。",
            defaultValue = "target")
    private static final String KEY = "key";

    @Override
    public String getKey() {
        return "remember targets";
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
        if (targets.size() == 0 || !settings.has(KEY))
        {
            return false;
        }

        String key = settings.getString(KEY);
        DynamicSkill.getCastData(caster).put(key, targets);
        return true;
    }
}
