/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.FlagToggleMechanic
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

import com.sucy.skill.dynamic.FlagKeys;
import com.sucy.skill.api.util.FlagManager;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a flag to each target
 */
@SkillNode(
        key = "flag toggle",
        name = "Flag Toggle",
        nameZh = "标记切换",
        description = "Toggles a flag on or off for the target. This can be used to make toggle effects.",
        descriptionZh = "对每个目标做标记的开关切换：目标已有该标记则移除，没有则添加一个永久标记（时长传 -1，不会自动过期）。适合做开关型的持续状态，配合「标记条件」判断当前是开还是关。未填写引用键或目标列表为空时不执行。")
public class FlagToggleMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique string for the flag. Use the same key when checking it in a Flag Condition",
            tooltipZh = "标记的唯一字符串，检查标记的节点需使用相同的键。{uuid} 替换为施法者 UUID，{player} 替换为施法者名称（非玩家实体退回实体类型名）。此项为必填，缺失则整个节点不执行。",
            defaultValue = "key")
    private static final String KEY = "key";

    @Override
    public String getKey() {
        return "flag toggle";
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

        String key = FlagKeys.resolve(settings.getString(KEY), caster);
        for (LivingEntity target : targets)
        {
            if (FlagManager.hasFlag(target, key))
            {
                FlagManager.removeFlag(target, key);
            }
            else
            {
                FlagManager.addFlag(target, key, -1);
            }
        }
        return targets.size() > 0;
    }
}
