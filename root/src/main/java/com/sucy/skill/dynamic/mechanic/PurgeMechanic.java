/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.PurgeMechanic
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

import com.google.common.collect.ImmutableSet;
import com.sucy.skill.api.util.FlagManager;
import com.sucy.skill.api.util.StatusFlag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Set;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Purges a target of positive potion or status effects
 */
@SkillNode(
        key = "purge",
        name = "Purge",
        nameZh = "驱散",
        description = "Purges the target of positive potion effects or statuses",
        descriptionZh = "移除目标身上的正面状态与正面药水效果。status 填 all 时清空所有正面状态标记，否则只移除同名的那一个标记；potion 填 ALL 时只清除内置白名单里的 13 种正面药水（吸收、抗性、急迫、防火、生命提升、力量、隐身、跳跃、夜视、再生、饱和、迅捷、水下呼吸），否则只移除指定的那一种。只有真正移除掉了至少一项才返回 true，目标身上本来就没有可清的东西时返回 false。")
public class PurgeMechanic extends MechanicComponent
{
    private static final Set<String> POTIONS = ImmutableSet.of(
            "ABSORPTION", "DAMAGE_RESISTANCE", "FAST_DIGGING", "FIRE_RESISTANCE", "HEALTH_BOOST",
            "INCREASE_DAMAGE", "INVISIBILITY", "JUMP", "NIGHT_VISION", "REGENERATION",
            "SATURATION", "SPEED", "WATER_BREATHING"
    );

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Status",
            labelZh = "状态",
            tooltip = "[status] The status to remove from the target, if any",
            tooltipZh = "要移除的状态。All 清空全部正面状态标记；None 或其他值则按名字精确移除单个标记。注意代码读取时的兜底默认值是 None，与标注的 All 不同。",
            options = {"None", "All", "Absorb", "Invincible"},
            optionsZh = {"无", "全部", "状态效果3", "状态效果4"},
            defaultValue = "All")
    private static final String STATUS = "status";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Potion",
            labelZh = "药水效果",
            tooltip = "[potion] The potion effect to remove from the target, if any",
            tooltipZh = "要移除的药水效果。All 清除内置正面药水白名单里的所有效果；None 表示不清药水；也可指定单一效果名（大写下划线形式匹配）。",
            options = {"All", "None", "Absorption", "Damage Resistance", "Fast Digging", "Fire Resistance", "Glowing", "Health Boost", "Increase Damage", "Invisibility", "Jump", "Luck", "Night Vision", "Regeneration", "Saturation", "Speed", "Water Breathing"},
            optionsZh = {"全部", "无", "状态效果3", "状态效果4", "状态效果5", "火焰", "状态效果7", "状态效果8", "状态效果9", "状态效果10", "状态效果11", "状态效果12", "状态效果13", "状态效果14", "饱和度", "速度", "水"},
            defaultValue = "All")
    private static final String POTION = "potion";

    @Override
    public String getKey() {
        return "purge";
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
        boolean worked = false;
        String status = settings.getString(STATUS, "None").toLowerCase();
        String potion = settings.getString(POTION, "all").toUpperCase().replace(' ', '_');
        PotionEffectType type = null;
        try
        {
            type = PotionEffectType.getByName(potion);
        }
        catch (Exception ex)
        {
            // Invalid potion type
        }

        for (LivingEntity target : targets)
        {
            if (status.equals("all"))
            {
                for (String flag : StatusFlag.POSITIVE)
                {
                    if (FlagManager.hasFlag(target, flag))
                    {
                        FlagManager.removeFlag(target, flag);
                        worked = true;
                    }
                }
            }
            else if (FlagManager.hasFlag(target, status))
            {
                FlagManager.removeFlag(target, status);
                worked = true;
            }

            if (potion.equals("ALL"))
            {
                for (PotionEffectType p : PotionEffectType.values())
                {
                    if (target.hasPotionEffect(p) && POTIONS.contains(p.getName()))
                    {
                        target.removePotionEffect(p);
                        worked = true;
                    }
                }
            }
            else if (type != null && target.hasPotionEffect(type))
            {
                target.removePotionEffect(type);
                worked = true;
            }
        }
        return worked;
    }
}
