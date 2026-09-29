package com.sucy.skill.dynamic.target;

import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/** Targets the current synchronous signal's sender while its context is on the stack. */
@SkillNode(key = "signal sender", name = "Signal Sender", nameZh = "信号发送者",
        descriptionZh = "仅在信号接收分支内有效，发送者失效时不产生目标。", container = true)
public final class SignalSenderTarget extends TargetComponent {
    @Override public String getKey() { return "signal sender"; }
    @Override List<LivingEntity> getTargets(LivingEntity caster, int level, List<LivingEntity> targets) {
        Object value = DynamicSkill.getCastData(caster).get("signal-sender");
        if (!(value instanceof LivingEntity)) return Collections.emptyList();
        LivingEntity sender = (LivingEntity) value;
        return sender.isValid() && isAllowed(sender) ? Collections.singletonList(sender) : Collections.emptyList();
    }
    /** Keep the global validity contract even when the sender is the current caster. */
    private boolean isAllowed(LivingEntity sender) {
        return com.sucy.skill.SkillAPI.getSettings().isValidTarget(sender);
    }
    @Override void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) { }
}
