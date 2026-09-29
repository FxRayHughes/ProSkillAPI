package com.sucy.skill.dynamic.target;

import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.TempEntity;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/** Turns a recorded impact point into the existing temporary location target. */
@SkillNode(key = "event impact location", name = "Event Impact Location", nameZh = "事件命中位置",
        descriptionZh = "读取投射物或方块事件记录的位置，不主动加载区块。", container = true)
public final class EventImpactLocationTarget extends TargetComponent {
    @Override public String getKey() { return "event impact location"; }
    @Override List<LivingEntity> getTargets(LivingEntity caster, int level, List<LivingEntity> targets) {
        Object raw = DynamicSkill.getCastData(caster).get("event-impact-location");
        if (!(raw instanceof Location)) return Collections.emptyList();
        Location location = (Location) raw;
        if (location.getWorld() == null || !location.getWorld().isChunkLoaded(
                location.getBlockX() >> 4, location.getBlockZ() >> 4)) return Collections.emptyList();
        return Collections.singletonList(TempEntity.create(location.clone()));
    }
    @Override void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) { }
}
