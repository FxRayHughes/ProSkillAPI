/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.FlagCondition
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
package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.FlagKeys;
import com.sucy.skill.api.util.FlagManager;
import org.bukkit.entity.LivingEntity;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target to have a specified flag active
 */
@SkillNode(
        key = "flag",
        name = "Flag",
        nameZh = "检查标记",
        description = "Applies child components when the target is marked by the appropriate flag.",
        descriptionZh = "检查目标身上是否带有指定标记（由标记类机制写入），是否带有需与配置一致。",
        container = true)
public class FlagCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] Whether or not the flag should be set",
            tooltipZh = "只有值恰好等于“not set”（忽略大小写）才要求标记不存在，其余任何值都要求标记存在。",
            options = {"Set", "Not Set"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "Set")
    private static final String TYPE = "type";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key representing the flag. This should match the key for when you set it using the Flag mechanic or the Flat Toggle mechanic",
            tooltipZh = "标记键名，需与写入标记的机制用的键一致。其中 {uuid} 会被替换为施法者的 UUID，{player} 会被替换为施法者名称（非玩家实体退回实体类型名）；用它们可让每个施法者拥有独立的标记。UUID 不随改名变化，名称可读但玩家改名后旧标记会失联。",
            defaultValue = "key")
    private static final String KEY  = "key";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final String flag = FlagKeys.resolve(settings.getString(KEY), caster);
        final boolean set = !settings.getString(TYPE, "set").toLowerCase().equals("not set");
        return FlagManager.hasFlag(target, flag) == set;
    }

    @Override
    public String getKey() {
        return "flag";
    }
}
