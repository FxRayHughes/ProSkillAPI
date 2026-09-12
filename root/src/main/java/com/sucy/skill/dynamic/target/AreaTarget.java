/**
 * SkillAPI
 * com.sucy.skill.dynamic.target.AreaTarget
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
package com.sucy.skill.dynamic.target;

import com.sucy.skill.api.util.Nearby;
import com.sucy.skill.cast.IIndicator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies child components to the closest all nearby entities around
 * each of the current targets.
 */
@SkillNode(
        key = "area",
        name = "Area",
        nameZh = "范围选取目标",
        description = "Targets all units in a radius from the current target (the casting player is the default target).",
        descriptionZh = "以每个当前目标所在位置为球心，选中半径内的所有生物（含该目标自身），按距离由近到远排列后交给子节点。默认按距离顺序取满「最多目标数」，开启随机后改为在半径内随机抽取。",
        container = true)
public class AreaTarget extends TargetComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Radius",
            labelZh = "半径",
            tooltip = "[radius] The radius of the area to target in blocks",
            tooltipZh = "球形半径，单位方块；只统计与球心直线距离小于该值的生物，半径越大命中越多。数值随技能等级/属性变化。")
    private static final String RADIUS = "radius";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Random",
            labelZh = "随机",
            tooltip = "[random] Whether or not to randomize the targets selected",
            tooltipZh = "「否」时按距离由近到远填满最多目标数；「是」时先把半径内的生物打乱再取，用于随机点名类效果。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String RANDOM = "random";

    private final Random random = new Random();

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        final double radius = parseValues(caster, RADIUS, level, 3.0);
        final boolean random = settings.getBool(RANDOM, false);
        return determineTargets(caster, level, targets, t -> shuffle(Nearby.getLivingNearby(t, radius, true), random));
    }

    /** {@inheritDoc} */
    @Override
    void makeIndicators(final List<IIndicator> list, final Player caster, final LivingEntity target, final int level) {
        makeCircleIndicator(list, target, parseValues(caster, RADIUS, level, 3.0));
    }

    @Override
    public String getKey() {
        return "area";
    }

    private List<LivingEntity> shuffle(final List<LivingEntity> targets, final boolean random) {
        if (!random) return targets;

        final List<LivingEntity> list = new ArrayList<>();
        while (!targets.isEmpty()) {
            list.add(targets.remove(this.random.nextInt(targets.size())));
        }
        return list;
    }
}
