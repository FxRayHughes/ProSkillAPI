package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.trigger.BlockBreakTrigger
 */
@SkillNode(
        key = "MOVE",
        name = "Move",
        nameZh = "移动时",
        description = "Applies skill effects when a player moves around. This triggers every tick the player is moving, so use this sparingly. Use the \"api-moved\" value to check/use the distance traveled.",
        descriptionZh = "玩家移动时触发，施法者与初始目标都是这名玩家。移动事件的频率极高（几乎每个移动包一次，仅转动视角也会发），务必配合冷却或条件节点使用，否则很容易拖垮服务器。仅在起点与终点处于同一世界时触发（跨世界传送时计算距离会抛异常，故直接跳过）。本次位移距离写入 api-distance。",
        container = true)
public class MoveTrigger implements Trigger<PlayerMoveEvent> {

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "MOVE";
    }

    /** {@inheritDoc} */
    @Override
    public Class<PlayerMoveEvent> getEvent() {
        return PlayerMoveEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final PlayerMoveEvent event, final int level, final Settings settings) {
        return event.getFrom().getWorld() == event.getTo().getWorld();
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final PlayerMoveEvent event, final Map<String, Object> data) {
        final double distance = event.getTo().distance(event.getFrom());
        data.put("api-distance", distance);
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final PlayerMoveEvent event) {
        return event.getPlayer();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final PlayerMoveEvent event, final Settings settings) {
        return event.getPlayer();
    }
}
