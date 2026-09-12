/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ItemCondition
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

import com.sucy.skill.dynamic.ItemChecker;
import org.bukkit.entity.LivingEntity;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * A condition for dynamic skills that requires the target to have a specified held item
 */
@SkillNode(
        key = "item",
        name = "Item",
        nameZh = "检查物品",
        description = "Applies child components when the target is wielding an item matching the given material.",
        descriptionZh = "检查目标主手物品是否满足物品条件。默认比对材质与耐久数据，可另开名称/Lore 检查；没有装备或主手为空时不通过。",
        container = true)
public class ItemCondition extends ConditionComponent {
    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        return target.getEquipment() != null && ItemChecker.check(
                target.getEquipment().getItemInHand(),
                level,
                settings);
    }

    @Override
    public String getKey() {
        return "item";
    }
}
