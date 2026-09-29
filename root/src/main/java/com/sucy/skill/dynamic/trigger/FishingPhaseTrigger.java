package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.Map;

/** Each fishing phase owns one key while sharing caster and catch payload rules. */
public abstract class FishingPhaseTrigger extends PlayerEventTrigger<PlayerFishEvent> {
    @SkillField(kind = FieldKind.StringValue, label = "Caught Type", labelZh = "捕获实体类型",
            tooltipZh = "留空不限；仅捕获阶段使用。")
    private static final String CAUGHT_TYPE = "caught-type";
    @Override public Class<PlayerFishEvent> getEvent() { return PlayerFishEvent.class; }
    @Override public boolean shouldTrigger(PlayerFishEvent event, int level, Settings settings) {
        if (!phase(event)) return false;
        String type = settings.getString(CAUGHT_TYPE, "");
        return type.isEmpty() || event.getCaught() != null
                && type.equalsIgnoreCase(event.getCaught().getType().name());
    }
    protected abstract boolean phase(PlayerFishEvent event);
    @Override public LivingEntity getTarget(PlayerFishEvent event, Settings settings) {
        return event.getCaught() instanceof LivingEntity ? (LivingEntity) event.getCaught() : event.getPlayer();
    }
    @Override public void setValues(PlayerFishEvent event, Map<String, Object> data) {
        super.setValues(event, data);
        Entity caught = event.getCaught();
        data.put("event-actor", event.getPlayer());
        data.put("event-source", event.getState().name());
        data.put("event-caught-type", caught == null ? "" : caught.getType().name());
        data.put("event-impact-location", event.getHook().getLocation().clone());
        if (caught instanceof Item) data.put("event-item-type", ((Item) caught).getItemStack().getType().name());
    }
}
