package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.event.player.PlayerFishEvent;

/** Distinguishes this fishing result from the other hook phases. */
@SkillNode(key = "FISH_FAILED", name = "FishFailed", nameZh = "钓鱼失败时",
        descriptionZh = "按原版钓鱼状态过滤，并写入钩位置与捕获对象信息。", container = true)
public final class FishFailedTrigger extends FishingPhaseTrigger {
    @Override public String getKey() { return "FISH_FAILED"; }
    @Override protected boolean phase(PlayerFishEvent event) { return "FAILED_ATTEMPT".equals(event.getState().name()); }
}
