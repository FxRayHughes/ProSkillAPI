package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.combat.shield.ShieldEvent;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;

/** A separate result key lets authors distinguish a hit from layer depletion. */
@SkillNode(key = "SHIELD_EMPTY", name = "Shield Empty", nameZh = "护盾层耗尽时",
        descriptionZh = "某一层容量降至零后触发；同一伤害可同时触发 SHIELD_HIT。", container = true)
public final class ShieldEmptyTrigger extends ShieldHitTrigger {
    @SkillField(kind = FieldKind.StringValue, label = "Layer Key", labelZh = "护盾键",
            tooltipZh = "留空监听任意护盾层。")
    private static final String KEY = "key";
    @Override public String getKey() { return "SHIELD_EMPTY"; }
    @Override public boolean shouldTrigger(ShieldEvent event, int level, Settings settings) {
        String key = settings.getString(KEY, "");
        return event.getKind() == ShieldEvent.Kind.DEPLETED && (key.isEmpty() || key.equals(event.getKey()));
    }
}
