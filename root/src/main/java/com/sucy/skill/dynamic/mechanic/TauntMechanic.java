/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.TauntMechanic
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

import com.sucy.skill.combat.threat.ThreatManager;
import com.sucy.skill.hook.MythicMobsHook;
import com.sucy.skill.hook.PluginChecker;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Mechanic for taunting mobs
 */
@SkillNode(
        key = "taunt",
        name = "Taunt",
        nameZh = "嘲讽",
        description = "Draws aggro of targeted creatures. Regular mobs are set to attack the caster. The Spigot/Bukkit API for this was not functional on older versions, so it may not work on older servers. For MythicMobs, this uses their aggro system using the amount chosen below.",
        descriptionZh = "拉取目标生物的仇恨。普通生物直接把攻击目标设为施法者；若 MythicMobs 激活且目标是 MythicMobs 怪物，则走其仇恨系统按 amount 增减仇恨值。另有一个未在编辑器暴露的 duration 键：当施法者是玩家且 duration 大于 0 时，改用内部仇恨表把目标锁定 duration 秒（内部乘 1000 转毫秒），到期自动回落；未配置 duration 时保持只加固定仇恨量、不做锁定的历史行为。只对 Creature 类型且不是施法者自身的目标生效，没有任何目标符合条件时返回 false。旧版本 Spigot/Bukkit 的相关接口不完善，低版本服务端上可能无效。",
        requiresPlugins = {"MythicMobs"})
public class TauntMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Amount",
            labelZh = "数量",
            tooltip = "[amount] The amount of aggro to apply if MythicMobs is active. Use negative amounts to reduce aggro",
            tooltipZh = "MythicMobs 激活时施加的仇恨值，随技能等级缩放，默认 1。负值用于降低仇恨。对普通生物无意义（普通生物只是被直接设为攻击施法者）。")
    private static final String AMOUNT   = "amount";
    private static final String DURATION = "duration";

    @Override
    public String getKey() {
        return "taunt";
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
        double amount = parseValues(caster, AMOUNT, level, 1);
        // 未配置 duration 时保持历史行为：只加固定仇恨量，不做锁定
        double duration = parseValues(caster, DURATION, level, 0);
        boolean taunted = false;
        for (LivingEntity entity : targets)
        {
            if (entity instanceof Creature && entity != caster)
            {
                if (PluginChecker.isMythicMobsActive() && MythicMobsHook.isMonster(entity)) {
                    // 玩家施放且配置了持续时间时走仇恨表，可把目标锁定一段时间后自动回落
                    if (caster instanceof Player && duration > 0) {
                        ThreatManager.taunt(entity, (Player) caster, (long) (duration * 1000));
                    } else {
                        MythicMobsHook.taunt(entity, caster, amount);
                    }
                }
                else {
                    ((Creature) entity).setTarget(caster);
                }
                taunted = true;
            }
        }
        return taunted;
    }
}
