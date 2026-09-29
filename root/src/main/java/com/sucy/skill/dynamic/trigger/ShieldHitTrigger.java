package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.combat.shield.ShieldEvent;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/** Routes committed absorption to the shielded owner's active skill. */
@SkillNode(key = "SHIELD_HIT", name = "Shield Hit", nameZh = "护盾吸收伤害时",
        descriptionZh = "护盾实际扣减容量后触发；提供本层吸收量与剩余容量。", container = true)
public class ShieldHitTrigger implements Trigger<ShieldEvent> {
    @SkillField(kind = FieldKind.StringValue, label = "Layer Key", labelZh = "护盾键",
            tooltipZh = "留空监听任意护盾层。")
    private static final String KEY = "key";
    @Override public String getKey() { return "SHIELD_HIT"; }
    @Override public Class<ShieldEvent> getEvent() { return ShieldEvent.class; }
    @Override public boolean shouldTrigger(ShieldEvent event, int level, Settings settings) {
        String key = settings.getString(KEY, "");
        return event.getKind() == ShieldEvent.Kind.ABSORBED && (key.isEmpty() || key.equals(event.getKey()));
    }
    @Override public LivingEntity getCaster(ShieldEvent event) { return event.getTarget(); }
    @Override public LivingEntity getTarget(ShieldEvent event, Settings settings) { return event.getTarget(); }
    @Override public void setValues(ShieldEvent event, Map<String, Object> data) {
        data.put("shield-absorbed", event.getAmount());
        data.put("shield-remaining", event.getRemaining());
        data.put("shield-key", event.getKey());
        data.put("shield-source-id", event.getSourceId() == null ? "" : event.getSourceId().toString());
    }
}
