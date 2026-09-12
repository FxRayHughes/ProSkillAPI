/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.HealMechanic
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
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, valueS OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.mechanic;

import com.rit.sucy.version.VersionManager;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.event.SkillHealEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.UUID;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Heals each target
 */
@SkillNode(
        key = "heal",
        name = "Heal",
        nameZh = "治疗",
        description = "Restores health to each target.",
        descriptionZh = "为每个目标恢复生命值，可选固定值或按目标最大生命值的百分比。已死亡的目标会被跳过；治疗前会抛出可取消的 SkillHealEvent，被其他插件取消则该目标不回血。若施法者带有主人标记（宠物/召唤物场景），数值会改用主人的属性来计算并把主人记为治疗来源；主人已不存在或已死亡时退回用施法者自身计算。计算出的数值为负数时整个节点直接失败，不能用它来造成伤害。")
public class HealMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The unit to use for the amount of health to restore. Health restores a flat amount while Percent restores a percentage of their max health.",
            tooltipZh = "数值单位：「生命值」按固定点数回复，「百分比」按目标最大生命值的百分之几回复（数值 100 即回满）。默认按固定生命值。",
            options = {"Health", "Percent"},
            optionsZh = {"生命值", "可选值2"},
            defaultValue = "Health")
    private static final String TYPE  = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Value",
            labelZh = "数值",
            tooltip = "[value] The amount of health to restore",
            tooltipZh = "回复量，配合类型解释为点数或百分比。负数会让整个节点直接返回失败，不产生任何治疗。数值随技能等级/属性变化，默认 1。")
    private static final String VALUE = "value";

    @Override
    public String getKey() {
        return "heal";
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
        boolean percent = settings.getString(TYPE, "health").equalsIgnoreCase("percent");

        double value = 0;
        LivingEntity other = caster;

        if (caster.getMetadata(AttributeAPI.FX_SKILL_API_MASTER).isEmpty()) {
            value = parseValues(caster, VALUE, level, 1.0);
        } else {
            UUID masterId = UUID.fromString(caster.getMetadata(AttributeAPI.FX_SKILL_API_MASTER).get(0).asString());
            Entity master = Bukkit.getEntity(masterId);
            if (master == null || master.isEmpty() || master.isDead()) {
                value = parseValues(caster, VALUE, level, 1.0);
            } else if (master instanceof LivingEntity) {
                other = (LivingEntity) master;
                value = parseValues((LivingEntity) master, VALUE, level, 1.0);
            }
        }
        
        if (value < 0) { return false; }
        for (LivingEntity target : targets) {
            if (target.isDead()) { continue; }

            double amount = value;
            if (percent) {
                amount = target.getMaxHealth() * value / 100;
            }

            SkillHealEvent event = new SkillHealEvent(other , target, skill, amount);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                VersionManager.heal(target, event.getAmount());
            }
        }
        return targets.size() > 0;
    }
}
