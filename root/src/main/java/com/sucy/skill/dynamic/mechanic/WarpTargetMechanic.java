/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.WarpTargetMechanic
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

import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Strikes lightning about each target with an offset
 */
@SkillNode(
        key = "warp target",
        name = "Warp Target",
        nameZh = "传送目标",
        description = "Warps either the target or the caster to the other. This does nothing when the target is the caster.",
        descriptionZh = "在施法者与目标之间做单向传送。选择“目标到施法者”时把每个目标都传送到施法者处；否则把施法者传送到目标处——此时会对目标列表逐个执行，施法者最终停在最后一个目标身上，所以这个方向通常只配合单目标使用。目标为施法者自身时等于无操作。目标列表为空时返回 false。")
public class WarpTargetMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The direction to warp the involved targets",
            tooltipZh = "传送方向。填 Target to Caster 时目标被拉到施法者身边；其他值（默认 Caster to Target）时施法者被传送到目标处，多目标时以最后一个为准。",
            options = {"Caster to Target", "Target to Caster"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "Caster to Target")
    private static final String TYPE = "type";

    @Override
    public String getKey() {
        return "warp target";
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
        if (targets.size() == 0)
        {
            return false;
        }

        boolean toCaster = settings.getString(TYPE, "caster to target").toLowerCase().equals("target to caster");
        for (LivingEntity target : targets)
        {
            if (toCaster)
            {
                target.teleport(caster);
            }
            else
            {
                caster.teleport(target);
            }
        }
        return targets.size() > 0;
    }
}
