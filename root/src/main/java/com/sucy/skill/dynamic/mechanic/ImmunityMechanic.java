/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ImmunityMechanic
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

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.util.FlagManager;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a damage immunity flag to each target
 */
@SkillNode(
        key = "immunity",
        name = "Immunity",
        nameZh = "免疫",
        description = "Provides damage immunity from one source for a duration.",
        descriptionZh = "给每个目标一段时间的指定伤害类型减免：以 immune:伤害类型 的形式打上限时标记，同时把伤害倍率写入目标的元数据，受到该类型伤害时按倍率缩放。倍率 0 为完全免疫，0.5 为受伤减半，大于 1 则是加重受伤。未填写伤害类型或目标列表为空时不执行。注意倍率存放在单一元数据键上，对同一目标叠加多个不同类型的免疫时，后写入的倍率会覆盖先前的。")
public class ImmunityMechanic extends MechanicComponent {
    public static final String META_KEY = "sapi_immunity";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The damage type to give an immunity for",
            tooltipZh = "要免疫的伤害类型，取值对应原版伤害原因；内部会转成大写并把空格换成下划线拼进标记名。此项为必填，缺失则整个节点不执行。",
            options = {"Block Explosion", "Contact", "Cramming", "Custom", "Dragon Breath", "Drowning", "Entity Attack", "Entity Explosion", "Entity Sweep Attack", "Fall", "Falling Block", "Fire", "Fire Tick", "Fly Into Wall", "Hot Floor", "Lava", "Lightning", "Magic", "Melting", "Poison", "Projectile", "Starvation", "Suffocation", "Suicide", "Thorns", "Void", "Wither"},
            optionsZh = {"爆炸", "可选值2", "可选值3", "自定义", "可选值5", "可选值6", "实体", "实体爆炸", "实体", "可选值10", "可选值11", "火焰", "火焰", "可选值14", "可选值15", "熔岩", "可选值17", "可选值18", "可选值19", "可选值20", "投射物", "可选值22", "可选值23", "可选值24", "可选值25", "可选值26", "可选值27"},
            defaultValue = "Poison")
    private static final String TYPE       = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Seconds",
            labelZh = "秒数",
            tooltip = "[seconds] How long to give an immunity for",
            tooltipZh = "免疫持续秒数，内部乘 20 转为 tick，到期后标记自动移除。数值随技能等级/属性变化，默认 3 秒。")
    private static final String SECONDS    = "seconds";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Multiplier",
            labelZh = "倍率",
            tooltip = "[multiplier] The multiplier for the incoming damage. Use 0 if you want full immunity.",
            tooltipZh = "该类型伤害的倍率：0 为完全免疫，0.5 为减半，1 等于无效果，大于 1 则伤害加重。数值随技能等级/属性变化，默认 0（完全免疫）。")
    private static final String MULTIPLIER = "multiplier";

    @Override
    public String getKey() {
        return "immunity";
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
        if (targets.size() == 0 || !settings.has(TYPE)) {
            return false;
        }

        String key = settings.getString(TYPE, "all");
        double seconds = parseValues(caster, SECONDS, level, 3.0);
        double multiplier = parseValues(caster, MULTIPLIER, level, 0);
        int ticks = (int) (seconds * 20);
        for (LivingEntity target : targets) {
            FlagManager.addFlag(target, "immune:" + key.toUpperCase().replace(" ", "_"), ticks);
            SkillAPI.setMeta(target, META_KEY, multiplier);
        }
        return targets.size() > 0;
    }
}
