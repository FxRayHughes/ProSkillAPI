/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.PermissionCondition
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
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

import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "permission",
        name = "Permission",
        nameZh = "检查权限",
        description = "Applies child components if the caster has the required permission",
        descriptionZh = "检查施法者（不是目标）是否拥有指定权限。该节点不逐目标判断，只判一次，通过后把原目标列表整体交给子节点。",
        container = true)
public class PermissionCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Permission",
            labelZh = "权限",
            tooltip = "[perm] The permission the player needs to have",
            tooltipZh = "要求施法者持有的权限节点。判定对象是施法者，与目标无关。",
            defaultValue = "some.permission")
    private static final String PERM = "perm";

    @Override
    public String getKey() {
        return "permission";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        return test(caster, level, null) && executeChildren(caster, level, targets);
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        return caster.hasPermission(settings.getString(PERM, ""));
    }
}
