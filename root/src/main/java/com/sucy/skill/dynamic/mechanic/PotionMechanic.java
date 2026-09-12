/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.PotionMechanic
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

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Executes child components after a delay
 */
@SkillNode(
        key = "potion",
        name = "Potion",
        nameZh = "药水",
        description = "Applies a potion effect to the target for a duration.",
        descriptionZh = "给每个目标施加一个原版药水效果，持续指定秒数。药水名会被转成大写并把空格换成下划线后查表，查不到或过程中抛异常时整个节点返回 false。目标列表为空时也返回 false。")
public class PotionMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Potion",
            labelZh = "药水效果",
            tooltip = "[potion] The type of potion effect to apply",
            tooltipZh = "药水效果类型。配置值会被转成大写并把空格替换为下划线再匹配原版 PotionEffectType，非法名称会让节点直接失效。",
            options = {"Absorption", "Blindness", "Confusion", "Damage Resistance", "Fast Digging", "Fire Resistance", "Glowing", "Harm", "Heal", "Health Boost", "Hunger", "Increase Damage", "Invisibility", "Jump", "Levitation", "Luck", "Night Vision", "Poison", "Regeneration", "Saturation", "Slow", "Slow Digging", "Speed", "Unluck", "Water Breathing", "Weakness", "Wither"},
            optionsZh = {"状态效果1", "状态效果2", "状态效果3", "状态效果4", "状态效果5", "火焰", "状态效果7", "状态效果8", "状态效果9", "状态效果10", "状态效果11", "状态效果12", "状态效果13", "状态效果14", "状态效果15", "状态效果16", "状态效果17", "状态效果18", "状态效果19", "饱和度", "状态效果21", "状态效果22", "速度", "状态效果24", "水", "状态效果26", "状态效果27"},
            defaultValue = "Absorption")
    private static final String POTION  = "potion";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Ambient Particles",
            labelZh = "环境粒子",
            tooltip = "[ambient] Whether or not to show ambient particles",
            tooltipZh = "是否显示环境粒子，默认 True。该开关同时控制 ambient 与 particles 两个参数，关掉后药水完全不显示粒子。只要不显式填 false 就视为 true。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String AMBIENT = "ambient";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Tier",
            labelZh = "阶级",
            tooltip = "[tier] The strength of the potion",
            tooltipZh = "药水强度等级，从 1 开始计（代码会减 1 转成原版 amplifier），随技能等级缩放，默认 1 即最低一级。")
    private static final String TIER    = "tier";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Seconds",
            labelZh = "秒数",
            tooltip = "[seconds] How long to apply the effect for",
            tooltipZh = "持续秒数，随技能等级缩放，默认 3 秒；内部乘 20 转成 tick。")
    private static final String SECONDS = "seconds";

    @Override
    public String getKey() {
        return "potion";
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
        if (targets.size() == 0) {
            return false;
        }

        try {
            PotionEffectType potion = PotionEffectType.getByName(settings.getString(POTION, "Absorption")
                    .toUpperCase()
                    .replace(' ', '_'));
            int tier = (int) parseValues(caster, TIER, level, 1) - 1;
            double seconds = parseValues(caster, SECONDS, level, 3.0);
            boolean ambient = !settings.getString(AMBIENT, "true").equals("false");
            int ticks = (int) (seconds * 20);
            for (LivingEntity target : targets) {
                target.addPotionEffect(new PotionEffect(potion, ticks, tier, ambient, ambient));
            }
            return targets.size() > 0;
        } catch (Exception ex) {
            return false;
        }
    }
}
