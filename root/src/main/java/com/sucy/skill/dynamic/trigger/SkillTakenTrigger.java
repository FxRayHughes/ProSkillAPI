package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.SkillDamageEvent;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.trigger.BlockBreakTrigger
 */
@SkillNode(
        key = "TOOK_SKILL_DAMAGE",
        name = "Took Skill Damage",
        nameZh = "受到技能伤害时",
        description = "Applies skill effects when a player takes damage from a skill other than their own.",
        descriptionZh = "持有该技能者受到技能伤害时触发，施法者是受伤方。本次伤害写入 api-taken；技能执行完后节点里的即时增益会回写到本次伤害上，可用来做对技能的减伤/魔抗。注意英文说明写的是“别人的技能”，但代码并没有排除伤害来源是自己的情况，自己技能溅到自己同样会触发。",
        container = true)
public class SkillTakenTrigger extends SkillTrigger {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Target Caster",
            labelZh = "以施法者为目标",
            tooltip = "[target] True makes children target the caster. False makes children target the attacking entity",
            tooltipZh = "决定初始目标是谁，字段名容易误解——True（默认）是以受伤者自己为目标（适合减伤、护盾），False 才是以放技能的一方为目标（适合反击）。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String TARGET = "target";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Min Damage",
            labelZh = "最小伤害",
            tooltip = "[dmg-min] The minimum damage that needs to be dealt",
            tooltipZh = "本次技能伤害的下限，低于该值不触发。默认 0。",
            defaultValue = "0")
    private static final String DMG_MIN = "dmg-min";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Max Damage",
            labelZh = "最大伤害",
            tooltip = "[dmg-max] The maximum damage that needs to be dealt",
            tooltipZh = "本次技能伤害的上限，高于该值不触发。键缺失时代码按 0 处理，会让技能永不触发；编辑器预填 999，手写 YAML 必须显式写上。",
            defaultValue = "999")
    private static final String DMG_MAX = "dmg-max";

    @SkillField(
            kind = FieldKind.StringListValue,
            label = "Category",
            labelZh = "类别",
            tooltip = "[category] The type of skill damage to apply for. Leave this empty to apply to all skill damage.",
            tooltipZh = "限定技能伤害的分类，需与来源技能的分类字符串完全一致且区分大小写。列表为空或首项为空字符串时视为不限；插件默认分类为 default。",
            defaultValue = "default")
    private static final String CATEGORY = "category";


    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "TOOK_SKILL_DAMAGE";
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final SkillDamageEvent event) {
        return event.getTarget();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final SkillDamageEvent event, final Settings settings) {
        return isUsingTarget(settings) ? event.getDamager() : event.getTarget();
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final SkillDamageEvent event, final Map<String, Object> data) {
        data.put("api-taken", event.getDamage());
    }
}
