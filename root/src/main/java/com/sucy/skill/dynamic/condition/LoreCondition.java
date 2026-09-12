/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.LoreCondition
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

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import com.sucy.skill.dynamic.meta.SkillNode;

@SkillNode(
        key = "lore",
        name = "Lore",
        nameZh = "物品说明文本",
        container = true,
        descriptionZh = "检查目标主手物品的 Lore 是否包含指定文本，任一行命中即通过。开启正则时按正则部分匹配（find），否则按子串包含。没有装备、主手为空、物品没有 meta 或没有 Lore 都不通过。注意此处不去除颜色符号，比对的是带颜色代码的原始文本。")
public class LoreCondition extends ConditionComponent {
    private static final String REGEX  = "regex";
    private static final String STRING = "str";

    private Predicate<String> test;

    @Override
    public String getKey() {
        return "lore";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        final boolean regex = settings.getString(REGEX, "false").toLowerCase().equals("true");
        final String str = settings.getString(STRING, "");
        if (regex) {
            final Pattern pattern = Pattern.compile(str);
            test = line -> pattern.matcher(line).find();
        } else {
            test = line -> line.contains(str);
        }
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final EntityEquipment items = target.getEquipment();
        if (items == null || items.getItemInHand() == null || !items.getItemInHand().hasItemMeta()) { return false; }

        final List<String> lore = items.getItemInHand().getItemMeta().getLore();
        return lore != null && lore.stream().anyMatch(test);
    }
}
