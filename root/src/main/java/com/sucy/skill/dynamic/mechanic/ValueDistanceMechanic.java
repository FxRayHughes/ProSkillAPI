package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2017
 * com.sucy.skill.dynamic.mechanic.ValueDistanceMechanic
 */
@SkillNode(
        key = "value distance",
        name = "Value Distance",
        nameZh = "数值距离",
        description = "Stores the distance between the target and the caster into a value",
        descriptionZh = "把第一个目标与施法者之间的直线距离（方块数）存入施法者 cast data。要求施法者是玩家，否则返回 false。两者不在同一世界时存入 -1，且该值以 Integer 装箱写入（其余数值节点写的是 Double），被 Value Condition 读取时可能抛 ClassCastException，跨世界场景建议先用其他方式过滤。")
public class ValueDistanceMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "cast data 里的键名，支持 {uuid} 替换为施法者 UUID。正常情况存 Double 距离，跨世界时存 Integer 的 -1。",
            defaultValue = "attribute")
    private static final String KEY = "key";

    @Override
    public String getKey() {
        return "value distance";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        if (!settings.has(KEY) || !(caster instanceof Player)) {
            return false;
        }

        final String key = settings.getString(KEY, "").replace("{uuid}", caster.getUniqueId().toString());
        ;
        final HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        if (targets.get(0).getLocation().getWorld() != caster.getLocation().getWorld()) {
            data.put(key, -1);
            return true;
        }
        data.put(key, targets.get(0).getLocation().distance(caster.getLocation()));
        return true;
    }
}
