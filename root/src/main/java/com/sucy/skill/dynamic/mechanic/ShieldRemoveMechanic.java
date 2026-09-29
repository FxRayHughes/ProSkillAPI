package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.combat.shield.ShieldManager;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import java.util.List;

/** Removes shield layers by key, including all layers when the key is blank. */
@SkillNode(key = "shield remove", name = "Shield Remove", nameZh = "移除护盾",
        descriptionZh = "按护盾键移除当前目标的护盾；空键移除全部。")
public class ShieldRemoveMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Layer Key", labelZh = "护盾键",
            tooltipZh = "留空移除全部护盾。", defaultValue = "shield")
    private static final String KEY = "key";
    @Override public String getKey() { return "shield remove"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        boolean removed = false;
        for (LivingEntity target : targets)
            removed |= ShieldManager.remove(target, settings.getString(KEY, "shield")) > 0;
        return removed;
    }
}
