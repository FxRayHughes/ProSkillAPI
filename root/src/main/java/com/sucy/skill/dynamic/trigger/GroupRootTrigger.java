package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.SkillOutcomeEvent;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/** Metadata descriptor only: DynamicSkill loads GROUP roots without a Bukkit listener. */
@SkillNode(key = "GROUP", name = "GROUP", nameZh = "共享组",
        descriptionZh = "根级共享函数，可由 run group 节点或指向本卡片的连线调用。", container = true)
public final class GroupRootTrigger implements Trigger<SkillOutcomeEvent> {
    @SkillField(kind = FieldKind.StringValue, label = "Group Key", labelZh = "共享组键", defaultValue = "group")
    private static final String GROUP = "group";
    @Override public String getKey() { return "GROUP"; }
    @Override public Class<SkillOutcomeEvent> getEvent() { return SkillOutcomeEvent.class; }
    @Override public boolean shouldTrigger(SkillOutcomeEvent event, int level, Settings settings) { return false; }
    @Override public void setValues(SkillOutcomeEvent event, Map<String, Object> data) { }
    @Override public LivingEntity getCaster(SkillOutcomeEvent event) { return null; }
    @Override public LivingEntity getTarget(SkillOutcomeEvent event, Settings settings) { return null; }
}
