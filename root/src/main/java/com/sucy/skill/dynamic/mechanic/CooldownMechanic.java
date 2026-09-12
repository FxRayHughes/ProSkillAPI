/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.CooldownMechanic
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

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.player.PlayerSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Lowers the cooldowns of the caster's skills
 */
@SkillNode(
        key = "cooldown",
        name = "Cooldown",
        nameZh = "冷却",
        description = "Lowers the cooldowns of the target's skill(s). If you provide a negative amount, it will increase the cooldown.",
        descriptionZh = "增减技能冷却时间。注意实际作用对象是施法者自己的技能，与目标列表无关；施法者不是玩家时直接返回 false。数值为正表示减少冷却，为负则拉长冷却。填 all 时对施法者已学的所有技能生效；填具体技能名但玩家没学过该技能时，会退而修改当前这个技能自己的冷却。")
public class CooldownMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Skill (or \"all\")",
            labelZh = "参数3",
            tooltip = "[skill] The skill to modify the cooldown for",
            tooltipZh = "对应配置键 skill。要修改冷却的技能名，填 all 表示全部已学技能。填了玩家未学会的技能名时，会退回修改当前技能自身的冷却。",
            defaultValue = "all")
    private static final String SKILL = "skill";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The modification unit to use. Seconds will add/subtract seconds from the cooldown while Percent will add/subtract a percentage of its full cooldown",
            tooltipZh = "数值单位。Seconds 直接加减秒数；Percent 按该技能完整冷却时长的百分比加减。",
            options = {"Seconds", "Percent"},
            optionsZh = {"秒数", "可选值2"},
            defaultValue = "Seconds")
    private static final String TYPE  = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Value",
            labelZh = "数值",
            tooltip = "[value] The amount to add/subtract from the skill's cooldown",
            tooltipZh = "冷却的调整量，默认 0，随技能等级缩放。正值减少冷却，负值增加冷却（内部走 subtractCooldown）。")
    private static final String VALUE = "value";

    @Override
    public String getKey() {
        return "cooldown";
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
        if (!(caster instanceof Player)) { return false; }

        String skill = settings.getString(SKILL, "");
        String type = settings.getString(TYPE, "all").toLowerCase();
        double value = parseValues(caster, VALUE, level, 0);

        PlayerData playerData = SkillAPI.getPlayerData((Player) caster);

        PlayerSkill skillData = playerData.getSkill(skill);
        if (skillData == null && !skill.equals("all")) {
            skillData = playerData.getSkill(this.skill.getName());
        }

        boolean worked = false;
        if (skill.equals("all")) {
            for (PlayerSkill data : playerData.getSkills()) {
                if (type.equals("percent")) {
                    data.subtractCooldown(value * data.getCooldown() / 100);
                } else {
                    data.subtractCooldown(value);
                }
                worked = true;
            }
        } else if (skillData != null) {
            if (type.equals("percent")) {
                skillData.subtractCooldown(value * skillData.getCooldown() / 100);
            } else {
                skillData.subtractCooldown(value);
            }
            worked = true;
        }
        return worked;
    }
}
