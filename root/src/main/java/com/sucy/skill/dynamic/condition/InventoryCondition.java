/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.InventoryCondition
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
import org.bukkit.entity.Player;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * A condition for dynamic skills that requires the target to have a specified item
 */
@SkillNode(
        key = "inventory",
        name = "Inventory",
        nameZh = "检查背包",
        description = "Applies child components when the target player contains the given item in their inventory. This does not work on mobs.",
        descriptionZh = "要求目标是玩家，并检查其背包中是否有足够数量的匹配物品，只检查不扣除。匹配默认比对材质与 data 值，可另开名称/Lore 检查（支持正则），数量按累加计算够数才通过。非玩家目标一律不通过。",
        container = true)
public class InventoryCondition extends ConditionComponent {
    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        return target instanceof Player && ItemChecker.check((Player) target, level, this, false);
    }

    @Override
    public String getKey() {
        return "inventory";
    }
}
