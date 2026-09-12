package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.PlayerLandEvent;
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
        key = "LAND",
        name = "Land",
        nameZh = "落地时",
        description = "Applies skill effects when a player lands on the ground.",
        descriptionZh = "玩家落地时触发，施法者与初始目标都是这名玩家。落地距离不是原版摔落高度，而是插件自己按移动事件记录的“腾空期间最高点 Y 减去落地点 Y”，因此平地起跳也会算出约 1.25 的距离，做落地特效时阈值要设得比这个高。距离写入 api-distance。挂着 NPC 标记的假人不触发。",
        container = true)
public class LandTrigger implements Trigger<PlayerLandEvent> {
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Min Distance",
            labelZh = "最小距离",
            tooltip = "[min-distance] The minimum distance the player should fall before effects activating.",
            tooltipZh = "触发所需的最小下落距离（方块），实际距离小于该值就不触发。默认 0 意味着连普通起跳都会触发；要只对真正的高空坠落生效，填 2 以上更稳妥。",
            defaultValue = "0")
    private static final String MIN_DISTANCE = "min-distance";


    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "LAND";
    }

    /** {@inheritDoc} */
    @Override
    public Class<PlayerLandEvent> getEvent() {
        return PlayerLandEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final PlayerLandEvent event, final int level, final Settings settings) {
        final double minDistance = settings.getDouble(MIN_DISTANCE, 0);
        return event.getDistance() >= minDistance;
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final PlayerLandEvent event, final Map<String, Object> data) {
        data.put("api-distance", event.getDistance());
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final PlayerLandEvent event) {
        return event.getPlayer();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final PlayerLandEvent event, final Settings settings) {
        return event.getPlayer();
    }
}
