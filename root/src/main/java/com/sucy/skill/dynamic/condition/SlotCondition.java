/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.SlotCondition
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "slot",
        name = "Slot",
        nameZh = "检查槽位",
        description = "Applies child components when the target player has a matching item in the given slot.",
        descriptionZh = "要求目标是玩家，检查指定背包槽位里的物品是否满足物品条件，列出的槽位任一命中即通过。槽位 0-8 为快捷栏、9-35 为主背包、36-39 为盔甲、40 为副手。匹配默认比对材质与耐久数据，可另开名称/Lore 检查。",
        container = true)
public class SlotCondition extends ConditionComponent
{
    @SkillField(
            kind = FieldKind.StringListValue,
            label = "Slots (one per line)",
            labelZh = "槽位列表",
            tooltip = "[slot] The slots to look at. Slots 0-8 are the hot bar, 9-35 are the main inventory, 36-39 are armor, and 40 is the offhand slot. Multiple slots will check if any of the slots match.",
            tooltipZh = "要检查的槽位编号列表，每行一个，任一命中即通过。必须是纯数字，写成非数字会在判定时报错。0-8 快捷栏、9-35 主背包、36-39 盔甲、40 副手。",
            defaultValue = "9")
    private static final String SLOT = "slot";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        if (!(target instanceof Player)) return false;

        final PlayerInventory inventory = ((Player) target).getInventory();
        for (String slot : settings.getStringList(SLOT)) {
            try {
                int index = Integer.parseInt(slot);
                if (index >= 0 && index < inventory.getSize()
                        && ItemChecker.check(inventory.getItem(index), level, settings)) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed user configuration entries and continue
                // checking the remaining valid slots.
            }
        }
        return false;
    }

    @Override
    public String getKey() {
        return "slot";
    }
}
