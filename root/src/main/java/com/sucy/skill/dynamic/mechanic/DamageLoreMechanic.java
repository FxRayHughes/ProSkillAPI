/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.DamageLoreMechanic
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

import com.rit.sucy.config.parse.NumberParser;
import com.rit.sucy.version.VersionManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Deals damage based on a held item's lore to each target
 */
@SkillNode(
        key = "damage lore",
        name = "Damage Lore",
        nameZh = "伤害描述",
        description = "Damages each target based on a value found in the lore of the item held by the caster.",
        descriptionZh = "读取施法者手持物品 Lore 里的数字，乘上倍率后作为伤害打给所有目标——用来做「武器面板攻击力」这类效果。按正则逐行匹配，取第一条匹配成功的行，命中后即停止扫描。施法者没有装备、手上没物品、物品没 Lore、或算出来的伤害不大于 0 时都返回 false，不造成伤害。已死亡的目标被跳过。注意 {value} 只能匹配非负整数，Lore 里带小数点或负号的数字匹配不到。")
public class DamageLoreMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Regex",
            labelZh = "正则表达式",
            tooltip = "[regex] The regex for the text to look for. Use {value} for where the important number should be. If you do not know about regex, consider looking it up on Wikipedia or avoid using major characters such as [ ] { } ( ) . + ? * ^ \\ |",
            tooltipZh = "匹配 Lore 的正则，用 {value} 标记数字所在位置（内部替换为 ([0-9]+)，只认非负整数）。匹配前会先去掉颜色代码。不熟悉正则的话避免使用 [ ] { } ( ) . + ? * ^ \\ | 这些字符。",
            defaultValue = "Damage: {value}")
    private static final String REGEX      = "regex";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Multiplier",
            labelZh = "倍率",
            tooltip = "[multiplier] The multiplier to use on the value to get the actual damage to deal",
            tooltipZh = "从 Lore 取到的数字乘上这个倍率得到最终伤害，默认 1.0，随技能等级缩放。乘积不大于 0 时不造成任何伤害。")
    private static final String MULTIPLIER = "multiplier";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Hand",
            labelZh = "手持位置",
            tooltip = "[hand] The hand to check for the item. Offhand items are MC 1.9+ only.",
            tooltipZh = "从哪只手的物品读取 Lore。选 Offhand 需要服务端至少为 1.9，低版本会自动退回主手。",
            options = {"Main", "Offhand"},
            optionsZh = {"物品1", "副手"},
            defaultValue = "Main")
    private static final String HAND       = "hand";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "True Damage",
            labelZh = "真实伤害",
            tooltip = "[true] Whether or not to deal true damage. True damage ignores armor and all plugin checks.",
            tooltipZh = "为 true 时打真实伤害，无视护甲和所有插件的伤害检查，同时忽略「伤害分类」。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String TRUE       = "true";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Classifier",
            labelZh = "伤害分类",
            tooltip = "[PREMIUM ONLY] The type of damage to deal. Can act as elemental damage or fake physical damage",
            tooltipZh = "伤害分类名，可当作元素伤害或伪装的物理伤害使用。选了真实伤害时该项无效。",
            defaultValue = "default")
    private static final String CLASSIFIER = "classifier";

    @Override
    public String getKey() {
        return "damage lore";
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
        String regex = settings.getString(REGEX, "Damage: {value}");
        regex = regex.replace("{value}", "([0-9]+)");
        Pattern pattern = Pattern.compile(regex);
        double m = parseValues(caster, MULTIPLIER, level, 1.0);
        boolean worked = false;
        boolean offhand = VersionManager.isVersionAtLeast(VersionManager.V1_9_0)
                && settings.getString(HAND, "mainhand").equalsIgnoreCase("offhand");
        boolean trueDmg = settings.getBool(TRUE, false);
        String classification = settings.getString(CLASSIFIER, "default");

        if (caster.getEquipment() == null) { return false; }

        ItemStack hand;
        if (offhand) { hand = caster.getEquipment().getItemInOffHand(); } else {
            hand = caster.getEquipment().getItemInHand();
        }

        if (hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasLore()) { return false; }

        List<String> lore = hand.getItemMeta().getLore();
        for (String line : lore) {
            line = ChatColor.stripColor(line);
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String value = matcher.group(1);
                try {
                    double base = NumberParser.parseDouble(value);
                    if (base * m > 0) {
                        for (LivingEntity target : targets) {
                            if (target.isDead()) { continue; }

                            if (trueDmg) { skill.trueDamage(target, base * m, caster); } else {
                                skill.damage(target, base * m, caster, classification);
                            }
                        }
                        worked = targets.size() > 0;
                        break;
                    }
                } catch (Exception ex) {
                    // Not a valid value
                }
            }
        }
        return worked;
    }
}
