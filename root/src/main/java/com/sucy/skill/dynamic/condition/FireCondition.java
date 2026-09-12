/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.FireCondition
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

import org.bukkit.entity.LivingEntity;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target to be on fire
 */
@SkillNode(
        key = "fire",
        name = "Fire",
        nameZh = "检查火焰",
        description = "Applies child components when the target is on fire.",
        descriptionZh = "按目标剩余燃烧时间判断是否着火（燃烧 tick 大于 0 即为着火），再与配置要求比对。",
        container = true)
public class FireCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] Whether or not the target should be on fire",
            tooltipZh = "只有值恰好等于“not on fire”（忽略大小写）才要求目标不在燃烧，其余任何值都要求目标正在燃烧。",
            options = {"On Fire", "Not On Fire"},
            optionsZh = {"火焰", "火焰"},
            defaultValue = "On Fire")
    private static final String TYPE = "type";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final boolean onFire = !settings.getString(TYPE, "on fire").toLowerCase().equals("not on fire");
        return (target.getFireTicks() > 0) == onFire;
    }

    @Override
    public String getKey() {
        return "fire";
    }
}
