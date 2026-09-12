/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.StatusMechanic
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

import com.sucy.skill.api.util.FlagManager;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a flag to each target
 */
@SkillNode(
        key = "status",
        name = "Status",
        nameZh = "状态",
        description = "Applies a status effect to the target for a duration.",
        descriptionZh = "给每个目标挂一个限时的自定义状态标记（如眩晕、沉默、定身等），由插件各处的状态检查逻辑读取生效，不是原版药水效果。未配置 status 键或目标列表为空时返回 false。状态名会被转成小写作为标记键，重复施放会刷新该标记的剩余时长。")
public class StatusMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Status",
            labelZh = "状态",
            tooltip = "[status] The status to apply",
            tooltipZh = "要施加的状态，可选眩晕、沉默、定身、缴械、诅咒、吸收、无敌等，默认 Stun。配置值转小写后作为标记键，未配置此项节点直接失效。",
            options = {"Absorb", "Curse", "Disarm", "Invincible", "Root", "Silence", "Stun"},
            optionsZh = {"状态效果1", "状态效果2", "状态效果3", "状态效果4", "状态效果5", "状态效果6", "状态效果7"},
            defaultValue = "Stun")
    private static final String KEY      = "status";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Duration",
            labelZh = "持续时间",
            tooltip = "[duration] How long in seconds to apply the status",
            tooltipZh = "状态持续秒数，随技能等级缩放，默认 3 秒；内部乘 20 转成 tick。")
    private static final String DURATION = "duration";

    @Override
    public String getKey() {
        return "status";
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

        String key = settings.getString(KEY, "stun").toLowerCase();
        double seconds = parseValues(caster, DURATION, level, 3.0);
        int ticks = (int) (seconds * 20);
        for (LivingEntity target : targets) {
            FlagManager.addFlag(target, key, ticks);
        }
        return targets.size() > 0;
    }
}
