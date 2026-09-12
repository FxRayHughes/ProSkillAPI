/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.DelayMechanic
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
import com.sucy.skill.api.util.StatusFlag;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Executes child components after a delay, applying "channeling" rules
 */
@SkillNode(
        key = "channel",
        name = "Channel",
        nameZh = "引导",
        description = "Applies child effects after a duration which can be interrupted. During the channel, the player cannot move, attack, or use other spells.",
        descriptionZh = "引导一段时间后再执行子节点，期间可被打断。引导期间给施法者挂上 CHANNEL 标记，到点时若标记还在（说明没被打断）才移除标记并执行子节点；标记被别的逻辑清掉则子节点永不执行。目标列表为空返回 false，否则安排完定时任务立刻返回 true——注意返回 true 不代表子节点已经跑过。",
        container = true)
public class ChannelMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Time",
            labelZh = "时间",
            tooltip = "[time] The amouont of time, in seconds, to channel for",
            tooltipZh = "引导时长，单位秒，默认 2.0，随技能等级缩放。内部乘 20 换算成 tick。")
    private static final String SECONDS = "time";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Still",
            labelZh = "仍然生效",
            tooltip = "[still] Whether or not to hold the player in place while channeling",
            tooltipZh = "对应配置键 still。为 true 时额外挂 CHANNELING 标记把施法者钉在原地（无法移动/攻击/放其他技能）；为 false 则只走引导计时，人可以自由行动。两个标记的时长都比引导时间多 2 tick 容错。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String STILL   = "still";

    @Override
    public String getKey() {
        return "channel";
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
    public boolean execute(final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        if (targets.size() == 0) {
            return false;
        }
        boolean still = settings.getBool(STILL);
        int ticks = (int) (20 * parseValues(caster, SECONDS, level, 2.0));
        if (still) { FlagManager.addFlag(caster, StatusFlag.CHANNELING, ticks + 2); }
        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("SkillAPI"), () -> {
                    if (FlagManager.hasFlag(caster, StatusFlag.CHANNEL)) {
                        FlagManager.removeFlag(caster, StatusFlag.CHANNEL);
                        FlagManager.removeFlag(caster, StatusFlag.CHANNELING);
                        executeChildren(caster, level, targets);
                    }
                }, ticks
        );
        FlagManager.addFlag(caster, StatusFlag.CHANNEL, ticks + 2);
        return true;
    }
}
