/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.PotionCondition
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Steven Sucy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.condition;

import com.rit.sucy.version.VersionManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target to have a specified potion effect
 */
@SkillNode(
        key = "potion",
        name = "Potion",
        nameZh = "检查药水",
        description = "Applies child components when the target has the potion effect.",
        descriptionZh = "检查目标的药水效果。选定具体效果时，要求该效果存在且等级落在 [最低阶级, 最高阶级] 闭区间内，再与“启用/未启用”比对；目标身上完全没有药水效果时按“未启用”处理。选“Any”时会走兜底逻辑：只要任意一个效果的等级在区间内就通过，此时“未启用”选项不起作用。",
        container = true)
public class PotionCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] Whether or not the potion should be active",
            tooltipZh = "只有值恰好等于“not active”（忽略大小写）才要求效果不存在，其余任何值都要求效果存在。选“Any”药水时该取反不生效。",
            options = {"Active", "Not Active"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "Active")
    private static final String TYPE = "type";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Potion",
            labelZh = "药水效果",
            tooltip = "[potion] The type of potion to look for",
            tooltipZh = "要查找的药水效果名，转大写并把空格替换为下划线后与 PotionEffectType 比对。“Any”不是合法效果名，会退化成“任意效果只要等级在区间内即通过”。",
            options = {"Any", "Absorption", "Blindness", "Confusion", "Damage Resistance", "Fast Digging", "Fire Resistance", "Glowing", "Harm", "Heal", "Health Boost", "Hunger", "Increase Damage", "Invisibility", "Jump", "Levitation", "Luck", "Night Vision", "Poison", "Regeneration", "Saturation", "Slow", "Slow Digging", "Speed", "Unluck", "Water Breathing", "Weakness", "Wither"},
            optionsZh = {"任意", "状态效果2", "状态效果3", "状态效果4", "状态效果5", "状态效果6", "火焰", "状态效果8", "状态效果9", "状态效果10", "状态效果11", "状态效果12", "状态效果13", "状态效果14", "状态效果15", "状态效果16", "状态效果17", "状态效果18", "状态效果19", "状态效果20", "饱和度", "状态效果22", "状态效果23", "速度", "状态效果25", "水", "状态效果27", "状态效果28"},
            defaultValue = "Any")
    private static final String POTION = "potion";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Min Rank",
            labelZh = "最低阶级",
            tooltip = "[min-rank] The minimum rank the potion effect can be",
            tooltipZh = "效果等级（amplifier）下限，闭区间。不填按 0 计算，即最低一级也算。")
    private static final String MIN_RANK = "min-rank";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Max Rank",
            labelZh = "最高阶级",
            tooltip = "[max-rank] The maximum rank the potion effect can be",
            tooltipZh = "效果等级（amplifier）上限，闭区间。不填按 999 计算，即不限上限。")
    private static final String MAX_RANK = "max-rank";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final boolean active = !settings.getString(TYPE, "active").toLowerCase().equals("not active");
        final Collection<PotionEffect> effects = target.getActivePotionEffects();
        if (effects.isEmpty()) return !active;

        final String potion = settings.getString(POTION, "").toUpperCase().replace(' ', '_');
        final int minRank = (int) parseValues(caster, MIN_RANK, level, 0);
        final int maxRank = (int) parseValues(caster, MAX_RANK, level, 999);
        try {
            final PotionEffectType type = PotionEffectType.getByName(potion);
            return has(target, type, minRank, maxRank) == active;
        } catch (Exception ex) {
            for (final PotionEffect check : effects) {
                if (check.getAmplifier() >= minRank && check.getAmplifier() <= maxRank) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean has(LivingEntity target, PotionEffectType type, int min, int max) {
        int rank;
        if (VersionManager.isVersionAtLeast(VersionManager.V1_9_0)) {
            rank = target.getPotionEffect(type).getAmplifier();
            if (!target.hasPotionEffect(type)) {
                return false;
            }
        } else {
            rank = target.getActivePotionEffects().stream()
                    .filter(effect -> effect.getType() == type)
                    .findFirst()
                    .map(PotionEffect::getAmplifier)
                    .orElse(-1);
            if (rank == -1) return false;
        }
        return rank >= min && rank <= max;
    }

    @Override
    public String getKey() {
        return "potion";
    }
}
