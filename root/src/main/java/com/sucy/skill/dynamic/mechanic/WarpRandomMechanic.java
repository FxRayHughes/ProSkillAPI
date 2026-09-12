/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.WarpRandomMechanic
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

import java.util.List;
import java.util.Random;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Strikes lightning about each target with an offset
 */
@SkillNode(
        key = "warp random",
        name = "Warp Random",
        nameZh = "传送随机",
        description = "Warps the target in a random direction the given distance.",
        descriptionZh = "把每个目标传送到其周围随机一点。先在 ±distance 的立方范围内取随机偏移，用拒绝采样循环直到该点落在半径 distance 的球（或圆）内，再走与 Warp 相同的通行检测与贴地修正。落点会被吸附到方块中心，实际位移一般小于配置的最大距离。")
public class WarpRandomMechanic extends MechanicComponent {
    private static final Random random = new Random();

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Through Walls",
            labelZh = "穿墙",
            tooltip = "[walls] Whether or not to allow the target to teleport through walls",
            tooltipZh = "是否允许穿墙。False 时沿路径遇到实心方块即停；True 时从随机落点反向回退到第一个非实心位置。默认 False。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String WALL       = "walls";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Only Horizontal",
            labelZh = "仅水平方向",
            tooltip = "[horizontal] Whether or not to limit the random position to the horizontal plane",
            tooltipZh = "是否只在水平面内随机。True（默认）时只随机 X/Z；False 时同时随机 Y，可能被传送到空中或地下。判定为“不等于 false 即视为 true”。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String HORIZONTAL = "horizontal";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Distance",
            labelZh = "距离",
            tooltip = "[distance] The max distance in blocks to teleport",
            tooltipZh = "随机传送的最大距离（方块），随技能等级缩放（base + scale×(等级-1)）。默认 3。")
    private static final String DISTANCE   = "distance";

    @Override
    public String getKey() {
        return "warp random";
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
        boolean horizontal = !settings.getString(HORIZONTAL, "true").toLowerCase().equals("false");
        double distance = parseValues(caster, DISTANCE, level, 3.0);

        for (LivingEntity target : targets) {
            Location loc;
            Location temp = target.getLocation();
            do {
                loc = temp.clone().add(rand(distance), 0, rand(distance));
                if (!horizontal) {
                    loc.add(0, rand(distance), 0);
                }
            }
            while (temp.distanceSquared(loc) > distance * distance);
            loc = TargetHelper.getOpenLocation(target.getLocation().add(0, 1, 0), loc, throughWalls);
            if (!loc.getBlock().getType().isSolid() && loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                loc.add(0, 1, 0);
            }
            target.teleport(loc.subtract(0, 1, 0));
        }
        return targets.size() > 0;
    }

    private double rand(double distance) {
        return random.nextDouble() * distance * 2 - distance;
    }
}
