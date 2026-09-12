/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ToolCondition
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
package com.sucy.skill.dynamic.condition;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target to have a specified potion effect
 */
@SkillNode(
        key = "tool",
        name = "Tool",
        nameZh = "检查工具",
        description = "Applies child components when the target is wielding a matching tool.",
        descriptionZh = "检查目标主手物品的材质名里是否同时包含所选材质关键字和工具关键字（子串包含，不是精确匹配）。“任意”表示该维度不限。Shovel 会先换成 SPADE 再比对，因此按 1.12 及更早的材质命名工作；主手为空时不通过。",
        container = true)
public class ToolCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Material",
            labelZh = "材质",
            tooltip = "[material] The material the held tool needs to be made out of",
            tooltipZh = "工具材质关键字（Wood/Stone/Iron/Gold/Diamond），转大写后按子串在主手材质名中查找；“Any”表示不限材质。",
            options = {"Any", "Wood", "Stone", "Iron", "Gold", "Diamond"},
            optionsZh = {"任意", "木头", "石头", "其他材质", "其他材质", "其他材质"},
            defaultValue = "Any")
    private static final String MATERIAL = "material";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Tool",
            labelZh = "工具",
            tooltip = "[tool] The type of tool it needs to be",
            tooltipZh = "工具种类关键字（Axe/Hoe/Pickaxe/Shovel/Sword），转大写并把 SHOVEL 换成 SPADE，再以“_关键字”形式按子串查找；“Any”表示不限种类。",
            options = {"Any", "Axe", "Hoe", "Pickaxe", "Shovel", "Sword"},
            optionsZh = {"任意", "斧", "物品3", "镐", "物品5", "剑"},
            defaultValue = "Any")
    private static final String TOOL     = "tool";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final String material = settings.getString(MATERIAL, "").toUpperCase();
        final String tool = "_" + settings.getString(TOOL, "").toUpperCase().replace("SHOVEL", "SPADE");

        final EntityEquipment equipment = target.getEquipment();
        if (equipment == null || equipment.getItemInHand() == null) return false;

        final String hand = equipment.getItemInHand().getType().name();
        return (material.equals("ANY") || hand.contains(material)) && (tool.equals("_ANY") || hand.contains(tool));
    }

    @Override
    public String getKey() {
        return "tool";
    }
}
