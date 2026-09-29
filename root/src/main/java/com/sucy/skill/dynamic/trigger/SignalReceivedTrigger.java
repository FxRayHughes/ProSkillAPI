package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.signal.SignalEvent;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/** A signal is a normal root trigger, so one skill may declare many independent receivers. */
@SkillNode(key = "SIGNAL_RECEIVED", name = "Signal Received", nameZh = "接收信号时",
        descriptionZh = "仅激活的技能所有者接收；参数只在本次同步执行期间有效。", container = true)
public final class SignalReceivedTrigger implements Trigger<SignalEvent> {
    @SkillField(kind = FieldKind.StringValue, label = "Channel", labelZh = "频道",
            tooltipZh = "同频道收发双方必须使用相同参数契约。", defaultValue = "signal")
    private static final String CHANNEL = "channel";
    @SkillField(kind = FieldKind.MapValue, label = "Contract", labelZh = "参数契约",
            tooltipZh = "参数名到 number、text、boolean 的映射；加载前检查同频道声明一致。")
    private static final String CONTRACT = "contract";

    @Override public String getKey() { return "SIGNAL_RECEIVED"; }
    @Override public Class<SignalEvent> getEvent() { return SignalEvent.class; }
    @Override public boolean shouldTrigger(SignalEvent event, int level, Settings settings) {
        return event.getChannel().equals(settings.getString(CHANNEL, ""));
    }
    @Override public void setValues(SignalEvent event, Map<String, Object> data) {
        data.put("signal-channel", event.getChannel());
        data.put("signal-sender", event.getSender());
        for (Map.Entry<String, Object> entry : event.getArguments().entrySet())
            data.put("signal-" + entry.getKey(), entry.getValue());
    }
    @Override public LivingEntity getCaster(SignalEvent event) { return event.getRecipient(); }
    @Override public LivingEntity getTarget(SignalEvent event, Settings settings) { return event.getRecipient(); }
}
