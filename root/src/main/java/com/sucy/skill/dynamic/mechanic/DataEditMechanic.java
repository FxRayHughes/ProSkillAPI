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

import com.sucy.skill.dynamic.data.DataSkill;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Adds to a cast data value
 */
@SkillNode(
        key = "data edit",
        name = "Data Edit",
        nameZh = "数据修改",
        description = "修改一个Data + - * / key [action] value",
        descriptionZh = "对目标身上已存的一个自定义数据值做四则运算（+ - * /），用来做叠层、计数器这类状态。数据按目标 UUID 存储，键名里的 {uuid} 会替换成施法者的 UUID（便于每个施法者各存一份互不干扰）。写入时有效期传的是 -1，即永久保留、不会自动过期。目标列表为空或没填「引用键」时返回 false。")
public class DataEditMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] 唯一识别标签 {uuid} 会被进行替换为 施法者的UUID",
            tooltipZh = "数据的唯一标识键。其中 {uuid} 会被替换成施法者的 UUID，可用来把数据按施法者私有化。未填则节点不执行。",
            defaultValue = "标签ID")
    private static final String KEY = "key";

    @SkillField(
            kind = FieldKind.StringValue,
            label = "Action",
            labelZh = "动作",
            tooltip = "[action] + - * /",
            tooltipZh = "运算符，取 + - * / 之一，表示把「数值」以何种方式作用到原有数据上。",
            defaultValue = "符号")
    private static final String ACTION = "action";

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Value",
            labelZh = "数值",
            tooltip = "[value] 数字内容",
            tooltipZh = "参与运算的数字，默认 1，随技能等级缩放。")
    private static final String VALUE = "value";

    @Override
    public String getKey() {
        return "data edit";
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
        if (targets.size() == 0 || !settings.has(KEY)) {
            return false;
        }

        String key = settings.getString(KEY, "").replace("{uuid}", caster.getUniqueId().toString());
        String action = settings.getString(ACTION, "set");
        double value = parseValues(caster, VALUE, level, 1);
        for (LivingEntity target : targets) {
            DataSkill.getDataData(target.getUniqueId(), true).putDataTagData(key, value, -1, action);
        }
        return true;
    }
}
