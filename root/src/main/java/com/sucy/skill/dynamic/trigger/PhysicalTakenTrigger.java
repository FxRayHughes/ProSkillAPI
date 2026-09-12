package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.PhysicalDamageEvent;
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
        key = "TOOK_PHYSICAL_DAMAGE",
        name = "Took Physical Damage",
        nameZh = "受到物理伤害时",
        description = "Applies skill effects when a player takes physical (or non-skill) damage. This includes melee attacks and projectiles not fired by a skill.",
        descriptionZh = "持有该技能者受到非技能伤害（近战、弹射物等）时触发，施法者是受伤方。只处理生物造成的伤害，技能伤害、CUSTOM 原因、伤害不大于 0 的都被排除，攻击来源是发射器等非生物时也不触发。本次伤害写入 api-taken；技能执行完后节点里的即时增益会回写到本次伤害上，因此可用来做减伤、护盾。",
        container = true)
public class PhysicalTakenTrigger extends PhysicalTrigger {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Target Caster",
            labelZh = "以施法者为目标",
            tooltip = "[target] True makes children target the caster. False makes children target the attacking entity",
            tooltipZh = "决定初始目标是谁，字段名容易误解——True（默认）是以受伤者自己为目标（适合做减伤、回血、护盾），False 才是以攻击者为目标（适合做反伤、反击）。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String TARGET = "target";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of damage dealt",
            tooltipZh = "限定受到的攻击方式：Both 全都算，Projectile 只算被弹射物命中，Melee 只算近战。代码只显式判断 projectile，其余取值都会落到近战分支，填错会静默变成只认近战。",
            options = {"Both", "Melee", "Projectile"},
            optionsZh = {"两者", "可选值2", "投射物"},
            defaultValue = "Both")
    private static final String TYPE = "type";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Min Damage",
            labelZh = "最小伤害",
            tooltip = "[dmg-min] The minimum damage that needs to be dealt",
            tooltipZh = "本次伤害的下限，低于该值不触发。默认 0。",
            defaultValue = "0")
    private static final String DMG_MIN = "dmg-min";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Max Damage",
            labelZh = "最大伤害",
            tooltip = "[dmg-max] The maximum damage that needs to be dealt",
            tooltipZh = "本次伤害的上限，高于该值不触发。键缺失时代码按 0 处理，会让技能永不触发；编辑器预填 999，手写 YAML 必须显式写上。",
            defaultValue = "999")
    private static final String DMG_MAX = "dmg-max";


    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "TOOK_PHYSICAL_DAMAGE";
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final PhysicalDamageEvent event) {
        return event.getTarget();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final PhysicalDamageEvent event, final Settings settings) {
        return isUsingTarget(settings) ? event.getDamager() : event.getTarget();
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final PhysicalDamageEvent event, final Map<String, Object> data) {
        data.put("api-taken", event.getDamage());
    }
}
