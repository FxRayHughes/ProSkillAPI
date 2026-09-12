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
        key = "SKILL_DAMAGE",
        name = "Skill Damage",
        nameZh = "技能伤害时",
        description = "Applies skill effects when a player deals damage with a skill.",
        descriptionZh = "持有该技能者用技能造成伤害时触发，施法者是造成伤害的一方。本次伤害写入 api-dealt；技能执行完后节点里的即时增益会回写到伤害上，可用来做技能增伤。要注意它对“技能伤害”一律生效、包含本技能自己打出的伤害，不过同一触发节点在执行期间有重入保护，不会被自己无限套娃，但多个节点相互串联仍可能形成连锁。",
        container = true)
public class SkillDealtTrigger extends SkillTrigger {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Target Caster",
            labelZh = "以施法者为目标",
            tooltip = "[target] True makes children target the caster. False makes children target the damaged entity",
            tooltipZh = "决定初始目标是谁，字段名容易误解——True（默认）是以施法者自己为目标（适合叠自身增益），False 才是以被技能命中的生物为目标（适合追加debuff）。",
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
            tooltipZh = "限定技能伤害的分类，需与技能设置的分类字符串完全一致且区分大小写。列表为空或首项为空字符串时视为不限；插件未指定分类时用的是 default。",
            defaultValue = "default")
    private static final String CATEGORY = "category";


    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "SKILL_DAMAGE";
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final SkillDamageEvent event) {
        return event.getDamager();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final SkillDamageEvent event, final Settings settings) {
        return isUsingTarget(settings) ? event.getTarget() : event.getDamager();
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final SkillDamageEvent event, final Map<String, Object> data) {
        data.put("api-dealt", event.getDamage());
    }
}
