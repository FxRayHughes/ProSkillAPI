/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.DisguiseMechanic
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

import com.sucy.skill.api.util.FlagManager;
import com.sucy.skill.dynamic.TempEntity;
import com.sucy.skill.hook.DisguiseHook;
import com.sucy.skill.hook.PluginChecker;
import com.sucy.skill.listener.MechanicListener;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Disguises each target
 */
@SkillNode(
        key = "disguise",
        name = "Disguise",
        nameZh = "伪装",
        description = "Disguises each target according to the settings. This mechanic requires the LibsDisguise plugin to be installed on your server.",
        descriptionZh = "借 LibsDisguises 把每个目标伪装成生物、玩家或杂项实体。服务端没装/没启用 LibsDisguises 时直接返回 false，「类型」填了三种之外的值也返回 false。插件内部生成的临时实体（TempEntity）一律跳过。伪装时长通过给目标挂标记来控制，填负数则不安排到期、伪装永久保留直到别的逻辑解除。",
        requiresPlugins = {"LibsDisguises"})
public class DisguiseMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of disguise to use, as defined by the LibsDisguise plugin.",
            tooltipZh = "伪装的大类：Mob 生物、Player 玩家、Misc 杂项实体。三者分别读取下面对应的「生物」「玩家」「其他实体」项，填其他值则节点不执行。",
            options = {"Mob", "Player", "Misc"},
            optionsZh = {"生物", "玩家", "其他实体"},
            defaultValue = "Mob")
    private static final String TYPE     = "type";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Mob",
            labelZh = "生物",
            tooltip = "[mob] The type of mob to disguise the target as",
            tooltipZh = "伪装成的生物种类，取值由 LibsDisguises 定义。仅在类型为 Mob 时生效。",
            options = {"Bat", "Blaze", "Cave Spider", "Chicken", "Cow", "Creeper", "Donkey", "Elder Guardian", "Ender Dragon", "Enderman", "Endermite", "Ghast", "Giant", "Guardian", "Horse", "Iron Golem", "Magma Cube", "Mule", "Mushroom Cow", "Ocelot", "Pig", "Pig Zombie", "Rabbit", "Sheep", "Shulker", "Silverfish", "Skeleton", "Slime", "Snowman", "Spider", "Squid", "Undead Horse", "Villager", "Witch", "Wither", "Wither Skeleton", "Wolf", "Zombie", "Zombie Villager"},
            optionsZh = {"实体类型1", "实体类型2", "实体类型3", "实体类型4", "实体类型5", "苦力怕", "实体类型7", "实体类型8", "实体类型9", "实体类型10", "实体类型11", "实体类型12", "实体类型13", "实体类型14", "实体类型15", "实体类型16", "实体类型17", "实体类型18", "实体类型19", "实体类型20", "实体类型21", "僵尸", "实体类型23", "实体类型24", "实体类型25", "实体类型26", "骷髅", "实体类型28", "实体类型29", "实体类型30", "实体类型31", "实体类型32", "实体类型33", "实体类型34", "实体类型35", "骷髅", "实体类型37", "僵尸", "僵尸"},
            defaultValue = "Zombie")
    private static final String MOB      = "mob";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Adult",
            labelZh = "成年",
            tooltip = "[adult] Whether or not to use the adult variant of the mob",
            tooltipZh = "是否使用成年体模型。为 false 则伪装成幼年体。仅在类型为 Mob 时生效。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String ADULT    = "adult";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Player",
            labelZh = "玩家",
            tooltip = "[player] The player to disguise the target as",
            tooltipZh = "伪装成的玩家名，其中 {player} 会替换成施法者名字。仅在类型为 Player 时生效。",
            defaultValue = "Eniripsa96")
    private static final String PLAYER   = "player";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Misc",
            labelZh = "其他实体",
            tooltip = "[misc] The object to disguise the target as",
            tooltipZh = "伪装成的杂项实体（盔甲架、掉落物、矿车这类）。仅在类型为 Misc 时生效。",
            options = {"Area Effect Cloud", "Armor Stand", "Arrow", "Boat", "Dragon Fireball", "Dropped Item", "Egg", "Ender Crystal", "Ender Pearl", "Ender Signal", "Experience Orb", "Falling Block", "Fireball", "Firework", "Fishing Hook", "Item Frame", "Leash Hitch", "Minecart", "Minecart Chest", "Minecart Command", "Minecart Furnace", "Minecart Hopper", "Minecart Mob Spawner", "Minecart TNT", "Painting", "Primed TNT", "Shulker Bullet", "Snowball", "Spectral Arrow", "Splash Potion", "Tipped Arrow", "Thrown EXP Bottle", "Wither Skull"},
            optionsZh = {"云雾", "可选值2", "箭", "可选值4", "可选值5", "物品", "可选值7", "可选值8", "可选值9", "可选值10", "可选值11", "可选值12", "可选值13", "可选值14", "可选值15", "物品", "可选值17", "可选值18", "可选值19", "可选值20", "可选值21", "可选值22", "可选值23", "可选值24", "可选值25", "可选值26", "可选值27", "可选值28", "箭", "药水", "箭", "可选值32", "可选值33"},
            defaultValue = "Painting")
    private static final String MISC     = "misc";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Data",
            labelZh = "数据值",
            tooltip = "[data] Data value to use for the disguise type. What it does depends on the disguise",
            tooltipZh = "杂项伪装的附加数据值，具体含义随伪装类型而定（例如掉落物的物品种类）。仅在类型为 Misc 时生效。",
            defaultValue = "0")
    private static final String DATA     = "data";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Duration",
            labelZh = "持续时间",
            tooltip = "[duration] How long to apply the disguise for in seconds. Use a negative number to permanently disguise the targets.",
            tooltipZh = "伪装持续秒数，随技能等级缩放，内部乘 20 换算成 tick。默认值为 -1，即不自动解除、永久伪装。")
    private static final String DURATION = "duration";

    @Override
    public String getKey() {
        return "disguise";
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
        if (!PluginChecker.isDisguiseActive()) { return false; }

        String type = settings.getString(TYPE, "");

        // Mob disguises
        if (type.equalsIgnoreCase("mob")) {
            for (LivingEntity target : targets) {
                if (!(target instanceof TempEntity)) {
                    DisguiseHook.disguiseMob(target, settings.getString(MOB, "Zombie"), settings.getBool(ADULT, true));
                }
            }
        }

        // Player disguises
        else if (type.equalsIgnoreCase("player")) {
            for (LivingEntity target : targets) {
                if (!(target instanceof TempEntity)) {
                    DisguiseHook.disguisePlayer(
                            target,
                            settings.getString(PLAYER, "Eniripsa96").replace("{player}", caster.getName()));
                }
            }
        }

        // Miscellaneous disguises
        else if (type.equalsIgnoreCase("misc")) {
            for (LivingEntity target : targets) {
                if (!(target instanceof TempEntity)) {
                    DisguiseHook.disguiseMisc(target, settings.getString(MISC, "Painting"), settings.getInt(DATA, 0));
                }
            }
        }

        // Invalid type
        else { return false; }

        // Apply Flag duration
        int ticks = (int) (parseValues(caster, DURATION, level, -1) * 20);
        for (LivingEntity target : targets) {
            if (!(target instanceof TempEntity)) { FlagManager.addFlag(target, MechanicListener.DISGUISE_KEY, ticks); }
        }

        return targets.size() > 0;
    }
}
