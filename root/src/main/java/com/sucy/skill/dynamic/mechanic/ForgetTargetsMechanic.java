/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ForgetTargets
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
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "forget targets",
        name = "Forget Targets",
        nameZh = "清除记忆目标",
        description = "Clears targets stored by the \"Remember Targets\" mechanic",
        descriptionZh = "从施法者的施法数据（cast data）中删除指定键保存的目标列表，与「记忆目标」成对使用：后者用同一个键存目标，此节点负责释放。不清理会让记忆的目标一直留在本次施法数据里被后续节点取到。该节点不关心当前目标，无论有无目标都返回成功。",
        requiresNodes = {"remember targets"})
public class ForgetTargetsMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key the targets were stored under",
            tooltipZh = "当初「记忆目标」存入时使用的键，必须完全一致才能删除；缺失时按空字符串处理，等于尝试删除键名为空的记录。",
            defaultValue = "key")
    private static final String KEY = "key";

    @Override
    public String getKey() {
        return "forget targets";
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
        String key = settings.getString(KEY, "");
        DynamicSkill.getCastData(caster).remove(key);
        return true;
    }
}
