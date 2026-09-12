/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.WarpValueMechanic
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
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a flag to each target
 */
@SkillNode(
        key = "warp value",
        name = "Warp Value",
        nameZh = "传送数值",
        description = "Warps all targets to a location remembered using the Value Location mechanic.",
        descriptionZh = "把所有目标传送到先前由 Value Location 存入 cast data 的位置。读取的是施法者的 cast data，键中的值必须是 Location 对象，键不存在或类型不符时返回 false。注意此节点不做 {uuid} 替换（与各 Value 节点不一致），若 Value Location 的键里用了 {uuid}，这里必须填写替换后的实际键名才能匹配上。同样不做落点检测。",
        requiresNodes = {"value location"})
public class WarpValueMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key the location is stored under. This should be the same key used in the Value Location mechanic.",
            tooltipZh = "存放位置的 cast data 键名，应与 Value Location 中使用的键一致。此处不会替换 {uuid} 占位符。默认 location。",
            defaultValue = "location")
    private static final String KEY = "key";

    @Override
    public String getKey() {
        return "warp value";
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
        if (targets.size() == 0 || !settings.has(KEY))
        {
            return false;
        }

        String key = settings.getString(KEY);
        HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        if (!data.containsKey(key) || !(data.get(key) instanceof Location))
        {
            return false;
        }

        Location loc = (Location) data.get(key);
        for (LivingEntity target : targets)
        {
            target.teleport(loc);
        }
        return true;
    }
}
