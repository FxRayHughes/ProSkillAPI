/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.WarpLocMechanic
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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Strikes lightning about each target with an offset
 */
@SkillNode(
        key = "warp location",
        name = "Warp Location",
        nameZh = "传送位置",
        description = "Warps the target to a specified location.",
        descriptionZh = "把所有目标传送到一个固定坐标。世界名填 current 时使用施法者所在世界；指定的世界不存在时返回 false。注意此节点不做任何落点检测，坐标是实心方块内部也会照样传送，且坐标与朝向都是固定配置值，不随技能等级缩放。")
public class WarpLocMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "World (or \"current\")",
            labelZh = "参数3",
            tooltip = "[world] The name of the world that the location is in",
            tooltipZh = "目标世界名，填 current 表示施法者当前所在世界。世界未加载或名称错误时整个节点返回 false。默认 current。",
            defaultValue = "current")
    private static final String WORLD = "world";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "X",
            labelZh = "参数4",
            tooltip = "[x] The X-coordinate of the desired position",
            tooltipZh = "落点 X 坐标，固定值，不随等级缩放。默认 0。",
            defaultValue = "0")
    private static final String X     = "x";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Y",
            labelZh = "参数5",
            tooltip = "[y] The Y-coordinate of the desired position",
            tooltipZh = "落点 Y 坐标，固定值，不随等级缩放。默认 0。",
            defaultValue = "0")
    private static final String Y     = "y";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Z",
            labelZh = "参数6",
            tooltip = "[z] The Z-coordinate of the desired position",
            tooltipZh = "落点 Z 坐标，固定值，不随等级缩放。默认 0。",
            defaultValue = "0")
    private static final String Z     = "z";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Yaw",
            labelZh = "偏航角",
            tooltip = "[yaw] The Yaw of the desired position (left/right orientation)",
            tooltipZh = "落点偏航角（左右朝向），单位度，固定值。默认 0。",
            defaultValue = "0")
    private static final String YAW   = "yaw";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Pitch",
            labelZh = "音调",
            tooltip = "[pitch] The Pitch of the desired position (up/down orientation)",
            tooltipZh = "落点俯仰角（上下朝向），单位度，固定值；负值朝上，正值朝下。默认 0。",
            defaultValue = "0")
    private static final String PITCH = "pitch";

    @Override
    public String getKey() {
        return "warp location";
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

        // Get the world
        String world = settings.getString(WORLD, "current");
        if (world.equalsIgnoreCase("current"))
        {
            world = caster.getWorld().getName();
        }
        World w = Bukkit.getWorld(world);
        if (w == null)
        {
            return false;
        }

        // Get the other values
        double x = settings.getDouble(X, 0.0);
        double y = settings.getDouble(Y, 0.0);
        double z = settings.getDouble(Z, 0.0);
        float yaw = (float) settings.getDouble(YAW, 0.0);
        float pitch = (float) settings.getDouble(PITCH, 0.0);

        Location loc = new Location(w, x, y, z, yaw, pitch);

        for (LivingEntity target : targets)
        {
            target.teleport(loc);
        }
        return targets.size() > 0;
    }
}
