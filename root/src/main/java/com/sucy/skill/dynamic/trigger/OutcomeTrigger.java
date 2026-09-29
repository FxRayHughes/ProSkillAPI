package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.SkillOutcomeEvent;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/**
 * Shared adapter for post-commit outcomes.
 *
 * <p>Concrete trigger keys map one-to-one to {@link SkillOutcomeEvent.Phase}
 * values. Matching is intentionally performed against the phase name rather
 * than a second registry so adding a new outcome cannot silently route to an
 * existing trigger. The optional source-name field narrows the trigger to one
 * skill, flag, attribute, or other producer name.</p>
 */
public abstract class OutcomeTrigger implements Trigger<SkillOutcomeEvent> {
    @SkillField(kind = FieldKind.StringValue, label = "Source Name", labelZh = "来源名称",
            tooltipZh = "留空匹配所有来源；非空时匹配技能名、标记名或属性名。")
    private static final String NAME = "name";
    /** All outcome triggers consume the same immutable post-commit event. */
    @Override public Class<SkillOutcomeEvent> getEvent() { return SkillOutcomeEvent.class; }
    @Override public boolean shouldTrigger(SkillOutcomeEvent event, int level, Settings settings) {
        String filter = settings.getString(NAME, "");
        return event.getPhase().name().equals(getKey().replaceFirst("^SKILL_", ""))
                && (filter.isEmpty() || filter.equals(event.getName()));
    }
    /** The actor remains the caster so follow-up mechanics retain the original owner. */
    @Override public LivingEntity getCaster(SkillOutcomeEvent event) { return event.getActor(); }
    /** Uses the affected target when present and falls back to actor-only outcomes. */
    @Override public LivingEntity getTarget(SkillOutcomeEvent event, Settings settings) {
        return event.getTarget() == null ? event.getActor() : event.getTarget();
    }
    /** Exposes one stable event namespace for downstream conditions and mechanics. */
    @Override public void setValues(SkillOutcomeEvent event, Map<String, Object> data) {
        data.put("event-actor", event.getActor());
        data.put("event-name", event.getName());
        data.put("event-source", event.getSource());
        data.put("event-amount", event.getAmount());
        data.put("event-previous", event.getPrevious());
        data.put("event-current", event.getCurrent());
    }
}
