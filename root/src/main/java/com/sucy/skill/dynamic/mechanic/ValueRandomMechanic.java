/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueSetMechanic
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
        key = "value random",
        name = "Value Random",
        nameZh = "数值随机",
        description = "Stores a specified value under a given key for the caster.",
        descriptionZh = "在最小值与最大值之间取一个随机数存入施法者 cast data（存为 Double）。注意代码缺陷：判断三角分布时先把配置值转成大写再与小写字面量 \"triangular\" 比较，条件永远不成立，因此当前只会使用均匀分布，Triangular 选项不生效。目标为空或未配置 key 时返回 false。")
public class ValueRandomMechanic extends MechanicComponent {
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
            tooltip = "[type] The type of random to use. Triangular favors numbers in the middle, similar to rolling two dice.",
            tooltipZh = "本意为 Normal 均匀分布、Triangular 三角分布（两次随机取平均，结果偏向中间值）；但因大小写比较错误，目前恒为均匀分布。",
            options = {"Normal", "Triangular"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "Normal")
    private static final String TYPE = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Min",
            labelZh = "最小值",
            tooltip = "[min] The minimum value it can be",
            tooltipZh = "随机区间下限，随技能等级缩放（base + scale×(等级-1)）。默认 1。")
    private static final String MIN  = "min";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Max",
            labelZh = "最大值",
            tooltip = "[max] The maximum value it can be",
            tooltipZh = "随机区间上限，随技能等级缩放（base + scale×(等级-1)）。默认 1。若 max 小于 min，结果会落在 [max, min] 区间。")
    private static final String MAX  = "max";

    @Override
    public String getKey() {
        return "value random";
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
        boolean triangular = settings.getString(TYPE, "uniform").toUpperCase().equals("TRIANGULAR");
        double min = parseValues(caster, MIN, level, 1);
        double max = parseValues(caster, MAX, level, 1);

        HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        double rand = triangular ? 0.5 * (Math.random() + Math.random()) : Math.random();
        data.put(key, rand * (max - min) + min);
        return true;
    }
}
