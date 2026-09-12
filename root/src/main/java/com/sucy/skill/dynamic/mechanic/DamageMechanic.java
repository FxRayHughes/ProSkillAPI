/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.DamageMechanic
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
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.attribute.AttributeAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.UUID;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Deals damage to each target
 */
@SkillNode(
        key = "damage",
        name = "Damage",
        nameZh = "伤害",
        description = "Inflicts skill damage to each target. Multiplier type would be a percentage of the target health.",
        descriptionZh = "对每个目标造成技能伤害，支持固定值和三种按生命值百分比计算的模式。若施法者身上带有「主人」标记（如由盔甲架/宠物类节点召唤出来的实体），伤害数值改用主人的属性计算、伤害来源也记作主人；主人已不存在或已死亡时退回用施法者自己算。算出的基础伤害为负数时整个节点返回 false；已死亡的目标被跳过。只要目标列表非空就返回 true。")
public class DamageMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The unit to use for the amount of damage. Damage will deal flat damage, Multiplier will deal a percentage of the target's max health, Percent Left will deal a percentage of their current health, and Percent Missing will deal a percentage of the difference between their max health and current health",
            tooltipZh = "伤害数值的含义。Damage 为固定伤害；Multiplier 按目标最大生命的百分比；Percent Left 按目标当前生命的百分比；Percent Missing 按目标已损失生命的百分比。三种百分比模式都是数值除以 100 后再乘，所以填 30 表示 30%。",
            options = {"Damage", "Multiplier", "Percent Left", "Percent Missing"},
            optionsZh = {"伤害", "倍率", "剩余生命百分比", "损失生命百分比"},
            defaultValue = "Damage")
    private static final String TYPE = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Value",
            labelZh = "数值",
            tooltip = "[value] The amount of damage to deal",
            tooltipZh = "伤害数值，默认 1.0，随技能等级缩放。为负数时节点不执行。施法者有主人时按主人的属性来解析这个值。")
    private static final String DAMAGE = "value";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "True Damage",
            labelZh = "真实伤害",
            tooltip = "[true] Whether or not to deal true damage. True damage ignores armor and all plugin checks, and doesn not have a damage animation nor knockback",
            tooltipZh = "为 true 时打真实伤害，无视护甲和所有插件检查，没有受击动画也没有击退，同时「伤害分类」和「施加击退」都被忽略。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String TRUE = "true";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Classifier",
            labelZh = "伤害分类",
            tooltip = "[classifier] The type of damage to deal. Can act as elemental damage or fake physical damage",
            tooltipZh = "伤害分类名，可当作元素伤害或伪装的物理伤害使用。",
            defaultValue = "default")
    private static final String CLASSIFIER = "classifier";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Apply Knockback",
            labelZh = "施加击退",
            tooltip = "[knockback] Whether or not the damage will inflict knockback. Ignored if it is True Damage",
            tooltipZh = "是否附带击退效果。选了真实伤害时该项无效。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String KNOCKBACK = "knockback";

    @Override
    public String getKey() {
        return "damage";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        String pString = settings.getString(TYPE, "damage").toLowerCase();
        boolean percent = pString.equals("multiplier") || pString.equals("percent");
        boolean missing = pString.equals("percent missing");
        boolean left = pString.equals("percent left");
        boolean trueDmg = settings.getBool(TRUE, false);
        double damage = 0;
        LivingEntity other = caster;

        if (caster.getMetadata(AttributeAPI.FX_SKILL_API_MASTER).isEmpty()) {
            damage = parseValues(caster, DAMAGE, level, 1.0);
        } else {
            UUID masterId = UUID.fromString(caster.getMetadata(AttributeAPI.FX_SKILL_API_MASTER).get(0).asString());
            Entity master = Bukkit.getEntity(masterId);
            if (master == null || master.isEmpty() || master.isDead()) {
                damage = parseValues(caster, DAMAGE, level, 1.0);
            } else if (master instanceof LivingEntity) {
                other = (LivingEntity) master;
                damage = parseValues((LivingEntity) master, DAMAGE, level, 1.0);
            }
        }
        boolean knockback = settings.getBool(KNOCKBACK, true);
        String classification = settings.getString(CLASSIFIER, "default");
        if (damage < 0) {
            return false;
        }
        for (LivingEntity target : targets) {
            if (target.isDead()) {
                continue;
            }
            double amount = damage;
            if (percent) {
                amount = damage * target.getMaxHealth() / 100;
            } else if (missing) {
                amount = damage * (target.getMaxHealth() - target.getHealth()) / 100;
            } else if (left) {
                amount = damage * target.getHealth() / 100;
            }
            if (trueDmg) {
                skill.trueDamage(target, amount, other);
            } else {
                skill.damage(target, amount, other, classification, knockback);
            }
        }
        return targets.size() > 0;
    }
}
