package com.sucy.skill.dynamic.target;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/** Selects the event's initiating entity instead of assuming it is the caster. */
@SkillNode(key = "event actor", name = "Event Actor", nameZh = "事件发起者",
        descriptionZh = "使用事件上下文中的发起者；缺失或失效时不产生目标。", container = true)
public final class EventActorTarget extends TargetComponent {
    @Override public String getKey() { return "event actor"; }
    @Override List<LivingEntity> getTargets(LivingEntity caster, int level, List<LivingEntity> targets) {
        Object raw = DynamicSkill.getCastData(caster).get("event-actor");
        if (!(raw instanceof LivingEntity)) return Collections.emptyList();
        LivingEntity actor = (LivingEntity) raw;
        return actor.isValid() && SkillAPI.getSettings().isValidTarget(actor)
                ? Collections.singletonList(actor) : Collections.emptyList();
    }
    @Override void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) { }
}
