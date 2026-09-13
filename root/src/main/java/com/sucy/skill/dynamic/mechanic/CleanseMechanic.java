/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.CleanseMechanic
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Set;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Cleanses a target of negative potion or status effects
 */
@SkillNode(
        key = "cleanse",
        name = "Cleanse",
        nameZh = "净化",
        description = "Cleanses negative potion or status effects from the targets.",
        descriptionZh = "清除目标身上的负面状态标记和负面药水效果，两者各自独立配置。状态选 All 时清掉全部内置负面状态标记，否则只清指定的那一个。药水选 All 时只清白名单里的九种负面效果（BLINDNESS、CONFUSION、HUNGER、LEVITATION、POISON、SLOW、SLOW_DIGGING、WEAKNESS、WITHER），Unluck 不在白名单内、必须单独指定才能清。只有确实移除掉了东西才返回 true。")
public class CleanseMechanic extends MechanicComponent {
    private static final Set<String> POTIONS = ImmutableSet.of(
            "BLINDNESS", "CONFUSION", "HUNGER", "LEVITATION", "POISON",
            "SLOW", "SLOW_DIGGING", "WEAKNESS", "WITHER"
    );

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Status",
            labelZh = "状态",
            tooltip = "[status] The status to remove from the target",
            tooltipZh = "要清除的状态标记。All 清全部负面标记；填具体名称则只清那一个；None（默认字符串）等于不清状态。",
            options = {"None", "All", "Curse", "Disarm", "Root", "Silence", "Stun"},
            optionsZh = {"无", "全部", "状态效果3", "状态效果4", "状态效果5", "状态效果6", "状态效果7"},
            defaultValue = "All")
    private static final String STATUS = "status";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Potion",
            labelZh = "药水效果",
            tooltip = "[potion] The type of potion effect to remove from the target",
            tooltipZh = "要清除的药水效果。All 只清九种内置负面效果（不含 Unluck）；填具体名称则只清那一个，名称非法时该部分静默跳过。",
            options = {"All", "None", "Blindness", "Confusion", "Hunger", "Levitation", "Poison", "Slow", "Slow Digging", "Unluck", "Weakness", "Wither"},
            optionsZh = {"全部", "无", "状态效果3", "状态效果4", "状态效果5", "状态效果6", "状态效果7", "状态效果8", "状态效果9", "状态效果10", "状态效果11", "状态效果12"},
            defaultValue = "All")
    private static final String POTION = "potion";

    @Override
    public String getKey() {
        return "cleanse";
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
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        boolean worked = false;
        String status = settings.getString(STATUS, "None").toLowerCase();
        String potion = settings.getString(POTION, "all").toUpperCase().replace(' ', '_');
        PotionEffectType type = null;
        try {
            type = PotionEffectType.getByName(potion);
        } catch (Exception ex) {
            // Invalid potion type
        }

        for (LivingEntity target : targets) {
            if (status.equals("all")) {
                for (String flag : StatusFlag.NEGATIVE) {
                    if (FlagManager.hasFlag(target, flag)) {
                        FlagManager.removeFlag(target, flag);
                        worked = true;
                    }
                }
            } else if (FlagManager.hasFlag(target, status)) {
                FlagManager.removeFlag(target, status);
                worked = true;
            }

            if (potion.equals("ALL")) {
                for (PotionEffect p : target.getActivePotionEffects()) {
                    if (POTIONS.contains(p.getType().getName())) {
                        target.removePotionEffect(p.getType());
                        worked = true;
                    }
                }
            } else if (type != null && target.hasPotionEffect(type)) {
                target.removePotionEffect(type);
                worked = true;
            }
        }
        return worked;
    }
}
