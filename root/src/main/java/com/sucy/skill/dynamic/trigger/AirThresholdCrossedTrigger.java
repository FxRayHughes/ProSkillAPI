package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;

/** Emits once when the configured off-ground movement sample count is reached. */
@SkillNode(key = "AIR_THRESHOLD_CROSSED", name = "Air Threshold Crossed", nameZh = "离地时长达到阈值",
        descriptionZh = "玩家持续离地移动达到空气时长阈值时触发一次；重新着地后计数清零。阈值按移动采样次数计算，不主动创建定时任务。", container = true)
public final class AirThresholdCrossedTrigger extends StateTrigger {
    @SkillField(kind = FieldKind.IntValue, label = "Air Threshold", labelZh = "空气时长阈值",
            tooltipZh = "按移动采样次数计数，达到阈值时只触发一次；重新着地后计数清零。", defaultValue = "10")
    private static final String THRESHOLD = AIR_THRESHOLD;
    @Override public String getKey() { return "AIR_THRESHOLD_CROSSED"; }
}
