/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.CommandMechanic
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

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Executes a command for each target
 */
@SkillNode(
        key = "command",
        name = "Command",
        nameZh = "命令",
        description = "Executes a command for each of the targets.",
        descriptionZh = "对每个目标执行一条指令。占位符 {player} 换成施法者名、{target} 换成目标名、{targetUUID} 换成目标 UUID（目标是非玩家时靠它定位），控制台模式下 <uuid> 也会被替换成目标 UUID。目标列表为空或没填指令则返回 false。OP 模式只对玩家目标生效，非玩家目标被跳过。")
public class CommandMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Command",
            labelZh = "命令",
            tooltip = "[command] The command to execute. {player} = caster's name, {target} = target's name, {targetUUID} = target's UUID (useful if targets are non players), &lc: \"{\", &rc: \"}\", &sq: \"'\"",
            tooltipZh = "要执行的指令内容（不带前导斜杠）。可用占位符：{player} 施法者名、{target} 目标名、{targetUUID} 目标 UUID；&lc &rc &sq 分别转义大括号和单引号。")
    private static final String COMMAND = "command";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Execute Type",
            labelZh = "执行方式",
            tooltip = "[type] Console: executes the command from the console. OP: Only if the target is a player, will have them execute it while given a temporary OP permission (If server closes in the meantime, the permission might stay, not recommended!!)",
            tooltipZh = "执行身份。Console 以控制台身份执行，权限最高且对任何目标都可用。OP 让目标玩家自己执行，执行前临时给 OP、执行后还原——若期间服务器崩溃或关闭，OP 权限可能残留，不建议用。配置里另有 silent console 分支但代码是空实现，选了它什么都不会发生。",
            options = {"Console", "OP"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "OP")
    private static final String TYPE    = "type";

    @Override
    public String getKey() {
        return "command";
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
        if (targets.size() == 0 || !settings.has(COMMAND)) {
            return false;
        }

        String command = settings.getString(COMMAND, "");
        String type = settings.getString(TYPE, "OP").toLowerCase();
        boolean worked = false;

        switch (type) {
            case "op":
                for (LivingEntity t : targets) {
                    if (t instanceof Player) {
                        worked = true;
                        String filteredCommand = filter(caster, t, command);
                        Player p = (Player) t;
                        boolean op = p.isOp();
                        p.setOp(true);
                        Bukkit.getServer().dispatchCommand(p, filteredCommand);
                        p.setOp(op);
                    }
                }
                break;
            case "console":
                for (LivingEntity t : targets) {
                    worked = true;
                    String filteredCommand = filter(caster, t, command).replace("<uuid>",t.getUniqueId().toString());
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), filteredCommand);
                }
                break;
            case "silent console":
                //figure out how to hide command output from console
                break;
        }
        return worked;
    }
}
