package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A per-node cooldown gate for author-controlled hot paths such as projectile ticks.
 *
 * <p>The map is local to this component instance and uses the configured
 * caster, target, or channel as its key. Only targets whose interval has
 * elapsed are forwarded to child mechanics. The map is bounded by stale-entry
 * cleanup, and caster cleanup removes the common caster key when a dynamic
 * execution is torn down; this gate is not a replacement for a global rate
 * limiter or a persistent cooldown.</p>
 */
@SkillNode(key = "flow throttle", name = "Flow Throttle", nameZh = "流程限流",
        descriptionZh = "按施法者、目标或频道限流；仅通过的目标进入子节点。", container = true)
public final class FlowThrottleMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.ListValue, label = "Key Mode", labelZh = "限流维度",
            options = {"caster", "target", "channel"}, optionsZh = {"施法者", "目标", "频道"}, defaultValue = "caster")
    private static final String MODE = "mode";
    @SkillField(kind = FieldKind.AttributeValue, label = "Seconds", labelZh = "间隔秒数",
            tooltipZh = "每个限流键两次通过的最短间隔。", defaultValue = "1")
    private static final String SECONDS = "seconds";
    @SkillField(kind = FieldKind.StringValue, label = "Channel", labelZh = "频道",
            tooltipZh = "频道模式使用本字段分组。", defaultValue = "default")
    private static final String CHANNEL = "channel";
    /** Last successful pass time for each configured throttle key. */
    private final Map<String, Long> last = new HashMap<>();
    /** @return legacy component key used by dynamic skill loading */
    @Override public String getKey() { return "flow throttle"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double seconds = parseValues(caster, SECONDS, level, 1);
        if (!Double.isFinite(seconds) || seconds < 0) return false;
        long window = Math.round(seconds * 1000);
        long now = System.currentTimeMillis();
        String mode = settings.getString(MODE, "caster");
        List<LivingEntity> accepted = new ArrayList<>();
        for (LivingEntity target : targets) {
            String key = "target".equals(mode) ? target.getUniqueId().toString()
                    : "channel".equals(mode) ? settings.getString(CHANNEL, "default")
                    : caster.getUniqueId().toString();
            Long previous = last.get(key);
            if (previous == null || now - previous >= window) {
                last.put(key, now);
                accepted.add(target);
            }
        }
        if (last.size() > 4096) last.entrySet().removeIf(entry -> now - entry.getValue() > 60000);
        return !accepted.isEmpty() && executeChildren(caster, level, accepted);
    }
    @Override protected void doCleanUp(LivingEntity caster) {
        last.remove(caster.getUniqueId().toString());
    }
}
