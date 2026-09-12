/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ArmorCondition
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

import com.google.common.collect.ImmutableList;
import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.ItemChecker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "armor",
        name = "Armor",
        nameZh = "检查盔甲",
        description = "Applies child components when the target is wearing an armor item matching the given details.",
        descriptionZh = "检查目标装备槽里的护甲是否与物品条件匹配。按“护甲”选择检查头盔/胸甲/护腿/靴子之一，选“任意”则四个槽位任一匹配即通过。匹配走通用物品校验：默认比对材质与耐久数据，可另开名称/Lore 检查；槽位为空一律不通过。",
        container = true)
public class ArmorCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Armor",
            labelZh = "护甲",
            tooltip = "[armor] The type of armor to check",
            tooltipZh = "要检查的护甲槽位：头盔/胸甲/护腿/靴子，或“任意”表示四个槽位逐一尝试、命中一个即通过。该值在技能加载时解析一次。",
            options = {"Helmet", "Chestplate", "Leggings", "Boots", "Any"},
            optionsZh = {"头盔", "胸甲", "护腿", "靴子", "任意"},
            defaultValue = "Any")
    private static final String ARMOR = "armor";

    private List<Function<EntityEquipment, ItemStack>> getters;

    @Override
    public String getKey() {
        return "armor";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        getters = determineGetters();
    }

    private List<Function<EntityEquipment, ItemStack>> determineGetters() {
        final String type = settings.getString(ARMOR).toLowerCase();
        switch (type) {
            case "helmet":
                return ImmutableList.of(EntityEquipment::getHelmet);
            case "chestplate":
                return ImmutableList.of(EntityEquipment::getChestplate);
            case "leggings":
                return ImmutableList.of(EntityEquipment::getLeggings);
            case "boots":
                return ImmutableList.of(EntityEquipment::getBoots);
            default: // All
                return ImmutableList.of(
                        EntityEquipment::getHelmet,
                        EntityEquipment::getChestplate,
                        EntityEquipment::getLeggings,
                        EntityEquipment::getBoots);
        }
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final EntityEquipment equipment = target.getEquipment();
        return equipment != null && getters.stream().anyMatch(
                getter -> ItemChecker.check(getter.apply(equipment), level, settings));
    }
}
