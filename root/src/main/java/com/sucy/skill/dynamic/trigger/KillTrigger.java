package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.trigger.BlockBreakTrigger
 */
@SkillNode(
        key = "KILL",
        name = "Kill",
        nameZh = "击杀时",
        description = "Applies skill effects upon killing something",
        descriptionZh = "持有该技能的玩家击杀生物时触发，施法者与初始目标都是击杀者本人，被杀死的生物不会作为目标传下去。底层取的是死亡实体的“击杀者”，该值只可能是玩家，因此怪物或环境造成的死亡不触发；要以死者视角做效果请用“死亡时”。本节点没有任何过滤条件，杀任何生物都会触发。",
        container = true)
public class KillTrigger implements Trigger<EntityDeathEvent> {

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "KILL";
    }

    /** {@inheritDoc} */
    @Override
    public Class<EntityDeathEvent> getEvent() {
        return EntityDeathEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final EntityDeathEvent event, final int level, final Settings settings) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final EntityDeathEvent event, final Map<String, Object> data) { }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final EntityDeathEvent event) {
        return event.getEntity().getKiller();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final EntityDeathEvent event, final Settings settings) {
        return event.getEntity().getKiller();
    }
}
