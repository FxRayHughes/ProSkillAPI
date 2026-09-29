package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/** Shared output contract: absent or nonfinite inputs leave cast data unchanged. */
public abstract class AbstractValueReadMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Output Key", labelZh = "输出引用键", defaultValue = "value")
    private static final String OUTPUT = "key";

    @Override public final boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (caster == null || targets.isEmpty()) return false;
        String key = settings.getString(OUTPUT, "").replace("{uuid}", caster.getUniqueId().toString());
        if (key.isEmpty()) return false;
        Double value = read(caster, targets.get(0));
        if (value == null || !Double.isFinite(value)) return false;
        DynamicSkill.getCastData(caster).put(key, value);
        return true;
    }

    /** A null result means the selected source does not exist in this context. */
    protected abstract Double read(LivingEntity caster, LivingEntity target);
}
