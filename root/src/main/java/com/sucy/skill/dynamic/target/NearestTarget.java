/**
 * SkillAPI
 * com.sucy.skill.dynamic.target.NearestTarget
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
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies child components to the closest all nearby entities around
 * each of the current targets.
 */
@SkillNode(
        key = "nearest",
        name = "Nearest",
        nameZh = "最近选取目标",
        description = "Targets the closest unit(s) in a radius from the current target (the casting player is the default target). If you include the caster, that counts towards the max number.",
        descriptionZh = "为每个当前目标各选出一个离它最近的生物（不含它自己），半径外的不算。注意：本节点直接返回结果，不经过阵营、穿墙、含施法者、最多目标数这些通用过滤，所以选中的可能是队友甚至施法者本人，每个输入目标固定只产出一个。",
        container = true)
public class NearestTarget extends TargetComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Radius",
            labelZh = "半径",
            tooltip = "[radius] The radius of the area to target in blocks",
            tooltipZh = "搜索半径，单位方块；在该球形范围内找距离最小的那一个生物，范围内没有生物则该输入目标不产出结果。数值随技能等级/属性变化。")
    private static final String RADIUS = "radius";

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        final double radius = parseValues(caster, RADIUS, level, 3.0);
        final List<LivingEntity> result = new ArrayList<>();
        for (LivingEntity target : targets) {
            final Comparator<LivingEntity> comparator = new DistanceComparator(target.getLocation());
            Nearby.getLivingNearby(target, radius).stream()
                    .min(comparator)
                    .ifPresent(result::add);

        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    void makeIndicators(final List<IIndicator> list, final Player caster, final LivingEntity target, final int level) {
        makeCircleIndicator(list, target, parseValues(caster, RADIUS, level, 3.0));
    }

    @Override
    public String getKey() {
        return "nearest";
    }

    private static class DistanceComparator implements Comparator<LivingEntity> {
        private Location loc;

        private DistanceComparator(final Location loc) {
            this.loc = loc;
        }

        @Override
        public int compare(final LivingEntity o1, final LivingEntity o2) {
            return Double.compare(o1.getLocation().distanceSquared(loc), o2.getLocation().distanceSquared(loc));
        }
    }
}
