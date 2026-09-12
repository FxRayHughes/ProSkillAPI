/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueMultiplyMechanic
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

import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Adds to a cast data value
 */
@SkillNode(
        key = "value multiply",
        name = "Value Multiply",
        nameZh = "数值相乘",
        description = "Multiplies a stored value under a unique key for the caster. If the value wasn't set before, this will not do anything.",
        descriptionZh = "把施法者 cast data 中已有的数值乘上一个倍率。与 Value Add 不同，键不存在时什么也不做（不会当作 0 或 1 初始化），但节点仍返回 true。要求该键原有值是 Double，否则会抛 ClassCastException，因此必须先由 Value Set 等节点建立数值。目标为空或未配置 key 时返回 false。",
        requiresNodes = {"value set"})
public class ValueMultiplyMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "要相乘的 cast data 键名，支持 {uuid} 替换为施法者 UUID；该键必须已存在且为数字。",
            defaultValue = "value")
    private static final String KEY        = "key";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Multiplier",
            labelZh = "倍率",
            tooltip = "[multiplier] The amount to multiply the value by",
            tooltipZh = "乘数，随技能等级缩放（base + scale×(等级-1)）。默认 1。与 Value Add 不同，此值不会乘以目标数量。")
    private static final String MULTIPLIER = "multiplier";

    @Override
    public String getKey() {
        return "value multiply";
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
        if (targets.size() == 0 || !settings.has(KEY)) {
            return false;
        }

        String key = settings.getString(KEY).replace("{uuid}", caster.getUniqueId().toString());
        double multiplier = parseValues(caster, MULTIPLIER, level, 1);
        HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        if (data.containsKey(key)) { data.put(key, multiplier * (Double) data.get(key)); }
        return true;
    }
}
