/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueLoreMechanic
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

import com.rit.sucy.version.VersionManager;
import com.sucy.skill.dynamic.ItemChecker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Adds to a cast data value
 */
@SkillNode(
        key = "value lore",
        name = "Value Lore",
        nameZh = "数值描述",
        description = "Loads a value from a held item's lore into a stored value under the given unique key for the caster.",
        descriptionZh = "用正则从施法者手持物品的 Lore 中抓出一个数字，乘上倍率后存入施法者 cast data。物品为空、没有 meta 或没有 Lore 时返回 false；但如果有 Lore 却没有任何行匹配，节点依然返回 true 而键不会被写入，后续读取会拿到旧值或缺失值，需自行先用 Value Set 兜底。")
public class ValueLoreMechanic extends MechanicComponent {
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
            tooltipZh = "匹配 Lore 行的正则，其中 {value} 会被替换成数字捕获组 ([+-]?[0-9]+([.,][0-9]+)?)。匹配前会剥除颜色代码，逐行找到第一个匹配即停止。不熟悉正则时避免使用 [ ] { } ( ) . + ? * ^ \\ | 等字符。",
            defaultValue = "Damage: {value}")
    private static final String REGEX      = "regex";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Multiplier",
            labelZh = "倍率",
            tooltip = "[multiplier] The multiplier for the acquired value. If you want the value to remain unchanged, leave this value at 1.",
            tooltipZh = "抓到的数字乘上的倍率，随技能等级缩放（base + scale×(等级-1)）；想保持原值填 1。默认 1。")
    private static final String MULTIPLIER = "multiplier";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Hand",
            labelZh = "手持位置",
            tooltip = "[hand] The hand to check for the item. Offhand items are MC 1.9+ only.",
            tooltipZh = "取主手还是副手物品。选 Offhand 且服务端为 1.9 及以上时读副手，否则回退读主手（1.8 环境下选副手等同主手）。默认 Main。",
            options = {"Main", "Offhand"},
            optionsZh = {"物品1", "副手"},
            defaultValue = "Main")
    private static final String HAND       = "hand";

    @Override
    public String getKey() {
        return "value lore";
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
        if (targets.size() == 0 || !settings.has(KEY)) { return false; }

        String key = settings.getString(KEY).replace("{uuid}", caster.getUniqueId().toString());;
        double multiplier = parseValues(caster, MULTIPLIER, level, 1);
        boolean offhand = settings.getString(HAND, "").equalsIgnoreCase("offhand");
        String regex = settings.getString(REGEX, "Damage: {value}");

        if (caster.getEquipment() == null) { return false; }

        ItemStack hand;
        if (offhand && VersionManager.isVersionAtLeast(VersionManager.V1_9_0)) {
            hand = caster.getEquipment().getItemInOffHand();
        } else { hand = caster.getEquipment().getItemInHand(); }

        return ItemChecker.findLore(caster, hand, regex, key, multiplier);
    }
}
