package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.event.player.PlayerFishEvent;

/** Distinguishes this fishing result from the other hook phases. */
@SkillNode(key = "FISH_BITE", name = "FishBite", nameZh = "鱼咬钩时",
        descriptionZh = "按原版钓鱼状态过滤，并写入钩位置与捕获对象信息。", container = true)
public final class FishBiteTrigger extends FishingPhaseTrigger {
    @Override public String getKey() { return "FISH_BITE"; }
    @Override protected boolean phase(PlayerFishEvent event) { return "BITE".equals(event.getState().name()); }
}
