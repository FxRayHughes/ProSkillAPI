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
import com.sucy.skill.hook.PlaceholderAPIHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Adds to a cast data value
 */
@SkillNode(
        key = "data set",
        name = "Data Set",
        nameZh = "数据设置",
        description = "设置一个Data数据 采用 {uuid} 进行私有化",
        descriptionZh = "直接给目标写入（覆盖）一个自定义数据值，并指定它的存活时长。数据按目标 UUID 存储，键名里的 {uuid} 会替换成施法者的 UUID。目标列表为空或没填「引用键」时返回 false。注意「刻数」默认值是 1（即一 tick 后就失效），要长期保留必须显式填 -1。")
public class DataSetMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] 唯一识别标签 {uuid} 会被进行替换为 施法者的UUID",
            tooltipZh = "数据的唯一标识键。其中 {uuid} 会被替换成施法者的 UUID，可用来把数据按施法者私有化。未填则节点不执行。",
            defaultValue = "标签ID")
    private static final String KEY = "key";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Amount",
            labelZh = "数量",
            tooltip = "[amount] 数字内容",
            tooltipZh = "要写入的数值，默认 1，随技能等级缩放。每个目标各自解析一次。")
    private static final String AMOUNT = "amount";

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Tick",
            labelZh = "刻数",
            tooltip = "[tick] 有效时间 -1为无限",
            tooltipZh = "数据的有效时长，单位 tick（20 tick = 1 秒），填 -1 表示永不过期。默认值只有 1 tick，几乎立刻失效，实际使用时基本都要显式设置。随技能等级缩放。")
    private static final String TICK = "tick";

    @Override
    public String getKey() {
        return "data set";
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

        String key = settings.getString(KEY).replace("{uuid}", caster.getUniqueId().toString());

        double tick = parseValues(caster, TICK, level, 1);
        for (LivingEntity target : targets) {
            double amount = parseValues(caster, AMOUNT, level, 1);
            DataSkill.setValue(target.getUniqueId(), key, amount, (int) tick);
        }
        return true;
    }
}
