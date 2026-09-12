/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueLocationMechanic
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
        key = "value location",
        name = "Value Location",
        nameZh = "数值位置",
        description = "Loads the first target's current location into a stored value for use at a later time.",
        descriptionZh = "把第一个目标当前所在的位置（Location 对象，含世界、坐标与朝向）存入施法者 cast data，供之后的 Warp Value 等节点取回使用。未配置 key 时返回 false；不检查目标列表是否为空。")
public class ValueLocationMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the location under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "cast data 里的键名，支持 {uuid} 替换为施法者 UUID。存入的是 Location 对象而非数字，不能被 Value Condition 当数值比较，只能被 Warp Value 之类读取位置的节点使用。",
            defaultValue = "location")
    private static final String KEY = "key";

    @Override
    public String getKey() {
        return "value location";
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
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets)
    {
        if (!settings.has(KEY))
        {
            return false;
        }

        String key = settings.getString(KEY).replace("{uuid}", caster.getUniqueId().toString());;
        HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        data.put(key, targets.get(0).getLocation());
        return true;
    }
}
