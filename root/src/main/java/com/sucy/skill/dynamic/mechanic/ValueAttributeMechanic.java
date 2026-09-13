/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueAttributeMechanic
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
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Adds to a cast data value
 */
@SkillNode(
        key = "value attribute",
        name = "Value Attribute",
        nameZh = "数值属性",
        description = "Loads a player's attribute count for a specific attribute as a stored value to be used in other mechanics.",
        descriptionZh = "读取第一个目标的指定属性点数，以 Double 存入施法者 cast data 的键中。目标列表为空时写入 0.0 并仍返回 true。未配置 key 或 attribute 时返回 false。读到的是属性点数（等级数），不是属性带来的加成数值。")
public class ValueAttributeMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "cast data 里的键名，支持 {uuid} 替换为施法者 UUID。存入的值为 Double。",
            defaultValue = "attribute")
    private static final String KEY  = "key";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Attribute",
            labelZh = "属性",
            tooltip = "[attribute] The name of the attribute you are loading the value of",
            tooltipZh = "要读取的属性名，需与服务器属性配置里的键一致（如 Vitality、Intelligence）。属性系统未启用或属性名不存在时读到 0。",
            defaultValue = "Vitality")
    private static final String ATTR = "attribute";

    @Override
    public String getKey() {
        return "value attribute";
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
        if (!settings.has(KEY) || !settings.has(ATTR)) {
            return false;
        }

        String key = settings.getString(KEY, "").replace("{uuid}", caster.getUniqueId().toString());
        String attr = settings.getString(ATTR, "");
        HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        if (targets.isEmpty()) {
            data.put(key, 0.0);
            return true;
        }
        int attribute = AttributeAPI.getAttribute(targets.get(0), attr);
        data.put(key, (double) attribute);
        return true;
    }
}
