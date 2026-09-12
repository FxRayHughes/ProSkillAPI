/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.WarpSwapMechanic
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

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * Strikes lightning about each target with an offset
 */
@SkillNode(
        key = "warp swap",
        name = "Warp Swap",
        nameZh = "传送切换",
        description = "Switches the location of the caster and the target. If multiple targets are provided, this takes the first one.",
        descriptionZh = "交换施法者与第一个目标的位置：目标被传送到施法者原位置，施法者被传送到目标原位置。多目标时只处理第一个。目标列表为空时返回 false。不做任何落点或穿墙检测，交换后双方可能卡在方块里。")
public class WarpSwapMechanic extends MechanicComponent
{
    @Override
    public String getKey() {
        return "warp swap";
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
        if (targets.size() > 0)
        {
            Location tloc = targets.get(0).getLocation();
            Location cloc = caster.getLocation();
            targets.get(0).teleport(cloc);
            caster.teleport(tloc);
            return true;
        }
        return false;
    }
}
