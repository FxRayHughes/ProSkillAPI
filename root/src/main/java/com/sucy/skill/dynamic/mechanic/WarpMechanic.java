/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.WarpMechanic
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

import com.sucy.skill.api.target.TargetHelper;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Strikes lightning about each target with an offset
 */
@SkillNode(
        key = "warp",
        name = "Warp",
        nameZh = "传送",
        description = "Warps the target relative to their forward direction. Use negative numbers to go in the opposite direction (e.g. negative forward will cause the target to warp backwards).",
        descriptionZh = "以每个目标自身的朝向为基准做相对位移传送（不是以施法者朝向为基准）。三个方向量可填负数表示反向。落点会先经过通行检测：不穿墙时从目标位置沿位移方向前进，遇到实心方块前停下；穿墙时从预期落点反向回退直到脱离实心方块；随后再做一次贴地修正（落点非实心且脚下实心时上抬一格）。因此实际落点通常会被吸附到方块中心并可能短于配置距离。")
public class WarpMechanic extends MechanicComponent {
    private static final Vector UP = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Through Walls",
            labelZh = "穿墙",
            tooltip = "[walls] Whether or not to allow the target to teleport through walls",
            tooltipZh = "是否允许穿墙。False 时被墙挡住就停在墙前；True 时从预期落点往回找第一个非实心位置，可穿过薄墙。默认 False。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String WALL    = "walls";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Forward",
            labelZh = "前方",
            tooltip = "[forward] How far forward in blocks to teleport. A negative value teleports backwards.",
            tooltipZh = "沿目标视线方向前进的格数，随技能等级缩放（base + scale×(等级-1)）；负值向后。默认 0。")
    private static final String FORWARD = "forward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Upward",
            labelZh = "上方",
            tooltip = "[upward] How far upward in blocks to teleport. A negative value teleports downward.",
            tooltipZh = "垂直上移的格数，随技能等级缩放；负值向下。默认 0。")
    private static final String UPWARD  = "upward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Right",
            labelZh = "右方",
            tooltip = "[right] How far to the right in blocks to teleport. A negative value teleports to the left.",
            tooltipZh = "向右横移的格数（由视线方向与竖直向上向量叉乘得出），随技能等级缩放；负值向左。默认 0。")
    private static final String RIGHT   = "right";

    @Override
    public String getKey() {
        return "warp";
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
        if (targets.size() == 0) {
            return false;
        }

        // Get the world
        boolean throughWalls = settings.getString(WALL, "false").toLowerCase().equals("true");
        double forward = parseValues(caster, FORWARD, level, 0.0);
        double upward = parseValues(caster, UPWARD, level, 0.0);
        double right = parseValues(caster, RIGHT, level, 0.0);

        for (LivingEntity target : targets) {
            Vector dir = target.getLocation().getDirection();
            Vector side = dir.clone().crossProduct(UP).multiply(right);
            Location loc = target.getLocation().add(dir.multiply(forward)).add(side).add(0, upward, 0).add(0, 1, 0);
            loc = TargetHelper.getOpenLocation(target.getLocation().add(0, 1, 0), loc, throughWalls);
            if (!loc.getBlock().getType().isSolid() && loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                loc.add(0, 1, 0);
            }
            target.teleport(loc.subtract(0, 1, 0));
        }
        return targets.size() > 0;
    }
}
