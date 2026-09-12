/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.LightningMechanic
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
import org.bukkit.util.Vector;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Strikes lightning about each target with an offset
 */
@SkillNode(
        key = "lightning",
        name = "Lightning",
        nameZh = "闪电",
        description = "Strikes lightning on or near the target. Negative offsets will offset it in the opposite direction (e.g. negative forward offset puts it behind the target).",
        descriptionZh = "在每个目标位置或其附近落下闪电，落点用目标的水平朝向做前后偏移、用其右方向做左右偏移，偏移填负数即为反方向（如前方偏移取负会落在目标背后）。可选择造成真实闪电伤害与点燃，或只播放闪电视觉与音效而不产生任何伤害。目标列表为空时不执行。")
public class LightningMechanic extends MechanicComponent {
    private static final Vector up = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Forward Offset",
            labelZh = "前方偏移",
            tooltip = "[forward] How far in front of the target in blocks to place the lightning",
            tooltipZh = "落点相对目标水平朝向前移的格数，负数为落在身后。数值随技能等级/属性变化，默认 0。")
    private static final String FORWARD = "forward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Right Offset",
            labelZh = "右方偏移",
            tooltip = "[right] How far to the right of the target in blocks to place the lightning",
            tooltipZh = "落点相对目标朝向右侧偏移的格数，负数为偏向左侧。数值随技能等级/属性变化，默认 0。")
    private static final String RIGHT   = "right";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Damage",
            labelZh = "伤害",
            tooltip = "[damage] Whether or not the lightning should deal damage",
            tooltipZh = "「是」落下真实闪电，按原版规则造成伤害并可能点燃；「否」只播放闪电效果与雷声，不造成任何伤害。默认「是」。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String DAMAGE  = "damage";

    @Override
    public String getKey() {
        return "lightning";
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
        boolean damage = settings.getBool(DAMAGE, true);
        double forward = parseValues(caster, FORWARD, level, 0);
        double right = parseValues(caster, RIGHT, level, 0);
        for (LivingEntity target : targets) {
            Vector dir = target.getLocation().getDirection().setY(0).normalize();
            Vector nor = dir.clone().crossProduct(up);
            Location loc = target.getLocation().add(dir.multiply(forward).add(nor.multiply(right)));
            if (damage) { target.getWorld().strikeLightning(loc); } else {
                target.getWorld().strikeLightningEffect(loc);
            }
        }
        return targets.size() > 0;
    }
}
