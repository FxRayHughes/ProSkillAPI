/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.WolfMechanic
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

import com.rit.sucy.text.TextFormatter;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.PassiveSkill;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.listener.MechanicListener;
import com.sucy.skill.task.RemoveTask;
import org.bukkit.DyeColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a flag to each target
 */
@SkillNode(
        key = "wolf",
        name = "Wolf",
        nameZh = "狼",
        description = "Summons a wolf on each target for a duration. Child components will start off targeting the wolf so you can add effects to it. You can also give it its own skillset, though Cast triggers will not occur.",
        descriptionZh = "在每个目标位置召唤若干只归施法者所有的狼，持续指定秒数后自动移除，子节点会以这些狼作为目标继续执行。要求施法者是玩家，否则返回 false。每次执行会先取消并立即结算上一次该施法者的移除任务，即重复施放会先清掉上一批狼。狼的 cast data 中写入 api-owner（内容为施法者的单元素列表），伤害通过 SUMMON_DAMAGE 元数据附加。一只狼都没生成时返回 false。",
        container = true)
public class WolfMechanic extends MechanicComponent {
    public static final  String SKILL_META = "sapi_wolf_skills";
    public static final  String LEVEL      = "sapi_wolf_level";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Collar Color",
            labelZh = "项圈颜色",
            tooltip = "[color] The color of the collar that the wolf should wear",
            tooltipZh = "狼项圈颜色，需与 Bukkit DyeColor 枚举名完全一致（全大写，如 BLACK、RED）。匹配失败会被静默忽略、保持默认项圈；注意默认值写的是首字母大写的 Black，因大小写不符实际不会生效。",
            options = {"BLACK", "BLUE", "BROWN", "CYAN", "GRAY", "GREEN", "LIGHT_BLUE", "LIGHT_GRAY", "LIME", "MAGENTA", "ORANGE", "PINK", "PURPLE", "RED", "WHITE", "YELLOW"},
            optionsZh = {"可选值1", "可选值2", "可选值3", "可选值4", "可选值5", "可选值6", "可选值7", "可选值8", "可选值9", "可选值10", "可选值11", "可选值12", "可选值13", "可选值14", "可选值15", "可选值16"},
            defaultValue = "Black")
    private static final String COLOR      = "color";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Health",
            labelZh = "生命值",
            tooltip = "[health] The starting health of the wolf",
            tooltipZh = "狼的初始生命与最大生命，随技能等级缩放（base + scale×(等级-1)）。默认 10。")
    private static final String HEALTH     = "health";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Duration",
            labelZh = "持续时间",
            tooltip = "[seconds] How long to summon the wolf for",
            tooltipZh = "狼的存活时间，单位秒，内部乘 20 转成 tick；随技能等级缩放。默认 10。到时由移除任务清除。")
    private static final String SECONDS    = "seconds";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Wolf Name",
            labelZh = "狼的名称",
            tooltip = "[name] The displayed name of the wolf. Use {player} to embed the caster's name.",
            tooltipZh = "狼的显示名，支持颜色代码，{player} 会替换成施法者名字。非空时同时开启名字常显。默认 {player}'s Wolf。",
            defaultValue = "{player}'s Wolf")
    private static final String NAME       = "name";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Damage",
            labelZh = "伤害",
            tooltip = "[damage] The damage dealt by the wolf each attack",
            tooltipZh = "狼每次攻击造成的伤害，随技能等级缩放。默认 3。以召唤物伤害元数据形式附加，而非修改原版攻击属性。")
    private static final String DAMAGE     = "damage";
    @SkillField(
            kind = FieldKind.StringListValue,
            label = "Skills (one per line)",
            labelZh = "技能列表",
            tooltip = "[skills] The skills to give the wolf. Skills are executed at the level of the skill summoning the wolf. Skills needing a Cast trigger will not work.",
            tooltipZh = "赋予狼的技能名列表，每行一个。只有被动技能（PassiveSkill）会被真正初始化，技能等级沿用召唤它的技能等级；依赖施法触发器的技能不会生效。",
            defaultValue = "")
    private static final String SKILLS     = "skills";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Amount",
            labelZh = "数量",
            tooltip = "[amount] How many wolves to summon",
            tooltipZh = "每个目标位置召唤的狼数量，随技能等级缩放。默认 1。多目标时总数为目标数 × 该值。")
    private static final String AMOUNT     = "amount";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Sitting",
            labelZh = "坐下",
            tooltip = "[PREMIUM] whether or not the wolf starts of sitting",
            tooltipZh = "狼是否以坐下状态出生。默认 False。（原提示标注为 PREMIUM，但当前代码已实现该项。）",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String SITTING    = "sitting";

    private final Map<Integer, RemoveTask> tasks = new HashMap<>();

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
        if (!(caster instanceof Player)) {
            return false;
        }

        cleanUp(caster);

        final Player player = (Player) caster;

        String color = settings.getString(COLOR);
        double health = parseValues(player, HEALTH, level, 10.0);
        String name = TextFormatter.colorString(settings.getString(NAME, "").replace("{player}", player.getName()));
        double damage = parseValues(player, DAMAGE, level, 3.0);
        double amount = parseValues(player, AMOUNT, level, 1.0);
        boolean sitting = settings.getString(SITTING, "false").equalsIgnoreCase("true");
        List<String> skills = settings.getStringList(SKILLS);

        DyeColor dye = null;
        if (color != null) {
            try {
                dye = DyeColor.valueOf(color);
            } catch (Exception ex) { /* Invalid color */ }
        }

        double seconds = parseValues(player, SECONDS, level, 10.0);
        int ticks = (int) (seconds * 20);
        List<LivingEntity> wolves = new ArrayList<>();
        for (LivingEntity target : targets) {
            for (int i = 0; i < amount; i++) {
                Wolf wolf = target.getWorld().spawn(target.getLocation(), Wolf.class);
                wolf.setOwner(player);
                wolf.setMaxHealth(health);
                wolf.setHealth(health);
                wolf.setSitting(sitting);
                SkillAPI.setMeta(wolf, MechanicListener.SUMMON_DAMAGE, damage);

                List<LivingEntity> owner = new ArrayList<>(1);
                owner.add(player);
                DynamicSkill.getCastData(wolf).put("api-owner", owner);

                if (dye != null) {
                    wolf.setCollarColor(dye);
                }
                if (name.length() > 0) {
                    wolf.setCustomName(name);
                    wolf.setCustomNameVisible(true);
                }

                // Setup skills
                for (String skillName : skills) {
                    Skill skill = SkillAPI.getSkill(skillName);
                    if (skill instanceof PassiveSkill) {
                        ((PassiveSkill) skill).initialize(wolf, level);
                    }
                }
                SkillAPI.setMeta(wolf, SKILL_META, skills);
                SkillAPI.setMeta(wolf, LEVEL, level);

                wolves.add(wolf);
            }
        }

        final RemoveTask task = new RemoveTask(wolves, ticks);
        tasks.put(caster.getEntityId(), task);

        // Apply children to the wolves
        if (wolves.size() > 0) {
            executeChildren(player, level, wolves);
            return true;
        }
        return false;
    }

    @Override
    public String getKey() {
        return "wolf";
    }

    @Override
    public void cleanUp(final LivingEntity caster) {
        final RemoveTask task = tasks.remove(caster.getEntityId());
        if (task != null) {
            task.cancel();
            task.run();
        }
    }
}
