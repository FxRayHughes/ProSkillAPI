/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueHealth
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

import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "value health",
        name = "Value Health",
        nameZh = "数值生命值",
        description = "Stores the target's current health as a value under a given key for the caster",
        descriptionZh = "把第一个目标的生命值按所选口径存入施法者 cast data。注意 percent 存的是 0~1 的比值而非百分数。本节点不检查目标列表是否为空，也不检查 key 是否配置，空目标会抛异常，因此务必配合能保证有目标的选择器使用。")
public class ValueHealthMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "cast data 里的键名，支持 {uuid} 替换为施法者 UUID。存入 Double。",
            defaultValue = "value")
    private static final String KEY  = "key";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] Current provides the health the target has, max provides their total health, missing provides how much health they have lost, and percent is the ratio of health to total health.",
            tooltipZh = "取值口径：Current 当前生命、Max 最大生命、Missing 已损失生命（max-current）、Percent 当前/最大的比值（0~1，不是 0~100）。默认 Current，填写无法识别的值时也按 Current 处理。",
            options = {"Current", "Max", "Missing", "Percent"},
            optionsZh = {"可选值1", "最大值", "可选值3", "可选值4"},
            defaultValue = "Current")
    private static final String TYPE = "type";

    @Override
    public String getKey() {
        return "value health";
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
        final String key = settings.getString(KEY).replace("{uuid}", caster.getUniqueId().toString());;
        final String type = settings.getString(TYPE, "current").toLowerCase();
        final HashMap<String, Object> data = DynamicSkill.getCastData(caster);

        final LivingEntity target = targets.get(0);
        switch (type) {
            case "max":
                data.put(key, target.getMaxHealth());
                break;
            case "percent":
                data.put(key, target.getHealth() / target.getMaxHealth());
                break;
            case "missing":
                data.put(key, target.getMaxHealth() - target.getHealth());
                break;
            default: // current
                data.put(key, target.getHealth());
        }
        return true;
    }
}
