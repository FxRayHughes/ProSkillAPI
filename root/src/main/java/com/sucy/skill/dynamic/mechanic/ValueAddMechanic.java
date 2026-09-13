/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueAddMechanic
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
        key = "value add",
        name = "Value Add",
        nameZh = "数值相加",
        description = "Adds to a stored value under a unique key for the caster. If the value wasn't set before, this will set the value to the given amount.",
        descriptionZh = "把指定数量累加到施法者 cast data 的某个键上（存为 Double）。键不存在时等于直接赋值。关键点：累加量会乘以当前目标数量（amount × targets.size()），所以对多目标使用时结果是总和而非单份。目标列表为空或未配置 key 时返回 false。",
        requiresNodes = {"value set"})
public class ValueAddMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "cast data 里的键名，可用 {uuid} 占位符替换成施法者 UUID 以避免多人互相干扰。该键可被 Value Condition 判定，也可直接填在其他节点的数值栏位里当变量使用。",
            defaultValue = "value")
    private static final String KEY = "key";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Amount",
            labelZh = "数量",
            tooltip = "[amount] The amount to add to the value",
            tooltipZh = "每个目标贡献的累加量，随技能等级缩放（base + scale×(等级-1)）；最终写入的是该值乘以目标数量。默认 1。")
    private static final String AMOUNT = "amount";

    @Override
    public String getKey() {
        return "value add";
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

        String key = settings.getString(KEY, "").replace("{uuid}", caster.getUniqueId().toString());
        double amount = parseValues(caster, AMOUNT, level, 1) * targets.size();
        HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        if (!data.containsKey(key)) {
            data.put(key, amount);
        } else {
            Object current = data.get(key);
            if (!(current instanceof Number)) return false;
            data.put(key, amount + ((Number) current).doubleValue());
        }
        return true;
    }
}
