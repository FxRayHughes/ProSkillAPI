package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.trigger.BlockBreakTrigger
 */
@SkillNode(
        key = "LAUNCH",
        name = "Launch",
        nameZh = "发射时",
        description = "Applies skill effects when a player launches a projectile.",
        descriptionZh = "玩家（或其他生物）发射弹射物时触发，施法者与初始目标都是发射者。弹射物的发射者不是生物时（发射器、刷怪笼等打出的箭）拿不到施法者，直接不触发。发射瞬间的速度大小写入 api-velocity，可用于生成同速的替代弹射物。",
        container = true)
public class LaunchTrigger implements Trigger<ProjectileLaunchEvent> {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of projectile that should be launched.",
            tooltipZh = "限定弹射物类型。Any 表示不限；否则直接与 Bukkit 的 EntityType 名做忽略大小写比较。这里没有像别的节点那样把空格换成下划线，所以带空格的选项（Fishing Hook、Ender Pearl、Thrown Exp Bottle 等）永远匹配不上，只有单词型的（Arrow、Egg、Snowball、Fireball）能生效；要筛多词类型请直接手写下划线形式（FISHING_HOOK）。",
            options = {"Any", "Arrow", "Egg", "Ender Pearl", "Fireball", "Fishing Hook", "Snowball"},
            optionsZh = {"任意", "箭", "可选值3", "可选值4", "可选值5", "可选值6", "可选值7"},
            defaultValue = "Any")
    private static final String TYPE = "type";


    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "LAUNCH";
    }

    /** {@inheritDoc} */
    @Override
    public Class<ProjectileLaunchEvent> getEvent() {
        return ProjectileLaunchEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final ProjectileLaunchEvent event, final int level, final Settings settings) {
        final String type = settings.getString(TYPE, "any");
        return type.equalsIgnoreCase("ANY") || type.equalsIgnoreCase(event.getEntity().getType().name());
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final ProjectileLaunchEvent event, final Map<String, Object> data) {
        data.put("api-velocity", event.getEntity().getVelocity().length());
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof LivingEntity) {
            return (LivingEntity) event.getEntity().getShooter();
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final ProjectileLaunchEvent event, final Settings settings) {
        return getCaster(event);
    }
}
