/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.FireMechanic
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
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Creates an explosion at the target's location
 */
@SkillNode(
        key = "explosion",
        name = "Explosion",
        nameZh = "爆炸",
        description = "Causes an explosion at the current target's position",
        descriptionZh = "在每个当前目标所在坐标调用原版爆炸，威力、是否点燃、是否破坏方块都可单独配置。爆炸伤害由原版计算，不走技能伤害事件，因此不受技能伤害修正影响，也可能炸到施法者自己。目标列表为空时不执行。")
public class ExplosionMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Power",
            labelZh = "强度",
            tooltip = "[power] The strength of the explosion",
            tooltipZh = "爆炸威力，含义与 TNT 相同（TNT 约为 4）；决定伤害范围与破坏半径。数值随技能等级/属性变化，默认 4。")
    private static final String POWER  = "power";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Damage Blocks",
            labelZh = "破坏方块",
            tooltip = "[damage] Whether or not to damage blocks with the explosion",
            tooltipZh = "是否允许爆炸破坏方块。默认「否」，即只产生伤害与特效而不毁地形。注意该项对应代码里的 damage-blocks 参数，与「火焰」一起决定是否改变环境。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String DAMAGE = "damage";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Fire",
            labelZh = "火焰",
            tooltip = "[fire] Whether or not to set affected blocks on fire",
            tooltipZh = "是否让爆炸把命中范围内的方块点燃。默认「否」。只有在服务器允许火焰蔓延时才会继续扩散。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String FIRE   = "fire";

    @Override
    public String getKey() {
        return "explosion";
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
        double power = parseValues(caster, POWER, level, 4);
        boolean fire = settings.getBool(FIRE, false);
        boolean damage = settings.getBool(DAMAGE, false);
        for (LivingEntity target : targets) {
            Location loc = target.getLocation();
            target.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), (float) power, fire, damage);
        }
        return targets.size() > 0;
    }
}
