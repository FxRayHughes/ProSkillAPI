/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.SpeedMechanic
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
import com.sucy.skill.listener.AttributeListener;
import com.sucy.skill.listener.MechanicListener;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a flag to each target
 */
@SkillNode(
        key = "speed",
        name = "Speed",
        nameZh = "速度",
        description = "Modifies the base speed of a player using a multiplier (stacks with potions)",
        descriptionZh = "按倍率修改玩家目标的基础移动速度，并挂一个限时标记以便到期后恢复。执行时先把该玩家的行走速度重置回属性计算出的基准值再乘以倍率，因此重复施放不会叠乘。只对玩家生效，非玩家目标被跳过；没有任何玩家目标时返回 false。注意原版接口要求行走速度不超过 1，倍率过大时 setWalkSpeed 会抛异常且本节点未捕获。")
public class SpeedMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Multiplier",
            labelZh = "倍率",
            tooltip = "[multiplier] The multiplier of the player's base speed to use",
            tooltipZh = "基础移动速度的倍率，随技能等级缩放，默认 1.2。会与药水效果叠加；数值过大可能因超出原版行走速度上限而报错。")
    private static final String MULTIPLIER = "multiplier";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Duration",
            labelZh = "持续时间",
            tooltip = "[duration] How long to multiply their speed for",
            tooltipZh = "加速持续秒数，随技能等级缩放，默认 3 秒；内部乘 20 转成 tick 作为标记时长，到期后由监听逻辑恢复速度。")
    private static final String DURATION   = "duration";

    @Override
    public String getKey() {
        return "speed";
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
        float multiplier = (float) parseValues(caster, MULTIPLIER, level, 1.2);
        double seconds = parseValues(caster, DURATION, level, 3.0);
        int ticks = (int) (seconds * 20);
        boolean worked = false;
        for (LivingEntity target : targets) {
            if (!(target instanceof Player)) { continue; }

            AttributeListener.refreshSpeed((Player) target);
            FlagManager.addFlag(target, MechanicListener.SPEED_KEY, ticks);
            ((Player) target).setWalkSpeed(multiplier * ((Player) target).getWalkSpeed());
            worked = true;
        }
        return worked;
    }
}
