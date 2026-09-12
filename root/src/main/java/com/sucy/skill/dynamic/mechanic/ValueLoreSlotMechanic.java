/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueLoreSlot
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
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.ItemChecker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "value lore slot",
        name = "Value Lore Slot",
        nameZh = "数值描述槽位",
        description = "Loads a value from an item's lore into a stored value under the given unique key for the caster.",
        descriptionZh = "与 Value Lore 相同，但从施法者背包的指定槽位取物品而非手持物品。要求施法者是玩家，且目标列表非空、已配置 key，否则返回 false。同样存在“有 Lore 但无匹配行时仍返回 true 且不写入键”的情况。")
public class ValueLoreSlotMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "cast data 里的键名，支持 {uuid} 替换为施法者 UUID。匹配成功时存入 Double（抓到的数字 × 倍率）。",
            defaultValue = "lore")
    private static final String KEY        = "key";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Regex",
            labelZh = "正则表达式",
            tooltip = "[regex] The regex string to look for, using {value} as the number to store. If you do not know about regex, consider looking it up on Wikipedia or avoid using major characters such as [ ] { } ( ) . + ? * ^ \\ |",
            tooltipZh = "匹配 Lore 行的正则，{value} 会被替换成数字捕获组；匹配前剥除颜色代码，取第一个匹配行。",
            defaultValue = "Damage: {value}")
    private static final String REGEX      = "regex";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Multiplier",
            labelZh = "倍率",
            tooltip = "[multiplier] The multiplier for the acquired value. If you want the value to remain unchanged, leave this value at 1.",
            tooltipZh = "抓到的数字乘上的倍率，随技能等级缩放（base + scale×(等级-1)）；保持原值填 1。")
    private static final String MULTIPLIER = "multiplier";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Slot",
            labelZh = "槽位",
            tooltip = "[slot] The slot of the inventory to fetch the item from. Slots 0-8 are the hotbar, 9-35 are the main inventory, 36-39 are armor, and 40 is the offhand slot.",
            tooltipZh = "背包槽位序号：0-8 快捷栏，9-35 主背包，36-39 盔甲，40 副手。该值为固定整数，不随等级缩放。默认 9。",
            defaultValue = "9")
    private static final String SLOT       = "slot";

    @Override
    public String getKey() {
        return "value lore slot";
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
        if (targets.size() == 0 || !settings.has(KEY) || !(caster instanceof Player)) { return false; }

        String key = settings.getString(KEY).replace("{uuid}", caster.getUniqueId().toString());;
        double multiplier = parseValues(caster, MULTIPLIER, level, 1);
        int slot = settings.getInt(SLOT);
        String regex = settings.getString(REGEX, "Damage: {value}");

        ItemStack item = ((Player) caster).getInventory().getItem(slot);

        return ItemChecker.findLore(caster, item, regex, key, multiplier);
    }
}
