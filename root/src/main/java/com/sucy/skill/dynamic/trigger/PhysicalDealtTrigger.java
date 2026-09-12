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
        key = "PHYSICAL_DAMAGE",
        name = "Physical Damage",
        nameZh = "物理伤害时",
        description = "Applies skill effects when a player deals physical (or non-skill) damage. This includes melee attacks and firing a bow.",
        descriptionZh = "持有该技能者造成非技能伤害（近战、射出的箭等）时触发，施法者是攻击方。走的是插件自己的物理伤害事件：技能造成的伤害、CUSTOM 原因的伤害、以及伤害值不大于 0 的都不算；伤害来源是发射器之类的非生物时也不触发。本次伤害写入 api-dealt；技能执行完后节点里的即时增益会回写到伤害上，可用来做增伤。",
        container = true)
public class PhysicalDealtTrigger extends PhysicalTrigger {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Target Caster",
            labelZh = "以施法者为目标",
            tooltip = "[target] True makes children target the caster. False makes children target the damaged entity",
            tooltipZh = "决定初始目标是谁，字段名容易误解——True（默认）是以施法者自己为目标（适合给自己叠增伤、加吸血），False 才是以被打的生物为目标（适合附加中毒、击退等debuff）。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String TARGET = "target";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of damage dealt",
            tooltipZh = "限定攻击方式：Both 全都算，Projectile 只算弹射物命中，Melee 只算近战。代码只显式判断 projectile，其余取值（包括拼错的）都会落到近战分支，所以填错不会报错而是静默变成只认近战。",
            options = {"Both", "Melee", "Projectile"},
            optionsZh = {"两者", "可选值2", "投射物"},
            defaultValue = "Both")
    private static final String TYPE = "type";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Min Damage",
            labelZh = "最小伤害",
            tooltip = "[dmg-min] The minimum damage that needs to be dealt",
            tooltipZh = "本次伤害的下限，低于该值不触发。默认 0，一般不用改。",
            defaultValue = "0")
    private static final String DMG_MIN = "dmg-min";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Max Damage",
            labelZh = "最大伤害",
            tooltip = "[dmg-max] The maximum damage that needs to be dealt",
            tooltipZh = "本次伤害的上限，高于该值不触发。键缺失时代码按 0 处理，会造成任何伤害都过不了判定、技能完全不触发；编辑器新建节点会预填 999，但手写 YAML 时必须显式写上这个键。",
            defaultValue = "999")
    private static final String DMG_MAX = "dmg-max";


    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "PHYSICAL_DAMAGE";
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final PhysicalDamageEvent event) {
        return event.getDamager();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final PhysicalDamageEvent event, final Settings settings) {
        return isUsingTarget(settings) ? event.getTarget() : event.getDamager();
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final PhysicalDamageEvent event, final Map<String, Object> data) {
        data.put("api-dealt", event.getDamage());
    }
}
