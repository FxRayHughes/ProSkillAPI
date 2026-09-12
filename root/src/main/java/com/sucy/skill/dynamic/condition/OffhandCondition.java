/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.OffhandCondition
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

import com.sucy.skill.dynamic.ItemChecker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * Item condition for a player's off hand
 */
@SkillNode(
        key = "offhand",
        name = "Offhand",
        nameZh = "检查副手",
        description = "Applies child components when the target is wielding an item matching the given material as an offhand item. This is for v1.9+ servers only.",
        descriptionZh = "检查目标副手物品是否满足物品条件。默认比对材质与耐久数据，可另开名称/Lore 检查；副手为空时不通过。仅适用于 1.9 及以上版本。",
        container = true)
public class OffhandCondition extends ConditionComponent {
    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final EntityEquipment equipment = target.getEquipment();
        return equipment != null && ItemChecker.check(target.getEquipment().getItemInOffHand(), level, settings);
    }

    @Override
    public String getKey() {
        return "offhand";
    }
}
