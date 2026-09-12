/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ClassCondition
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

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "class",
        name = "Class",
        nameZh = "检查职业",
        description = "Applies child components when the target is the given class or optionally a profession of that class. For example, if you check for \"Fighter\" which professes into \"Warrior\", a \"Warrior\" will pass the check if you do not enable \"exact\".",
        descriptionZh = "要求目标是玩家且职业符合配置。精确匹配为否时，该职业的后续转职也算通过；为是时必须正好是这个职业。非玩家目标一律不通过。",
        container = true)
public class ClassCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Class",
            labelZh = "职业",
            tooltip = "[class] The class the player should be",
            tooltipZh = "职业名，按插件注册的职业查询。查不到该职业时判定不通过。",
            defaultValue = "Fighter")
    private static final String CLASS = "class";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Exact",
            labelZh = "精确匹配",
            tooltip = "[exact] Whether or not the player must be exactly the given class. If false, they can be a later profession of the class.",
            tooltipZh = "是=必须正好是该职业；否（默认）=该职业或它的后续转职都算通过。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String EXACT = "exact";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        if (!(target instanceof Player)) return false;

        final RPGClass rpgClass = SkillAPI.getClass(settings.getString(CLASS));
        final boolean exact = settings.getBool(EXACT, false);

        final PlayerData data = SkillAPI.getPlayerData((Player) target);
        return exact ? data.isExactClass(rpgClass) : data.isClass(rpgClass);
    }

    @Override
    public String getKey() {
        return "class";
    }
}
