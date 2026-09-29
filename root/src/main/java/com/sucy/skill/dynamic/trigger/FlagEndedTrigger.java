package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.FlagExpireEvent;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/** The existing expiry event is already post-removal and has the required reason. */
@SkillNode(key = "FLAG_ENDED", name = "Flag Ended", nameZh = "标记结束后",
        descriptionZh = "标记已移除；提供原因与标记名。", container = true)
public final class FlagEndedTrigger implements Trigger<FlagExpireEvent> {
    @SkillField(kind = FieldKind.StringValue, label = "Flag", labelZh = "标记名")
    private static final String FLAG = "flag";
    @Override public String getKey() { return "FLAG_ENDED"; }
    @Override public Class<FlagExpireEvent> getEvent() { return FlagExpireEvent.class; }
    @Override public boolean shouldTrigger(FlagExpireEvent event, int level, Settings settings) {
        String name = settings.getString(FLAG, "");
        return name.isEmpty() || name.equals(event.getFlag());
    }
    @Override public LivingEntity getCaster(FlagExpireEvent event) { return event.getEntity(); }
    @Override public LivingEntity getTarget(FlagExpireEvent event, Settings settings) { return event.getEntity(); }
    @Override public void setValues(FlagExpireEvent event, Map<String, Object> data) {
        data.put("event-name", event.getFlag());
        data.put("event-source", event.getReason().name());
    }
}
