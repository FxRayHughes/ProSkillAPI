/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.HeldItemMechanic
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

import com.sucy.skill.SkillAPI;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "held item",
        name = "Held Item",
        nameZh = "手持物品",
        description = "Sets the held item slot of the target player. This will do nothing if trying to set it to a skill slot.",
        descriptionZh = "切换玩家目标手持的快捷栏槽位，非玩家目标会被跳过。仅在服务器启用了技能栏、且该槽位属于该玩家的武器槽（非技能槽）时才真正切换，否则静默不动作。注意：只要目标里有玩家就会返回成功，即使因技能栏未启用或槽位是技能槽而实际什么都没做。")
public class HeldItemMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Slot",
            labelZh = "槽位",
            tooltip = "[slot] The slot to set it to",
            tooltipZh = "目标快捷栏槽位编号，取整后使用（快捷栏为 0-8）。指向技能槽时不生效。数值随技能等级/属性变化，默认 0。")
    private static final String SLOT = "slot";

    @Override
    public String getKey() {
        return "held item";
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
        int slot = (int) parseValues(caster, SLOT, level, 0);

        boolean worked = false;
        for (LivingEntity target : targets)
        {
            if (!(target instanceof Player))
                continue;

            worked = true;
            Player player = (Player) target;
            if (SkillAPI.getSettings().isSkillBarEnabled() && SkillAPI.getPlayerData(player).getSkillBar().isWeaponSlot(slot))
                player.getInventory().setHeldItemSlot(slot);
        }

        return worked;
    }
}
