/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.MessageMechanic
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

import com.rit.sucy.text.TextFormatter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Sends a message to each player target
 */
@SkillNode(
        key = "message",
        name = "Message",
        nameZh = "消息",
        description = "Sends a message to each player target. To include numbers from Value mechanics, use the filters {<key>} where <key> is the key the value is stored under.",
        descriptionZh = "向每个玩家目标发送一条聊天消息，非玩家目标会被跳过。消息支持颜色代码，并会做占位符替换：{player} 为施法者名、{target} 为目标名、{targetUUID} 为目标 UUID（目标非玩家时有用），以及 {键名} 取用施法数据中该键存下的值（多由「数值」类节点写入）。未填写消息内容或目标列表为空时不执行；目标中没有任何玩家时返回失败。")
public class MessageMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Message",
            labelZh = "消息",
            tooltip = "[message] The message to display. {player} = caster's name, {target} = target's name, {targetUUID} = target's UUID (useful if targets are non players), &lc: \"{\", &rc: \"}\", &sq: \"'\"",
            tooltipZh = "要发送的消息文本，支持颜色代码与占位符：{player} 施法者名、{target} 目标名、{targetUUID} 目标 UUID、{键名} 取施法数据里该键存下的值（存的是生物时显示其名称）。同名的自定义键优先于内置占位符。需要输出字面大括号或单引号时用 &lc、&rc、&sq 转义。此项为必填，缺失则整个节点不执行。",
            defaultValue = "text")
    private static final String MESSAGE = "message";

    @Override
    public String getKey() {
        return "message";
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
        if (targets.size() == 0 || !settings.has(MESSAGE))
            return false;

        String message = TextFormatter.colorString(settings.getString(MESSAGE));
        if (message == null) return false;

        // Display message
        boolean worked = false;
        for (LivingEntity target : targets)
        {
            if (target instanceof Player)
            {
                Player player = (Player) target;
                player.sendMessage(filter(caster, target, message));
                worked = true;
            }
        }
        return worked;
    }
}
