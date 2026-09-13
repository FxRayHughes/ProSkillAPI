/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.FlagClearMechanic
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
        key = "flag clear",
        name = "Flag Clear",
        nameZh = "标记清除",
        description = "Clears a flag from the target.",
        descriptionZh = "移除每个目标身上指定键的标记，无论该标记原本是限时的还是永久的。键里的 {uuid} 会替换为施法者 UUID，必须与当初设置标记时写法一致才能清掉。未填写引用键或目标列表为空时不执行；目标本来没有该标记也不会报错。")
public class FlagClearMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique string for the flag. This should match that of the mechanic that set the flag to begin with.",
            tooltipZh = "要清除的标记键，必须与设置该标记时使用的键完全一致（包括占位符写法）。{uuid} 替换为施法者 UUID，{player} 替换为施法者名称（非玩家实体退回实体类型名）。此项为必填，缺失则整个节点不执行。",
            defaultValue = "key")
    private static final String KEY = "key";

    @Override
    public String getKey() {
        return "flag clear";
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

        String key = FlagKeys.resolve(settings.getString(KEY, ""), caster);
        for (LivingEntity target : targets)
        {
            FlagManager.removeFlag(target, key);
        }
        return targets.size() > 0;
    }
}
