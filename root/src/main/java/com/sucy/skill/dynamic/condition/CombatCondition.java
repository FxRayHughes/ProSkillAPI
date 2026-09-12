/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.CombatCondition
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
package com.sucy.skill.dynamic.condition;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.api.util.Combat;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target to be a player
 * who's combat status matches the settings
 */
@SkillNode(
        key = "combat",
        name = "Combat",
        nameZh = "检查战斗",
        description = "Applies child components to targets that are in/out of combat, depending on the settings.",
        descriptionZh = "要求目标是玩家且战斗状态符合配置。以“距离最近一次战斗行为的秒数”判断是否算在战斗中，超过该秒数即视为脱战。非玩家目标一律不通过。",
        container = true)
public class CombatCondition extends ConditionComponent
{
    @SkillField(
            kind = FieldKind.ListValue,
            label = "In Combat",
            labelZh = "战斗中",
            tooltip = "[combat] Whether or not the target should be in or out of combat",
            tooltipZh = "只有值恰好等于“false”（忽略大小写）才要求脱战，其余任何值都要求处于战斗中。该值在加载时解析一次。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String COMBAT  = "combat";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Seconds",
            labelZh = "秒数",
            tooltip = "[seconds] The time in seconds since the last combat activity before something is considered not in combat",
            tooltipZh = "脱战判定的时间窗口，单位秒：最近一次战斗行为在该秒数之内算在战斗中。加载时读取，不随技能等级缩放，不填按 10 秒计算。",
            defaultValue = "10")
    private static final String SECONDS = "seconds";

    private boolean combat;
    private double seconds;

    @Override
    public String getKey() {
        return "combat";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        combat = !settings.getString(COMBAT, "true").toLowerCase().equals("false");
        seconds = settings.getDouble(SECONDS, 10);
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        return target instanceof Player && Combat.isInCombat((Player) target, seconds) == combat;
    }
}
