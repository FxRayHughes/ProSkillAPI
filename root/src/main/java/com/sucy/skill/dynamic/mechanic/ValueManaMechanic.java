/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueMana
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

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "value mana",
        name = "Value Mana",
        nameZh = "数值法力",
        description = "Stores the target player's current mana as a value under a given key for the caster",
        descriptionZh = "把第一个目标（必须是玩家，否则返回 false）的法力值存入施法者 cast data。注意代码缺陷：switch 分支全部缺少 break，四种类型都会一路穿透到 default，实际写入的永远是“当前法力”，type 选项目前不生效。")
public class ValueManaMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "cast data 里的键名，支持 {uuid} 替换为施法者 UUID。存入 Double。",
            defaultValue = "value")
    private static final String KEY  = "key";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] Current provides the mana the target has, max provides their total mana, missing provides how much mana they have lost, and percent is the ratio of health to total mana.",
            tooltipZh = "本意为 Current 当前法力、Max 最大法力、Missing 已损失法力、Percent 当前/最大比值；但由于 switch 缺少 break，当前无论选哪个都只会写入当前法力值。",
            options = {"Current", "Max", "Missing", "Percent"},
            optionsZh = {"可选值1", "最大值", "可选值3", "可选值4"},
            defaultValue = "Current")
    private static final String TYPE = "type";

    @Override
    public String getKey() {
        return "value mana";
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
        if (!(targets.get(0) instanceof Player)) return false;

        final PlayerData player = SkillAPI.getPlayerData((Player)targets.get(0));
        final String key = settings.getString(KEY, "").replace("{uuid}", caster.getUniqueId().toString());
        final String type = settings.getString(TYPE, "current").toLowerCase();
        final HashMap<String, Object> data = DynamicSkill.getCastData(caster);

        switch (type) {
            case "max":
                data.put(key, player.getMaxMana());
            case "percent":
                data.put(key, player.getMana() / player.getMaxMana());
            case "missing":
                data.put(key, player.getMaxMana() - player.getMana());
            default: // current
                data.put(key, player.getMana());
        }
        return true;
    }
}
